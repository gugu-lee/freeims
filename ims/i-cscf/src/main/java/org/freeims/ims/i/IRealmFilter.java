package org.freeims.ims.i;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.freeims.common.SipUtil;
import org.freeims.ims.config.ICscfConfig;
import org.freeims.sipproxy.servlet.SipProxyRequest;
import org.freeims.sipproxy.servlet.SipProxyResponse;
import org.freeims.sipproxy.servlet.SubsequentAction;
import org.freeims.sipproxy.servlet.impl.SipProxyFilter;
import org.freeims.sipproxy.servlet.impl.SipProxyRequestImpl;

public class IRealmFilter extends SipProxyFilter
{

	private final static String BODY_TYPE_TEXT="text/plain";
	private final static String BODY_RESPONSE_403="you has not legal realm.you could send email to service@freeims.org if you need any help.";
	
	private static Logger logger = Logger.getLogger(IRealmFilter.class);
	
	
	private ICscfConfig iConfig = null;
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		super.init(filterConfig);
		
	}

	@Override
	protected void doFilterRegister(SipProxyRequest req, FilterChain chain) throws ServletException, IOException {
		try {
			iConfig = (ICscfConfig)((SipProxyRequestImpl)req).getServletContext().getAttribute("configInstance");
			String realm = SipUtil.extractRealm(req);
			if (iConfig.getRealmConfig(realm)==null)
			{
				SipProxyResponse resp = req.createResponse(403);
				resp.setContent(BODY_RESPONSE_403, BODY_TYPE_TEXT);
				
				SubsequentAction action = req.createResponseAction(403, "No support your realm.By I-CSCF.");
				action.setResponse(resp);
				return;
			}
		} catch (Exception e) {
			logger.info(e.getMessage(),e);
			req.createResponseAction(500,"Server Exception");
			return;
		}
		logger.info("realm checked ok");
		super.doFilterRegister(req, chain);
	}



	@Override
	protected void doFilterInvite(SipProxyRequest req, FilterChain chain) throws ServletException, IOException {
		
		try {
			iConfig = (ICscfConfig)((SipProxyRequestImpl)req).getServletContext().getAttribute("configInstance");
			String realm = SipUtil.extractRealm((SipURI)req.getTo().getURI());
			if (iConfig.getRealmConfig(realm)==null)
			{
				SipProxyResponse resp = req.createResponse(403);
				resp.setContent(BODY_RESPONSE_403, BODY_TYPE_TEXT);
				
				SubsequentAction action = req.createResponseAction(403, "No support your realm.By I-CSCF.");
				action.setResponse(resp);
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
