package org.freeims.sipproxy.servlet.impl;

import java.text.ParseException;
import java.util.List;

import javax.sip.Dialog;
import javax.sip.PeerUnavailableException;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.Transaction;
import javax.sip.address.Address;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.Parameters;
import javax.sip.header.ToHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.freeims.sipproxy.container.JainSipUtils;
import org.freeims.sipproxy.servlet.SipProxyMessage;
import org.freeims.sipproxy.servlet.SipProxyRequest;

import org.freeims.javax.sip.address.AddressImpl;
import org.freeims.javax.sip.header.HeaderExt;
import org.freeims.javax.sip.header.SIPHeader;
import org.freeims.javax.sip.header.To;

public  class SipProxyMessageImpl implements SipProxyMessage {

	private static Logger logger = Logger.getLogger(SipProxyMessageImpl.class);
	protected Message message;

	private String[] initialMethod = { SipProxyRequest.REGISTER, SipProxyRequest.INVITE };
	private Transaction translation = null;
	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	@Override
	public String getMethod() {
		if (message instanceof Request) {
			return ((Request) message).getMethod();
		}

		CSeqHeader h = (CSeqHeader) message.getHeader(CSeqHeader.NAME);

		return h.getMethod();

	}
	
	public Transaction getTranslation() {
		return translation;
	}

	public void setTranslation(Transaction translation) {
		this.translation = translation;
	}

	@Override
	public Dialog getDialog() {
		return translation.getDialog();
	}

	public boolean isInitial() {
		for (String method : initialMethod) {
			if (getMethod().equals(method)) {
				return true;
			}
		}
		return false;
	}

	public Address getFrom() {
		FromHeader from = (FromHeader) this.message.getHeader(FromHeader.NAME);
		return from.getAddress();
	}

	public Address getTo() {
		ToHeader to = (ToHeader) this.message.getHeader(To.NAME);
		return to.getAddress();
	}

	public String getHeader(String name) {
		SIPHeader header = (SIPHeader) this.message.getHeader(name);
		if (header == null)
		{
			return null;
		}
		
		return header.getValue();
	}

	public Parameters getParameterableHeader(String name) {
		Header h = message.getHeader(name);
		if (h instanceof Parameters) {
			return (Parameters) h;
		}
		return null;

	}

	public Header getHeaderInstance(String name)
	{
		return this.message.getHeader(name);
	}
	public void setHeader(String name, String value) {
		if (name == null) {
			throw new NullPointerException("name parameter is null");
		}
		if (value == null) {
			throw new NullPointerException("value parameter is null");
		}

		try {
			// Dealing with Allow:INVITE, ACK, CANCEL, OPTIONS, BYE kind of
			// headers
			if (JainSipUtils.LIST_HEADER_NAMES.contains(name)) {
				this.message.removeHeader(name);
				List<Header> headers = SipFactory.getInstance().createHeaderFactory().createHeaders(name + ":" + value);
				for (Header header : headers) {
					this.message.addHeader(header);
				}
			} else {
				// dealing with non list headers and extension header
				Header header = SipFactory.getInstance().createHeaderFactory().createHeader(name, value);
				this.message.setHeader(header);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Error creating header!", e);
		}
	}

	public void pushRoute(Address address) {
		javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) ((AddressImpl) address).getURI();
		pushRoute(sipUri);
	}

	private void pushRoute(javax.sip.address.SipURI sipUri) {

		sipUri.setLrParam();
		try {
			javax.sip.header.Header p = SipFactory.getInstance().createHeaderFactory()
					.createRouteHeader(SipFactory.getInstance().createAddressFactory().createAddress(sipUri));
			this.message.addFirst(p);
		} catch (SipException e) {
			logger.error("Error while pushing route [" + sipUri + "]");
			throw new IllegalArgumentException("Error pushing route ", e);
		}

	}
	
