package org.freeims.sipproxy.container;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.HostConfig;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;


public class SipProxyHost extends StandardHost {
	Log log = LogFactory.getLog(SipProxyHost.class);
	private boolean initialized = false;

	private String bindConnectors;
	public String getBindConnectors() {
		return bindConnectors;
	}

	public void setBindConnectors(String bindConnectors) {
		this.bindConnectors = bindConnectors;
	}

	@Override
	public void initInternal() {

		if (initialized)
			return;
		initialized = true;

		try {
			super.initInternal();
		} catch (LifecycleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	@Override
	protected synchronized void startInternal() throws LifecycleException {
		

		super.startInternal();
	}

}
