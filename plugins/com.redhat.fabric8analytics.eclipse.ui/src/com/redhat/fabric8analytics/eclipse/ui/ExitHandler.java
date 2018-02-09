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

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.redhat.fabric8analytics.eclipse.core.RecommenderAPIProvider;
import com.redhat.fabric8analytics.eclipse.core.data.AnalyticsAuthData;
import com.redhat.fabric8analytics.eclipse.ui.internal.AnalysesJobHandler;
import com.redhat.fabric8analytics.eclipse.ui.internal.GetAnalyticsAuthDataJob;
import com.redhat.fabric8analytics.eclipse.ui.internal.MessageDialogUtils;
import com.redhat.fabric8analytics.eclipse.ui.internal.WorkspaceFilesFinder;

public class ExitHandler extends AbstractHandler {

	static String jobId;
	static IViewPart mainView = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Set<IFile> pomFiles;
		final IFile license;
		try {
			pomFiles = WorkspaceFilesFinder.getInstance().findPOMs();
			license = WorkspaceFilesFinder.getInstance().findLicense();
		} catch (CoreException e1) {
			MessageDialogUtils.displayErrorMessage("Error while searching for Pom or license files", e1);
			return null;
		}
		if (pomFiles.isEmpty()) {
			MessageDialogUtils.displayInfoMessage("No POM files found in the selection");
			return null;
		}
		GetAnalyticsAuthDataJob getAuthDataJob = new GetAnalyticsAuthDataJob();
		getAuthDataJob.addJobChangeListener(new JobChangeAdapter() {

			@Override
			public void done(IJobChangeEvent event) {
				AnalyticsAuthData analyticsAuthData = getAuthDataJob.getAuthData();
				if (analyticsAuthData != null) {
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							try {
								IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
										.showView(StackAnalysesView.NAME);
								setView(view);
							} catch (PartInitException e) {
								MessageDialogUtils.displayErrorMessage("Error while opening stack analysis view", e);
							}

						}
					});

					RecommenderAPIProvider provider = new RecommenderAPIProvider(analyticsAuthData);
					new AnalysesJobHandler(provider, pomFiles, license, null).analyze();
				}

			}
		});
		getAuthDataJob.schedule();
		return null;
	}

	public static void setJobId(String jobId) {
		ExitHandler.jobId = jobId;
	}

	public static String getJobId() {
		return ExitHandler.jobId;
	}

	public static void setView(IViewPart mainView) {
		ExitHandler.mainView = mainView;
	}

	public static IViewPart getView() {
		return mainView;
	}

}
