package org.freeims.ims.p;


import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.Parameters;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.freeims.common.DnsUtil;
import org.freeims.common.SipUtil;
import org.freeims.ims.cache.CacheEvent;
import org.freeims.ims.cache.CacheEventListener;
import org.freeims.ims.cache.CacheObjectPool;
import org.freeims.ims.config.Config;
import org.freeims.ims.config.PCscfConfig;
import org.freeims.ims.config.RealmConfig;
import org.freeims.ims.rtpproxy.RtpProxyControlClient;
import org.freeims.javax.sip.address.SipUri;
import org.freeims.javax.sip.header.Contact;
import org.freeims.javax.sip.header.To;
import org.freeims.javax.sip.header.ViaList;
import org.freeims.javax.sip.address.AddressImpl;
import org.freeims.javax.sip.message.SIPMessage;
import org.freeims.sipproxy.servlet.SipProxyRequest;
import org.freeims.sipproxy.servlet.SipProxyResponse;
import org.freeims.sipproxy.servlet.SubsequentAction;
import org.freeims.sipproxy.servlet.impl.GenericServlet;
import org.freeims.sipproxy.servlet.impl.SipProxyRequestImpl;

@WebServlet(value="/MainServlet",initParams={@WebInitParam(name="configFile",value="pConfig.xml")},loadOnStartup=1)
public class PServlet extends GenericServlet
{
	private static final long serialVersionUID = 8661236325745602526L;
	private static Log logger = LogFactory.getLog(PServlet.class);
	
	private RtpProxyControlClient rptProxyCtrlClient ;//= new RtpProxyControlClient("120.24.95.155", 9000);
	private PCscfConfig pcscfConf = null;
	private Hashtable<Address,String> ueIcidValueTable = new  Hashtable<Address,String>();
	private CacheObjectPool<URI,Contact> onlineUEPool = null;
	public PServlet()
	{
		

	}


