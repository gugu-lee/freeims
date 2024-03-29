/*
 * $Id: HSSProperties.java 612 2009-01-07 13:58:44Z vingarzan $
 *
 * Copyright (C) 2004-2006 FhG Fokus
 *
 * Parts by Instrumentacion y Componentes S.A. (Inycom). Contact at: ims at inycom dot es
 *
 * This file is part of Open IMS Core - an open source IMS CSCFs & HSS
 * implementation
 *
 * Open IMS Core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * For a license to use the Open IMS Core software under conditions
 * other than those described here, or to purchase support for this
 * software, please contact Fraunhofer FOKUS by e-mail at the following
 * addresses:
 *     info@open-ims.org
 *
 * Open IMS Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * It has to be noted that this Open Source IMS Core System is not
 * intended to become or act as a product in a commercial context! Its
 * sole purpose is to provide an IMS core reference implementation for
 * IMS technology testing and IMS application prototyping for research
 * purposes, typically performed in IMS test-beds.
 *
 * Users of the Open Source IMS Core System have to be aware that IMS
 * technology may be subject of patents and licence terms, as being
 * specified within the various IMS-related IETF, ITU-T, ETSI, and 3GPP
 * standards. Thus all Open IMS Core users have to take notice of this
 * fact and have to agree to check out carefully before installing,
 * using and extending the Open Source IMS Core System, if related
 * patents and licenses may become applicable to the intended usage
 * context. 
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  
 * 
 */
package org.freeims.hss.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
/**
 * It contains some variables which describe the HSS properties.Further it reads
 * the values for those variables from a property file.
 * <p>
 * This class has been modified by Instrumentacion y Componentes S.A (ims at inycom dot es)
 * to support the erasing of expired Subscriptions from Sh_Subscription table (variable expiry_time_lim).
 *
 * @author Andre Charton (dev -at- open-ims dot org)
 * @author Instrumentacion y Componentes S.A (Inycom)
 * for modifications (ims at inycom dot es)
 */

public class HSSProperties {
	
	private static final Logger LOGGER = Logger.getLogger(HSSProperties.class);
//	public static String TOMCAT_HOST;
//	public static String TOMCAT_PORT;
	public static String OPERATOR_ID;
	public static String AMF_ID;

	// Authentication & Security settings
	public static boolean USE_AK = false;
	public static int IND_LEN = 5;
	public static int delta = 268435456;
	public static int L = 32;
	
	public static boolean iFC_NOTIF_ENABLED = false;
	public static boolean AUTO_PPR_ENABLED = false;
	
	public static int CX_EVENT_CHECK_INTERVAL = 10;
	public static int SH_NOTIF_CHECK_INTERVAL = 10;

	public static int Expiry_time_lim = -1;  // If this parameter is -1, unlimited subscribtions are allowed
	
	private static String fileName = "hss.properties";
	
	static {
		Properties props;
		
		try{
			props = new Properties();
			props.load(Loader.getResource(fileName).openStream());
			
//			TOMCAT_HOST = props.getProperty("host");
//			TOMCAT_PORT = props.getProperty("port");
			OPERATOR_ID = props.getProperty("operatorId");
			AMF_ID = props.getProperty("amfId");
			USE_AK = Boolean.valueOf(props.getProperty("USE_AK")).booleanValue();
			IND_LEN = Integer.parseInt(props.getProperty("IND_LEN"));
			delta = Integer.parseInt(props.getProperty("delta"));
			L = Integer.parseInt(props.getProperty("L"));
			iFC_NOTIF_ENABLED = Boolean.valueOf(props.getProperty("iFC_NOTIF_ENABLED")).booleanValue();
			AUTO_PPR_ENABLED = Boolean.valueOf(props.getProperty("AUTO_PPR_ENABLED")).booleanValue();
			CX_EVENT_CHECK_INTERVAL = Integer.parseInt(props.getProperty("CX_EVENT_CHECK_INTERVAL"));
			SH_NOTIF_CHECK_INTERVAL = Integer.parseInt(props.getProperty("SH_NOTIF_CHECK_INTERVAL"));
			Expiry_time_lim = Integer.parseInt(props.getProperty("expiry_time_lim"));
		}
		catch (FileNotFoundException e) {
			LOGGER.error("FileNotFoundException !", e);
			LOGGER.error("Failed to load configuration from \"" + fileName + "\" file!");
			System.exit(0);
		}
		catch (IOException e) {
			LOGGER.error("IOException !", e);
			LOGGER.error("Failed to load configuration from \"" + fileName + "\" file!");
			System.exit(0);
		}
	}
}
