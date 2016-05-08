package org.freeims.sipproxy.container;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.util.ContextName;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class SipProxyHostConfig extends HostConfig
{

	public static final String APPLICATION_SIP_XML = "WEB-INF/sip.xml";
	private static final String WAR_EXTENSION = ".war";
	private static final String SAR_EXTENSION = ".sar";
	public static final String SIP_CONTEXT_CLASS = "org.freeims.container.SipStandardContext";
	public static final String SIP_CONTEXT_CONFIG_CLASS = "org.freeims.container.SipContextConfig";
	
	private static final Log logger = LogFactory.getLog(SipProxyHostConfig.class);
	/**
	 * 
	 */
	public SipProxyHostConfig() {
		super();				
	}


    
	@Override
	protected void deployApps(String name) {		
		File appBase = host.getAppBaseFile();
        File configBase = host.getConfigBaseFile();

        // String baseName = getBaseName(name);
        
        ContextName cn = new ContextName(name, false);
        String baseName = cn.getBaseName();
        
        //No need for docBase, replace by baseName
        //String docBase = getBaseName(name);
        if (deploymentExists(baseName)) {
            return;
        }
        
        // Deploy XML descriptors from configBase
        File xml = new File(configBase, baseName + ".xml");
        if (xml.exists()) {
            deployDescriptor(cn, xml);
            return;
        }
        // Deploy WARs, and loop if additional descriptors are found
        File war = new File(appBase, baseName + ".war");
        if (war.exists()) {
        	boolean isSipServletApplication = isSipServletArchive(war);
            if(isSipServletApplication) {
                if(logger.isDebugEnabled()) {
                    logger.debug(APPLICATION_SIP_XML + " found in "
                            + baseName + ". Enabling sip servlet archive deployment");
                }
            	deploySAR(cn, war);
            } else {
                if(logger.isDebugEnabled()) {
                    logger.debug(APPLICATION_SIP_XML + " not found in "
                            + baseName + ". Not Enabling sip servlet archive deployment");
                }
            	deployWAR(cn, war);
            }
            return;
        }
        // Deploy expanded folders
        File dir = new File(appBase, baseName);
        if (dir.exists()) {
            deployDirectory(cn, dir);
        }
		// Deploy SARs, and loop if additional descriptors are found
        File sar = new File(appBase, baseName + SAR_EXTENSION);
        if (sar.exists()) {
            deploySAR(cn, sar);
        }
	}
	
	/**
	 *
	 * @param NAME
	 * @param sar
	 * @param string
	 */
	protected void deploySAR(ContextName cn, File sar) {
		if (deploymentExists(cn.getName()))
            return;
		if(logger.isDebugEnabled()) {
    		logger.debug(APPLICATION_SIP_XML + " found in "
    				+ sar + ". Enabling sip servlet archive deployment");
    	}
		String initialConfigClass = host.getConfigClass();
		String initialContextClass = contextClass;
		host.setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
		//host.setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
		setContextClass(SIP_CONTEXT_CLASS);
		deployWAR(cn, sar);
		host.setConfigClass(initialConfigClass);
		//configClass = initialConfigClass;
        contextClass = initialContextClass;
	}
	
	@Override
	protected void deployWAR(ContextName cn, File dir) {
		if(logger.isDebugEnabled()) {
    		logger.debug("Context class used to deploy the WAR : " + contextClass);
    		logger.debug("Context config class used to deploy the WAR : " + host.getConfigClass());
    	}
		super.deployWAR(cn, dir);
	}

	@Override
	protected void deployDirectory(ContextName cn, File dir) {
		if (deploymentExists(cn.getName()))
            return;
		
		boolean isSipServletApplication = isSipServletDirectory(dir);
		if(isSipServletApplication) {
			if(logger.isDebugEnabled()) {
        		logger.debug(APPLICATION_SIP_XML + " found in " 
        				+ dir + ". Enabling sip servlet archive deployment");
        	}
			String initialConfigClass = host.getConfigClass();
			String initialContextClass = contextClass;
			host.setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
			//setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
			setContextClass(SIP_CONTEXT_CLASS);
			super.deployDirectory(cn, dir);
			host.setConfigClass(initialConfigClass);
			//configClass = initialConfigClass;
	        contextClass = initialContextClass;
		} else {
            if(logger.isDebugEnabled()) {
                logger.debug(APPLICATION_SIP_XML + " not found in "
                        + dir + ". Not Enabling sip servlet archive deployment");
            }
            // https://code.google.com/p/sipservlets/issues/detail?id=257 make sure the digester to parse directories
            // containing META-INF/context.xml is the correct one
            digester = createDigester(StandardContext.class.getName());
            if(logger.isDebugEnabled()) {
                logger.debug("Context class used to deploy the directory : " + contextClass);
                logger.debug("Context config class used to deploy the directory : " + host.getConfigClass());
            }
			super.deployDirectory(cn, dir);
		}
	}
	
	@Override
	protected void deployDescriptor(ContextName cn, File contextXml) {
		super.deployDescriptor(cn, contextXml);
	}
	
	@Override
	protected void deployWARs(File appBase, String[] files) {		
		if (files == null)
            return;
        
		ExecutorService es = host.getStartStopExecutor();
        List<Future<?>> results = new ArrayList<>();
		
        for (int i = 0; i < files.length; i++) {
            
            if (files[i].equalsIgnoreCase("META-INF"))
                continue;
            if (files[i].equalsIgnoreCase("WEB-INF"))
                continue;
            File war = new File(appBase, files[i]);
            if (files[i].toLowerCase(Locale.ENGLISH).endsWith(".war") &&
                    war.isFile() && !invalidWars.contains(files[i]) ) {
	            boolean isSipServletApplication = isSipServletArchive(war);
	            if(isSipServletApplication) {
	                if(logger.isDebugEnabled()) {
	                    logger.debug(APPLICATION_SIP_XML + " found in "
	                            + files[i] + ". Enabling sip servlet archive deployment");
	                }
	            	ContextName cn = new ContextName(files[i], true);
	                // Calculate the context path and make sure it is unique
	//                String contextPath = "/" + files[i];
	//                int period = contextPath.lastIndexOf(".");
	//                if (period >= 0)
	//                    contextPath = contextPath.substring(0, period);
	//                if (contextPath.equals("/ROOT"))
	//                    contextPath = "";
	                
	                if (isServiced(cn.getName()))
	                    continue;                               
	                
	                if (deploymentExists(cn.getName())) {
	                    DeployedApplication app = deployed.get(cn.getName());
	                    if (!unpackWARs && app != null) {
	                        // Need to check for a directory that should not be
	                        // there
	                        File dir = new File(appBase, cn.getBaseName());
	                        if (dir.exists()) {
	                            if (!app.loggedDirWarning) {
	                                logger.warn(sm.getString(
	                                        "hostConfig.deployWar.hiddenDir",
	                                        dir.getAbsoluteFile(),
	                                        war.getAbsoluteFile()));
	                                app.loggedDirWarning = true;
	                            }
	                        } else {
	                            app.loggedDirWarning = false;
	                        }
	                    }
	                    continue;
	                }

	                // Check for WARs with /../ /./ or similar sequences in the name
	                if (!validateContextPath(appBase, cn.getBaseName())) {
	                    logger.error(sm.getString(
	                            "hostConfig.illegalWarName", files[i]));
	                    invalidWars.add(files[i]);
	                    continue;
	                }

	                results.add(es.submit(new DeploySar(this, cn, war)));
//	                String initialConfigClass = configClass;
//	        		String initialContextClass = contextClass;
//	        		host.setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
//	        		setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
//	        		setContextClass(SIP_CONTEXT_CLASS);
//	                deploySAR(cn, dir);
//	                host.setConfigClass(initialConfigClass);
//	                configClass = initialConfigClass;
//	                contextClass = initialContextClass;        		
	            } else {
	                if(logger.isDebugEnabled()) {
	                    logger.debug(APPLICATION_SIP_XML + " not found in "
	                            + files[i] + ". Not Enabling sip servlet archive deployment");
	                }
	                setContextClass(StandardContext.class.getName());
	            }
	        }

	        for (Future<?> result : results) {
	            try {
	                result.get();
	            } catch (Exception e) {
	                logger.error(sm.getString(
	                        "hostConfig.deployWar.threaded.error"), e);
	            }
	        }
        }
        super.deployWARs(appBase, files);
	}
	
	private boolean validateContextPath(File appBase, String contextPath) {
        // More complicated than the ideal as the canonical path may or may
        // not end with File.separator for a directory

        StringBuilder docBase;
        String canonicalDocBase = null;

        try {
            String canonicalAppBase = appBase.getCanonicalPath();
            docBase = new StringBuilder(canonicalAppBase);
            if (canonicalAppBase.endsWith(File.separator)) {
                docBase.append(contextPath.substring(1).replace(
                        '/', File.separatorChar));
            } else {
                docBase.append(contextPath.replace('/', File.separatorChar));
            }
            // At this point docBase should be canonical but will not end
            // with File.separator

            canonicalDocBase =
                (new File(docBase.toString())).getCanonicalPath();

            // If the canonicalDocBase ends with File.separator, add one to
            // docBase before they are compared
            if (canonicalDocBase.endsWith(File.separator)) {
                docBase.append(File.separator);
            }
        } catch (IOException ioe) {
            return false;
        }

        // Compare the two. If they are not the same, the contextPath must
        // have /../ like sequences in it
        return canonicalDocBase.equals(docBase.toString());
    }
	
	/**
	 * Check if the file given in parameter match a sip servlet application, i.e.
	 * if it contains a sip.xml in its WEB-INF directory
	 * @param file the file to check (war or sar)
	 * @return true if the file is a sip servlet application, false otherwise
	 */
	private boolean isSipServletArchive(File file) {
		if (file.getName().toLowerCase().endsWith(SAR_EXTENSION)) {
			return true;
		} else if (file.getName().toLowerCase().endsWith(WAR_EXTENSION)) {
			try{
                JarFile jar = new JarFile(file);                                  
                JarEntry entry = jar.getJarEntry(APPLICATION_SIP_XML);
                if(entry != null) {
                        return true;
                }                
	        } catch (IOException e) {
	        	if(logger.isInfoEnabled()) {
	        		logger.info("couldn't find WEB-INF/sip.xml in " + file + " checking for package-info.class");
	        	}
	        }
			return SipApplicationAnnotationUtils.findPackageInfoInArchive(file);
		} 		
		return false;
	}

	/**
	 * Check if the file given in parameter match a sip servlet application, i.e.
	 * if it contains a sip.xml in its WEB-INF directory
	 * @param file the file to check (war or sar)
	 * @return true if the file is a sip servlet application, false otherwise
	 */
	private boolean isSipServletDirectory(File dir) {
		 if(dir.isDirectory()) {
			 //Fix provided by Thomas Leseney for exploded directory deployments
			File sipXmlFile = new File(dir.getAbsoluteFile(), APPLICATION_SIP_XML);
			if(sipXmlFile.exists()) {
				return true;
			}
			if(SipApplicationAnnotationUtils.findPackageInfoinDirectory(dir)) return true;
		}		
		return false;
	}

	

	
	private static class DeploySar implements Runnable {

        private SipProxyHostConfig config;
        private ContextName cn;
        private File war;

        public DeploySar(SipProxyHostConfig config, ContextName cn, File war) {
            this.config = config;
            this.cn = cn;
            this.war = war;
        }

        @Override
        public void run() {
            config.deploySAR(cn, war);
        }
    }

}
