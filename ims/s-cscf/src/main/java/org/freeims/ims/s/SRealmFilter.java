package org.freeims.ims.s;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import org.apache.log4j.Logger;
import org.freeims.common.SipUtil;
import org.freeims.ims.config.PCscfConfig;
import org.freeims.ims.config.SCscfConfig;
import org.freeims.sipproxy.servlet.SipProxyRequest;
import org.freeims.sipproxy.servlet.impl.SipProxyFilter;
import org.freeims.sipproxy.servlet.impl.SipProxyRequestImpl;


public class SRealmFilter extends SipProxyFilter
{

	private static Logger logger = Logger.getLogger(SRealmFilter.class);
	private SCscfConfig sConfig = null;
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		super.init(filterConfig);
		sConfig = (SCscfConfig)filterConfig.getServletContext().getAttribute("configInstance");
		logger.info("init FilterConfig");
	}

	@Override
	protected void doFilterRegister(SipProxyRequest req, FilterChain chain) throws ServletException, IOException {
		try {
			
			if (sConfig == null)
			{
				logger.info("scscfConf is null");
				sConfig = (SCscfConfig)((SipProxyRequestImpl)req).getServletContext().getAttribute("configInstance");
			}
			
			String realm = SipUtil.extractRealm(req);
			if (sConfig.getRealmConfig(realm)==null)
			{
				req.createResponseAction(403, "No support your realm.By S-CSCF.");
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
