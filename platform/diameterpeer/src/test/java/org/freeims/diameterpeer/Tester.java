package org.freeims.diameterpeer;

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;


public class Tester {

	private static Logger logger = Logger.getLogger(Tester.class);
	public static void main(String[] args)
	{
		//logger.info("diameterpeer config file:"+dpFileName);
		URL url = Loader.getResource("diameter-peer-ims.xml");
		String filePath = url.getPath();
		logger.info(filePath);
		//Thread.currentThread().setContextClassLoader(oldCls);
		
		DiameterPeer diameterPeer = new DiameterPeer(filePath, "ICSCF");
		diameterPeer.enableTransactions(10, 1);
	}
}
