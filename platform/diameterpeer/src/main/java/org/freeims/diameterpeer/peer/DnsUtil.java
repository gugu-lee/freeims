package org.freeims.diameterpeer.peer;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class DnsUtil {
	public static String lookupIpAddressByDns(String domain,String dnsServer) throws TextParseException
	{
		if (dnsServer != null && dnsServer.length() >0){
			System.setProperty("dns.server", dnsServer);
		}
		Lookup l = new Lookup(domain,Type.A,DClass.IN);
		l.run();
		ARecord ar = null;
		if (l.getResult() == Lookup.SUCCESSFUL) {
			Record [] answers = l.getAnswers();
			for (int i = 0; i < answers.length; i++){
				if (answers[i] instanceof ARecord){
					ar = (ARecord)answers[i];
					return ar.getAddress().getHostAddress();
				}
			}
		}
		return null;
	}
}
