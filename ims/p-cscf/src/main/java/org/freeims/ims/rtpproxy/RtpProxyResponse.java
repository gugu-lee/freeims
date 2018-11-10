package org.freeims.ims.rtpproxy;

public class RtpProxyResponse 
{
	
	private String responseText;
	private String requestCommand;
	
	public String getRequestCommand() {
		return requestCommand;
	}

	public void setRequestCommand(String requestCommand) {
		this.requestCommand = requestCommand;
	}

	public String getResponseText() {
		return responseText;
	}

	public void setResponseText(String responseText) {
		this.responseText = responseText;
	}

	public String getValue()
	{
		int indexBlankChar = responseText.indexOf(' ');
		responseText= responseText.substring(indexBlankChar).trim();
		indexBlankChar = responseText.indexOf(' ');
		if (indexBlankChar>=0){
			return responseText.substring(0,indexBlankChar);
		}
		return responseText;
	}
	public RtpProxyResponse(String command,String response)
	{
		setResponseText(response);
	}
	
}
