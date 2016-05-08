package org.freeims.sipproxy.servlet.impl;

import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;

public class SipProxyResponseWrapper extends ServletResponseWrapper
{

	public SipProxyResponseWrapper(ServletResponse response) {
		super(response);
	}
	public SipProxyResponseWrapper(ServletResponse response,
            boolean included)
	{
		super(response);
	}
	public void recycle()
	{
		
	}
}
