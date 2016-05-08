package org.freeims.sipproxy.servlet.impl;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.freeims.sipproxy.servlet.SipProxyRequest;
import org.freeims.sipproxy.servlet.SipProxyResponse;

public class SipProxyFilter implements  Filter
{

	private FilterConfig filterConfig = null;
	
	@Override
	public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		SipProxyRequest sipProxyRequest = null;
		if (request != null) {
			if (!(request instanceof SipProxyRequest)) {
				throw new ServletException("Request is not SipProxyRequest.it is " + request.getClass().getName());
			}
			sipProxyRequest = (SipProxyRequest) request;
		}
		SipProxyResponse sipProxyResponse = null;
		if (response != null) {
			if (!(response instanceof SipProxyResponse)) {
				throw new ServletException("Response is not SipProxyResponse.it is " + response.getClass().getName());
			}
			sipProxyResponse = (SipProxyResponse) response;
		}
		doFilter(sipProxyRequest, sipProxyResponse, chain);
	}

	protected  void doFilter(SipProxyRequest request, SipProxyResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		if (request != null)
		{
			doFilterRequest(request,chain);
			return;
		}
		if (response!=null)
		{
			doFilterResponse(response,chain);
			return;
		}
		chain.doFilter(request, response);
	}
	
	protected void doFilterResponse(SipProxyResponse response, FilterChain chain) throws ServletException, IOException
	{
		chain.doFilter(null, response);
	}
	
	protected void doFilterRequest(SipProxyRequest req, FilterChain chain) throws ServletException, IOException {

		String m = req.getMethod();
		if ("INVITE".equals(m))
			doFilterInvite(req,chain);
		else if ("MESSAGE".equals(m))
			 doFilterMessage(req,chain);
		else if ("REGISTER".equals(m))
			 doFilterRegister(req,chain);
		 else if ("ACK".equals(m))
			  doFilterAck(req,chain);
		// else if ("OPTIONS".equals(m))
		// doOptions(req);
		 else if ("BYE".equals(m))
			  doFilterBye(req,chain);
		 else if ("CANCEL".equals(m))
			  doFilterCancel(req,chain);
		// else if ("SUBSCRIBE".equals(m))
		// doSubscribe(req);
		// else if ("NOTIFY".equals(m))
		// doNotify(req);
		// else if ("INFO".equals(m))
		// doInfo(req);
		// else if ("REFER".equals(m))
		// doRefer(req);
		// else if ("PUBLISH".equals(m))
		// doPublish(req);
		 else if ("UPDATE".equals(m))
			  doFilterUpdate(req,chain);
		 else if ("PRACK".equals(m))
			  doFilterPrack(req,chain);
		 else{
			 chain.doFilter(req, null);
		 }

	}


	protected void doFilterRegister(SipProxyRequest req, FilterChain chain) throws ServletException,IOException
	{
		chain.doFilter(req, null);
	}
	protected void doFilterInvite(SipProxyRequest req, FilterChain chain) throws ServletException,IOException
	{
		chain.doFilter(req, null);
	}
	protected void doFilterAck(SipProxyRequest req, FilterChain chain) throws ServletException,IOException
	{
		chain.doFilter(req, null);
	}
	protected void doFilterMessage(SipProxyRequest req, FilterChain chain) throws ServletException,IOException
	{
		chain.doFilter(req, null);
	}
	protected void doFilterBye(SipProxyRequest req, FilterChain chain) throws ServletException,IOException
	{
		chain.doFilter(req, null);
	}
	protected void doFilterCancel(SipProxyRequest req, FilterChain chain) throws ServletException,IOException
	{
		chain.doFilter(req, null);
	}
	protected void doFilterUpdate(SipProxyRequest req, FilterChain chain) throws ServletException,IOException
	{
		chain.doFilter(req, null);
	}
	protected void doFilterPrack(SipProxyRequest req, FilterChain chain) throws ServletException,IOException
	{
		chain.doFilter(req, null);
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
		this.filterConfig = filterConfig;
	}

	@Override
	public void destroy() {
		this.filterConfig=null;
	}

}
