package org.freeims.ims.config;


public class PCscfConfig extends CscfConfig {
	private RtpProxyConfig rtpProxy;
	public RtpProxyConfig getRtpProxy() {
		return rtpProxy;
	}

	public void setRtpProxy(RtpProxyConfig rtpProxy) {
		this.rtpProxy = rtpProxy;
	}

	public String toString()
	{
		String str = super.toString();
		return str +"\r\nRtpProxy:\r\n"+rtpProxy.toString();
	}

	
}
