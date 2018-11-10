

package org.freeims.diameterpeer;


import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.freeims.diameterpeer.data.AVP;
import org.freeims.diameterpeer.data.DiameterMessage;
import org.freeims.diameterpeer.data.DiameterTask;
import org.freeims.diameterpeer.peer.Application;
import org.freeims.diameterpeer.peer.Peer;
import org.freeims.diameterpeer.peer.PeerManager;
import org.freeims.diameterpeer.peer.StateMachine;
import org.freeims.diameterpeer.routing.RoutingEngine;
import org.freeims.diameterpeer.transaction.TransactionListener;
import org.freeims.diameterpeer.transaction.TransactionWorker;
import org.freeims.diameterpeer.transport.Acceptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
  * This class defines the Peer Manager functionality
  * 
  * @author Dragos Vingarzan vingarzan -at- fokus dot fraunhofer dot de
  *
  */
public class DiameterPeer {
	
	/** The logger */
	private static final Logger LOGGER = Logger.getLogger(DiameterPeer.class);
	
	/** FQDN of itself */
	public String FQDN;
	
	/** Realm of itself */
	public String Realm;
	
	/** Vendor id of itself */
	public int Vendor_Id;
	
	/** Product Name of itself */
	public String Product_Name;
	
	/** Tc timer value - interval to attempt peer reconnect */
	public int Tc;
	
	/** Accepting sockets */
	public Vector<Acceptor> acceptors;
	
	/** If unknown (unconfigured) peers are allowed to connect */
	public boolean AcceptUnknownPeers;

	/** If to delete unknown peers after they disconnect */
	public boolean DropUnknownOnDisconnect;

	/** Supported Applications */
	public Vector<Application> AuthApp,AcctApp;
	
	/** Routing Engine */
	public RoutingEngine routingTable=null;
	
	/** PeerManger manages connection with other Diameter peers */
	public PeerManager peerManager;
    	
	/** 
	 * Sevice must define new eventListeners in order to process received 
	 * Diameter answers 
	 */
	public Vector<EventListener> eventListeners;
	
	/** Received Queue Max Length */
	public int queueLength;
	
	/** Received Messages queue */
	public ArrayBlockingQueue<DiameterTask> queueTasks;
	
	/** workers for running the event listeners*/
	private DiameterWorker workers[]; 
	
	/** The number of workers that process DiameterTasks from the taks queue */
	public int workerCount=1;

	/** transaction worker */
	private TransactionWorker transactionWorker=null;
	
	/** Configuration DOM */
	//; 	
	
	/** Hop-by-Hop identifier */
	public int hopbyhop_id=0;

	/** End-to-End identifier */
	public int endtoend_id=0;
	
	public String dnsServer;
	
	/** Generates Hop-by-Hop id. */
	public synchronized int getNextHopByHopId()
	{
		return ++hopbyhop_id;
	}
	/** Generates End-to-End id. */
	public synchronized int getNextEndToEndId()
	{
		return ++endtoend_id;
	}
	
	/**
	 * Construct a java bean friendly DiameterPeer (no args).
	 */
	public DiameterPeer()
	{
		eventListeners = new Vector<EventListener>();
	}
	
	/**
	 * Construct a DiameterPeer based on a configuration file. 
	 * 
	 * @param xml Configuration file.
	 */
	public DiameterPeer(String xml)
	{
		this();
		configure(xml,false);
	}
	
	public DiameterPeer(String xmlFile,String elemName)
	{
		this();
		Document config =readConfigFile(xmlFile);
		Element docElem=config.getDocumentElement();
		NodeList nodeList = docElem.getElementsByTagName("DiameterPeer");
		int listSize = nodeList.getLength();
		for (int i=0;i<listSize;++i)
		{
			Element elem = (Element)nodeList.item(i);
			LOGGER.info("elemName:"+elem.getAttribute("Name"));
			if (elem.getAttribute("Name").equals(elemName)){
				LOGGER.info("config this element");
				configure(elem);
				return;
			}
		}
	}
	
