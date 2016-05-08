/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.freeims.sipproxy.container;




import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.Properties;
import java.util.Random;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import javax.sip.SipProvider;
import javax.sip.SipStack;


import org.apache.catalina.Executor;

import org.apache.catalina.core.StandardService;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.Adapter;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.UpgradeProtocol;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.res.StringManager;
import org.freeims.javax.sip.ListeningPointExt;

/**
 * This is the sip protocol handler that will get called upon creation of the
 * tomcat connector defined in the server.xml.<br/>
 * To use a sip connector, one need to specify a new connector in server.xml
 * with org.mobicents.servlet.sip.startup.SipProtocolHandler as the value for
 * the protocol attribute.<br/>
 * 
 * Some of the fields (representing the sip stack propeties) get populated
 * automatically by the container.<br/>
 * 
 * @author Jean Deruelle
 * 
 */
public class SipProxyProtocolHandler implements ProtocolHandler, MBeanRegistration {

	public static final String IS_SIP_CONNECTOR = "isSipProxyConnector";
	// the logger
	private static final Log logger = LogFactory.getLog(SipProxyProtocolHandler.class.getName());
	// *
	protected ObjectName tpOname = null;
	// *
	protected ObjectName rgOname = null;


	private Adapter adapter = null;

	private Map<String, Object> attributes = new HashMap<String, Object>();

	private SipStack sipStack = null;

	private String name;

