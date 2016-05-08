package org.freeims.sipproxy.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;



public interface SipProxyServlet extends Servlet
{
	
	
	public void doResponse(SipProxyResponse resp) throws ServletException,IOException;
	public void doException(Exception ex);
	
	public void doRequest(SipProxyRequest req) throws ServletException,IOException;
	public void doRegister(SipProxyRequest req) throws ServletException,IOException;
	public void doMessage(SipProxyRequest req) throws ServletException,IOException;
	public void doInvite(SipProxyRequest req) throws ServletException,IOException;
	public void doAck(SipProxyRequest req) throws ServletException,IOException;
	public void doBye(SipProxyRequest req) throws ServletException,IOException;
	public void doCancel(SipProxyRequest req) throws ServletException,IOException;
	public void doUpdate(SipProxyRequest req) throws ServletException,IOException;
	public void doPrack(SipProxyRequest req) throws ServletException,IOException;

	
}