	public void configure(Element rootElem)
	{
		
		
		java.util.Random rand = new java.util.Random();
		hopbyhop_id = rand.nextInt();
		endtoend_id = ((int) (System.currentTimeMillis()&0xFFF))<<20;
		endtoend_id |= rand.nextInt() & 0xFFFFF;
	
		Acceptor acc;
		NodeList nl;
		Node n,nv;
		int port,app_id,vendor_id;
		InetAddress addr;
		String fqdn,realm;
		Application app;
		
		FQDN = rootElem.getAttribute("FQDN");
		LOGGER.info("FQDN: " + FQDN);
		Realm = rootElem.getAttribute("Realm");
		LOGGER.info("Realm: " + rootElem.getAttribute("Realm"));
		Vendor_Id = Integer.parseInt(rootElem.getAttribute("Vendor_Id"));
		LOGGER.info("Vendor_ID : " + Integer.parseInt(rootElem.getAttribute("Vendor_Id")));
		Product_Name = rootElem.getAttribute("Product_Name");
		LOGGER.info("Product Name: " + rootElem.getAttribute("Product_Name"));
		AcceptUnknownPeers = Integer.parseInt(rootElem.getAttribute("AcceptUnknownPeers"))!=0;
		LOGGER.info("AcceptUnknwonPeers: " + AcceptUnknownPeers);
		DropUnknownOnDisconnect = Integer.parseInt(rootElem.getAttribute("DropUnknownOnDisconnect"))!=0;
		LOGGER.info("DropUnknownOnDisconnect: " + DropUnknownOnDisconnect);
		
		Tc = Integer.parseInt(rootElem.getAttribute("Tc"));
		workerCount = Integer.parseInt(rootElem.getAttribute("Workers"));
		queueLength = Integer.parseInt(rootElem.getAttribute("QueueLength"));
		dnsServer = rootElem.getAttribute("dnsServer");
		queueTasks = new ArrayBlockingQueue<DiameterTask>(queueLength,true);
		
		startWorkers();
		
		/* Read Supported Application ids */
		this.AuthApp = new Vector<Application>();
		this.AcctApp = new Vector<Application>();
		nl = rootElem.getElementsByTagName("Auth");
		for(int i=0;i<nl.getLength();i++){
			n = nl.item(i);
			app_id = 0;
			app_id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
			vendor_id=0;
			if (n.getAttributes().getNamedItem("vendor")!=null)
				vendor_id = Integer.parseInt(n.getAttributes().getNamedItem("vendor").getNodeValue());
			
			app = new Application(app_id,vendor_id,Application.Auth);
			this.AuthApp.add(app);
		}
		nl = rootElem.getElementsByTagName("Acct");
		for(int i=0;i<nl.getLength();i++){
			n = nl.item(i);
			app_id = 0;
			app_id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
			vendor_id=0;
			if (n.getAttributes().getNamedItem("vendor")!=null)
				vendor_id = Integer.parseInt(n.getAttributes().getNamedItem("vendor").getNodeValue());
			
			app = new Application(app_id,vendor_id,Application.Acct);
			this.AcctApp.add(app);
		}
		/* Initialize the Peer Manager */
		peerManager = new PeerManager(this);
		
		/* Read the peers from the configuration file */
		nl = rootElem.getElementsByTagName("Peer");
		for(int i=0;i<nl.getLength();i++){
			n = nl.item(i);
 
			fqdn = n.getAttributes().getNamedItem("FQDN").getNodeValue();
			
			realm = n.getAttributes().getNamedItem("Realm").getNodeValue();	
			
			port = 3868;
			nv = n.getAttributes().getNamedItem("port");
			if (nv==null) port = 3868;
			else port = Integer.parseInt(nv.getNodeValue());
				
			peerManager.configurePeer(fqdn,realm,port);
		}
		
		/* Create & start connection acceptors */
		acceptors = new Vector<Acceptor>();
		nl = rootElem.getElementsByTagName("Acceptor");
		for(int i=0;i<nl.getLength();i++){
			n = nl.item(i);
			
			port = 3868;
			nv = n.getAttributes().getNamedItem("port");
			if (nv==null) port = 3868;
			else port = Integer.parseInt(nv.getNodeValue());
			
			addr = null;
			nv = n.getAttributes().getNamedItem("bind");
			if (nv !=null && nv.getNodeValue().length()!=0	)				
				try {
					addr = InetAddress.getByName(nv.getNodeValue());
				} catch (UnknownHostException e) {
					LOGGER.error("DiameterPeer: Can not resolve "+nv.getNodeValue());
					e.printStackTrace();
					continue;
				} 
			acc = new Acceptor(port,addr,this);
			acc.startAccepting();
			acceptors.add(acc);
		}
		
		initRoutingTable(rootElem);

		peerManager.start();
	}
	
