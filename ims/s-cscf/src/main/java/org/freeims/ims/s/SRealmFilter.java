package org.freeims.ims.s;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.freeims.common.SipUtil;
import org.freeims.ims.config.ICscfConfig;
import org.freeims.ims.config.SCscfConfig;
import org.freeims.sipproxy.servlet.SipProxyRequest;
import org.freeims.sipproxy.servlet.SipProxyResponse;
import org.freeims.sipproxy.servlet.SubsequentAction;
import org.freeims.sipproxy.servlet.impl.SipProxyFilter;
import org.freeims.sipproxy.servlet.impl.SipProxyRequestImpl;


public class SRealmFilter extends SipProxyFilter
{

	private final static String BODY_TYPE_TEXT="text/plain";
	private final static String BODY_RESPONSE_403="you has not legal realm.you could send email to service@freeims.org if you need any help.";
	
	
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
				SipProxyResponse resp = req.createResponse(403,"No support your realm.By S-CSCF.");
				resp.setContent(BODY_RESPONSE_403, BODY_TYPE_TEXT);
				 req.createResponseAction(resp);
				return;
			}
		} catch (Exception e) {
			logger.info(e.getMessage(),e);
			req.createResponseAction(500,"Server Exception");
			return;
		}
		
		super.doFilterRegister(req, chain);
	}
	@Override
	protected void doFilterInvite(SipProxyRequest req, FilterChain chain) throws ServletException, IOException {
		
		try {
			sConfig = (SCscfConfig)((SipProxyRequestImpl)req).getServletContext().getAttribute("configInstance");
			String realm = null;
			if (!SipUtil.isMessageInTerm(req)) {
				realm = SipUtil.extractRealm((SipURI)req.getFrom().getURI());
			}else{
				realm = SipUtil.extractRealm((SipURI)req.getTo().getURI());
			}
			
			realm = SipUtil.extractRealm((SipURI)req.getTo().getURI());
			if (sConfig.getRealmConfig(realm)==null)
			{
				SipProxyResponse resp = req.createResponse(403,"No support your realm.By S-CSCF.");
				resp.setContent(BODY_RESPONSE_403, BODY_TYPE_TEXT);
				
				 req.createResponseAction(resp);
				
				return;
			}
		} catch (Exception e) {
			logger.info(e.getMessage(),e);
			req.createResponseAction(500,"Server Exception");
			return;
		}
		logger.info("realm checked ok");
		super.doFilterInvite(req, chain);
	}
	
}
