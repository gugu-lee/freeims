package org.freeims.ims.p;

import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.sip.address.Address;
import javax.sip.address.URI;



public class SipOnlineSessionImpl implements SipOnlineSession
{
	
	private Hashtable<String,Object> attrTable = new Hashtable<String,Object>();
	
	/** 15 minutes */
	private int deltaMinutes=15;
	private long creationTime =0;
	private long lastAccessedTime =0;

	@Override
	public void access()
	{
		lastAccessedTime=System.currentTimeMillis();
	}

	@Override
	public Object getAttribute(String name) {
		return attrTable.get(name);
	}



	@Override
	public long getCreationTime() {
		
		return creationTime;
	}

	@Override
	public long getExpirationTime() {

		return creationTime + deltaMinutes * 60 * 1000;
	}

	@Override
	public String getId() {

		return null;
	}




	@Override
	public long getLastAccessedTime() {
		
		return lastAccessedTime;
	}

	@Override
	public void invalidate() {
		deltaMinutes=-1;
		
	}

	@Override
	public boolean isValid() {
		
		return getExpirationTime()>System.currentTimeMillis();
	}

	@Override
	public void removeAttribute(String name) {
		attrTable.remove(name);
		
	}

	@Override
	public void setAttribute(String name, Object attribute) {
		attrTable.put(name, attribute);
		
	}

	@Override
	public int setExpires(int deltaMinutes) {

		this.deltaMinutes = deltaMinutes;
		return deltaMinutes;
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Address getLocalParty() {

		return null;
	}

	@Override
	public ServletContext getServletContext() {

		return null;
	}

	@Override
	public URI getSubscriberURI() {

		return null;
	}





}
