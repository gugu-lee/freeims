package org.freeims.sipproxy.servlet.impl;

import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.message.Request;

import org.freeims.sipproxy.servlet.SipProxyRequest;
import org.freeims.sipproxy.servlet.SipProxyResponse;

public class SipProxyMessageFactory {

	public static SipProxyRequest createRequest(Request req,ServerTransaction st) {
		SipProxyRequestImpl request = new SipProxyRequestImpl();

		request.setMessage(req);
		request.setTranslation(st);
		return request;
	}

	public static SipProxyResponse createResponse(ResponseEvent event) {
		SipProxyResponseImpl resp = new SipProxyResponseImpl();
		resp.setMessage(event.getResponse());
		if (event.getClientTransaction() != null) {
			resp.setOriginalRequest(event.getClientTransaction().getRequest());
		}
		resp.setTranslation(event.getClientTransaction());
		return resp;
	}
}
