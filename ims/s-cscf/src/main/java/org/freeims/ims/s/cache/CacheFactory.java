package org.freeims.ims.s.cache;

import javax.sip.address.Address;
import javax.sip.address.URI;

import org.freeims.ims.s.handler.UEStoreInfo;

public class CacheFactory 
{

	private static SCscfHostCache scscfHostCache = new SCscfHostCache();
	
//	public static OnlineUECache getOnlineUECache()
//	{
//		return onlineUECache;
//	}
//	public static void putOnlineUE(URI key,UEStoreInfo info)
//	{
//		onlineUECache.put(key, info);
//	}
//	public static UEStoreInfo getOnlineUE(URI key)
//	{
//		return onlineUECache.get(key);
//	}
//	
	public static SCscfHostCache getTargetSCscfHostCache()
	{
		return scscfHostCache;
	}
	public static void putTargetSCschHost(String key,Address addr)
	{
		scscfHostCache.put(key, addr);
	}
	public static Address getTargetSCschHost(String key)
	{
		return scscfHostCache.get(key);
	}
	
}