	@Override
	public void doRegister(SipProxyRequest req)  throws ServletException,IOException{
		
		logger.info("register:TO:" + req.getTo().getURI().toString());
		
		// Expires
		SipProxyRequestImpl requestImpl = (SipProxyRequestImpl)req;
		String expires = req.getHeader("Expires");
		Parameters paramable = req.getParameterableHeader("Contact");
		String expires2 = paramable.getParameter("Expires");

		if ((expires != null && expires.equals("0")) || (expires2 != null && expires2.equals("0"))) {
			unRegister(req);
			onlineUEPool.remove(((To)req.getHeaderInstance(To.NAME)).getAddress().getURI());
			requestImpl.createForwardAction();

		}
		SipUtil.alterRequestContact(req);
		try{
			initRegister(req);
		}catch(Exception e)
		{
			logger.info(e.getMessage(),e);
			requestImpl.createResponseAction(500, null);
			return;

		}
		//requestImpl.createForwardAction();
	}
	private static java.util.concurrent.atomic.AtomicInteger cscf_icid_value_count= new java.util.concurrent.atomic.AtomicInteger(0);
	private char hex_chars[]={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	private String createIcidValue()
	{
		
    	String s = new String();
    	int i=0;
    	int cnt=cscf_icid_value_count.get();
    	cscf_icid_value_count.addAndGet(1);
    	long t = System.currentTimeMillis();
    	//sizeof(time_t) == 8
    	for(i=8*2-1;i>=0;i--){
    		s = hex_chars[(int)(t & 0x0F)] +s;
    		t >>= 4;
    	}
    	//sizeof(unsigned int) == 4
    	for(i=4 *2-1;i>=0;i--){
    		s = hex_chars[cnt & 0x0F] +s;
    		cnt >>= 4;
    	}
    	return s;
	}
	@Override
	public void doInvite(SipProxyRequest req)  throws ServletException,IOException 
	{
		logger.info("doInvite");
		SubsequentAction action =  SubsequentAction.createForwardAction();
		SipUri uri = null;
		RealmConfig d =null;
		if (!SipUtil.isMessageInTerm(req))
		{
			
			uri = (SipUri)req.getFrom().getURI();
			if (onlineUEPool.get(uri) == null){
				req.createResponseAction(404, "you have not login.");
				return;
			}
			onlineUEPool.active(uri);
			
			 d = pcscfConf.getRealmConfig(uri.getHost());
			req.setHeader("P-Asserted-Identity", req.getFrom().getURI().toString());

			req.setHeader("P-Charging-Vector",
					"icid-value=\"P-CSCF"+createIcidValue() //ueIcidValueTable.get(req.getFrom())
					+"\";icid-generated-at="+d.getHost());

			req.addHeader(RecordRouteHeader.NAME, "sip:mo@"+d.getHost()+":"+d.getPort());

			RecordRouteHeader rh = (RecordRouteHeader)req.getParameterableHeader(RecordRouteHeader.NAME);

			((SipURI)((AddressImpl)rh.getAddress()).getURI()).setLrParam();
			

			SipUtil.alterRequestContact(req);
			
			if (pcscfConf.getRtpProxy() != null){
				rptProxyCtrlClient.applyRtpProxy(req);
			}else{
				SipUtil.adjustSdpParameter(req);
			}
			action.setAppId("mo");

		}else{
			uri = (SipUri)req.getTo().getURI();
			if (onlineUEPool.get(uri) == null){
				req.createResponseAction(404, "you have not login.");
				return;
			}
			onlineUEPool.active(uri);
			d = pcscfConf.getRealmConfig(uri.getHost());
			req.addHeader(RecordRouteHeader.NAME, "sip:mt@"+d.getHost()+":"+d.getPort());
			RecordRouteHeader rh = (RecordRouteHeader)req.getParameterableHeader(RecordRouteHeader.NAME);
			((SipURI)((AddressImpl)rh.getAddress()).getURI()).setLrParam();
			req.removeFirst(RouteHeader.NAME);
			action.setAppId("mt");
		}
		logger.info("return ");
		((SipProxyRequestImpl)req).setSubsequentAction(action);
		
	}
	
	
	@Override
	public void doPrack(SipProxyRequest req) throws ServletException, IOException {
		SubsequentAction action =  SubsequentAction.createForwardAction();
		String route = req.getHeader(RouteHeader.NAME);
		if (route.indexOf("mo")>-1){
			SipUtil.alterRequestContact(req);
			req.setHeader("P-Asserted-Identity", req.getFrom().getURI().toString());
			req.setHeader("Require","precondition");
			action.setAppId("mo");
		}else{
			action.setAppId("mt");
		}
		req.removeFirst(RouteHeader.NAME);
		((SipProxyRequestImpl)req).setSubsequentAction(action);
	}

	@Override
	public void doAck(SipProxyRequest req) throws ServletException, IOException {
		SubsequentAction action =  SubsequentAction.createForwardAction();
		String route = req.getHeader(RouteHeader.NAME);
		if (route.indexOf("mo")>-1){
			SipUtil.alterRequestContact(req);
			req.setHeader("P-Asserted-Identity", req.getFrom().getURI().toString());
			action.setAppId("mo");
		}else{
			action.setAppId("mt");
		}
		req.removeFirst(RouteHeader.NAME);
		((SipProxyRequestImpl)req).setSubsequentAction(action);
	}

	@Override
	public void doBye(SipProxyRequest req) throws ServletException, IOException {
		SubsequentAction action =  SubsequentAction.createForwardAction();
		String route = req.getHeader(RouteHeader.NAME);
		if (route.indexOf("mo")>-1){
			SipUtil.alterRequestContact(req);
			req.setHeader("P-Asserted-Identity", req.getFrom().getURI().toString());
			action.setAppId("mo");
		}else{
			action.setAppId("mt");
		}
		req.removeFirst(RouteHeader.NAME);
		((SipProxyRequestImpl)req).setSubsequentAction(action);
	}

	@Override
	public void doCancel(SipProxyRequest req) throws ServletException, IOException {
		SubsequentAction action =  SubsequentAction.createForwardAction();
		String route = req.getHeader(RouteHeader.NAME);
		if (route.indexOf("mo")>-1){
			SipUtil.alterRequestContact(req);
			req.setHeader("P-Asserted-Identity", req.getFrom().getURI().toString());
			action.setAppId("mo");
		}else{
			action.setAppId("mt");
		}
		req.removeFirst(RouteHeader.NAME);
		((SipProxyRequestImpl)req).setSubsequentAction(action);
	}

	public void doMessage(SipProxyRequest req)  throws ServletException,IOException
	{
		logger.info("doMessage");
		SubsequentAction action =  SubsequentAction.createForwardAction();
		SipUri uri = null;
		RealmConfig d =null;
		if (!SipUtil.isMessageInTerm(req))
		{
			
			uri = (SipUri)req.getFrom().getURI();
			if (onlineUEPool.get(uri) == null){
				req.createResponseAction(404, "you have not login.");
				return;
			}
			onlineUEPool.active(uri);
			
			 d = pcscfConf.getRealmConfig(uri.getHost());
			req.setHeader("P-Asserted-Identity", req.getFrom().getURI().toString());

			req.setHeader("P-Charging-Vector",
					"icid-value=\"P-CSCF"+createIcidValue() //ueIcidValueTable.get(req.getFrom())
					+"\";icid-generated-at="+d.getHost());
			SipUtil.alterRequestContact(req);
			
			action.setAppId("mo");

		}else{
			uri = (SipUri)req.getTo().getURI();
			if (onlineUEPool.get(uri) == null){
				req.createResponseAction(404, "you have not login.");
				return;
			}
			onlineUEPool.active(uri);

			req.removeFirst(RouteHeader.NAME);
			action.setAppId("mt");
		}
		logger.info("return ");
		((SipProxyRequestImpl)req).setSubsequentAction(action);
	}
	
	public void doResponse(SipProxyResponse resp)  throws ServletException,IOException
	{
		//resp.removeHeader(RecordRoute.NAME);
		String method = resp.getMethod();
		logger.info("response status:"+resp.getStatus() + " method:"+method);
		if (resp.getStatus()==180 && method.equals("INVITE")) {
			Request req = resp.getOriginalRequest();
			RecordRouteHeader rh = (RecordRouteHeader)req.getHeaders(RecordRouteHeader.NAME).next();
			String user = ((SipURI)rh.getAddress().getURI()).getUser();
			logger.info("recordRoute user:"+user);
			if (user.equals("mt")){
				logger.info("alter contact");
				SipUtil.alterResponseContact(resp);
			}
		}
		if (resp.getStatus()==200 && 
				(method.equals("INVITE")  || method.equals(Request.PRACK) 
						/*|| method.equals(Request.CANCEL) || method.equals(Request.BYE) */ || method.equals(Request.UPDATE))) {
			//Request req = resp.getOriginalRequest();
			SIPMessage sipMessage = (SIPMessage)resp.getMessage();
			if (sipMessage == null)
			{
				return;
			}
			ViaList viaList = sipMessage.getViaHeaders();
			if (viaList == null)
			{
				logger.info("viaList is null");
				return ;
			}
			ViaHeader via = (ViaHeader)viaList.getFirst();
			if (via == null)
			{
				logger.info("via is null");
				return ;
			}
			if (via.getParameter("app_id").equals("mt"))
			{
				logger.info("alter contact");
				SipUtil.alterResponseContact(resp);
			}
			logger.info("response applyRtpProxy");
			
			if (pcscfConf.getRtpProxy() != null){
				rptProxyCtrlClient.applyRtpProxy(resp);
			}else{
				SipUtil.adjustSdpParameter(resp);
			}
			logger.info("response applyRtpProxy OK");
		}
		if (resp.getStatus()==200 && 
				(method.equals(Request.REGISTER)) )
		{

			onlineUEPool.put(((To)resp.getHeaderInstance(To.NAME)).getAddress().getURI(),(Contact)resp.getHeaderInstance(Contact.NAME));
		}
				

	}
	@Override
	public void doException(Exception ex) {
		logger.info(ex.getMessage(),ex);
	}
	
	public void initRegister(SipProxyRequest request) throws Exception
	{
			String securityClient = request.getHeader("Security-Client");
			if (securityClient != null) {
				// request.getSession().setAttribute("Security-Client",
				// securityClient);
				// forkedRequest.removeHeader("Security-Client");
			}
			SipUri fromUri = (SipUri)request.getFrom().getURI();
			RealmConfig d = pcscfConf.getRealmConfig(fromUri.getHost());
			
			if (d == null)
			{
				request.createResponseAction(500, "no support your realm.");
				return;
			}
			
			ueIcidValueTable.put(request.getFrom(), createIcidValue());
			request.setHeader("P-Charging-Vector",
					"icid-value=\"P-CSCF"+ueIcidValueTable.get(request.getFrom())
					+"\";icid-generated-at="+d.getHost()+";orig-ioi=\""+d.getName()+"\"");
			
			request.setHeader("P-Visited-Network-ID", d.getName());
			request.setHeader("Require", "path");
			request.setHeader("path", "<sip:term@"+d.getHost()+":"+d.getPort()+";lr>");

			// String[] authParam
			// 120.24.95.155
			SipURI requestUri = (SipURI)request.getFrom().getURI();
			Address address = DnsUtil.resolveICSCFHostByDNS(requestUri.getHost(),"UDP",pcscfConf.getDnsServer());
			logger.info("icscf:"+address.getURI().toString());
			request.pushRoute(address);
			request.createForwardAction();
	}



	public void unRegister(SipProxyRequest request) {

		try {
			SipUri fromUri = (SipUri)request.getFrom().getURI();
			RealmConfig d = pcscfConf.getRealmConfig(fromUri.getHost());
			
			request.setHeader("P-Visited-Network-ID", d.getName());
			request.setHeader("path", "<sip:term@"+d.getHost()+":"+d.getPort()+";lr>");
			SipURI requestUri = (SipURI)request.getRequestURI();
			Address address = DnsUtil.resolveICSCFHostByDNS(requestUri.getHost(),"UDP",pcscfConf.getDnsServer());
			
			//Address address =SipFactory.getInstance().createAddressFactory().createAddress("sip:120.24.95.155:5060");
			request.pushRoute(address);
		} catch (Exception e) {
			logger.info(e.getMessage(),e);
		}

	}

	private String pcscfConfigFile = null;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		logger.info("init PServlet");
		pcscfConfigFile = servletConfig.getInitParameter("configFile");
		try{
		Config config = Config.load(pcscfConfigFile);
		pcscfConf = config.getPConfig();
		logger.info("pcscfconfig is null:"+(pcscfConf == null));
		ServletContext sc = getServletContext();
		sc.setAttribute("configInstance", pcscfConf);
		logger.info("servletContext:"+sc);
		
		}catch(Exception e)
		{
			logger.info("init pconfig heppend exception.");
			logger.info(e.getMessage(),e);
		}
		
		try{

			if (pcscfConf.getRtpProxy() != null){
			logger.info("rtpproxy host:"+pcscfConf.getRtpProxy().getHost());
			logger.info("rtpproxy port:"+pcscfConf.getRtpProxy().getPort());
			rptProxyCtrlClient = 
					new RtpProxyControlClient(pcscfConf.getRtpProxy().getHost(),pcscfConf.getRtpProxy().getPort());
			}
			onlineUEPool =
					new CacheObjectPool<URI,Contact>( new OnlineUEEventListener() );
			onlineUEPool.setMaxIdleTime(10* 60 * 1000);
		}catch(Exception e)
		{
			logger.info(e.getMessage(),e);
		}
	}



}
class OnlineUEEventListener implements CacheEventListener
{

	@Override
	public void onEvent(CacheEvent event) {

		
	}
}