	/**
	 * Configure a DiameterPeer based on a configuration file or string.
	 * Path && isFile==true is equivalent to DiameterPeer(Path).
	 * 
	 * @param xml XML configuration payload.
	 * @param isFile Set to true if string is a path/uri, false if raw xml.
	 */
	public void  configure(String xml, boolean isFile)
	{
		
		Document config=null;
		if (isFile){
			/* parse the config */
			if ((config=readConfigFile(xml))==null) {
				LOGGER.error("DiameterPeer: Error parsing config file");
				//return null;
			}
		}
		else {
			/* parse the config */
			if ((config=readConfigString(xml))==null) {
				LOGGER.error("DiameterPeer: Error parsing config String");
				//return null;
			}
		}
		configure(config.getDocumentElement());
		
		
	}
	
	private Document readConfigFile(String cfgFile)
	{
		Document config=null;
		DocumentBuilderFactory factory =
			DocumentBuilderFactory.newInstance();
		//factory.setValidating(true);   
		//factory.setNamespaceAware(true);
	    try {
	       DocumentBuilder builder = factory.newDocumentBuilder();
	       config = builder.parse( cfgFile );
	    } catch (SAXException sxe) {
	       // Error generated during parsing)
	       Exception  x = sxe;
	       if (sxe.getException() != null)
	           x = sxe.getException();
	       x.printStackTrace();
	       return null;
	    } catch (ParserConfigurationException pce) {
	        // Parser with specified options can't be built
	        pce.printStackTrace();
	        return null;
	    } catch (IOException ioe) {
	       // I/O error
	       ioe.printStackTrace();
	       return null;
	    }
	    return config;
	}
	
	private Document  readConfigString(String cfgString)
	{
		Document config = null;
		DocumentBuilderFactory factory =
			DocumentBuilderFactory.newInstance();
		//factory.setValidating(true);   
		//factory.setNamespaceAware(true);
	    try {
	       DocumentBuilder builder = factory.newDocumentBuilder();
	       config = builder.parse( new InputSource(new StringReader(cfgString)) );
	    } catch (SAXException sxe) {
	       // Error generated during parsing)
	       Exception  x = sxe;
	       if (sxe.getException() != null)
	           x = sxe.getException();
	       x.printStackTrace();
	       return null;
	    } catch (ParserConfigurationException pce) {
	        // Parser with specified options can't be built
	        pce.printStackTrace();
	        return null;
	    } catch (IOException ioe) {
	       // I/O error
	       ioe.printStackTrace();
	       return null;
	    }
	    return config;
	}
	
	/* configure routing table */
	private void initRoutingTable(Element config) {		
		NodeList nl, nlc;
		String fqdn,realm;
		int metric;
		
		this.routingTable = new RoutingEngine();
		nl = config.getElementsByTagName("DefaultRoute");
		for(int i=0;i<nl.getLength();i++){
			fqdn = nl.item(i).getAttributes().getNamedItem("FQDN").getNodeValue();
			metric = Integer.valueOf(nl.item(i).getAttributes().getNamedItem("metric").getNodeValue());
			routingTable.addDefaultRoute(fqdn, metric);
		}
		nl = config.getElementsByTagName("Realm");
		for(int i=0;i<nl.getLength();i++){
			realm = nl.item(i).getAttributes().getNamedItem("name").getNodeValue();
			nlc = nl.item(i).getChildNodes();
			for(int j=0;j<nlc.getLength();j++)
				if (nlc.item(j).getNodeName().equalsIgnoreCase("Route")){
					fqdn = nlc.item(j).getAttributes().getNamedItem("FQDN").getNodeValue();
					metric = Integer.valueOf(nlc.item(j).getAttributes().getNamedItem("metric").getNodeValue());
					routingTable.addRealmRoute(realm, fqdn, metric);
				}
		}
		 
	}
		
	
	private void startWorkers()
	{
		workers = new DiameterWorker[workerCount];
		for(int i=0;i<workerCount;i++)
			workers[i] = new DiameterWorker(i,this.queueTasks);
	}
	
