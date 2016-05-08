package org.freeims.sipproxy.servlet;

import javax.sip.Dialog;
import javax.sip.address.Address;
import javax.sip.address.URI;
import javax.sip.header.Header;
import javax.sip.header.Parameters;
import javax.sip.message.Message;

public interface SipProxyMessage {

	public Message getMessage();
	public String getMethod();
	public boolean isInitial();
	public Address getFrom();
	public Address getTo();
	
	
	public void addHeader(Header h);
	public void addHeader(String name, String value);
	public String getHeader(String name);
	public Header getHeaderInstance(String name);
	public Parameters getParameterableHeader(String name);
	public void setHeader(String name, String value);
	public void setHeader(Header h);
	
	
	public void pushRoute(Address address);
	public int getContentLength();
	public String getContentType();
	public byte[] getRawContent();
	public void setContent(Object content,String type);
	public String getCharacterEncoding();

	public void removeHeader(String name);
	public void removeFirst(String name);

	public Dialog getDialog();

}
