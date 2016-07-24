package org.freeims.ims.i;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.sip.address.Address;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.freeims.common.SipUtil;
import org.freeims.diameter.DiameterConstants;
import org.freeims.diameter.UtilAVP;
import org.freeims.diameterpeer.DiameterPeer;
import org.freeims.diameterpeer.EventListener;
import org.freeims.diameterpeer.data.AVP;
import org.freeims.diameterpeer.data.AVPDecodeException;
import org.freeims.diameterpeer.data.DiameterMessage;
import org.freeims.diameterpeer.transaction.TransactionListener;
import org.freeims.ims.config.Config;
import org.freeims.ims.config.ICscfConfig;
import org.freeims.javax.sip.header.Authorization;
import org.freeims.javax.sip.header.Route;
import org.freeims.sipproxy.servlet.SipProxyFactory;
import org.freeims.sipproxy.servlet.SipProxyRequest;
import org.freeims.sipproxy.servlet.SipProxyResponse;
import org.freeims.sipproxy.servlet.SubsequentAction;
import org.freeims.sipproxy.servlet.impl.GenericServlet;

@WebServlet(value="/MainServlet",
initParams={@WebInitParam(name="configFile",value="ims-config.xml"),
		@WebInitParam(name="diameterPeerConfig",value="diameter-peer-ims.xml")},
loadOnStartup=1)
public class IServlet extends GenericServlet implements EventListener, TransactionListener {

	private static final long serialVersionUID = 4322251577075098963L;
	private static Logger logger = Logger.getLogger(IServlet.class);
	private DiameterPeer diameterPeer = null;
	private String icscfConfigFile = null;
	private ICscfConfig iConfig = null;
	class ExDiameterMessage extends DiameterMessage {
		public ExDiameterMessage(int Command_Code, boolean Request, boolean Proxiable, int Application_id,
				int HopByHop_id, int EndToEnd_id) {
			super(Command_Code, Request, Proxiable, Application_id, HopByHop_id, EndToEnd_id);
		}

		private Object exData;

		public Object getExData() {
			return exData;
		}

		public void setExData(Object exData) {
			this.exData = exData;
		}

	}

	public IServlet() {

	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		//URLClassLoader cl = (URLClassLoader)Config.class.getClassLoader();
		//ClassLoader oldCls = Thread.currentThread().getContextClassLoader();
		//Thread.currentThread().setContextClassLoader(cl);
		String dpFileName = servletConfig.getInitParameter("diameterPeerConfig");
		logger.info("diameterpeer config file:"+dpFileName);
		URL url = Loader.getResource(dpFileName);
		String filePath = url.getPath();
		//Thread.currentThread().setContextClassLoader(oldCls);
		
		diameterPeer = new DiameterPeer(filePath, "ICSCF");
		diameterPeer.enableTransactions(10, 1);
		
		icscfConfigFile = servletConfig.getInitParameter("configFile");
		logger.info("icscfConfigFile:"+icscfConfigFile);
		try{
		Config config = Config.load(icscfConfigFile);
		iConfig = config.getIConfig();
		getServletContext().setAttribute("configInstance", iConfig);
		logger.info("init FilterConfig");
		}catch(Exception e)
		{
			logger.info(e.getMessage(),e);
		}
		// diameterPeer.addEventListener(this);
	}

	public void doResponse(SipProxyResponse resp) throws ServletException, IOException {

	}

	public void doException(Exception ex) {

	}

