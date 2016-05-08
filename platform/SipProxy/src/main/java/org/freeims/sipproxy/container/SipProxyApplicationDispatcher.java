package org.freeims.sipproxy.container;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.sip.ClientTransaction;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.catalina.Container;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.mapper.Mapper;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.freeims.javax.sip.SipProviderImpl;
import org.freeims.javax.sip.SipStackImpl;
import org.freeims.javax.sip.Utils;
import org.freeims.javax.sip.header.From;
import org.freeims.javax.sip.header.MaxForwards;
import org.freeims.javax.sip.message.SIPMessage;
import org.freeims.javax.sip.message.SIPRequest;
import org.freeims.javax.sip.stack.SIPTransaction;
import org.freeims.sipproxy.servlet.SipProxyFactory;
import org.freeims.sipproxy.servlet.SipProxyResponse;
import org.freeims.sipproxy.servlet.SipProxyServlet;
import org.freeims.sipproxy.servlet.SubsequentAction;
import org.freeims.sipproxy.servlet.SubsequentAction.ActionType;
import org.freeims.sipproxy.servlet.impl.SipProxyMessageFactory;
import org.freeims.sipproxy.servlet.impl.SipProxyRequestImpl;




public class SipProxyApplicationDispatcher implements SipListener {

	private static Log log = LogFactory.getLog(SipProxyApplicationDispatcher.class);
	private Mapper mapper;
	private StandardService service;
	private List<SipProxyHost> hosts = new ArrayList<SipProxyHost>();
	private List<SipProxyProtocolHandler> protocolHandlers = new ArrayList<SipProxyProtocolHandler>();
	private Hashtable<URI, ClientTransaction> clientTxTable = new Hashtable<URI, ClientTransaction>();
	
	public void setService(StandardService service) {
		hosts.clear();
		this.service = service;
		Container[] hostsInContainer = this.service.getContainer().findChildren();
		for (Container c : hostsInContainer) {
			if (c instanceof SipProxyHost) {
				log.info("host class:" + c.getClass().getName());
				SipProxyHost h = (SipProxyHost) c;
				hosts.add(h);

			}
		}
	}

	public void addProtocolhandler(SipProxyProtocolHandler handler) {
		protocolHandlers.add(handler);
	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent arg0) {

	}