	/**
	 * Bundles Diameter request and answer to a transaction.
	 * 
	 * @param timeout		Lifetime of a transaction.
	 * @param checkInterval
	 */
	public void enableTransactions(long timeout,long checkInterval)
	{
		if (this.transactionWorker == null)
			this.transactionWorker = new TransactionWorker(this,timeout,checkInterval);
	}
	
	
	
	/**
	 * Creates a new Diameter request with proxiable bit set. 
	 * 
	 * @param command_code		Command code of the request.
	 * @param application_id	Application id in the message header. 
	 * @return Created Diameter request.
	 */
	public DiameterMessage newRequest(int command_code, int application_id)
	{
		return newRequest(command_code, true, application_id);
	}
	
	
	
	/**
	 * Creates a new Diameter request. 
	 * 
	 * @param command_code		Command code of the request.
	 * @param Proxiable			Proxiable is set if true. 
	 * @param application_id	Application id in the message header. 
	 * @return Created Diameter request.
	 */
	public DiameterMessage newRequest(int command_code, boolean Proxiable, 
			int application_id)
	{
		DiameterMessage msg = new DiameterMessage(command_code, 
												  true, 
												  Proxiable, 
												  application_id, 
												  this.getNextHopByHopId(), 
												  this.getNextEndToEndId());
		AVP avp;
		
		avp = new AVP(AVP.Origin_Host,true,0);
		avp.setData(this.FQDN);
		msg.addAVP(avp);
		
		avp = new AVP(AVP.Origin_Realm,true,0);
		avp.setData(this.Realm);
		msg.addAVP(avp);

		return msg;
	}
	
	
	
	/**
	 * Creates a new Diameter answer.
	 * 
	 * @param request Diameter request.
	 * @return Diameter anwer; null, if fails.
	 */
	public DiameterMessage newResponse(DiameterMessage request)
	{
		DiameterMessage msg = new DiameterMessage(request.commandCode,false,request.applicationID);
		AVP avp;
		
		msg.endToEndID = request.endToEndID;
		msg.hopByHopID = request.hopByHopID;
		
		if (request.getSessionId() != null) {
			avp = request.getSessionId();
			msg.addAVP(avp);
		}
		
		avp = new AVP(AVP.Origin_Host,true,0);
		avp.setData(this.FQDN);
		msg.addAVP(avp);
		
		avp = new AVP(AVP.Origin_Realm,true,0);
		avp.setData(this.Realm);
		msg.addAVP(avp);
		
		
		return msg;
	}
	
	/**
	 * Sends a Diameter message to the designated peer.
	 * 
	 * @param peerFQDN 	FQDN of the designated peer.
	 * @param msg 		Diameter message should be sent.
	 * @return true, if the message is sent successfully.  
	 */
	public boolean sendMessage(String peerFQDN,DiameterMessage msg)
	{
		Peer p;
		p = peerManager.getPeerByFQDN(peerFQDN);
		if (p==null){
			LOGGER.error("DiameterPeer: Peer "+peerFQDN+" not found in peer list.");
			return false;
		}
		if (p.state!=StateMachine.I_Open &&
			p.state!=StateMachine.R_Open){
			LOGGER.error("DiameterPeer: Peer "+peerFQDN+" not connected.");
			return false;
		}
		return p.sendMessage(msg);
	}

