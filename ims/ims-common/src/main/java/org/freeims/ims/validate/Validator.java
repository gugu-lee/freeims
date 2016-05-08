package org.freeims.ims.validate;

import org.freeims.sipproxy.servlet.SipProxyRequest;

public interface Validator 
{
	public boolean validate(SipProxyRequest request);
}
