package org.freeims.ims.config;

import org.freeims.diameter.cx.CxConstants;

public class SRealmConfig extends RealmConfig
{
	private String defaultAuthSchemeName = CxConstants.Auth_Scheme_AKAv1_Name;

	public String getDefaultAuthScheme() {
		return defaultAuthSchemeName;
	}

	public void setDefaultAuthScheme(String defaultAuthSchemeName) {
		this.defaultAuthSchemeName = defaultAuthSchemeName;
	}
}
