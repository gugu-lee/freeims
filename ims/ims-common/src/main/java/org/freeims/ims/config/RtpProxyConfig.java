package org.freeims.ims.config;

public class RtpProxyConfig {
	private boolean enabled;
	private String ctrlHost;
	private int ctrlPort;
	private String proxyHost;
	
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getCtrlHost() {
		return ctrlHost;
	}
	public void setCtrlHost(String host) {
		this.ctrlHost = host;
	}
	public int getCtrlPort() {
		return ctrlPort;
	}
	public void setCtrlPort(int port) {
		this.ctrlPort = port;
	}
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(" host:" + this.getCtrlHost() + " port:" + this.getCtrlPort() +" enabled:"+this.isEnabled()+"\r\n");
		return sb.toString();
	}
	public String getProxyHost() {
		return proxyHost;
	}
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}
}
