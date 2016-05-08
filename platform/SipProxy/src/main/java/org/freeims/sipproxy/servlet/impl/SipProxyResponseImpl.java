package org.freeims.sipproxy.servlet.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.freeims.sipproxy.servlet.SipProxyResponse;

public class SipProxyResponseImpl extends SipProxyMessageImpl implements SipProxyResponse {
	
	private Request request;
	public Request getOriginalRequest() {
		return request;
	}
	public void setOriginalRequest(Request request) {
		this.request = request;
	}
	public int getStatus()
	{
		return ((Response)this.getMessage()).getStatusCode();
	}
	@Override
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public PrintWriter getWriter() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void resetBuffer() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setBufferSize(int arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setCharacterEncoding(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setContentLengthLong(long arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setContentType(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub
		
	}
	/*
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public boolean isCrossContext() {
		return crossContext;
	}
	public void setCrossContext(boolean crossContext) {
		this.crossContext = crossContext;
	}
*/
}
