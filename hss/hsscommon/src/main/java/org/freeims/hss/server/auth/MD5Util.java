/*
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

package org.freeims.hss.server.auth;

/**
* @author Florin Popescu florin dot popescu -at- fokus dot fraunhofer dot de
*/


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
	public static final byte [] colon = ":".getBytes();
	public static byte [] av_HA1(byte [] username, byte [] realm, byte [] password)
	{
		byte [] ha1;
		MessageDigest md = null;
		//HASH HA1;
		try {
			md = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException nsae)
		{
			return null;
		}
		md.update(username);
		md.update(colon);
		md.update(realm);
		md.update(colon);
		md.update(password);
		
		ha1 = md.digest();
		
		return ha1;
	}
	/*response-digest */
	public static byte []  calcResponse(byte[] _ha1,      /* H(A1) */
	                    		   byte [] _nonce,       /* nonce from server */
	                    		   byte [] _method,      /* method from the request */
	                    		   byte []  _uri         /* requested URL */)
	                    		    
	{
    	byte [] HA2;
    	byte [] HA2Hex;
    	byte [] RespHash;
    	byte [] _response;
    	
    	MessageDigest md = null;
		//HASH HA1;
		try {
			md = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException nsae)
		{
			return null;
		}
    	
         /* calculate H(A2) */
		md.update(_method);
		md.update(colon);
		md.update(_uri);
    	
		HA2 = md.digest();
    	
		HA2Hex = HexCodec.encode(HA2).getBytes();
    	
    	 /* calculate response */
    	md.update(_ha1);
    	md.update(colon);
    	md.update(_nonce);
    	md.update(colon);
    	md.update(HA2Hex);
    	
    	RespHash = md.digest();
    	_response = HexCodec.encode(RespHash).getBytes();
    	return _response;
    } 
	
}
