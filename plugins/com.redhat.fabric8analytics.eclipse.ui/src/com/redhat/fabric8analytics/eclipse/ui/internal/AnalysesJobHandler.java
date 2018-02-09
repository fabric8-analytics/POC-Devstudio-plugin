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

package com.redhat.fabric8analytics.eclipse.ui.internal;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;

import com.redhat.fabric8analytics.eclipse.core.RecommenderAPIProvider;
import com.redhat.fabric8analytics.eclipse.core.job.RetrieveResultsJob;
import com.redhat.fabric8analytics.eclipse.core.job.SendToFabric8AnalysisJob;
import com.redhat.fabric8analytics.eclipse.ui.EditorComposite;
import com.redhat.fabric8analytics.eclipse.ui.ExitHandler;
import com.redhat.fabric8analytics.eclipse.ui.Fabric8AnalysisLSUIActivator;
import com.redhat.fabric8analytics.eclipse.ui.StackAnalysesView;

public class AnalysesJobHandler {

	private static final int TIMER_INTERVAL = 5000;

	private RecommenderAPIProvider provider;

	private Set<IFile> pomFiles;

	private EditorComposite editorComposite;
	
	private IFile license;

	public AnalysesJobHandler(RecommenderAPIProvider provider, Set<IFile> pomFiles, IFile license, EditorComposite editorComposite) {
		this.provider = provider;
		this.pomFiles = pomFiles;
		this.editorComposite = editorComposite;
		this.license = license;
	}

	public void analyze() {
		URL url;
		try {
			url = new URL("platform:/plugin/com.redhat.fabric8analytics.lsp.eclipse.ui/templates/index.html");
			url = FileLocator.toFileURL(url);
		} catch (IOException e ) {
			Fabric8AnalysisLSUIActivator.getDefault().logError("Error while retrieving browser template", e);
			return;
		}
		if (editorComposite == null) {
			IViewPart mainView = ExitHandler.getView();
			((StackAnalysesView) mainView).updatebrowserUrl(url.toString());
		} else {
			editorComposite.updateBrowser(url.toString());
		}
		SendToFabric8AnalysisJob sentToAnalysisJob = new SendToFabric8AnalysisJob(provider, pomFiles, license);
		sentToAnalysisJob.schedule();

		sentToAnalysisJob.addJobChangeListener(new JobChangeAdapter() {

			@Override
			public void done(IJobChangeEvent event) {
				if(event.getResult().isOK()) {
					RetrieveResultsJob retrieveResultsJob = new RetrieveResultsJob(provider, sentToAnalysisJob.getJobID());
					retrieveResultsJob.schedule();
					
					retrieveResultsJob.addJobChangeListener(new JobChangeAdapter() {
						
						private int numOfRequests = 0;
						
						@Override
						public void done(IJobChangeEvent event) {
							if(numOfRequests == 5) {
								disposeBrowsers();
								MessageDialogUtils.displayErrorMessage("Analysis timeout");
								return;
							}
							numOfRequests++;
							if(event.getResult().getSeverity() == IStatus.INFO) {
								//reschedule if analysis is not yet complete
								retrieveResultsJob.schedule(TIMER_INTERVAL);
							} else if(event.getResult().isOK()) {
								//analysis finished, get the result
								syncWithUi(retrieveResultsJob.getAnalysisURL());
							}
						}
					});
				}
			}
		});
	}

	private void syncWithUi(String analysisURL) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (editorComposite == null) {
					((StackAnalysesView) ExitHandler.getView()).updatebrowserUrl(analysisURL);
				} else {
					editorComposite.updateBrowser(analysisURL);
				}
			}
		});

	}

	private void disposeBrowsers() {
		Display.getDefault().syncExec(new Runnable () {
			public void run() {
				if(editorComposite == null) {
					((StackAnalysesView) ExitHandler.getView()).disposebrowserUrl();
				} else {
					editorComposite.disposeBrowser();
				}
			}
		});
	}
}
