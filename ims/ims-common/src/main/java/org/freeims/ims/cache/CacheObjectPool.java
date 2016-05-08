package org.freeims.ims.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class CacheObjectPool<K,V>
{
	protected ConcurrentHashMap<K,CacheObject<V>> hashMap = new ConcurrentHashMap<K,CacheObject<V>>();
	CacheEventListener listener = null;
	EvictService<K,V> service = null;
	private long maxIdleTime = 10 * 60 * 1000;
	private static Logger logger = Logger.getLogger(CacheObjectPool.class);
	public CacheObjectPool(CacheEventListener listener)
	{
		this.listener = listener;
		 service = new EvictService<K,V>(this);
		
		new Thread(service).start();
	}
	public void put(K key,V v)
	{

		hashMap.put(key, new CacheObject<V>(v,maxIdleTime));
	}
	public void put(K key,V v,long maxIdleTime)
	{

		hashMap.put(key, new CacheObject<V>(v,maxIdleTime));
	}
	public V get(K key)
	{


		if (hashMap.containsKey(key)){
			return hashMap.get(key).getEntry();
		}
		return null;
	}

	public void active(K k)
	{
		hashMap.get(k).access();
	}
	public void remove(K k)
	{
		if (!hashMap.containsKey(k)){
			return;
		}
		if (listener != null)
		{
			CacheEvent event = new CacheEvent(hashMap.get(k).getEntry(),CacheEvent.EventType.REMOVE);
			listener.onEvent(event);
		}

		
			hashMap.remove(k);

	}
	public void remove(Map.Entry<K, CacheObject<V>> entry)
	{
		if (!hashMap.containsKey(entry.getKey())){
			return;
		}
		if (listener != null)
		{
			CacheEvent event = new CacheEvent(entry.getValue().getEntry(),CacheEvent.EventType.REMOVE);
			listener.onEvent(event);
		}
		
			hashMap.remove(entry.getKey());
		
	}
	public void clean()
	{
		for (Map.Entry<K, CacheObject<V>> entry: hashMap.entrySet())
		{
			remove(entry);
		}
		//service.runningFlag=false;
	}
	public static void main(String[] args)
	{
		CacheObjectPool<Integer,String> pool =
				new CacheObjectPool<Integer,String>( new CacheEventListener()
						{
							@Override
							public void onEvent(CacheEvent event) {
								System.out.println(event.getSource());
								
							}
					
						});
		pool.put(1, "asdfas");
	}
	public long getMaxIdleTime() {
		return maxIdleTime;
	}
	public void setMaxIdleTime(long maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}
}

class EvictService<K,V> implements Runnable {

	protected boolean runningFlag = true;
	protected CacheObjectPool<K,V> pool = null;

	public EvictService(CacheObjectPool<K,V> pool) 
	{
		this.pool = pool;

	}

	public void run() 
	{ 
		while (runningFlag)
		{
			//Collection<CacheObject> coll = pool.hashMap.keys();
			for (Map.Entry<K, CacheObject<V>> entry: pool.hashMap.entrySet())
			{
				CacheObject<V> o = entry.getValue();
				if (o.getMaxIdleTime()<0){
					continue;
				}
				if (System.currentTimeMillis() - o.getLastAccess() >o.getMaxIdleTime())
				{
					pool.remove(entry);
				}
			}
			try{
				Thread.sleep(200);
			}catch(Exception e)
			{}
		}
	}
}
