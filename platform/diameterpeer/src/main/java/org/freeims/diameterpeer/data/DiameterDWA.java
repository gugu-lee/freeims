/*
 * $Id: DiameterDWA.java 2 2006-11-14 22:37:20Z vingarzan $
 *
 * Copyright (C) 2004-2006 FhG Fokus
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
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.freeims.diameterpeer.data;

/**
 * This class defines the Diameter Message Diameter-Watchdog-Answer
 * 
 * @author Dragos Vingarzan vingarzan -at- fokus dot fraunhofer dot de
 *
 */
public class DiameterDWA extends DiameterMessage {

	public AVP result_code;
	public AVP origin_host;
	public AVP origin_realm;
	
	public DiameterDWA(int result_code)
	{
		super(DiameterMessage.Code_DW,false,0);
		AVP avp;

		this.flagProxiable=false;
		
		/* Result Code */
		avp = new AVP(AVP.Result_Code,true,0);
		avp.setData(result_code);
		this.addAVP(avp);
		this.result_code = avp;
		
		/* Origin-Host */
		avp = new AVP(AVP.Origin_Host,true,0);
		this.addAVP(avp);
		this.origin_host = avp;
		
		/* Origin-Realm */
		avp = new AVP(AVP.Origin_Realm,true,0);
		this.addAVP(avp);
		this.origin_realm = avp;
		
	}
	
}
