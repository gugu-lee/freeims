package org.freeims.ims.config;

import java.util.ArrayList;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class CscfConfig {
	private static Log logger = LogFactory.getLog(PCscfConfig.class);
	private ArrayList<RealmConfig> realms = new ArrayList<RealmConfig>();
	private String dnsServer = null;
	private UserProperties props = null;
	
	public ArrayList<RealmConfig> getRealmConfigs() {
		return realms;
	}

	public void addRealmConfig(RealmConfig d) {
	
		realms.add(d);
	}

	public void setRealmConfigs(ArrayList<RealmConfig> realms) {
		this.realms = realms;
	}

	public RealmConfig getRealmConfig(String name) {

		for (RealmConfig d : realms) {
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

	public void setProps(UserProperties props) {
		this.props = props;

		if (props == null)
		{
			logger.info("props is null");
			return;
		}
		for (RealmConfig d : realms) {
			d.setHost(props.replaceUserProperty(d.getHost()));
			d.setHssAddress(props.replaceUserProperty(d.getHssAddress()));
			d.setIpAddress(props.replaceUserProperty(d.getIpAddress()));
			d.setName(props.replaceUserProperty(d.getName()));

		}

		this.dnsServer = props.replaceUserProperty(dnsServer);
	}



}
