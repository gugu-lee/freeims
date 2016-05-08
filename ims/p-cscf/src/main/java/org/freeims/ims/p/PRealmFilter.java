package org.freeims.ims.p;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;

import org.apache.log4j.Logger;
import org.freeims.common.SipUtil;
import org.freeims.ims.config.Config;
import org.freeims.ims.config.PCscfConfig;
import org.freeims.sipproxy.servlet.SipProxyRequest;
import org.freeims.sipproxy.servlet.impl.SipProxyFilter;
import org.freeims.sipproxy.servlet.impl.SipProxyRequestImpl;

public class PRealmFilter extends SipProxyFilter
{

	private static Logger logger = Logger.getLogger(PRealmFilter.class);

	private PCscfConfig pcscfConf = null;
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		super.init(filterConfig);
		logger.info("init FilterConfig");
		pcscfConf = (PCscfConfig)filterConfig.getServletContext().getAttribute("configInstance");
	}

	@Override
	protected void doFilterRegister(SipProxyRequest req, FilterChain chain) throws ServletException, IOException {
		try {
			
			if (pcscfConf == null)
			{
				logger.info("pcscfConf is null");
				pcscfConf = (PCscfConfig)((SipProxyRequestImpl)req).getServletContext().getAttribute("configInstance");
			}
			
			String realm = SipUtil.extractRealm(req);
			if (pcscfConf.getRealmConfig(realm)==null)
			{
				req.createResponseAction(500, "No support your realm.By P-CSCF.");
				return;
			}
		} catch (Exception e) {
			logger.info(e.getMessage(),e);
			req.createResponseAction(500,"Server Exception");
			return;
		}
		
		super.doFilterRegister(req, chain);
	}

	
}
