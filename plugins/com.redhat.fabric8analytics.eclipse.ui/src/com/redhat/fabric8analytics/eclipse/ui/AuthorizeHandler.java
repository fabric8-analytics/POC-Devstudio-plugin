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

package com.redhat.fabric8analytics.eclipse.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.redhat.fabric8analytics.eclipse.core.data.AnalyticsAuthData;
import com.redhat.fabric8analytics.eclipse.core.internal.AnalyticsAuthService;
import com.redhat.fabric8analytics.eclipse.ui.internal.MessageDialogUtils;
import com.redhat.fabric8analytics.eclipse.core.Fabric8AnalysisPreferences;

/**
 * Handler for menu item "Enable Fabric8 Analyses".
 *
 * @author Geetika Batra
 *
 */
public class AuthorizeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		LoginToOSIOJob authJob = new LoginToOSIOJob();

		authJob.addJobChangeListener(new JobChangeAdapter() {

			@Override
			public void done(IJobChangeEvent event) {
				AnalyticsAuthData analyticsAuthData = authJob.getAnalyticsAuthData();
				if (analyticsAuthData == null) {
					Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(false);
					MessageDialogUtils.displayInfoMessage("Authorization to Openshift.io was not successful");
					return;
				}
				Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(true);
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						PreferencesUtil
								.createPreferenceDialogOn(shell, "com.redhat.fabric8analytics.lsp.eclipse.preferences",
										new String[] { "com.redhat.fabric8analytics.lsp.eclipse.preferences" }, null)
								.open();

					}
				});
			}
		});

		authJob.schedule();
		return null;
	}

	class LoginToOSIOJob extends Job {

		private AnalyticsAuthData analyticsAuthData;

		public LoginToOSIOJob() {
			super("Login to Openshift.io");
		}

		@Override
		protected IStatus run(IProgressMonitor progressMonitor) {
			try {
				analyticsAuthData = AnalyticsAuthService.getInstance().login(progressMonitor);
			} catch (StorageException e) {
				return new Status(IStatus.ERROR, Fabric8AnalysisLSUIActivator.PLUGIN_ID, "Error occured while storing Fabric8Analytics data", e);
			}
			return Status.OK_STATUS;
		}

		public AnalyticsAuthData getAnalyticsAuthData() {
			return analyticsAuthData;
		}

	}
}
