package org.freeims.common;

import java.text.ParseException;

import javax.sdp.Connection;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.Parameters;

import org.apache.log4j.Logger;
import org.freeims.sipproxy.servlet.SipProxyMessage;
import org.freeims.sipproxy.servlet.SipProxyRequest;
import org.freeims.sipproxy.servlet.SipProxyResponse;
import org.freeims.sipproxy.servlet.impl.SipProxyMessageImpl;

import org.freeims.javax.sip.address.SipUri;
import org.freeims.javax.sip.header.Authorization;
import org.freeims.javax.sip.header.Contact;
import org.freeims.javax.sip.header.From;
import org.freeims.javax.sip.header.To;
import org.freeims.javax.sip.header.Via;
import org.freeims.javax.sip.header.ViaList;
import org.freeims.javax.sip.message.SIPMessage;

public class SipUtil {
	private static Logger logger = Logger.getLogger(SipUtil.class);

	public void pushTopVia(SipProxyMessage req, SipURI uri, String user) {

	}

	public void pushTopVia(SipProxyMessage req, String viaTxt) {

	}

	public static boolean isSameRealm(From from, To to) {
		return isSameRealm(from.getAddress(), to.getAddress());
	}

	public static boolean isSameRealm(Address from, Address to) {
		return ((SipURI) from.getURI()).getHost().equalsIgnoreCase(((SipURI) to.getURI()).getHost());
	}

	public static boolean isMessageInTerm(SipProxyMessage message) {
		String routeAddress = message.getHeader("Route");
		boolean isInTerm = true;
		if (routeAddress == null) {

		} else {
			if (routeAddress.indexOf("sip:orig@") > -1) {
				isInTerm = false;
			}
		}
		return isInTerm;
	}

	public static String extractRealm(SipProxyRequest req) {
		Parameters p = req.getParameterableHeader(Authorization.NAME);

		if (p != null) {
			return ((Authorization) p).getRealm();
		}
		SipURI fromURI = (SipURI) req.getFrom().getURI();
		return fromURI.getHost();
	}

	public static String extractUsername(SipProxyRequest req) {
		Authorization auth = (Authorization) req.getParameterableHeader(Authorization.NAME);

		if (auth != null) {
			return auth.getUsername();
		}
		SipURI fromURI = (SipURI) req.getFrom().getURI();
		return fromURI.getUser()+"@"+fromURI.getHost();

	}

	public static boolean isRegister(SipProxyRequest req) {
		String expires = req.getHeader("Expires");
		Parameters paramable = req.getParameterableHeader("Contact");
		String expires2 = paramable.getParameter("Expires");

		if ((expires != null && expires.equals("0")) || (expires2 != null && expires2.equals("0"))) {
			return false;
		}
		return true;
	}

	public static void alterRequestURI(SipProxyRequest req, SipURI uri) {
		SipURI reqURI = (SipURI) req.getRequestURI();
		reqURI.setPort(uri.getPort());
		try {
			reqURI.setHost(uri.getHost());
		} catch (ParseException pe) {

		}
	}
	private static final String CONTENT_TYPE_SDP = "application/sdp";
	public static void adjustSdpParameter(SipProxyMessage message)
	{	
		if ( message.getContentLength() == 0 ||  !message.getContentType().equals("application/sdp") )
		{
		return ;
		}
		SdpFactory sdpFactory = SdpFactory.getInstance();
		try {
			SessionDescription sd = sdpFactory.createSessionDescription(new String(message.getRawContent()));

			Connection cn = sd.getConnection();
			
			String address = cn.getAddress();
			
			if (address == null || address.equals(""))
			{
				return;
			}
			if (!address.equals("0.0.0.0"))
			{
				return;
			}
			Contact c = (Contact)message.getHeaderInstance(Contact.NAME);
			URI uri = c.getAddress().getURI();
			if (!uri.isSipURI())
			{
				return;
			}
			SipURI sipUri = (SipURI)uri;
			logger.info("set contact host to sdp conection:"+sipUri.getHost().toString());
			cn.setAddress(sipUri.getHost().toString());
			sd.getOrigin().setAddress(sipUri.getHost().toString());
			message.setContent(sd.toString(), CONTENT_TYPE_SDP);
		}catch(SdpParseException e)
		{
			logger.info(e.getMessage(),e);
		}catch(SdpException e)
		{
			logger.info(e.getMessage(),e);
		}
	}
	
	public static void alterRequestContact(SipProxyRequest req) {
		SIPMessage sipMessage = (SIPMessage) ((SipProxyMessageImpl) req).getMessage();
//		logger.info("host:" + sipMessage.getPeerPacketSourceAddress().getHostAddress() + " port:"
//				+ sipMessage.getPeerPacketSourcePort());

		ViaList viaList = sipMessage.getViaHeaders();
		Via via = (Via) viaList.getFirst();
		boolean hasRPort = via.hasParameter(Via.RPORT);

		boolean hasReceived = via.hasParameter(Via.RECEIVED);

//		if (hasRPort) {
//			logger.info("rPort:" + via.getRPort());
//
//			try {
//				via.setPort(via.getRPort());
//			} catch (Exception e) {
//				logger.info(e.getMessage(), e);
//			}
//		}
//
//		if (hasReceived) {
//			try {
//				logger.info("received:" + via.getReceived());
//				via.setHost(via.getReceived());
//			} catch (Exception e) {
//				logger.info(e.getMessage(), e);
//			}
//		}

		Contact c = sipMessage.getContactHeader();
		SipUri contactAddress = null;
		if (c != null) {
			if (c.getAddress() != null) {
				if (c.getAddress().getURI().isSipURI()) {
					contactAddress = (SipUri) c.getAddress().getURI();
					if (hasRPort) {
						logger.info("set contact  port:" + via.getRPort());
						contactAddress.setPort(via.getRPort());
					}
					if (hasReceived) {
						try {
							logger.info("set contact host:" + via.getReceived());
							contactAddress.setHost(via.getReceived());
						} catch (Exception e) {
						}
					}
				} else {
					logger.info("contact.address is not SipUri:" + c.getAddress().getClass().getName());
				}
			} else {
				logger.info("contact.address is null");
			}
		} else {
			logger.info("contact is null");
		}
	}

	public static void alterResponseContact(SipProxyResponse resp) {
		SIPMessage sipMessage = (SIPMessage) ((SipProxyMessageImpl) resp).getMessage();
		logger.info("response host:" + sipMessage.getRemoteAddress().getHostAddress() + " port:"
				+ sipMessage.getRemotePort());

		Contact c = sipMessage.getContactHeader();
		SipUri contactAddress = null;
		if (c != null) {
			if (c.getAddress() != null) {
				if (c.getAddress().getURI().isSipURI()) {
					contactAddress = (SipUri) c.getAddress().getURI();

					contactAddress.setPort(sipMessage.getRemotePort());

					try {
						logger.info("set contact host:" + sipMessage.getRemoteAddress().getHostAddress());
						contactAddress.setHost(sipMessage.getRemoteAddress().getHostAddress());
					} catch (Exception e) {
					}

				} else {
					logger.info("contact.address is not SipUri:" + c.getAddress().getClass().getName());
				}
			} else {
				logger.info("contact.address is null");
			}
		} else {
			logger.info("contact is null");
		}
	}
}
