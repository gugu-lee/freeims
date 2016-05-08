package org.freeims.ims.p;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.sip.address.Address;
import javax.sip.address.URI;



public interface SipOnlineSession {

	public void access();
	/**
	 * Returns the object bound with the specified name in this session, or null
	 * if no object is bound under the name.
	 */
	java.lang.Object getAttribute(java.lang.String name);

	/**
	 * Returns an Enumeration over the String objects containing the names of
	 * all the objects bound to this session.
	 */
	Enumeration<java.lang.String> getAttributeNames();


	/**
	 * Returns the time when this session was created, measured in milliseconds
	 * since midnight January 1, 1970 GMT.
	 */
	long getCreationTime();

	/**
	 * Returns a string containing the unique identifier assigned to this
	 * session. The identifier is assigned by the servlet container and is
	 * implementation dependent.
	 */
	java.lang.String getId();





	/**
	 * Returns the last time the client sent a request associated with this
	 * session, as the number of milliseconds since midnight January 1, 1970
	 * GMT. Actions that your application takes, such as getting or setting a
	 * value associated with the session, do not affect the access time.
	 */
	long getLastAccessedTime();

	/**
	 * Returns the Address identifying the local party. This is the value of the
	 * From header of locally initiated requests in this leg.
	 */
	Address getLocalParty();



	/**
	 * Returns the ServletContext to which this session belongs. By definition,
	 * there is one ServletContext per sip (or web) module per JVM. Though, a
	 * SipSession belonging to a distributed application deployed to a
	 * distributed container may be available across JVMs, this method returns
	 * the context that is local to the JVM on which it was invoked.
	 * 
	 * @return ServletContext object for the sip application
	 * @since 1.1
	 */
	ServletContext getServletContext();


	/**
	 * Returns the URI of the subscriber for which this application is invoked
	 * to serve. This is only available if this SipSession received an initial
	 * request. Otherwise, this method throws IllegalStateException.
	 * 
	 * @throws IllegalStateException
	 *             if this method is called on an invalidated session
	 */
	URI getSubscriberURI();

	/**
	 * Invalidates this session and unbinds any objects bound to it. A session
	 * cannot be invalidate if it is in the EARLY or CONFIRMED state, or if
	 * there exist ongoing transactions where a final response is expected. One
	 * exception is if this session has an associated unsupervised proxy, in
	 * which case the session can be invalidate even if transactions are
	 * ongoing.
	 */
	void invalidate();

	/**
	 * Returns true if this SipSession is valid, false otherwise. The SipSession
	 * can be invalidated by calling the method on it. Also the SipSession can
	 * be invalidated by the container when either the associated times out or
	 * is invoked.
	 */
	boolean isValid();


	/**
	 * Removes the object bound with the specified name from this session. If
	 * the session does not have an object bound with the specified name, this
	 * method does nothing.
	 */
	void removeAttribute(java.lang.String name);

	/**
	 * Binds an object to this session, using the name specified. If an object
	 * of the same name is already bound to the session, the object is replaced.
	 */
	void setAttribute(java.lang.String name, java.lang.Object attribute);

	

	public long getExpirationTime();

	public int setExpires(int deltaMinutes);
}
