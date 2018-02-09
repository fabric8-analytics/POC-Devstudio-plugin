/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package com.redhat.fabric8analytics.eclipse.core;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

import com.redhat.fabric8analytics.eclipse.core.data.ThreeScaleData;

public class Fabric8AnalysisPreferences {
	
	public static final String LSP_SERVER_ENABLED = "fabric8AnalysisPreferences.LSP_ENABLED";
	public static final String PROD_URL = "fabric8AnalysisPreferences.PROD_URL";
	public static final String STAGE_URL = "fabric8AnalysisPreferences.STAGE_URL";
	public static final String USER_KEY = "fabric8AnalysisPreferences.USER_KEY";
	
	private ISecurePreferences secureNode;
	private static Fabric8AnalysisPreferences instance;
	
	private Fabric8AnalysisPreferences() {
		ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault();
		secureNode = securePreferences.node("secured routes and keys");
	}
	
	public static Fabric8AnalysisPreferences getInstance() {
		if(instance == null) {
			instance = new Fabric8AnalysisPreferences();
		}
		return instance;
	}
	
	public boolean isLSPServerEnabled() {
		return InstanceScope.INSTANCE.getNode(Fabric8AnalysisCoreActivator.PLUGIN_ID).getBoolean(LSP_SERVER_ENABLED, false);
	}
	
	public void setLSPServerEnabled(boolean enabled) {
		InstanceScope.INSTANCE.getNode(Fabric8AnalysisCoreActivator.PLUGIN_ID).putBoolean(LSP_SERVER_ENABLED, enabled);
	}
	
	public String getProdURL() {
		return InstanceScope.INSTANCE.getNode(Fabric8AnalysisCoreActivator.PLUGIN_ID).get(PROD_URL, null);
	}
	
	public void setProdURL(String prodURL) {
		InstanceScope.INSTANCE.getNode(Fabric8AnalysisCoreActivator.PLUGIN_ID).put(PROD_URL, prodURL);
	}
	
	public String getStageURL() {
		return InstanceScope.INSTANCE.getNode(Fabric8AnalysisCoreActivator.PLUGIN_ID).get(STAGE_URL, null);
	}
	
	public void setStageURL(String stageURL) {
		InstanceScope.INSTANCE.getNode(Fabric8AnalysisCoreActivator.PLUGIN_ID).put(STAGE_URL, stageURL);
	}
	
	public String getUserKey() throws StorageException {
		return secureNode.get(USER_KEY, null);
	}
	
	public void setUserKey(String userKey) throws StorageException {
		secureNode.put(USER_KEY, userKey, true);
	}
	
	public ThreeScaleData getThreeScaleData() throws StorageException {
		if(getProdURL() == null || getStageURL() == null || getUserKey() == null) {
			return null;
		}
		return new ThreeScaleData(getProdURL(), getStageURL(), getUserKey());
	}

}