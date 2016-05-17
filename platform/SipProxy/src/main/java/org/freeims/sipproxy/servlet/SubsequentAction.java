package org.freeims.sipproxy.servlet;

import java.text.ParseException;

import javax.sip.message.Response;

public class SubsequentAction {
	public enum ActionType{NONE, //do nothing
		RESPONSE, // only send back response 
		FORWARD, // only forward the request 
		BOTH }; //send back response and forward the request
	
		
	private int responseStatus;
	private String responseReason;
	private ActionType type;
	private Object targetData;
	private String appId = null;
	
	private SipProxyResponse response=null;
	
	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public SubsequentAction()
	{
		
	}
	
	public static SubsequentAction createNoneAction()
	{
		SubsequentAction action = new SubsequentAction();
		action.setType(ActionType.NONE);
		return action;
	}
	
	public static SubsequentAction createResponseAction(int status)
	{
		return createResponseAction(status,null);
	}
	public static SubsequentAction createResponseAction(int status,String reason)
	{
		SubsequentAction action = new SubsequentAction();
		action.setType(ActionType.RESPONSE);
		action.setResponseStatus(status);
		if (reason != null)
		{
			action.setResponseReason(reason);
		}
		return action;
	}
	public static SubsequentAction createBothAction(int status)
	{
		return createBothAction(status,null);
	}
	public static SubsequentAction createBothAction(int status,String reason)
	{
		SubsequentAction action = new SubsequentAction();
		action.setType(ActionType.BOTH);
		action.setResponseStatus(status);
		if (reason != null)
		{
			action.setResponseReason(reason);
		}
		return action;
	}
	
	public static SubsequentAction createForwardAction()
	{
//		if (forwardRequest==null)
//		{
		SubsequentAction forwardAction = new SubsequentAction();
		forwardAction.setType(ActionType.FORWARD);
		//}
		return forwardAction;
	}
			
	public Object getTargetData() {
		return targetData;
	}
	public void setTargetData(Object targetData) {
		this.targetData = targetData;
	}
	public ActionType getType() {
		return type;
	}
	public void setType(ActionType type) {
		this.type = type;
	}
	
	public int getResponseStatus() {
		if (response!=null){
			return response.getStatus();
		}
		return responseStatus;
	}
	
	public void setResponseStatus(int responseStatus) {
		if (response!=null){
			try{
			((Response)response.getMessage()).setStatusCode(responseStatus);
			}catch(ParseException e)
			{}
		}
		this.responseStatus = responseStatus;
	}
	
	public String getResponseReason() {
		if (response!=null){
			return ((Response)response.getMessage()).getReasonPhrase();
		}
		return responseReason;
	}
	
	public void setResponseReason(String responseReason) {
		if (response!=null){
			try{
			((Response)response.getMessage()).setReasonPhrase(responseReason);
			}catch(ParseException e)
			{}
		}
		this.responseReason = responseReason;
	}

	public SipProxyResponse getResponse() {
		return response;
	}

	public void setResponse(SipProxyResponse response) {
		this.response = response;
	}
	
	
}
