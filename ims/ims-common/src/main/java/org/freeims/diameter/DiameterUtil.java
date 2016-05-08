package org.freeims.diameter;

import java.util.concurrent.atomic.AtomicLong;

public class DiameterUtil 
{
	 private static AtomicLong HopByHop_id = new AtomicLong(System.currentTimeMillis()); 
	 private static AtomicLong EndToEnd_id= new AtomicLong(System.currentTimeMillis()-1);
	 
	 public static long nextHopByHopId()
	 {
		 return HopByHop_id.getAndIncrement();
	 }
	 public static long nextEndToEndId()
	 {
		 return EndToEnd_id.getAndDecrement();
	 }
}
