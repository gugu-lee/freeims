package org.freeims.ims.config;

public class RealmConfig {
	private String name;
	private String host;
	private int port;
	private String ipAddress;
	private String hssAddress =null;
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	public String getHssAddress() {
		return hssAddress;
	}

	public void setHssAddress(String hssAddress) {
		this.hssAddress = hssAddress;
	}
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("name:"+this.getName()+" host:"+this.getHost()+" ipAddress:"+this.getIpAddress()+" port:"+this.getPort());
		return sb.toString();
	}

}
