package org.freeims.ims.config;

import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RulesBase;


public class RealmRuleSet extends RulesBase {
	protected final String prefix;
	public RealmRuleSet()
	{
		this("");
	}
    public RealmRuleSet(String prefix) {
        this.namespaceURI = null;
        this.prefix = prefix;
    }

    public void addRuleInstances(Digester digester) {
    	
    }
}