	/**
	 * Sends a Diameter message to a realm routed peer.
	 * 
	 * @param msg 		Diameter message should be sent.
	 * @return true, if the message is sent successfully.  
	 */
	public boolean sendMessage(DiameterMessage msg)
	{
		Peer p;
		if (routingTable==null){
			LOGGER.error("DiameterPeer: RoutingTable not initialized!");
			return false;
		}
		p = routingTable.getRoute(msg,peerManager);
		if (p==null){
			LOGGER.error("DiameterPeer: No suitable peer to route to could be found.");
			return false;
		}
		return p.sendMessage(msg);
	}

	/**
	 * Sends a Diameter request and a TransactionWorker handles the answer. 
	 *
	 * @param peerFQDN 	FQDN of the peer.
	 * @param req		Diameter request.
	 * @param tl		TransactionListener.
	 * @return true of a message is sent successfully, otherwise false.
	 */
	public boolean sendRequestTransactional(String peerFQDN,DiameterMessage req,TransactionListener tl)
	{
		if (this.transactionWorker!=null) return transactionWorker.sendRequestTransactional(peerFQDN,req,tl);
		else {
			LOGGER.error("DiameterPeer:sendRequestTransactional(): Transactions are not enabled on this peer!");
			return false;
		}
	}

	/**
	 * Sends a Diameter request and a TransactionWorker handles the answer. 
	 *
	 * @param req		Diameter request.
	 * @param tl		TransactionListener.
	 * @return true of a message is sent successfully, otherwise false.
	 */
	public boolean sendRequestTransactional(DiameterMessage req,TransactionListener tl)
	{
		if (this.transactionWorker!=null) return transactionWorker.sendRequestTransactional(req,tl);
		else {
			LOGGER.error("DiameterPeer:sendRequestTransactional(): Transactions are not enabled on this peer!");
			return false;
		}
	}
	
	/**
	 * Sends a Diameter message to the designated peer. The thread will wait for
	 * a Diameter answer until it arrives or timeout.
	 * 
	 * @param peerFQDN 	FQDN of the designated peer.
	 * @param req 		Diameter message should be sent.
	 * @return The Diameter answer returned. Null if timeout.
	 */	
	public DiameterMessage sendRequestBlocking(String peerFQDN,DiameterMessage req)
	{
		if (this.transactionWorker!=null) return transactionWorker.sendRequestBlocking(peerFQDN,req);
		else {
			LOGGER.error("DiameterPeer:sendRequestBlocking(): Transactions are not enabled on this peer!");
			return null;
		}
	}

	/**
	 * Sends a Diameter message to a realm routing determined peer. The thread will wait for
	 * a Diameter answer until it arrives or timeout.
	 * 
	 * @param req 		Diameter message should be sent.
	 * @return The Diameter answer returned. Null if timeout.
	 */	
	public DiameterMessage sendRequestBlocking(DiameterMessage req)
	{
		if (this.transactionWorker!=null){
			LOGGER.info("DiameterPeer:sendRequestBlocking(): Transactions are OK!");
			return transactionWorker.sendRequestBlocking(req);
		}
		LOGGER.error("DiameterPeer:sendRequestBlocking(): Transactions are not enabled on this peer!");
		return null;
		
	}
	

	/**
	 * Adds an EventListener.
	 * 
	 * @param l EventListener added.
	 */
	public void addEventListener(EventListener l)
	{
		eventListeners.add(l);
	}
	
	
	/**
	 * Removes an EventListener.
	 * 
	 * @param l EventListener removed.
	 */
	public void removeEventListener(EventListener l)
	{
		eventListeners.remove(l);
	}
	
	/**
	 * Deactives the DiameterPeer.
	 */
	public void shutdown()
	{
		Acceptor acc;
		Iterator<Acceptor> i = acceptors.iterator();
		while(i.hasNext()){
			acc = i.next();
			acc.stopAccepting();
		}
		peerManager.shutdown();
		for(int j=0;j<workerCount;j++)
			workers[j].shutdown();
		if (transactionWorker!=null) transactionWorker.shutdown();
	}
}