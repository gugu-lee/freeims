package org.freeims.sipproxy.servlet;

import javax.servlet.ServletResponse;
import javax.sip.message.Request;

public interface SipProxyResponse extends ServletResponse,SipProxyMessage
{
	public int getStatus();
	public String getReasonPhrase();
	public Request getOriginalRequest();
	

}
