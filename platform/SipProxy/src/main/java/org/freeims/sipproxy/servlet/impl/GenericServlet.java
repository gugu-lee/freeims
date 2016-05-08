package org.freeims.sipproxy.servlet.impl;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.freeims.sipproxy.servlet.SipProxyRequest;
import org.freeims.sipproxy.servlet.SipProxyResponse;
import org.freeims.sipproxy.servlet.SipProxyServlet;
import org.freeims.sipproxy.servlet.SubsequentAction;
import org.freeims.sipproxy.servlet.SubsequentAction.ActionType;



public abstract class GenericServlet extends javax.servlet.GenericServlet implements SipProxyServlet,Serializable {

	private static final long serialVersionUID = -4100710484876302974L;
	protected SubsequentAction subsequentAction = new SubsequentAction();
	@Override
	public void doRequest(SipProxyRequest req) throws ServletException, IOException {

		String m = req.getMethod();
		if ("INVITE".equals(m))
			doInvite(req);
		else if ("MESSAGE".equals(m))
			 doMessage(req);
		else if ("REGISTER".equals(m))
			 doRegister(req);
		 else if ("ACK".equals(m))
			  doAck(req);
		// else if ("OPTIONS".equals(m))
		// doOptions(req);
		 else if ("BYE".equals(m))
			  doBye(req);
		 else if ("CANCEL".equals(m))
			  doCancel(req);
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
			  doUpdate(req);
		 else if ("PRACK".equals(m))
			  doPrack(req);
		else if (req.isInitial())
			 notHandled(req);
		 notHandled(req);
	}

	private SubsequentAction notHandled(SipProxyRequest req) {
		
		subsequentAction.setType(ActionType.RESPONSE);
		subsequentAction.setResponseStatus(500);
		return subsequentAction;
		
	}
	public void doAck(SipProxyRequest req) throws ServletException,IOException
	{
		SubsequentAction.createForwardAction();
	}
	public void doBye(SipProxyRequest req) throws ServletException,IOException
	{
		 SubsequentAction.createForwardAction();
	}
	public void doCancel(SipProxyRequest req) throws ServletException,IOException
	{
		SubsequentAction.createForwardAction();
	}
	public void doUpdate(SipProxyRequest req) throws ServletException,IOException
	{
		SubsequentAction.createForwardAction();
	}
	public void doPrack(SipProxyRequest req) throws ServletException,IOException
	{
		SubsequentAction.createForwardAction();
	}

	@Override
	public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
		if (req != null)
		{
			doRequest((SipProxyRequest)req);
			return;
		}
		if (resp != null)
		{
			doResponse((SipProxyResponse)resp);
			return;
		}
		throw new ServletException("ServletRequest and ServletResponse could not both be null in same time.");
	}

	
}
