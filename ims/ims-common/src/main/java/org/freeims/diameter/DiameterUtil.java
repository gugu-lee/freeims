package org.freeims.diameter;

import java.util.concurrent.atomic.AtomicInteger;

public class DiameterUtil 
{
	//System.currentTimeMillis()
	 private static AtomicInteger HopByHop_id = new AtomicInteger(1); 
	 private static AtomicInteger EndToEnd_id= new AtomicInteger(Integer.MAX_VALUE);
	 
	 public static int nextHopByHopId()
	 {
		 return HopByHop_id.getAndIncrement();
	 }
	 public static int nextEndToEndId()
	 {
		 return EndToEnd_id.getAndDecrement();
	 }
}
