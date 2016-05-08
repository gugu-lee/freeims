package org.freeims.ims.s.handler;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.sip.SipFactory;
import javax.sip.address.URI;
import javax.sip.header.ContactHeader;
import org.apache.log4j.Logger;
import org.freeims.common.SipUtil;
import org.freeims.diameter.CxFinalResultException;
import org.freeims.diameter.DiameterConstants;
import org.freeims.diameter.DiameterUtil;
import org.freeims.diameter.UtilAVP;
import org.freeims.diameter.auth.DigestAKA;
import org.freeims.diameter.auth.HexCodec;
import org.freeims.diameter.auth.MD5Util;
import org.freeims.diameter.cx.CxConstants;
import org.freeims.diameterpeer.DiameterPeer;
import org.freeims.diameterpeer.data.AVP;
import org.freeims.diameterpeer.data.AVPDecodeException;
import org.freeims.diameterpeer.data.DiameterMessage;
import org.freeims.ims.cache.CacheEvent;
import org.freeims.ims.cache.CacheEventListener;
import org.freeims.ims.cache.CacheObjectPool;
import org.freeims.ims.config.RealmConfig;
import org.freeims.ims.config.SCscfConfig;
import org.freeims.ims.s.handler.UEStoreInfo;
import org.freeims.javax.sip.header.Authorization;
import org.freeims.javax.sip.header.Contact;
import org.freeims.javax.sip.header.ims.Path;
import org.freeims.sipproxy.servlet.SipProxyRequest;
import org.freeims.sipproxy.servlet.SipProxyResponse;
import org.freeims.javax.sip.header.CSeq;
import org.freeims.javax.sip.header.CallID;
import org.freeims.javax.sip.header.To;
import org.freeims.javax.sip.header.WWWAuthenticate;

public class RegisterHandler implements CacheEventListener
{

	private DiameterPeer diameterPeer = null;
	private static Logger logger = Logger.getLogger(RegisterHandler.class);
	private SCscfConfig scscfConf = null;
	private CacheObjectPool<URI,UEStoreInfo> onlineUEPool = null;
	public RegisterHandler(SCscfConfig scscfConf,CacheObjectPool<URI,UEStoreInfo> onlineUEPool,DiameterPeer diameterPeer)
	{
		this.diameterPeer = diameterPeer;
//		URLClassLoader cl = (URLClassLoader)RegisterHandler.class.getClassLoader();
//		for (URL u:cl.getURLs()){
//			logger.info("url:"+u.toString());
//		}

		//diameterPeer.addEventListener(this);
		this.scscfConf = scscfConf;
		this.onlineUEPool = onlineUEPool;
	}
	public boolean handlerInitRegister(SipProxyRequest req) {

		DiameterMessage message = createMAR(req);
		logger.info("createMAR OK");
		try {
			DiameterMessage maa = sendMAR(req, message);
			logger.info("maa is null:"+(maa == null));
			if (maa == null) {
				req.createResponseAction(403, "send MAR message failed.");
				return false;
			}
			logger.info("receive MAA");
			processMAA(req,maa);
			return true;
		} catch (Exception e) {
			logger.info(e.getMessage(),e);
			req.createResponseAction(500, "Server Error. at S-CSCF");
		}

		return true;
	}

