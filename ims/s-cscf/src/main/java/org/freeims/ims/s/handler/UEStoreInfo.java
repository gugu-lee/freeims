package org.freeims.ims.s.handler;
import java.io.Serializable;

import javax.sip.address.Address;
import javax.sip.header.ContactHeader;

public class UEStoreInfo implements Serializable
	{
		
		private static final long serialVersionUID = 2220608222651734555L;
		private String publicIdentity;
		private long registerTime;
		private ContactHeader contact;
		private Address path;
		private String realm;
		private String username;
		
		public Address getPath() {
			return path;
		}
		public void setPath(Address path) {
			this.path = path;
		}
		public UEStoreInfo()
		{
			registerTime = System.currentTimeMillis();
		}
		public String getPublicIdentity() {
			return publicIdentity;
		}
		public void setPublicIdentity(String publicIdentity) {
			this.publicIdentity = publicIdentity;
		}
		public ContactHeader getContact() {
			return contact;
		}
		public void setContact(ContactHeader contact) {
			this.contact = contact;
		}
		
		public long getRegisterTime() {
			return registerTime;
		}
		public String getRealm() {
			return realm;
		}
		public void setRealm(String realm) {
			this.realm = realm;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		
	}