	private String ipAddress;
	private int port;
	private String transport;

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getTransport() {
		return transport;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/*
	 * the extended listening point with global ip address and port for it
	 */
	public ExtendedListeningPointImpl extendedListeningPoint;

	// defining sip stack properties

	private static SipProxyApplicationDispatcher sipApplicationDispatcher = null;
	private boolean started = false;

	/**
	 * {@inheritDoc}
	 */
	public void destroy() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Stopping a sip protocol handler");
		}
		// Jboss specific unloading case
		SipProxyApplicationDispatcher sipApplicationDispatcher = (SipProxyApplicationDispatcher) getAttribute(
				SipProxyApplicationDispatcher.class.getSimpleName());
		if (sipApplicationDispatcher != null && extendedListeningPoint != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Removing the Sip Application Dispatcher as a sip listener for listening point "
						+ extendedListeningPoint);
			}
			extendedListeningPoint.getSipProvider().removeSipListener(sipApplicationDispatcher);
			// sipApplicationDispatcher.getSipNetworkInterfaceManager().removeExtendedListeningPoint(extendedListeningPoint);
		}
		// removing listening point and sip provider
		if (sipStack != null) {
			if (extendedListeningPoint != null) {
				if (extendedListeningPoint.getSipProvider().getListeningPoints().length == 1) {
					sipStack.deleteSipProvider(extendedListeningPoint.getSipProvider());
					if (logger.isDebugEnabled()) {
						logger.debug("Removing the sip provider");
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Removing the following Listening Point " + extendedListeningPoint
								+ " from the sip provider");
					}
					extendedListeningPoint.getSipProvider()
							.removeListeningPoint(extendedListeningPoint.getListeningPoint());
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Removing the following Listening Point " + extendedListeningPoint);
				}
				sipStack.deleteListeningPoint(extendedListeningPoint.getListeningPoint());
				extendedListeningPoint = null;
			}
		}
		if (tpOname != null)
			Registry.getRegistry(null, null).unregisterComponent(tpOname);
		if (rgOname != null)
			Registry.getRegistry(null, null).unregisterComponent(rgOname);
		setStarted(false);
		sipStack = null;
	}

	public Adapter getAdapter() {
		return adapter;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAttribute(String attribute) {
		return attributes.get(attribute);
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator getAttributeNames() {
		return attributes.keySet().iterator();
	}



	public void setAdapter(Adapter adapter) {
		this.adapter = adapter;

	}

	/**
	 * {@inheritDoc}
	 */
	public void setAttribute(String arg0, Object arg1) {
		attributes.put(arg0, arg1);
	}

	public void start() throws Exception {

		final String ipAddress = this.getIpAddress();
		final int port = this.getPort();
		final String signalingTransport = this.getTransport();

		try {

			logger.info("ipaddress:" + ipAddress + " port:" + port);
			
			ListeningPointExt listeningPoint = (ListeningPointExt) sipStack.createListeningPoint(ipAddress, port,
					signalingTransport);

			// boolean createSipProvider = false;
			SipProvider sipProvider = null;

			sipProvider = sipStack.createSipProvider(listeningPoint);

			if (sipApplicationDispatcher == null) {

				sipApplicationDispatcher = new SipProxyApplicationDispatcher();
				sipApplicationDispatcher.setService(this.service);
				this.setAttribute(SipProxyApplicationDispatcher.class.getSimpleName(), sipApplicationDispatcher);
				logger.info("add sipListener");

			}
			sipProvider.addSipListener(sipApplicationDispatcher);
			sipApplicationDispatcher.addProtocolhandler(this);

			setStarted(true);
		} catch (Exception ex) {
			logger.error("A problem occured while setting up SIP Connector " + ipAddress + ":" + port + "/"
					+ signalingTransport + "-- check server.xml for tomcat. ", ex);
		} finally {
			if (!isStarted()) {
				destroy();
			}
		}
	}

	/**
	 * @param started
	 *            the started to set
	 */
	public void setStarted(boolean started) {
		this.started = started;
	}

	/**
	 * @return the started
	 */
	public boolean isStarted() {
		return started;
	}

	public void setSipStack(SipStack sipStack) {
		logger.info("set sipStack");
		this.sipStack = sipStack;
	}

	private StandardService service;

	public void setService(StandardService service) {
		this.service = service;
	}

	public Executor getExecutor() {
		// TODO Auto-generated method stub
		// throw new UnsupportedOperationException();

		// Issue 80: http://code.google.com/p/sipservlets/issues/detail?id=80
		return null;
	}

	@Override
	public void stop() throws Exception {

		destroy();
	}

	// ----------------------------------------------------- JMX related methods

	private Log getLog()
	{
		return logger;
	}
    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;

    public ObjectName getObjectName() {
        return oname;
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception {
        oname = name;
        mserver = server;
        domain = name.getDomain();
        return name;
    }

    @Override
    public void postRegister(Boolean registrationDone) {
        // NOOP
    }

    @Override
    public void preDeregister() throws Exception {
        // NOOP
    }

    @Override
    public void postDeregister() {
        // NOOP
    }

    private ObjectName createObjectName() throws MalformedObjectNameException {
        // Use the same domain as the connector
        domain = getAdapter().getDomain();

        if (domain == null) {
            return null;
        }

        StringBuilder name = new StringBuilder(getDomain());
        name.append(":type=ProtocolHandler,port=");
        int port = getPort();
        if (port > 0) {
            name.append(getPort());
        } /*else {
            name.append("auto-");
            name.append(getNameIndex());
        }*/
        //InetAddress address = getAddress();
        if (this.ipAddress != null) {
            name.append(",address=");
            name.append(ObjectName.quote(this.ipAddress));
        }
        return new ObjectName(name.toString());
    }


    // ------------------------------------------------------- Lifecycle methods

    /*
     * NOTE: There is no maintenance of state or checking for valid transitions
     * within this class. It is expected that the connector will maintain state
     * and prevent invalid state transitions.
     */
    private static final StringManager sm = StringManager.getManager(AbstractProtocol.class);
    @Override
    public void init() throws Exception {
    	setAttribute(IS_SIP_CONNECTOR, Boolean.TRUE);
        if (getLog().isInfoEnabled())
            getLog().info(sm.getString("abstractProtocolHandler.init",
                    getName()));

        if (oname == null) {
            // Component not pre-registered so register it
            oname = createObjectName();
            if (oname != null) {
                Registry.getRegistry(null, null).registerComponent(this, oname,
                    null);
            }
        }

        if (this.domain != null) {
            try {
                tpOname = new ObjectName(domain + ":" +
                        "type=ThreadPool,name=" + getName());
                Registry.getRegistry(null, null).registerComponent(sipStack,
                        tpOname, null);
            } catch (Exception e) {
                getLog().error(sm.getString(
                        "abstractProtocolHandler.mbeanRegistrationFailed",
                        tpOname, getName()), e);
            }
//            rgOname=new ObjectName(domain +
//                    ":type=GlobalRequestProcessor,name=" + getName());
//            Registry.getRegistry(null, null).registerComponent(
//                    getHandler().getGlobal(), rgOname, null );
        }



    }












//    @Override
//    public void destroy() {
//
//
//        if (oname != null) {
//            if (mserver == null) {
//                Registry.getRegistry(null, null).unregisterComponent(oname);
//            } else {
//                // Possibly registered with a different MBeanServer
//                try {
//                    mserver.unregisterMBean(oname);
//                } catch (MBeanRegistrationException |
//                        InstanceNotFoundException e) {
//                    getLog().info(sm.getString(
//                            "abstractProtocol.mbeanDeregistrationFailed",
//                            oname, mserver));
//                }
//            }
//        }
//
//        if (tpOname != null)
//            Registry.getRegistry(null, null).unregisterComponent(tpOname);
//        if (rgOname != null)
//            Registry.getRegistry(null, null).unregisterComponent(rgOname);
//    
//
//	}

	@Override
	public void addSslHostConfig(SSLHostConfig arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addUpgradeProtocol(UpgradeProtocol arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SSLHostConfig[] findSslHostConfigs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UpgradeProtocol[] findUpgradeProtocols() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAprRequired() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSendfileSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void pause() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() throws Exception {
		// TODO Auto-generated method stub
		
	}

	
}