	public void handlerChallengeRequest(SipProxyRequest req) {
		Authorization auth = (Authorization) req.getParameterableHeader(Authorization.NAME);
		byte password[] = userAuthorizations.get(auth.getUsername());
		byte[] ha1 = MD5Util.av_HA1(auth.getUsername().getBytes(), auth.getRealm().getBytes(), password);
		userAuthorizations.remove(auth.getUsername());
		byte content[] = req.getRawContent();
		if (content == null) {
			content = "".getBytes();
		}
		byte[] h = MD5Util.calc_H(content);
		int nc = auth.getNonceCount();

		String uri = auth.getURI().toString();

		byte[] UERespData = MD5Util.calcResponse(HexCodec.encode(ha1).getBytes(), auth.getNonce().getBytes(),
				"REGISTER".getBytes(), uri.getBytes(), String.format("%08d", nc).getBytes(),
				auth.getCNonce().getBytes(), "auth-int".getBytes(), 1, h);
		logger.info("we expect:" + new String(UERespData));
		logger.info("UE said:" + auth.getResponse());
		DiameterMessage message = this.createSAR(req,CxConstants.Server_Assignment_Type_Registration);
		if (auth.getResponse().equals(new String(UERespData))) {
			SipProxyResponse resp = req.createResponse(200);
			resp.addHeader(req.getHeaderInstance(Contact.NAME));
			resp.addHeader("Service-Route", "<sip:orig@scscf.saygreet.com:6060;lr>");
			resp.addHeader("P-Associated-URI", resp.getFrom().getURI().toString());
			//P-Associated-URI
			req.sendResponse(resp);
			try {
				UEStoreInfo ue = new UEStoreInfo();
				ue.setContact((ContactHeader)req.getParameterableHeader(ContactHeader.NAME));
				ue.setPublicIdentity(req.getFrom().toString());
				ue.setPath(((Path)req.getParameterableHeader(Path.NAME)).getAddress());
				logger.info("put current user into into cache");
				onlineUEPool.put(req.getTo().getURI(), ue);
				
				
					DiameterMessage saa = sendSAR(message);
					if (saa == null) {
						req.createResponseAction(403, "send SAR message failed.");
						return;
					}
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			req.sendResponse(480, "authoricate fail");
		}
	}

	public void handlerUnregister(SipProxyRequest req) {
		req.sendResponse(200);
		DiameterMessage message = this.createSAR(req, CxConstants.Server_Assignment_Type_Unregistered_User);

		try {
			DiameterMessage saa = sendSAR(message);
			if (saa == null) {
				// return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DiameterMessage createMAR(SipProxyRequest req) {
		DiameterMessage message = new DiameterMessage(DiameterConstants.Command.MAR, true, false,
				DiameterConstants.Application.Cx,5,8/* DiameterUtil.nextHopByHopId(), DiameterUtil.nextEndToEndId()*/);
		String realm = SipUtil.extractRealm(req);
		
		//Authorization auth = (Authorization) req.getParameterableHeader(Authorization.NAME);
		String username = SipUtil.extractUsername(req);
		
		RealmConfig realmConf = scscfConf.getRealmConfig(realm);
		
		UtilAVP.addPublicIdentity(message, req.getFrom().getURI().toString());

		UtilAVP.addUserName(message, username);
		UtilAVP.addDestinationRealm(message, realm);
		UtilAVP.addDestinationHost(message, realmConf.getHssAddress());
		//"sip:scscf.open-ims.test:6060"
		UtilAVP.addServerName(message, "sip:"+realmConf.getHost()+":"+realmConf.getPort());
		UtilAVP.addOriginatingHost(message, realmConf.getHost());

		UtilAVP.addSIPNumberAuthItems(message, 1);
		AVP sipAuthDataItem = new AVP(DiameterConstants.AVPCode.IMS_SIP_AUTH_DATA_ITEM, true,
				DiameterConstants.Vendor.V3GPP);
		AVP child0 = new AVP(DiameterConstants.AVPCode.IMS_SIP_AUTHENTICATION_SCHEME, false,
				DiameterConstants.Vendor.V3GPP);
		child0.setData(CxConstants.Auth_Scheme_AKAv1_Name);
		sipAuthDataItem.addChildAVP(child0);
		message.addAVP(sipAuthDataItem);
		return message;
	}

	private DiameterMessage createSAR(String realm,String publicIdentity,String username,int serverAssignmentType) {
		
		RealmConfig realmConf = scscfConf.getRealmConfig(realm);
		
		DiameterMessage message = new DiameterMessage(DiameterConstants.Command.SAR, true, false,
				DiameterConstants.Application.Cx, DiameterUtil.nextHopByHopId(), DiameterUtil.nextEndToEndId());


		logger.info("create SAR: publicIdentity:"+publicIdentity+":");
		UtilAVP.addPublicIdentity(message, publicIdentity);
		

		logger.info("create SAR: username:"+username+":");
		UtilAVP.addUserName(message,username);
		UtilAVP.addDestinationRealm(message, realmConf.getName());

		UtilAVP.addDestinationHost(message,realmConf.getHssAddress());
		UtilAVP.addServerName(message, "sip:"+realmConf.getHost()+":"+realmConf.getPort());
		UtilAVP.addOriginatingHost(message, realmConf.getHost());

		UtilAVP.addOriginatingRealm(message, realmConf.getName());
		UtilAVP.addVendorSpecificApplicationID(message, DiameterConstants.Vendor.V3GPP,
				DiameterConstants.Application.Cx);
		
		UtilAVP.addAuthSessionState(message, 1);
		UtilAVP.addServerAssignmentType(message, serverAssignmentType /**/);
		UtilAVP.addUserDataAlreadyAvailable(message, 1);
		return message;
	}
	private DiameterMessage createSAR(SipProxyRequest req,int serverAssignmentType) {

		String realm = SipUtil.extractRealm(req);
		String publicIdentity = req.getFrom().getURI().toString();
		logger.info("create SAR: publicIdentity:"+publicIdentity+":");		
		Authorization auth = (Authorization) req.getParameterableHeader(Authorization.NAME);
		String username = auth.getUsername();
		logger.info("create SAR: username:"+username+":");
		
		return createSAR(realm,publicIdentity,username,serverAssignmentType);
	}
	
	private static ConcurrentHashMap<String,SipProxyRequest> waitCx_SipProxyRequest = new ConcurrentHashMap<String,SipProxyRequest>();
	private static ConcurrentHashMap<String,byte[]> userAuthorizations = new ConcurrentHashMap<String,byte[]>();
	

	private DiameterMessage sendMAR(SipProxyRequest sipRequest,DiameterMessage message)
	{

		//logger.info("send diametermessage username:"+UtilAVP.getUserName(message));
		//waitCx_SipProxyRequest.put(UtilAVP.getUserName(message), sipRequest);
		logger.info("send MAR");
		return diameterPeer.sendRequestBlocking(message);
	}
	
	private byte hexchars[]={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	private int bin_to_base16(byte []from,int len, byte[] to)
	{
		int i,j;
		for(i=0,j=0;i<len;i++,j+=2){
			to[j] = hexchars[((from[i]) >>4 )&0x0F];
			to[j+1] = hexchars[((from[i]))&0x0F];
		}	
		return 2*len;
	}
	public DiameterMessage sendSAR(DiameterMessage message)
	{
		return diameterPeer.sendRequestBlocking(message);
		
	}
	public void processMAA(SipProxyRequest sipRequest, DiameterMessage message)
	{
		logger.info("process MAA");
		if (message.flagError)
		{
			sipRequest.sendResponse(404,"username is not found .by S-CSCF");
			return;
		}
		String username = UtilAVP.getUserName(message);
		logger.info("MAA username:"+username);

		try{
			SipProxyResponse sipResponse = null;

			
			
			
			String realm = SipUtil.extractRealm(sipRequest);
			
			RealmConfig realmConf = scscfConf.getRealmConfig(realm);

			sipResponse = sipRequest.createResponse(401);
			sipResponse.getMessage().setHeader(sipRequest.getMessage().getHeader(CallID.NAME));
			sipResponse.getMessage().setHeader(sipRequest.getMessage().getHeader(CSeq.NAME));
			
			sipResponse.addHeader("Allow","INVITE, ACK, CANCEL, OPTIONS, BYE, REFER, SUBSCRIBE, NOTIFY, PUBLISH, MESSAGE, INFO");
			
			sipResponse.addHeader("Server",realmConf.getName());

			//sipResponse.addHeader("Path","<sip:term@pcscf.saygreet.com:4060;lr>");
			
			To to = (To)sipResponse.getMessage().getHeader(To.NAME);
			to.setTag(String.valueOf(System.currentTimeMillis()));
			int auth_scheme = -1;
			//byte[] authorization = null;

			AVP authDataItem = UtilAVP.getSipAuthDataItem(message);
			if (authDataItem == null){
	                            logger.warn("SIP-Auth-Data-Item AVP not found");
				throw new CxFinalResultException(DiameterConstants.ResultCode.DIAMETER_MISSING_AVP);
			}
			
			try {
				authDataItem.ungroup();
			} catch (AVPDecodeException e) {
				e.printStackTrace();
				throw new CxFinalResultException(DiameterConstants.ResultCode.DIAMETER_MISSING_AVP);
			}
			
			Vector<AVP> childs = authDataItem.childs;
			if (childs == null){
				throw new CxFinalResultException(DiameterConstants.ResultCode.DIAMETER_MISSING_AVP);
			}
			String authSchemeName = null;
			Iterator it = childs.iterator();			
			while (it.hasNext()){
				AVP child = (AVP)it.next();
				if (child.code == DiameterConstants.AVPCode.IMS_SIP_AUTHENTICATION_SCHEME){
					String data = new String(child.getData());
					authSchemeName=data;
					if (data.equals(CxConstants.Auth_Scheme_AKAv1_Name)){
						auth_scheme = CxConstants.Auth_Scheme_AKAv1; 
					}
					else if (data.equals(CxConstants.Auth_Scheme_AKAv2_Name)){
						auth_scheme = CxConstants.Auth_Scheme_AKAv2;
					}
					else if (data.equals(CxConstants.Auth_Scheme_MD5_Name)){
						auth_scheme = CxConstants.Auth_Scheme_MD5;
					}
					else if (data.equals(CxConstants.Auth_Scheme_Digest_Name)){
						auth_scheme = CxConstants.Auth_Scheme_Digest;
					}
					else if (data.equals(CxConstants.Auth_Scheme_SIP_Digest_Name)){
						auth_scheme = CxConstants.Auth_Scheme_SIP_Digest;
					}
					else if (data.equals(CxConstants.Auth_Scheme_HTTP_Digest_MD5_Name)){
						auth_scheme = CxConstants.Auth_Scheme_HTTP_Digest_MD5;
					}
					else if (data.equals(CxConstants.Auth_Scheme_NASS_Bundled_Name)){
						auth_scheme = CxConstants.Auth_Scheme_NASS_Bundled;
					}
					else if (data.equals(CxConstants.Auth_Scheme_Early_Name)){
						auth_scheme = CxConstants.Auth_Scheme_Early;
					}
					else if (data.equals(CxConstants.Auth_Scheme_Unknown_Name) || data.equals(CxConstants.Auth_Scheme_Unknown_Name_2)){
						
						
					}

				}
				else if (child.code == DiameterConstants.AVPCode.IMS_SIP_AUTHORIZATION){
					//authorization = child.data;
				}
			}

			
			if (auth_scheme == -1){
	             logger.warn("SIP-Authentication-Scheme not found or has invalid value");
				//throw new CxFinalResultException(DiameterConstants.ResultCode.DIAMETER_MISSING_AVP);
			}
			logger.info("auth_scheme:"+auth_scheme);
			WWWAuthenticate authHeader = (WWWAuthenticate)SipFactory.getInstance().createHeaderFactory().createWWWAuthenticateHeader("Digest");
			String nonce = null;
					byte []ik = new byte[32];
					byte []ck = new byte[32];
			

			for (AVP item:childs)
			{
				logger.info("child code:"+item.code);
				switch (item.code)
				{
	
				case DiameterConstants.AVPCode.IMS_SIP_AUTHENTICATE:
					nonce = new String(DigestAKA.bin2base64(item.data));
					break;
				case DiameterConstants.AVPCode.IMS_SIP_AUTHORIZATION:
					logger.info("set AVPCode.IMS_SIP_AUTHORIZATION:"+item.data.length);
					 logger.info(HexCodec.encode(item.data));
					userAuthorizations.put(username, item.data);
					break;
				case DiameterConstants.AVPCode.IMS_INTEGRITY_KEY:
					bin_to_base16(item.data,16,ik);
					break;
				case  DiameterConstants.AVPCode.IMS_CONFIDENTIALITY_KEY:
					bin_to_base16(item.data,16,ck);
					break;
				}
			}
			
			//Authorization auth = (Authorization) sipRequest.getParameterableHeader(Authorization.NAME);


			
	        authHeader.setRealm(realm);
	        authHeader.setAlgorithm("AKAv1-MD5");
	        authHeader.setNonce(nonce);

	        authHeader.setIK(new String(ik));
	        authHeader.setCK(new String(ck));
	        authHeader.setQop("auth,auth-int");
	        sipResponse.getMessage().addHeader(authHeader);
	        sipResponse.addHeader("Service-Route", "<sip:orig@"+realmConf.getHost()+":"+realmConf.getPort()+";lr>");
	        sipRequest.sendResponse(sipResponse);
		}catch(Exception e)
		{
			logger.error(e.getMessage(),e);
			sipRequest.sendResponse(500);
		}
		/*
		WWW-Authenticate: Digest realm="saygreet.com",
		                         nonce="WcUFAi5sH1JFNwV2jI46B3MutjWstgAAQ4p1b9RQ1jM=",
		                         algorithm=AKAv1-MD5,
		                         ck="b17720415b8881fdf83133549529be3d",
		                         ik="f2ce9632634d1f9036aee1e8c708ff43",
		                         qop="auth,auth-int"
		
		str S_WWW_Authorization_AKA={"WWW-Authenticate: Digest realm=\"%.*s\","
				" nonce=\"%.*s\", algorithm=%.*s, ck=\"%.*s\", ik=\"%.*s\"%.*s\r\n",106};
	
		sprintf(x.s,S_WWW_Authorization_AKA.s,
				realm.len,realm.s,
				av->authenticate.len,av->authenticate.s,
				algorithm_types[av->type].len,algorithm_types[av->type].s,
				ck_len,ck,
				ik_len,ik,
				registration_qop_str.len,registration_qop_str.s);
				
			str S_WWW_Authorization_MD5={"WWW-Authenticate: Digest realm=\"%.*s\","
				" nonce=\"%.*s\", algorithm=%.*s%.*s\r\n",101};
				
			sprintf(x.s,S_WWW_Authorization_MD5.s,
				realm.len,realm.s,
				av->authenticate.len,av->authenticate.s,
				algorithm_types[AUTH_MD5].len,algorithm_types[AUTH_MD5].s,
				registration_qop_str.len,registration_qop_str.s);
	
			*/
		
		
//		// 3. check if the Authentication-Scheme is supported
//		if ((auth_scheme & impi.getAuth_scheme()) == 0){
//			throw new CxExperimentalResultException(
//					DiameterConstants.ExperimentalResultCode.RC_IMS_DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED);
//		}
//		
//		// 4. Synchronisation
//		String scscf_name = IMSU_DAO.get_SCSCF_Name_by_IMSU_ID(session, impi.getId_imsu());
//		String server_name = UtilAVP.getServerName(request);
//		if (server_name == null){
//			throw new CxFinalResultException(DiameterConstants.ResultCode.DIAMETER_MISSING_AVP);
//		}
//		
//		if (authorization != null){
//			if (scscf_name == null || scscf_name.equals("")) {
//				logger.error("Synchronization for: " + impi.getIdentity() + " but no scscf_name in db!");
//				throw new CxFinalResultException(DiameterConstants.ResultCode.DIAMETER_UNABLE_TO_COMPLY);
//			}
//			 
//			if (server_name.equals(scscf_name)){
//				AuthenticationVector av = null;
//				if ((av = synchronize(session, authorization, auth_scheme, impi)) == null){
//					throw new CxFinalResultException(DiameterConstants.ResultCode.DIAMETER_UNABLE_TO_COMPLY);
//				}
//				UtilAVP.addPublicIdentity(response, publicIdentity);
//				UtilAVP.addUserName(response, privateIdentity);
//				
//				List avList = new LinkedList();
//				avList.add(av);
//				
//				// add the number of auth items (is 1 for synch)
//				UtilAVP.addSIPNumberAuthItems(response, 1);
//				UtilAVP.addAuthVectors(response, avList);
//				UtilAVP.addResultCode(response, DiameterConstants.ResultCode.DIAMETER_SUCCESS.getCode());
//				return response;
//			}
//		}
	}
	@Override
	public void onEvent(CacheEvent event) {
		if (event.getEventType() != CacheEvent.EventType.REMOVE)
		{
			return ;
		}
		UEStoreInfo ueInfo = (UEStoreInfo)event.getSource();
		
		DiameterMessage message = createSAR(ueInfo.getRealm(),ueInfo.getPublicIdentity(),ueInfo.getUsername(), CxConstants.Server_Assignment_Type_Timeout_Deregistration);
		try {
			DiameterMessage saa = sendSAR(message);
			if (saa == null) {
				// return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		String addr = "sip:bob@open-ims.test";
		System.out.println(addr.substring(4));
		
		int pos = addr.indexOf('@');
		System.out.println(addr.substring(pos+1));
	}

}
