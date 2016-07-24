package org.freeims.ims.s;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.Parameters;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.freeims.common.DnsUtil;
import org.freeims.common.SipUtil;
import org.freeims.diameterpeer.DiameterPeer;
import org.freeims.ims.cache.CacheObjectPool;
import org.freeims.ims.config.Config;
import org.freeims.ims.config.RealmConfig;
import org.freeims.ims.config.SCscfConfig;
import org.freeims.ims.s.handler.RegisterHandler;
import org.freeims.ims.s.handler.UEStoreInfo;
import org.freeims.javax.sip.address.AddressImpl;
import org.freeims.javax.sip.header.Authorization;
import org.freeims.javax.sip.header.Route;
import org.freeims.javax.sip.header.ims.PCalledPartyID;
import org.freeims.javax.sip.header.ims.PChargingVector;
import org.freeims.sipproxy.servlet.SipProxyFactory;
import org.freeims.sipproxy.servlet.SipProxyRequest;
import org.freeims.sipproxy.servlet.SipProxyResponse;
import org.freeims.sipproxy.servlet.SubsequentAction;
import org.freeims.sipproxy.servlet.impl.GenericServlet;

@WebServlet(value="/MainServlet",
	initParams={@WebInitParam(name="configFile",value="ims-config.xml"),
			@WebInitParam(name="diameterPeerConfig",value="diameter-peer-ims.xml")},
	loadOnStartup=1)