	public int getContentLength()
	{
		return this.message.getContentLength().getContentLength();
	}
	public String getContentType()
	{
		ContentTypeHeader cth = (ContentTypeHeader) this.message
				.getHeader(ContentTypeHeader.NAME);
		if (cth != null) {
			// Fix For Issue http://code.google.com/p/mobicents/issues/detail?id=2659
			// getContentType doesn't return the full header value
//			String contentType = cth.getContentType();
//			String contentSubType = cth.getContentSubType();
//			if(contentSubType != null) 
//				return contentType + "/" + contentSubType;
			return ((HeaderExt)cth).getValue();
		} 
		return null;
	}
	public byte[] getRawContent()
	{
		return this.message.getRawContent();
	}
	public String getCharacterEncoding()
	{
		if (this.message.getContentEncoding() != null) {
			return this.message.getContentEncoding().getEncoding();
		} else {
			ContentTypeHeader cth = (ContentTypeHeader)
				this.message.getHeader(ContentTypeHeader.NAME);
			if(cth == null) return null;
			return cth.getParameter("charset");
		}
	}
	public void setContent(Object content,String contentType)
	{
		if(contentType != null && contentType.length() > 0) {

			ContentTypeHeader contentTypeHeader = (ContentTypeHeader)this.message.getHeader(ContentTypeHeader.NAME);
			if (contentTypeHeader == null)
			{
				
				try {
					contentTypeHeader = (ContentTypeHeader)SipFactory.getInstance().createHeaderFactory().createHeader(ContentTypeHeader.NAME, contentType);
				} catch (PeerUnavailableException e) {
					throw new IllegalArgumentException(e.getMessage(),e);
				} catch (ParseException e) {
					throw new IllegalArgumentException(e.getMessage(),e);
				}
				this.addHeader(contentTypeHeader);
			}
			String charset = this.getCharacterEncoding();
			try {	
				//only for String content temporary.
				
//			    // https://code.google.com/p/sipservlets/issues/detail?id=169
//				if(contentType.contains(CONTENT_TYPE_MULTIPART) && content instanceof Multipart) {
//					// Fix for Issue 2667 : Correct Handling of MimeMultipart
//					Multipart multipart = (Multipart) content;
//					OutputStream os = new ByteArrayOutputStream();
//					multipart.writeTo(os);
//					this.message.setContent(os.toString(), contentTypeHeader);
//				} else {
					Object tmpContent = content;
					if(tmpContent instanceof String  && charset != null) {
						//test for unsupportedencoding exception

						tmpContent = new String(((String)tmpContent).getBytes());
					}
					this.message.setContent(content, contentTypeHeader);
//				}
//			} catch (UnsupportedEncodingException uee) {
//				throw uee;
			} catch (Exception e) {
				throw new IllegalArgumentException("Parse error reading content " + content + " with content type " + contentType, e);				
			} 
		}
	}
	
	public void addHeader(String name, String value)
	{
		try {
			// Fix to Issue 1015 by alexander.kozlov.IV			
			if(JainSipUtils.SINGLETON_HEADER_NAMES.contains(name)) {
				Header header = SipFactory.getInstance().createHeaderFactory().createHeader(name, value);
				this.message.setHeader(header);				
			} else {	
				// Dealing with Allow:INVITE, ACK, CANCEL, OPTIONS, BYE kind of values
				if(JainSipUtils.LIST_HEADER_NAMES.contains(name)) {
					List<Header> headers = SipFactory.getInstance().createHeaderFactory()
						.createHeaders(name + ":" + value);
					for (Header header : headers) {
						this.message.addHeader(header);
					}
				} else {
					// Extension Header: those cannot be lists. See jain sip issue 270
					Header header = SipFactory.getInstance().createHeaderFactory()
						.createHeader(name, value);
					this.message.addLast(header);
				}				
			}
		} catch (Exception ex) {
			throw new IllegalArgumentException("Illegal args supplied ", ex);
		}
	}
	public void removeHeader(String name)
	{
		this.message.removeHeader(name);
	}
	public void removeFirst(String name)
	{
		this.message.removeFirst(name);
	}
	public void setHeader(Header h)
	{
		this.message.setHeader(h);
	}
	public void addHeader(Header h)
	{
		this.message.addHeader(h);
	}

}
