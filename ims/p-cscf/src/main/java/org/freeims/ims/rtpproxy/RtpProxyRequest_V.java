package org.freeims.ims.rtpproxy;

public class RtpProxyRequest_V extends RtpProxyRequest 
{

	public RtpProxyRequest_V()
	{
		command = Command.V;
	}
	
	@Override
	public byte[] encode() 
	{
		return (createToken() + " " + command).getBytes();
	}
}
