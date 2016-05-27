package org.freeims.hss.server.main;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import javax.servlet.annotation.WebServlet;
//@WebServlet(value="HssServlet",loadOnStartup=1)
public class HssServlet extends HttpServlet
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4120034718938518730L;
	private static Logger logger = Logger.getLogger(HssServlet.class);
	private HSSContainer hssContainer = null;
	@Override
	public void destroy() {
		super.destroy();
		 hssContainer.diamStack.diameterPeer.shutdown();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		
		super.init(config);
		logger.info("init HssServlet");
		hssContainer = new HSSContainer();
	}

}
