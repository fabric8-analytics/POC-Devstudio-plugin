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
package com.redhat.fabric8analytics.eclipse.core.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.redhat.fabric8analytics.eclipse.core.Fabric8AnalysisCoreActivator;
import com.redhat.fabric8analytics.eclipse.core.RecommenderAPIException;
import com.redhat.fabric8analytics.eclipse.core.RecommenderAPIProvider;

public class RetrieveResultsJob extends Job {

	private String jobID;
	private RecommenderAPIProvider provider;
	private String analysisURL;

	public RetrieveResultsJob(RecommenderAPIProvider provider, String jobID) {
		super("Retrieve results from fabric8-analysis");
		this.jobID = jobID;
		this.provider = provider;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			if (!provider.analysesFinished(jobID)) {
				return new Status(IStatus.INFO, Fabric8AnalysisCoreActivator.PLUGIN_ID, "Analysis is not ready yet");
			}
		} catch (RecommenderAPIException e) {
			return new Status(IStatus.ERROR, Fabric8AnalysisCoreActivator.PLUGIN_ID,
					"Error during communication with analytics server", e);
		}

		analysisURL = provider.getAnalysesURL(jobID);
		return Status.OK_STATUS;
	}

	public String getAnalysisURL() {
		return analysisURL;
	}
}