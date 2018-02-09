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

package com.redhat.fabric8analytics.eclipse.core.internal;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.security.storage.StorageException;
import org.jboss.tools.openshift.io.core.AccountService;
import org.jboss.tools.openshift.io.core.AccountStatus;
import org.jboss.tools.openshift.io.core.model.IAccount;

import com.redhat.fabric8analytics.eclipse.core.Fabric8AnalysisCoreActivator;
import com.redhat.fabric8analytics.eclipse.core.Fabric8AnalysisPreferences;
import com.redhat.fabric8analytics.eclipse.core.ThreeScaleAPIException;
import com.redhat.fabric8analytics.eclipse.core.ThreeScaleAPIProvider;
import com.redhat.fabric8analytics.eclipse.core.data.AnalyticsAuthData;
import com.redhat.fabric8analytics.eclipse.core.data.ThreeScaleData;

public class AnalyticsAuthService {

	private static final AnalyticsAuthService INSTANCE = new AnalyticsAuthService();

	public static AnalyticsAuthService getInstance() {
		return INSTANCE;
	}

	private AnalyticsAuthService() {
	}
	
	/**
	 * Returns osio account
	 * @return osio account or null if there is none
	 */
	public IAccount getAccount() {
		AccountService service = AccountService.getDefault();
		List<IAccount> accounts = service.getModel().getClusters().get(0).getAccounts();
		if(accounts == null || accounts.isEmpty()) {
			return null;
		}
		return accounts.get(0);
	}

	/**
	 * Returns auth data - token and 3scale data. 
	 * Auth token is refreshed if needed.
	 * If user was not logged before, null is returned.
	 * 
	 * @return OSIO data (token and 3scale data)
	 * @throws ThreeScaleAPIException 
	 */
	public AnalyticsAuthData getAnalyticsAuthData(IProgressMonitor progressMonitor) throws StorageException{
		SubMonitor monitor = getSubMonitor(progressMonitor);
		monitor.setWorkRemaining(2);
		monitor.setTaskName("Check Openshift.io accout status");
		IAccount account = getAccount();
		if(account == null) {
			return null;
		}
		AccountStatus accountStatus = AccountService.getDefault().getStatus(account);
		if (accountStatus == AccountStatus.NEEDS_REFRESH) {
			return login(progressMonitor);
		}
		String token = account.getAccessToken();
		ThreeScaleData threeScaleData = Fabric8AnalysisPreferences.getInstance().getThreeScaleData();
		if(threeScaleData == null) {
			try {
				threeScaleData = registerThreeScale(monitor, token);
			} catch (ThreeScaleAPIException e) {
				Fabric8AnalysisCoreActivator.getDefault().logError("Unable to get data from 3scale", e);
				return null;
			}
		}
		return new AnalyticsAuthData(threeScaleData, token);
		
	}

	/**
	 * Login to OSIO. If anything goes wrong during login process, null is returned
	 * 
	 * @param progressMonitor progressMonitor to report login progress.
	 * @return OSIO data (token and 3scale data) or null if login process fails
	 */
	public AnalyticsAuthData login(IProgressMonitor progressMonitor) throws StorageException{
		SubMonitor monitor = getSubMonitor(progressMonitor);
		monitor.setWorkRemaining(2);
		String token = AccountService.getDefault().getToken(null);
		ThreeScaleData threeScaleData = null;
		if (token != null) {
			try {
				threeScaleData = registerThreeScale(monitor, token);
			} catch (ThreeScaleAPIException e) {
				Fabric8AnalysisCoreActivator.getDefault().logError("Unable to get data from 3scale", e);
				return null;
			}
		}
		AnalyticsAuthData analyticsAuthData = new AnalyticsAuthData(threeScaleData, token);
		return analyticsAuthData;
	}
	
	private ThreeScaleData registerThreeScale(SubMonitor monitor, String token) throws ThreeScaleAPIException, StorageException {
		monitor.setWorkRemaining(1);
		monitor.setTaskName("Get ThreeScale data");
		Fabric8AnalysisPreferences preferences = Fabric8AnalysisPreferences.getInstance();
		ThreeScaleData threeScaleData = new ThreeScaleAPIProvider(token).register3Scale();
		preferences.setProdURL(threeScaleData.getProd());
		preferences.setStageURL(threeScaleData.getStage());
		preferences.setUserKey(threeScaleData.getUserKey());
		return threeScaleData;
	}
	
	private SubMonitor getSubMonitor(IProgressMonitor progressMonitor) {
		if(progressMonitor == null) {
			progressMonitor = new NullProgressMonitor();
		}
		return SubMonitor.convert(progressMonitor);
	}
}
