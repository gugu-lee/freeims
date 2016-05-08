package org.freeims.sipproxy.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.SipStack;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardService;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;


//extends LifecycleMBeanBase
public class SipProxyService extends  StandardService
{

	private  SipStack sipStack=null;
	private String sipStackPropertyFileName = null;
	
	private static Log log = LogFactory.getLog(SipProxyService.class);
	
	@Override
	protected void destroyInternal() throws LifecycleException {
		super.destroyInternal();
	}
	Properties defaultProperties = new Properties();
	public SipProxyService()
	{
		defaultProperties.setProperty("javax.sip.STACK_NAME", "org.freeims");
		// You need 16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		defaultProperties.setProperty("org.freeims.javax.sip.TRACE_LEVEL","INFO");
		//properties.setProperty("org.freeims.javax.sip.TRACE_LEVEL", "32");
		defaultProperties.setProperty("org.freeims.javax.sip.DEBUG_LOG", "sipproxy-svr.txt");
		defaultProperties.setProperty("org.freeims.javax.sip.SERVER_LOG", "sipproxy.txt");
		defaultProperties.setProperty("org.freeims.javax.sip.AUTOMATIC_DIALOG_ERROR_HANDLING", "false");
		defaultProperties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");
		defaultProperties.setProperty("org.freeims.javax.sip.MESSAGE_PROCESSOR_FACTORY","org.freeims.javax.sip.stack.NioMessageProcessorFactory");
	}
	@Override
	protected void initInternal() throws LifecycleException {
		super.initInternal();
		
		
		Properties properties ;
		if (this.getSipStackPropertyFile() == null || this.getSipStackPropertyFile().equals(""))
		{
			properties = defaultProperties;
		}else{
			File catalinaBase = this.getServer().getCatalinaBase();
			properties = new Properties();
			try{
			properties.load(new FileInputStream(new File(catalinaBase,"conf"+File.separatorChar+this.getSipStackPropertyFile())));
			}catch(IOException ioEx)
			{
				LifecycleException lifecycleEx = new LifecycleException("Load SipStack Properties is failed.",ioEx);
				
				throw lifecycleEx;
			}
		}
		try {

			System.setProperty("org.freeims.core.STRIP_ADDR_SCOPES", "true");
			System.setProperty("org.freeims.javax.sip.REENTRANT_LISTENER","true");
			
			SipFactory sipFactory = null;
			
			sipFactory = SipFactory.getInstance();
			sipFactory.setPathName("org.freeims");

			try {
				// Create SipStack object
				sipStack = sipFactory.createSipStack(properties);

			} catch (PeerUnavailableException e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
				if (e.getCause() != null)
					e.getCause().printStackTrace();
			}

			for (Connector c: this.connectors)
			{
				if (!(c.getProtocolHandler() instanceof SipProxyProtocolHandler)){
					continue;
				}
				SipProxyProtocolHandler handler = (SipProxyProtocolHandler)c.getProtocolHandler();
				handler.setSipStack(sipStack);
				handler.setService(this);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}

	public String getSipStackPropertyFile() {
		return sipStackPropertyFileName;
	}

	public void setSipStackPropertyFile(String sipStackPropertyFileName) {
		this.sipStackPropertyFileName = sipStackPropertyFileName;
	}

	
}