	public void doRegister(SipProxyRequest req) throws ServletException, IOException {
		
		ExDiameterMessage message = new ExDiameterMessage(DiameterConstants.Command.UAR, true, false,
				DiameterConstants.Application.Cx, diameterPeer.getNextHopByHopId(), diameterPeer.getNextHopByHopId());

		Authorization auth = (Authorization) req.getParameterableHeader(Authorization.NAME);
		String username = null;
		String realm = null;
		if (auth != null) {
			logger.info("has Authorization Header");
			username = auth.getUsername();
			realm = auth.getRealm();
		} else {
			logger.info("has not Authorization Header");
			String fromURI = req.getFrom().getURI().toString();

			if (fromURI.startsWith("sips:")) {
				username = fromURI.substring(5);
			} else if (fromURI.startsWith("sip:")) {
				username = fromURI.substring(4);
			}
			int pos = fromURI.indexOf('@');
			realm = fromURI.substring(pos + 1);
		}

		UtilAVP.addDestinationRealm(message, realm);
		UtilAVP.addDestinationHost(message, this.iConfig.getRealmConfig(realm).getHssAddress());

		logger.info("public identity:" + req.getTo().getURI().toString());
		UtilAVP.addPublicIdentity(message, req.getTo().getURI().toString());
		logger.info("username:" + username);
		UtilAVP.addUserName(message, username);
		logger.info("realm:" + realm);
		UtilAVP.addDestinationRealm(message, realm);

		UtilAVP.addUARFlags(message, DiameterConstants.AVPValue.UAR_No_Flag_Emergency);
		UtilAVP.addVisitedNetwork(message, realm);
		message.setExData(req);



		if (SipUtil.isRegister(req)){

		UtilAVP.addUserAuthorizationType(message, DiameterConstants.AVPValue.UAT_Registration);
		}else{
			// De-registration
			UtilAVP.addUserAuthorizationType(message, DiameterConstants.AVPValue.UAT_De_Registration);
		}
		try {

//			if (!diameterPeer.sendRequestTransactional(message, this)) {
//				return SubsequentAction.createResponseAction(500, "send UAR fail");
//			}
//			return SubsequentAction.createNoneAction();

			 DiameterMessage uaa = diameterPeer.sendRequestBlocking(message);
			 if (uaa == null)
			 {
				 logger.info("uaa is null");
				 req.createResponseAction(403);
				 return;
			 }
			 logger.info(uaa.toString());
			 logger.info("uss.flagError:"+uaa.flagError);
			 if (uaa.flagError)
			 {

				AVP group = uaa.findAVP(DiameterConstants.AVPCode.EXPERIMENTAL_RESULT, true,
						DiameterConstants.Vendor.DIAM);
				if (group != null) {
					AVP vendorID = group.findChildAVP(DiameterConstants.AVPCode.VENDOR_ID, true,
							DiameterConstants.Vendor.DIAM);
					if (vendorID != null) {
						logger.info("vendorID" + vendorID.getIntData());
					}

					AVP expResult = group.findChildAVP(DiameterConstants.AVPCode.EXPERIMENTAL_RESULT_CODE, true,
							DiameterConstants.Vendor.DIAM);
					if (expResult != null) {
						logger.info("expResult:" + expResult.getIntData());
					}
			
				}
			
				 logger.info("resultCode"+UtilAVP.getResultCode(uaa));
				req.createResponseAction(404);
				return;
			 }
			 req.removeFirst(Route.NAME);
			 String scscfName = this.fetchSCscfName(uaa);
			 Address scscfAddress =
			 SipProxyFactory.getAddressFactory().createAddress(scscfName);
			 req.pushRoute(scscfAddress);
			 req.createForwardAction();

		} catch (Exception e) {
			logger.info(e.getMessage(), e);
			req.createResponseAction(500);
		}

	}

	public String fetchSCscfName(DiameterMessage uaa) {

		logger.info(uaa.toString());
		logger.info("uss.flagError:" + uaa.flagError);
		if (uaa.flagError) {

			AVP group = uaa.findAVP(DiameterConstants.AVPCode.EXPERIMENTAL_RESULT, true, DiameterConstants.Vendor.DIAM);
			if (group != null) {
				AVP vendorID = group.findChildAVP(DiameterConstants.AVPCode.VENDOR_ID, true,
						DiameterConstants.Vendor.DIAM);
				if (vendorID != null) {
					logger.info("vendorID" + vendorID.getIntData());
				}

				AVP expResult = group.findChildAVP(DiameterConstants.AVPCode.EXPERIMENTAL_RESULT_CODE, true,
						DiameterConstants.Vendor.DIAM);
				if (expResult != null) {
					logger.info("expResult:" + expResult.getIntData());
				}

			}

			logger.info("resultCode" + UtilAVP.getResultCode(uaa));
			return null;

		}
		String scscfName = UtilAVP.getServerName(uaa);
		logger.info("SCscf name:" + scscfName);
		if (scscfName == null) {
			AVP server_cap = uaa.findAVP(DiameterConstants.AVPCode.IMS_SERVER_CAPABILITIES
						, true, DiameterConstants.Vendor.V3GPP);
			try{
			server_cap.ungroup();
			logger.info("server_cap.flag_mandatory" + server_cap.flag_mandatory);
			logger.info("server_cap.vendor_id" + server_cap.vendor_id);
			logger.info("server_cap.childs.size:" + server_cap.childs.size());
			for (AVP avp : (Vector<AVP>) server_cap.childs) {
				logger.info("avp.code:" + avp.code);
				logger.info("avpData:" + new String(avp.getData()));
				logger.info("");
			}
			AVP server_name_avp = server_cap.findChildAVP(DiameterConstants.AVPCode.IMS_SERVER_NAME, true,
					DiameterConstants.Vendor.V3GPP);
			scscfName = new String(server_name_avp.getData());
			}catch(AVPDecodeException avpEx)
			{
				logger.info(avpEx.getMessage(),avpEx);
			}
		}
		//
		logger.info("SCscf name:" + scscfName);
		return scscfName;
	}

