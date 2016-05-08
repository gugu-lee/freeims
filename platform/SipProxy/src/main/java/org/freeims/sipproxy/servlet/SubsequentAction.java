package org.freeims.sipproxy.servlet;


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
		return responseStatus;
	}
	public void setResponseStatus(int responseStatus) {
		this.responseStatus = responseStatus;
	}
	public String getResponseReason() {
		return responseReason;
	}
	public void setResponseReason(String responseReason) {
		this.responseReason = responseReason;
	}
	
	
}
