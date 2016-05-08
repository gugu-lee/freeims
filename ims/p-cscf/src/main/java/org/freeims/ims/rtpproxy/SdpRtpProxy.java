package org.freeims.ims.rtpproxy;

import org.freeims.sipproxy.servlet.SipProxyRequest;

public class SdpRtpProxy 
{
	private SipProxyRequest request;
	private String rtpProxyIp;
	private int port;
	private String publicRtpProxyIp;
	
	public SipProxyRequest getRequest() {
		return request;
	}
	public void setRequest(SipProxyRequest request) {
		this.request = request;
	}
	public String getRtpProxyIp() {
		return rtpProxyIp;
	}
	public void setRtpProxyIp(String rtpProxyIp) {
		this.rtpProxyIp = rtpProxyIp;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getPublicRtpProxyIp() {
		return publicRtpProxyIp;
	}
	public void setPublicRtpProxyIp(String publicRtpProxyIp) {
		this.publicRtpProxyIp = publicRtpProxyIp;
	}
	
	public SdpRtpProxy(SipProxyRequest request,String rtpproxyIp,int port)
	{
		this(request,rtpproxyIp,port,null);
	}
	public SdpRtpProxy(SipProxyRequest request,String rtpproxyIp,int port,String publicRtpProxyIp)
	{
		setRequest(request);
		setPort(port);
		setRtpProxyIp(rtpproxyIp);
		setPublicRtpProxyIp(publicRtpProxyIp);
	}
	public void applyRtpProxy()
	{
		
	}
}
