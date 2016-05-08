package org.freeims.sipproxy.servlet.impl;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.sip.address.URI;
import org.apache.catalina.Context;

public class SipProxyRequestWrapper extends ServletRequestWrapper 
{

	
	private URI uri;
	public SipProxyRequestWrapper(ServletRequest request) {
		super(request);
        this.context = null;
        this.crossContext = false;
	}
	public SipProxyRequestWrapper(ServletRequest request, Context context,
            boolean crossContext)
	{
		super(request);
		((SipProxyRequestImpl)request).setContext(context);
        this.context = context;
        this.crossContext = crossContext;
        
        setRequest(request);
	}
	public void setRequestURI(URI uri)
	{
		this.uri = uri;
	}
	public URI getRequestURI()
	{
		return uri;
	}
	public void recycle()
	{
		
	}
    /**
     * The context for this request.
     */
    protected final Context context ;


    /**
     * The context path for this request.
     */
    protected String contextPath = null;


    /**
     * If this request is cross context, since this changes session access
     * behavior.
     */
    protected final boolean crossContext;
	
}
