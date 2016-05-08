package org.freeims.ims.rtpproxy;


public abstract class RtpProxyRequest 
{
	protected int len;
	protected String command;
	
	public abstract byte[] encode();
	
	private static long tokenSeq = 0;

	
	private String tokenPrefix;
	
	public String getTokenPrefix() {
		return tokenPrefix;
	}
	
	public void setTokenPrefix(String tokenPrefix) {
		this.tokenPrefix = tokenPrefix;
	}


	public String createToken()
	{
		if (tokenSeq == Long.MAX_VALUE){
			tokenSeq =0;
		}
		return tokenPrefix + "_" + tokenSeq++;
	}
	public String getCommand()
	{
		return command;
	}
}
