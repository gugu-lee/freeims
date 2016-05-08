package org.freeims.sipproxy.servlet;

import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

public class SipProxyFactory {
	private static AddressFactory addressFactory;

	private static MessageFactory messageFactory;

	private static HeaderFactory headerFactory;
	private static SipFactory sipFactory = SipFactory.getInstance();

	static {
		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
		} catch (Exception e) {
		}
	}

	public static AddressFactory getAddressFactory() {
		if (addressFactory == null) {
			try {
				addressFactory = sipFactory.createAddressFactory();

			} catch (Exception e) {
			}
		}
		return addressFactory;
	}
	public static MessageFactory getMessageFactory() {
		if (messageFactory == null) {
			try {
				messageFactory = sipFactory.createMessageFactory();

			} catch (Exception e) {
			}
		}
		return messageFactory;
	}
	public static HeaderFactory getHeaderFactory() {
		if (headerFactory == null) {
			try {
				headerFactory = sipFactory.createHeaderFactory();

			} catch (Exception e) {
			}
		}
		return headerFactory;
	}
}
