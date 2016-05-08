package org.freeims.ims.config;

import java.util.ArrayList;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class CscfConfig {
	private static Log logger = LogFactory.getLog(PCscfConfig.class);
	private ArrayList<RealmConfig> domains = new ArrayList<RealmConfig>();
	private String dnsServer = null;
	
	public ArrayList<RealmConfig> getRealmConfigs() {
		return domains;
	}

	public void addRealmConfig(RealmConfig d) {
		domains.add(d);
	}

	public void setRealmConfigs(ArrayList<RealmConfig> domains) {
		this.domains = domains;
	}

	public RealmConfig getRealmConfig(String name) {

		logger.info("name:" + name);
		for (RealmConfig d : domains) {
			if (d.getName().equals(name)) {
				return d;
			}

		}
		return null;
	}
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		for (RealmConfig r:getRealmConfigs())
		{
			sb.append(r.toString()).append("\r\n");
		}
		return sb.toString();
	}

	public String getDnsServer() {
		return dnsServer;
	}

	public void setDnsServer(String dnsServer) {
		this.dnsServer = dnsServer;
	}



}