public class SServlet extends GenericServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3596025537547454962L;
	private static Logger logger = Logger.getLogger(SServlet.class);

	private CacheObjectPool<URI,UEStoreInfo> onlineUEPool = null;
	private String scscfConfigFile = null;

	
	private SCscfConfig scscfConf = null;
	private RegisterHandler h = null;
	private DiameterPeer diameterPeer = null;
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		logger.info("init sServlet");
		scscfConfigFile = servletConfig.getInitParameter("configFile");
		try{
			Config config = Config.load(scscfConfigFile);
			scscfConf = config.getSConfig();
			getServletContext().setAttribute("configInstance",scscfConf);
		}catch(Exception e)
		{
			logger.info(e.getMessage(),e);
		}
		
		try{
			//scscfConf = (SCscfConfig)this.getServletContext().getAttribute("configInstance");
			
			String filePath = Loader.getResource(servletConfig.getInitParameter("diameterPeerConfig")).getPath();
			logger.info("filePath:"+filePath);
			diameterPeer = new DiameterPeer(filePath, "SCSCF");
			diameterPeer.enableTransactions(10, 1);
		}catch(Exception e)
		{
			logger.info(e.getMessage(),e);
		}
		
		onlineUEPool =
				new CacheObjectPool<URI,UEStoreInfo>( h );
		onlineUEPool.setMaxIdleTime(10* 60 * 1000);
		h = new RegisterHandler(scscfConf,onlineUEPool,diameterPeer);
		
	}

	public void doException(Exception ex) {

	}

	public void doRegister(SipProxyRequest req) throws ServletException, IOException {

		

		String expires = req.getHeader("Expires");
		Parameters paramable = req.getParameterableHeader("Contact");
		String expires2 = paramable.getParameter("Expires");

		if ((expires != null && expires.equals("0")) || (expires2 != null && expires2.equals("0"))) {
			h.handlerUnregister(req);
			onlineUEPool.remove(req.getTo().getURI());
			req.createResponseAction(200);
			return;
		}

		Parameters p = req.getParameterableHeader(Authorization.NAME);
		if (p == null) {
			logger.info("init Register");
			h.handlerInitRegister(req);
			return ;
		}

		String respValue = p.getParameter("Response");

		if (respValue == null || respValue.equals("")) {
			h.handlerInitRegister(req);
			return;
			
		}
		h.handlerChallengeRequest(req);
		req.createNoneAction();
		return;

	}

	public void doMessage(SipProxyRequest req) throws ServletException, IOException {
		RealmConfig realmConfig = scscfConf.getRealmConfig(SipUtil.extractRealm(req));
		req.sendResponse(100);
		

		SubsequentAction action = SubsequentAction.createForwardAction();

		if (SipUtil.isSameRealm(req.getFrom(), req.getTo())) {
			try {
				
				UEStoreInfo fromUEInfo = onlineUEPool.get(req.getFrom().getURI());
				if (fromUEInfo == null) {
					req.createResponseAction(404);
					return;
				}

				PChargingVector pcv = (PChargingVector) req.getHeaderInstance(PChargingVector.NAME);
				pcv.setOriginatingIOI(realmConfig.getName());

				req.removeFirst(Route.NAME);

				UEStoreInfo toUEInfo = onlineUEPool.get(req.getTo().getURI());

				if (toUEInfo == null) {
					req.createResponseAction(404);
					return;
				}

				SipUtil.alterRequestURI(req, (SipURI) toUEInfo.getContact().getAddress().getURI());

				pcv.setTerminatingIOI(realmConfig.getName());

				PCalledPartyID pcpID = new PCalledPartyID((AddressImpl) req.getTo());
				req.setHeader(pcpID);


				Address path = toUEInfo.getPath();
				req.pushRoute(path);
				action.setAppId("mo");
				req.setSubsequentAction(action);
				return;
				
			} catch (ParseException e) {
				logger.info(e.getMessage(), e);
				req.createResponseAction(500);
				return ;
			}
		}

		// orig
		if (!SipUtil.isMessageInTerm(req)) {

			UEStoreInfo ueInfo = onlineUEPool.get(req.getFrom().getURI());
			if (ueInfo == null) {
				req.createResponseAction(404);
				return;
			}

			try {
				SipURI requestUri = (SipURI) req.getRequestURI();
				PChargingVector pcv = (PChargingVector) req.getHeaderInstance(PChargingVector.NAME);
				pcv.setOriginatingIOI(realmConfig.getName());

				req.removeFirst(Route.NAME);
				Address address = DnsUtil.resolveICSCFHostByDNS(requestUri.getHost(), "UDP",scscfConf.getDnsServer());
				req.pushRoute(address);
				action.setAppId("mo");
			} catch (Exception e) {
				logger.info(e.getMessage(), e);
				req.createResponseAction(500);
				return;
			}
		} else {
			// term
			UEStoreInfo ueInfo = onlineUEPool.get(req.getTo().getURI());

			if (ueInfo == null) {
				req.createResponseAction(404);
				return;
			}
			try {
				SipURI toURI = (SipURI) req.getTo().getURI();

				realmConfig = scscfConf.getRealmConfig(toURI.getHost());
				SipUtil.alterRequestURI(req, (SipURI) ueInfo.getContact().getAddress().getURI());



				PChargingVector pcv = (PChargingVector) req.getHeaderInstance(PChargingVector.NAME);
				pcv.setTerminatingIOI(realmConfig.getName());

//				PCalledPartyID pcpID = new PCalledPartyID((AddressImpl) req.getTo());
//				req.setHeader(pcpID);

				req.removeFirst(Route.NAME);
				Address path = ueInfo.getPath();
				req.pushRoute(path);
				action.setAppId("mt");
			} catch (ParseException e) {
				logger.info(e.getMessage(), e);
				req.createResponseAction(500);
				return;
			}
		}
		req.setSubsequentAction(action);
		return ;
	}

	public void doInvite(SipProxyRequest req) throws ServletException, IOException {

		RealmConfig realmConfig = scscfConf.getRealmConfig(SipUtil.extractRealm(req));
		req.sendResponse(100);
		

		SubsequentAction action = SubsequentAction.createForwardAction();

		if (SipUtil.isSameRealm(req.getFrom(), req.getTo())) {
			try {
				
				UEStoreInfo fromUEInfo = onlineUEPool.get(req.getFrom().getURI());
				if (fromUEInfo == null) {
					req.createResponseAction(404);
					return;
				}
				
				Address addr = SipProxyFactory.getAddressFactory().createAddress("sip:mo@"+realmConfig.getHost()+":"+realmConfig.getPort());
				((SipURI) addr.getURI()).setLrParam();
				RecordRouteHeader rrh = SipProxyFactory.getHeaderFactory().createRecordRouteHeader(addr);
				req.addHeader(rrh);

				// ;orig-ioi=\""+d.getName()+"\""
				PChargingVector pcv = (PChargingVector) req.getHeaderInstance(PChargingVector.NAME);
				pcv.setOriginatingIOI(realmConfig.getName());

				req.removeFirst(Route.NAME);

				UEStoreInfo toUEInfo = onlineUEPool.get(req.getTo().getURI());

				if (toUEInfo == null) {
					req.createResponseAction(404);
					return;
				}

				SipUtil.alterRequestURI(req, (SipURI) toUEInfo.getContact().getAddress().getURI());

				pcv.setTerminatingIOI(realmConfig.getName());

				PCalledPartyID pcpID = new PCalledPartyID((AddressImpl) req.getTo());
				req.setHeader(pcpID);


				Address path = toUEInfo.getPath();
				req.pushRoute(path);
				action.setAppId("mo");
				req.setSubsequentAction(action);
				return;
				
			} catch (ParseException e) {
				logger.info(e.getMessage(), e);
				req.createResponseAction(500);
				return ;
			}
		}

		// orig
		if (!SipUtil.isMessageInTerm(req)) {

			UEStoreInfo ueInfo = onlineUEPool.get(req.getFrom().getURI());
			if (ueInfo == null) {
				req.createResponseAction(404);
				return;
			}

			try {
				SipURI requestUri = (SipURI) req.getRequestURI();

				// Max-Forwards: 68
				// Record-Route: <sip:scscf1.home1.net;lr>,
				// <sip:pcscf1.visited1.net;lr>
				// P-Asserted-Identity: "John Doe"
				// <sip:user1_public1@home1.net>, <tel:+1-212-555-1111>
				// P-Charging-Vector:
				// icid-value="AyretyU0dm+6O2IrT5tAFrbHLso=023551024";
				// orig-ioi=home1.net

				Address addr = SipProxyFactory.getAddressFactory().createAddress("sip:mo@"+realmConfig.getHost()+":"+realmConfig.getPort());
				((SipURI) addr.getURI()).setLrParam();
				RecordRouteHeader rrh = SipProxyFactory.getHeaderFactory().createRecordRouteHeader(addr);
				req.addHeader(rrh);

				// ;orig-ioi=\""+d.getName()+"\""
				PChargingVector pcv = (PChargingVector) req.getHeaderInstance(PChargingVector.NAME);
				pcv.setOriginatingIOI(realmConfig.getName());

				req.removeFirst(Route.NAME);
				Address address = DnsUtil.resolveICSCFHostByDNS(requestUri.getHost(), "UDP",scscfConf.getDnsServer());
				req.pushRoute(address);
				action.setAppId("mo");
			} catch (Exception e) {
				logger.info(e.getMessage(), e);
				req.createResponseAction(500);
				return;
			}
		} else {
			// term
			UEStoreInfo ueInfo = onlineUEPool.get(req.getTo().getURI());

			if (ueInfo == null) {
				req.createResponseAction(404);
				return;
			}
			try {
				SipURI toURI = (SipURI) req.getTo().getURI();

				realmConfig = scscfConf.getRealmConfig(toURI.getHost());
				SipUtil.alterRequestURI(req, (SipURI) ueInfo.getContact().getAddress().getURI());

				Address addr = SipProxyFactory.getAddressFactory().createAddress("sip:mt@"+realmConfig.getHost()+":"+realmConfig.getPort());
				((SipURI) addr.getURI()).setLrParam();
				RecordRouteHeader rrh = SipProxyFactory.getHeaderFactory().createRecordRouteHeader(addr);
				req.addHeader(rrh);

				PChargingVector pcv = (PChargingVector) req.getHeaderInstance(PChargingVector.NAME);
				pcv.setTerminatingIOI(realmConfig.getName());

				PCalledPartyID pcpID = new PCalledPartyID((AddressImpl) req.getTo());
				req.setHeader(pcpID);

				req.removeFirst(Route.NAME);
				Address path = ueInfo.getPath();
				req.pushRoute(path);
				action.setAppId("mt");
			} catch (ParseException e) {
				logger.info(e.getMessage(), e);
				req.createResponseAction(500);
				return;
			}
		}
		req.setSubsequentAction(action);
	}

	public void doResponse(SipProxyResponse resp) throws ServletException, IOException {
		if (resp.getStatus() == 183) {

		}
	}

	@Override
	public void doAck(SipProxyRequest req) throws ServletException, IOException {

		SubsequentAction action = SubsequentAction.createForwardAction();
		String route = req.getHeader(RouteHeader.NAME);
		if (route.indexOf("mo") > -1) {
			action.setAppId("mo");
		} else {
			action.setAppId("mt");
		}
		req.removeFirst(RouteHeader.NAME);
		req.setSubsequentAction(action);

	}

	@Override
	public void doBye(SipProxyRequest req) throws ServletException, IOException {

		SubsequentAction action = SubsequentAction.createForwardAction();
		String route = req.getHeader(RouteHeader.NAME);
		if (route.indexOf("mo") > -1) {
			action.setAppId("mo");
		} else {
			action.setAppId("mt");
		}
		req.removeFirst(RouteHeader.NAME);
		req.setSubsequentAction(action);
	}

	@Override
	public void doCancel(SipProxyRequest req) throws ServletException, IOException {

		SubsequentAction action = SubsequentAction.createForwardAction();
		String route = req.getHeader(RouteHeader.NAME);
		if (route.indexOf("mo") > -1) {
			action.setAppId("mo");
		} else {
			action.setAppId("mt");
		}
		req.removeFirst(RouteHeader.NAME);
		req.setSubsequentAction(action);
	}

	@Override
	public void doUpdate(SipProxyRequest req) throws ServletException, IOException {

		SubsequentAction action = SubsequentAction.createForwardAction();
		String route = req.getHeader(RouteHeader.NAME);
		if (route.indexOf("mo") > -1) {
			action.setAppId("mo");
		} else {
			action.setAppId("mt");
		}
		req.removeFirst(RouteHeader.NAME);
		req.setSubsequentAction(action);
	}

	@Override
	public void doPrack(SipProxyRequest req) throws ServletException, IOException {
		SubsequentAction action = SubsequentAction.createForwardAction();
		String route = req.getHeader(RouteHeader.NAME);
		if (route.indexOf("mo") > -1) {
			action.setAppId("mo");
		} else {
			action.setAppId("mt");
		}
		req.removeFirst(RouteHeader.NAME);
		req.setSubsequentAction(action);
	}

}
