package org.freeims.sipproxy.servlet.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sip.RequestEvent;
import javax.sip.SipFactory;
import javax.sip.address.URI;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.catalina.Context;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.freeims.javax.sip.SipProviderImpl;
import org.freeims.sipproxy.container.SipProxyApplicationDispatcher;
import org.freeims.sipproxy.servlet.SipProxyRequest;
import org.freeims.sipproxy.servlet.SipProxyResponse;
import org.freeims.sipproxy.servlet.SubsequentAction;

//,SipProxyMessageImpl

public class SipProxyRequestImpl extends  SipProxyMessageImpl  implements SipProxyRequest {


	private Context context;
	private boolean crossContext;
	private static Log log = LogFactory.getLog(SipProxyRequestImpl.class);
	private Object applicationData;
	private RequestEvent event = null;
	
	private SipProxyApplicationDispatcher sipProxyDispatcher = null;
	
	
    /**
     * Filter chain associated with the request.
     */
    protected FilterChain filterChain = null;

    /**
     * Get filter chain associated with the request.
     *
     * @return the associated filter chain
     */
    public FilterChain getFilterChain() {
        return this.filterChain;
    }

    /**
     * Set filter chain associated with the request.
     *
     * @param filterChain new filter chain
     */
    public void setFilterChain(FilterChain filterChain) {
        this.filterChain = filterChain;
    }
    
	public void setDispatcher(SipProxyApplicationDispatcher dispatcher) {
		this.sipProxyDispatcher = dispatcher;
	}

	public void setApplicationData(Object o) {
		applicationData = o;
	}

	public Object getApplicationData() {
		return applicationData;
	}

	public SipProxyResponse createResponse(int status) {
		return this.createResponse(status, null);
	}
	
	public SipProxyResponse createResponse(int status,String label) {
		try {
			Response r = SipFactory.getInstance().createMessageFactory().createResponse(status,
					(Request) this.getMessage());
			SipProxyResponseImpl resp = new SipProxyResponseImpl();
			if (label != null){
				r.setReasonPhrase(label);
			}
			resp.setMessage(r);
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void sendResponse(int code)
	{
		sendResponse(this.createResponse(code, null));
	}
	public void sendResponse(int code,String label)
	{
		sendResponse(this.createResponse(code, label));
	}

	public void sendResponse(SipProxyResponse resp) {
		sipProxyDispatcher.sendResponse(event, (Response)resp.getMessage());
	}
	public void proxyTo(String appId)
	{
		try{
		sipProxyDispatcher.proxyRequest((SipProviderImpl) event.getSource(), sipProxyDispatcher.getServerTransaction(event), (Request)this.getMessage(), appId);
		}catch(Exception e)
		{
			log.info(e.getMessage(),e);
		}
	}
	public URI getRequestURI()
	{
		return ((Request)message).getRequestURI();
	}
	
	SubsequentAction subsequentAction = null;
	public SubsequentAction getSubsequentAction()
	{
		return subsequentAction;
	}
	public void setSubsequentAction(SubsequentAction action)
	{
		subsequentAction=action;
	}
	public SubsequentAction createForwardAction()
	{
		SubsequentAction action = SubsequentAction.createForwardAction();
		setSubsequentAction(action);
		return action;
	}
	public SubsequentAction createNoneAction()
	{
		SubsequentAction action = SubsequentAction.createNoneAction();
		setSubsequentAction(action);
		return action;
	}
	public SubsequentAction createResponseAction(int status)
	{
		return this.createResponseAction(status, null);
	}
	public SubsequentAction createResponseAction(int status,String reason)
	{
		SubsequentAction action = SubsequentAction.createResponseAction(status, reason);
		setSubsequentAction(action);
		return action;
	}
	public SubsequentAction createResponseAction(SipProxyResponse response)
	{
		SubsequentAction action = SubsequentAction.createResponseAction(response.getStatus(), response.getReasonPhrase());
		action.setResponse(response);
		setSubsequentAction(action);
		return action;
	}
	public SubsequentAction createBothAction(int status)
	{
		return this.createBothAction(status, null);
	}
	public SubsequentAction createBothAction(int status,String reason)
	{
		SubsequentAction action = SubsequentAction.createBothAction(status, reason);
		setSubsequentAction(action);
		return action;
	}
	@Override
	public void setRequestEvent(RequestEvent event) {
		this.event = event;
		
	}

	@Override
	public RequestEvent getRequestEvent() {

		return null;
	}

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * The request attributes for this request.  This is initialized from the
     * wrapped request, but updates are allowed.
     */
    protected final HashMap<String, Object> attributes = new HashMap<>();


    // ------------------------------------------------- ServletRequest Methods


    /**
     * Override the <code>getAttribute()</code> method of the wrapped request.
     *
     * @param name Name of the attribute to retrieve
     */
    @Override
    public Object getAttribute(String name) {

        synchronized (attributes) {
            return (attributes.get(name));
        }

    }


    /**
     * Override the <code>getAttributeNames()</code> method of the wrapped
     * request.
     */
    @Override
    public Enumeration<String> getAttributeNames() {

        synchronized (attributes) {
            return Collections.enumeration(attributes.keySet());
        }

    }


    /**
     * Override the <code>removeAttribute()</code> method of the
     * wrapped request.
     *
     * @param name Name of the attribute to remove
     */
    @Override
    public void removeAttribute(String name) {

        synchronized (attributes) {
            attributes.remove(name);
//            if (!isSpecial(name))
//                getRequest().removeAttribute(name);
        }

    }


    /**
     * Override the <code>setAttribute()</code> method of the
     * wrapped request.
     *
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set
     */
    @Override
    public void setAttribute(String name, Object value) {

        synchronized (attributes) {
            attributes.put(name, value);
//            if (!isSpecial(name))
//                getRequest().setAttribute(name, value);
        }

    }


	@Override
	public long getContentLengthLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParameter(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getParameterValues(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return this.getServletContext().getRequestDispatcher(path);
	}

	@Override
	public String getScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getServerPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		if (context == null) {
            return null;
        }
		return context.getServletContext();
	}

	protected void setContext(Context cx)
	{
		context = cx;
	}
	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getCrossContext() {
		return crossContext;
	}

	public void setCrossContext(boolean crossContext) {
		this.crossContext = crossContext;
	}


}
