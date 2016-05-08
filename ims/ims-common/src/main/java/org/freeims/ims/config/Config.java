package org.freeims.ims.config;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.log4j.helpers.Loader;
import org.apache.tomcat.util.digester.Digester;

public class Config {
	private static Log logger = LogFactory.getLog(Config.class);
	private PCscfConfig pConfig;
	private ICscfConfig iConfig;
	private SCscfConfig sConfig;
	private RtpProxyConfig rtpproxyConfig;
	private static Config config = null;
	private ArrayList<RealmConfig> realms = new ArrayList<RealmConfig>();

	public void addRealmConfig(RealmConfig d) {
		logger.info("addRealmConfig:"+realms.toString());
		realms.add(d);
	}
	//
	public static Config  load(String configXml) throws Exception {
		if (config != null){
			return config;
		}
		Digester digester = new Digester();
		URLClassLoader cl = (URLClassLoader)Config.class.getClassLoader();

		digester.setClassLoader(cl);
		digester.setValidating(false);
		String prefix="";
		digester.addObjectCreate("config", "org.freeims.ims.config.Config");
		digester.addSetProperties("config");
		
		prefix="config/";
		digester.addObjectCreate(prefix+"pcscf", "org.freeims.ims.config.PCscfConfig");
		digester.addSetProperties(prefix+"pcscf");
		digester.addSetNext(prefix+"pcscf", "setPConfig", "org.freeims.ims.config.PCscfConfig");

		prefix="config/pcscf/";
		digester.addObjectCreate(prefix+"rtpproxy", "org.freeims.ims.config.RtpProxyConfig");
		digester.addSetProperties(prefix+"rtpproxy");
		digester.addSetNext(prefix+"rtpproxy", "setRtpProxy", "org.freeims.ims.config.RtpProxyConfig");
		
		prefix="config/pcscf/";
		digester.addObjectCreate(prefix+"realm", "org.freeims.ims.config.RealmConfig");
		digester.addSetProperties(prefix+"realm");
		digester.addSetNext(prefix+"realm", "addRealmConfig", "org.freeims.ims.config.RealmConfig");
		
		prefix="config/";
		digester.addObjectCreate(prefix+"icscf", "org.freeims.ims.config.ICscfConfig");
		digester.addSetProperties(prefix+"icscf");
		digester.addSetNext(prefix+"icscf", "setIConfig", "org.freeims.ims.config.ICscfConfig");
		prefix="config/icscf/";
		digester.addObjectCreate(prefix+"realm", "org.freeims.ims.config.RealmConfig");
		digester.addSetProperties(prefix+"realm");
		digester.addSetNext(prefix+"realm", "addRealmConfig", "org.freeims.ims.config.RealmConfig");
		
		prefix="config/";
		digester.addObjectCreate(prefix+"scscf", "org.freeims.ims.config.SCscfConfig");
		digester.addSetProperties(prefix+"scscf");
		digester.addSetNext(prefix+"scscf", "setSConfig", "org.freeims.ims.config.SCscfConfig");
		prefix="config/scscf/";
		digester.addObjectCreate(prefix+"realm", "org.freeims.ims.config.RealmConfig");
		digester.addSetProperties(prefix+"realm");
		digester.addSetNext(prefix+"realm", "addRealmConfig", "org.freeims.ims.config.RealmConfig");
		
		prefix="config/realms/";
		digester.addObjectCreate(prefix+"realm", "org.freeims.ims.config.RealmConfig");
		digester.addSetProperties(prefix+"realm");
		digester.addSetNext(prefix+"realm", "addRealmConfig", "org.freeims.ims.config.RealmConfig");
		
		
		URL url = Loader.getResource(configXml);
		if (url == null)
		{
			System.out.println("url is null");
		}
		config = (Config)digester.parse(url.openStream());
		return config;
	}


	public PCscfConfig getPConfig() {
		return pConfig;
	}

	public void setPConfig(PCscfConfig pConfig) {
		logger.info("setPcscfConfig");
		this.pConfig = pConfig;
		this.pConfig.getRealmConfigs().addAll(this.realms);
	}

	public ICscfConfig getIConfig() {
		return iConfig;
	}

	public void setIConfig(ICscfConfig iConfig) {
		this.iConfig = iConfig;
		this.iConfig.getRealmConfigs().addAll(this.realms);
	}

	public SCscfConfig getSConfig() {
		return sConfig;
	}

	public void setSConfig(SCscfConfig sConfig) {
		this.sConfig = sConfig;
		this.sConfig.getRealmConfigs().addAll(this.realms);
	}

	public RtpProxyConfig getRtpproxyConfig() {
		return rtpproxyConfig;
	}

	public void setRtpproxyConfig(RtpProxyConfig rtpproxyConfig) {
		this.rtpproxyConfig = rtpproxyConfig;
	}
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("ims-config:\r\n");
		sb.append("P-CSCF:\r\n");
		sb.append(this.getPConfig().toString());
		sb.append("I-CSCF:\r\n");
		sb.append(this.getIConfig().toString());
		sb.append("S-CSCF:\r\n");
		sb.append(this.getSConfig().toString());
		return sb.toString();
	}
	public static void main(String[] args) throws Exception
	{
		Config c = Config.load("ims-config.xml");
		System.out.println(c.toString());
	}
}