	public void setMapper(Mapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public void processIOException(IOExceptionEvent e) {


	}

	@Override
	public void processRequest(RequestEvent event) {

		try {

			Request request = event.getRequest();
			if (event.getRequest() == null) {
				log.info("the request is not inavlide.");
				return;
			}
			request = (Request)request.clone();
			ListeningPoint listenPoint = null;
			SipProviderImpl sipProvider = (SipProviderImpl) event.getSource();
			listenPoint = sipProvider.getListeningPoint();
			
			ServerTransaction st = (ServerTransaction) event.getServerTransaction();
			log.info("ServerTransaction is null:"+(st==null));
//			if (st == null) {
//				log.info("ServerTransaction fail");
//				SipStackImpl stackImpl = (SipStackImpl) sipProvider.getSipStack();
//				SIPTransaction t = stackImpl.findTransaction((SIPMessage) event.getRequest(), true);
//				if (t != null) {
//					log.info("find serverTransaction ok");
//					st = (ServerTransaction) t;
//				} else {
//					st = sipProvider.getNewServerTransaction(event.getRequest());
//					log.info("new ServerTransaction ok");
//				}
//			}else{
//				log.info("ServerTransaction OK");
//			}

			ApplicationDispatcher appDispatch = allocateServlet(listenPoint, request);
			
			if (appDispatch == null)
			{
				log.info("ApplicationDispatcher is null");
				try {
					Response r = null;
					r = SipFactory.getInstance().createMessageFactory().createResponse(500, request);
					this.sendResponse(event,r);
				} catch (Exception ex) {
					log.info(ex.getMessage(),ex);
				} 
				return;
			}

			SipProxyRequestImpl proxyRequest = (SipProxyRequestImpl)SipProxyMessageFactory.createRequest(request,event.getServerTransaction());
			MaxForwards maxForward = ((MaxForwards)proxyRequest.getMessage().getHeader(MaxForwards.NAME));
			maxForward.setMaxForwards(maxForward.getMaxForwards());
			proxyRequest.setDispatcher(this);
			proxyRequest.setRequestEvent(event);
			//SipProxyResponse proxyResponse = proxyRequest.createResponse(200);
			try {
				appDispatch.dispatch(proxyRequest, null);
			} catch (Exception e) {
				log.info(e.getMessage(),e);
				try {

					Response r = null;
					r = SipFactory.getInstance().createMessageFactory().createResponse(500, request);
					sendResponse(event,r);
				} catch (Exception ex) {
					log.info(ex.getMessage(),ex);
				} 
				return;
			}
			SubsequentAction subAction = proxyRequest.getSubsequentAction();
			if (subAction == null)
			{
				try {
					Response r = null;
					r = SipFactory.getInstance().createMessageFactory().createResponse(500, request);
					sendResponse(event,r);
					return;
				} catch (Exception ex) {
					log.info(ex.getMessage(),ex);
					return;
				} finally{
					
				}
			}
			if (subAction.getType() == ActionType.NONE) {
				return;
			}

			if (subAction.getType() == ActionType.BOTH || subAction.getType() == ActionType.RESPONSE) {
				try {
					Response r = null;
					r = SipFactory.getInstance().createMessageFactory().createResponse(subAction.getResponseStatus(),
							request);
					if (subAction.getResponseReason() != null) {
						r.setReasonPhrase(subAction.getResponseReason());
					}
					sendResponse(event,r);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (subAction.getType() == ActionType.BOTH || subAction.getType() == ActionType.FORWARD) {
				proxyRequest(sipProvider,getServerTransaction(event),request,subAction.getAppId());
			}
			
		} catch (Exception e) {
			log.info(e.getMessage(), e);
		}
	}

	public ServerTransaction getServerTransaction(RequestEvent event)
	{
		if (event.getRequest().getMethod().equals("ACK"))
		{
			return null;
		}
		SipProviderImpl sipProvider = (SipProviderImpl) event.getSource();
		ServerTransaction st = (ServerTransaction) event.getServerTransaction();
		try {
			if (st == null) {
				SipStackImpl stackImpl = (SipStackImpl)sipProvider.getSipStack();
				SIPTransaction t = stackImpl.findTransaction((SIPMessage)event.getRequest(), true);
				if (t!=null)
				{
					st = (ServerTransaction)t;
				}else{
					log.info("getNewServerTransaction");
					st = sipProvider.getNewServerTransaction(event.getRequest());
				}
				ClientTransaction ct = this.clientTxTable.get(((SIPMessage)event.getRequest()).getFrom().getAddress().getURI());
				if (ct != null)
				{
					ct.setApplicationData(st);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return st;
	}
	public void sendResponse(RequestEvent event,Response resp)
	{
		SipProviderImpl sipProvider = (SipProviderImpl) event.getSource();
		ServerTransaction st = getServerTransaction(event);
		try {
			if (st != null) {
				st.sendResponse(resp);
			} else {
				sipProvider.sendResponse(resp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void proxyRequest(SipProviderImpl sipProvider,ServerTransaction st,Request request,String appId)
		throws Exception
	{
		
		
//		((SIPRequest)newRequest).setTransaction(null);
		//createViaHeader(String host,int port,String transport,String branch)
		///*, */
		ListeningPoint listenPoint = sipProvider.getListeningPoint();
		String bid=Utils.getInstance().generateBranchId();
		ViaHeader viaHeader = SipProxyFactory.getHeaderFactory().createViaHeader(listenPoint.getIPAddress(),listenPoint.getPort(),
				listenPoint.getTransport(),bid);

		//if (subAction.getAppId() != null) {
		if (appId != null){
			viaHeader.setParameter("app_id", appId);
		}
		request.addFirst(viaHeader);

		
		if (request.getMethod().equals("ACK")){
			//ACK method need not response,so ...

			sipProvider.sendRequest(request);
			
		}else
		{
			ClientTransaction proxyCt = sipProvider.getNewClientTransaction(request);
		
		proxyCt.setApplicationData(st);//this.getServerTransaction(event));
		From from = (From)request.getHeader(From.NAME);
		this.clientTxTable.put(from.getAddress().getURI(), proxyCt);
		proxyCt.sendRequest();
		}
		
	}
	@Override
	public void processResponse(ResponseEvent responseEvent) {
		try {
			ListeningPoint listenPoint = null;
			SipProviderImpl sipProvider = (SipProviderImpl) responseEvent.getSource();
			listenPoint = sipProvider.getListeningPoint();
			
			SipProxyResponse servletResp =SipProxyMessageFactory
					.createResponse(responseEvent);

			Response response = (Response) servletResp.getMessage();
			
			ApplicationDispatcher appDispatch = allocateServlet(listenPoint, null/*(responseEvent.getClientTransaction().getRequest()*/);
			
			if (appDispatch == null)
			{
				log.info("response ApplicationDispatcher is null");
				try {
//					Response r = null;
//					r = SipFactory.getInstance().createMessageFactory().createResponse(500, request);
//					this.sendResponse(responseEvent,r);
				} catch (Exception ex) {
					log.info(ex.getMessage(),ex);
				} 
				return;
			}

			log.info("ApplicationDispatcher is not null");

			
			

			//SipProxyResponse proxyResponse = proxyRequest.createResponse(200);
			try {
				appDispatch.dispatch(null, servletResp);
			} catch (Exception e) {
				log.info(e.getMessage(),e);
//				try {
//
//					Response r = null;
//					r = SipFactory.getInstance().createMessageFactory().createResponse(500, request);
//
//				} catch (Exception ex) {
//					log.info(ex.getMessage(),ex);
//				} 
				return;
			}

			log.info("ClientTxID = " + responseEvent.getClientTransaction() + " client tx id "
					+ ((ViaHeader) response.getHeader(ViaHeader.NAME)).getBranch() + " CSeq header = "
					+ response.getHeader(CSeqHeader.NAME) + " status code = " + response.getStatusCode());

			ClientTransaction ct = responseEvent.getClientTransaction();
			Response newResponse = (Response) response.clone();
			newResponse.removeFirst(ViaHeader.NAME);
			if (ct != null) {
				
				ServerTransaction st = (ServerTransaction) ct.getApplicationData();
				// The server tx goes to the terminated state.
				try{
					st.sendResponse(newResponse);
				}catch(Exception e)
				{
					//log.info(e.getMessage(),e);
					sipProvider.sendResponse(newResponse);
				}
			} /*else {
				// Client tx has already terminated but the UA is retransmitting
				// just forward the response statelessly.
				// Strip the topmost via header

				
				// Send the retransmission statelessly

				sipProvider.sendResponse(newResponse);
			}*/

		} catch (Exception ex) {
			ex.printStackTrace();

		}

	}

	@Override
	public void processTimeout(TimeoutEvent arg0) {


	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
		log.info("Transaction terminated event occured -- cleaning up");
		if (!transactionTerminatedEvent.isServerTransaction()) {
			ClientTransaction ct = transactionTerminatedEvent.getClientTransaction();
			for (Iterator it = this.clientTxTable.values().iterator(); it.hasNext();) {
				if (it.next().equals(ct)) {
					it.remove();
				}
			}
		} else {
			log.info("Server tx terminated! ");
		}

	}
	private Hashtable<String,SipProxyHost> hostMap = new Hashtable<String,SipProxyHost>();
	private Hashtable<String,SipProxyServlet> servletMap = new Hashtable<String,SipProxyServlet>();
	private ApplicationDispatcher  allocateServlet(String address,int port,String transport)
	{
		String key = "sip:"+address+":"+port+":"+transport;
//		if (servletMap.containsKey(key))
//		{
//			return servletMap.get(key);
//		}
		
		SipProxyHost h = matcherHost(address,port,transport);
		if (h == null) {
			log.info("not match any host");
			return null;
		}
		Container[] contexts = h.findChildren();

		for (Container context : contexts) {
			log.info("context class:"+context.getClass().getName());
			if (context instanceof SipProxyContext) {
				SipProxyContext cx = (SipProxyContext) context;
				log.info("cx.path:"+cx.getPath());
				if (!cx.getPath().equals("/Main"))
				{
					continue;
				}
				ApplicationDispatcher appDisp;
				ServletContext sc = cx.getServletContext();
				RequestDispatcher reqDisp ;
				if (sc instanceof ApplicationContextFacade)
				{
					ApplicationContextFacade acf = (ApplicationContextFacade)sc;
					reqDisp = acf.getRequestDispatcher("/MainServlet");
					return (ApplicationDispatcher)reqDisp;
				}
				
				ApplicationContext appCx = (ApplicationContext)cx.getServletContext();
				appDisp = (ApplicationDispatcher)appCx.getRequestDispatcher("/MainServlet");
				return appDisp;
				
			}
		}
		return null;
	}
	private ApplicationDispatcher allocateServlet(ListeningPoint listenPoint,Request request)
	{
		return allocateServlet(listenPoint.getIPAddress(),listenPoint.getPort(),listenPoint.getTransport());
	}
	
	private SipProxyHost matcherHost(String address,int port,String transport)
	{
		String key = "sip:"+address+":"+port+":"+transport;
		if (hostMap.containsKey(key))
		{
			return hostMap.get(key);
		}

		Connector cs[] = this.service.findConnectors();
		SipProxyProtocolHandler ph = null;
		for (Connector c : cs) {
			if (c.getProtocolHandler() instanceof SipProxyProtocolHandler) {
				ph = (SipProxyProtocolHandler) c.getProtocolHandler();

				if (ph.getIpAddress().equalsIgnoreCase(address) 
						&& ph.getPort() == port
						&& ph.getTransport().equalsIgnoreCase(transport)) {

					break;
				}
			}
		}

		for (SipProxyHost h : hosts) {


			String []ss = h.getBindConnectors().split(",");
			for (String s:ss)
			{

				if (s.equals(ph.getName()))
				{
					log.info("s1:"+s);
					hostMap.put(key, h);
					return h;
				}
			}
		}
		return null;
	}
	
	private SipProxyHost matcherHost(ListeningPoint listenPoint) {
		return matcherHost(listenPoint.getIPAddress(),listenPoint.getPort(),listenPoint.getTransport());
	}

	public void addHostName(String name) {

	}
}
