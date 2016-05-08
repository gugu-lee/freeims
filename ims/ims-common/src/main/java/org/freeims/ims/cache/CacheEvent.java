package org.freeims.ims.cache;



public class CacheEvent extends java.util.EventObject 
{

	public enum EventType{
		ADD,
		ACCESS,
		
		REMOVE};
	private static final long serialVersionUID = -8739947127742012509L;
	private EventType eventType;
	public CacheEvent(Object source) {
		super(source);
	}
	public CacheEvent(Object source,EventType t) {
		super(source);
		setEventType(t);
	}
	public EventType getEventType() {
		return eventType;
	}
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
	

}
