/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package com.redhat.fabric8analytics.eclipse.ui.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.swt.widgets.Display;

import com.redhat.fabric8analytics.eclipse.core.data.AnalyticsAuthData;
import com.redhat.fabric8analytics.eclipse.core.internal.AnalyticsAuthService;
import com.redhat.fabric8analytics.eclipse.ui.Fabric8AnalysisLSUIActivator;

public class GetAnalyticsAuthDataJob extends Job {

	private AnalyticsAuthData authData;

	public GetAnalyticsAuthDataJob() {
		super("Get Fabric8Analytics auth data");
	}

	@Override
	protected IStatus run(IProgressMonitor progressMonitor) {
		SubMonitor monitor = SubMonitor.convert(progressMonitor);
		try {
			authData = AnalyticsAuthService.getInstance().getAnalyticsAuthData(progressMonitor);
			if (authData == null) {
				Display.getDefault().syncExec(new LoginRunnable(monitor));
			}
			return Status.OK_STATUS;
		} catch (StorageException e) {
			Fabric8AnalysisLSUIActivator.getDefault().logError("Error while retrieving stored data", e);
			return new Status(IStatus.ERROR, Fabric8AnalysisLSUIActivator.PLUGIN_ID,
					"Error while retrieving Fabric8Analytics auth data", e);
		}
	}
	
	public AnalyticsAuthData getAuthData() {
		return authData;
	}
	
	class LoginRunnable implements Runnable {
		
		private IProgressMonitor monitor;
		
		public LoginRunnable(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		@Override
		public void run() {
			authData = MessageDialogUtils.proptForLogin(monitor);
		}
		
	}
}
