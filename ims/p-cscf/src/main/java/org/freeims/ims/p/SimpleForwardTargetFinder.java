package org.freeims.ims.p;

import javax.sip.address.Address;

import org.freeims.sipproxy.servlet.SipProxyMessage;


public class SimpleForwardTargetFinder implements ForwardTargetFinder
{

	@Override
	public Address findTarget(SipProxyMessage message) {
		
		return null;
	}

}
