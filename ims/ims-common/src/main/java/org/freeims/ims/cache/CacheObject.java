package org.freeims.ims.cache;

public class CacheObject<K>
{
	private long lastAccess = 0;
	private long maxIdleTime = 10 * 60 * 1000;
	private K entry;
	
	public CacheObject ()
	{
		lastAccess = System.currentTimeMillis();
	}
	public CacheObject(K k)
	{
		entry = k;
		lastAccess = System.currentTimeMillis();
	}
	public CacheObject(K k,long maxIdleTime)
	{
		this.maxIdleTime = maxIdleTime;
		entry = k;
		lastAccess = System.currentTimeMillis();
	}
	public void access()
	{
		lastAccess = System.currentTimeMillis();
	}
	public long getMaxIdleTime()
	{
		return maxIdleTime;
	}
	public void setMaxIdleTime(long millis)
	{
		maxIdleTime = millis;
	}
	public long getLastAccess()
	{
		return lastAccess;
	}
	public K getEntry() {
		return entry;
	}
	public void setEntry(K entry) {
		this.entry = entry;
	}
	
}