	public void doMessage(SipProxyRequest req) throws ServletException, IOException {

		DiameterMessage message = new DiameterMessage(DiameterConstants.Command.LIR, true, false,
				DiameterConstants.Application.Cx, diameterPeer.getNextHopByHopId(), diameterPeer.getNextHopByHopId());

		String realm =  SipUtil.extractRealm((SipURI)req.getTo().getURI());
		logger.info("realm:"+realm);
		
		UtilAVP.addDestinationRealm(message, realm);
		String hssAddress = this.iConfig.getRealmConfig(realm).getHssAddress();
		logger.info("hssAddress:"+hssAddress);
		UtilAVP.addDestinationHost(message, hssAddress);

		UtilAVP.addPublicIdentity(message, req.getTo().getURI().toString());
		UtilAVP.addOriginatingRequest(message, 1);

		try {
			DiameterMessage lia = diameterPeer.sendRequestBlocking(message);

			if (lia == null) {
				req.createResponseAction(500);
				return ;
			}

			if (lia.flagError) {
				req.createResponseAction(403);
				return ;
			}
			String scscfName = fetchSCscfName(lia);
			Address scscfAddress = SipProxyFactory.getAddressFactory().createAddress(scscfName);
			req.pushRoute(scscfAddress);
			
			SubsequentAction action = SubsequentAction.createForwardAction();
			if (SipUtil.isMessageInTerm(req)){
				action.setAppId("mt");
			}else{
				action.setAppId("mo");
			}
			req.setSubsequentAction(action);
			return ;

		} catch (Exception e) {
			logger.info(e.getMessage(), e);
			req.createResponseAction(500);
		}
	}

	public void doInvite(SipProxyRequest req) throws ServletException, IOException {

		DiameterMessage message = new DiameterMessage(DiameterConstants.Command.LIR, true, false,
				DiameterConstants.Application.Cx, diameterPeer.getNextHopByHopId(), diameterPeer.getNextHopByHopId());

		String realm =  SipUtil.extractRealm((SipURI)req.getTo().getURI());
		logger.info("realm:"+realm);
		
		UtilAVP.addDestinationRealm(message, realm);
		String hssAddress = this.iConfig.getRealmConfig(realm).getHssAddress();
		logger.info("hssAddress:"+hssAddress);
		UtilAVP.addDestinationHost(message, hssAddress);

		UtilAVP.addPublicIdentity(message, req.getTo().getURI().toString());
		UtilAVP.addOriginatingRequest(message, 1);

		try {
			DiameterMessage lia = diameterPeer.sendRequestBlocking(message);

			if (lia == null) {
				req.createResponseAction(500);
				return ;
			}

			if (lia.flagError) {
				req.createResponseAction(403);
				return ;
			}
			String scscfName = fetchSCscfName(lia);
			Address scscfAddress = SipProxyFactory.getAddressFactory().createAddress(scscfName);
			req.pushRoute(scscfAddress);
			
			SubsequentAction action = SubsequentAction.createForwardAction();
			if (SipUtil.isMessageInTerm(req)){
				action.setAppId("mt");
			}else{
				action.setAppId("mo");
			}
			req.setSubsequentAction(action);
			return ;

		} catch (Exception e) {
			logger.info(e.getMessage(), e);
			req.createResponseAction(500);
		}
	}

	public void receiveAnswer(String arg0, DiameterMessage dmRequest, DiameterMessage answer) {
		logger.info("receive answer");

		if (answer.flagRequest) {
			logger.info("OH NO,answer message is request?");

		}

		SipProxyRequest sipRequest = (SipProxyRequest) ((ExDiameterMessage) dmRequest).getExData();
		if (answer.flagError) {
			logger.info("uss.flagError:" + answer.flagError);
			sipRequest.sendResponse(403);
			return;
		}
		switch (answer.commandCode) {
		case DiameterConstants.Command.UAR:
			processUAA(sipRequest, answer);
			break;
		}
	}

	private void processUAA(SipProxyRequest sipRequest, DiameterMessage answer) {
		logger.info("process UAA");
		try{
			String scscfName = this.fetchSCscfName(answer);
			Address scscfAddress = SipProxyFactory.getAddressFactory().createAddress(scscfName);
			sipRequest.pushRoute(scscfAddress);
			sipRequest.proxyTo("mo");
		} catch (Exception e) {
			logger.info(e.getMessage(), e);
			sipRequest.sendResponse(500, "forward message to SCSCF fail.");

		}
	}

	public void timeout(DiameterMessage diaReq) {
		SipProxyRequest sipRequest = (SipProxyRequest) ((ExDiameterMessage) diaReq).getExData();
		logger.info("timeout for " + sipRequest.getFrom().toString());
		sipRequest.sendResponse(500, "read UAA data from HSS fail");
	}

	public void recvMessage(String fqdn, DiameterMessage message) {
		logger.info("receive DiameterMessage");

	}

}
