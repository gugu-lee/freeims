package org.freeims.common;

import java.text.ParseException;

import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.Address;

import org.apache.log4j.Logger;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class DnsUtil {
	private static Logger logger = Logger.getLogger(DnsUtil.class);
	/**
	 * @param domain which domain for find I-CSCF in
	 * @param transport unused.
	 * */
	public static Address resolveICSCFHostByDNS(String domain, String transport,String dnsServer)
			throws PeerUnavailableException, ParseException, TextParseException {
		if (dnsServer != null && dnsServer.length() >0){
			logger.info("DNS Server:"+dnsServer);
		 System.setProperty("dns.server", dnsServer);
		//ResolverConfig resolverConfig = ResolverConfig.getCurrentConfig();
		}
		// String servers[] = resolverConfig.servers();

		Lookup l = new Lookup(domain, Type.NAPTR, DClass.IN);
		l.run();
		NAPTRRecord record = null;
		if (l.getResult() == Lookup.SUCCESSFUL) {
			Record[] answers = l.getAnswers();
			for (int i = 0; i < answers.length; i++) {
				if (answers[i] instanceof NAPTRRecord) {
					NAPTRRecord r = (NAPTRRecord) answers[i];
					if (r.getService().equals("SIP+D2T")) {
						record = r;
						break;
					}
				}
			}
		}
		if (record == null) {
			logger.info("not find Type.NAPTR domain:"+domain);
			return null;
		}
		l = new Lookup(record.getReplacement(), Type.SRV, DClass.IN);
		l.run();
		SRVRecord srvR=null;
		
		if (l.getResult() == Lookup.SUCCESSFUL) {
			Record[] answers = l.getAnswers();
			for (int i = 0; i < answers.length; i++) {
				if (answers[i] instanceof SRVRecord) {
					srvR = (SRVRecord) answers[i];
	
					break;

				}
			}
		}
		if (srvR == null)
		{
			logger.info("not find Type.SRV server:"+record.getReplacement());
			return null;
		}
		int port =srvR.getPort();
		l = new Lookup(srvR.getAdditionalName(),Type.A,DClass.IN);
		l.run();
		ARecord ar = null;
		if (l.getResult() == Lookup.SUCCESSFUL) {
			Record [] answers = l.getAnswers();
			for (int i = 0; i < answers.length; i++){
				
				if (answers[i] instanceof ARecord){
					ar = (ARecord)answers[i];
					
					Address sipUri = SipFactory.getInstance().createAddressFactory().createAddress("sip:"+
							ar.getAddress().getHostAddress()+":"+port);
					return sipUri;
				}
			}
		}
		logger.info("not find Type.A server:"+srvR.getAdditionalName());
		return null;
	}
	public static void main(String[] args) throws Exception
	{
		Address a = resolveICSCFHostByDNS("saygreet.com","","127.0.0.1");
		System.out.println(a);
	}
}
