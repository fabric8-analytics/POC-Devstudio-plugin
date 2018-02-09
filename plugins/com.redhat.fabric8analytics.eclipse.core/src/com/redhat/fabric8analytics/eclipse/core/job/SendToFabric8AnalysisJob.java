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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;

import com.redhat.fabric8analytics.eclipse.core.Fabric8AnalysisCoreActivator;
import com.redhat.fabric8analytics.eclipse.core.RecommenderAPIProvider;

public class SendToFabric8AnalysisJob extends Job {
		
		private String jobID;
		private Set<IFile> pomFiles;
		private RecommenderAPIProvider provider;
		private IFile license;

		public SendToFabric8AnalysisJob(RecommenderAPIProvider provider, Set<IFile> pomFiles, IFile license) {
			super("Send poms to fabric8-analysis");
			this.pomFiles = pomFiles;
			this.provider = provider;
			this.license = license;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				Map<String, String> effectivePomFiles = new HashMap<String, String>();

				for (IFile pomFile : pomFiles) {
					IMavenProjectRegistry registry = MavenPlugin.getMavenProjectRegistry();
					IMavenProjectFacade facade = registry.create(pomFile, true, monitor);
					MavenProject mavenProject = facade.getMavenProject(monitor);
					StringWriter sw = new StringWriter();
					new MavenXpp3Writer().write(sw, mavenProject.getModel());

					String effectivePom = sw.toString();
					effectivePomFiles.put(pomFile.getFullPath().toString(), effectivePom);
				}
				
				jobID = provider.requestAnalyses(effectivePomFiles, license);
			} catch (Exception e) {
				return new Status(Status.ERROR, Fabric8AnalysisCoreActivator.PLUGIN_ID, "Error during communication with server", e);
			}
			return Status.OK_STATUS;
		}
		
		public String getJobID() {
			return jobID;
		}

	}