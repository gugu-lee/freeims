package org.freeims.ims.p;

import javax.sip.address.Address;


import org.freeims.sipproxy.servlet.SipProxyMessage;

public interface ForwardTargetFinder {
	public Address findTarget(SipProxyMessage message);
}
