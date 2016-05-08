package org.freeims.ims.config;

public class RtpProxyConfig {
	private boolean enabled;
	private String host;
	private int port;
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(" host:" + this.getHost() + " port:" + this.getPort() +" enabled:"+this.isEnabled()+"\r\n");
		return sb.toString();
	}
}
