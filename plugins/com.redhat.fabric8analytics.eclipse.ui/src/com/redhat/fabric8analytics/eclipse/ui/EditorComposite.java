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

import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.m2e.editor.pom.MavenPomEditor;
import org.eclipse.m2e.editor.pom.MavenPomEditorPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.redhat.fabric8analytics.eclipse.core.RecommenderAPIProvider;
import com.redhat.fabric8analytics.eclipse.core.data.AnalyticsAuthData;
import com.redhat.fabric8analytics.eclipse.ui.internal.AnalysesJobHandler;
import com.redhat.fabric8analytics.eclipse.ui.internal.GetAnalyticsAuthDataJob;
import com.redhat.fabric8analytics.eclipse.ui.internal.MessageDialogUtils;

/**
 * Class to create composite for page Fabric8Analyses.
 * 
 * @author Geetika Batra
 *
 */
public class EditorComposite extends Composite {

	protected MavenPomEditorPage editorPage;

	public Browser editorBrowser;

	public String jobID;

	private MavenPomEditor pomEditor;
	private FormToolkit toolkit = new FormToolkit(Display.getCurrent());

	public EditorComposite(Composite composite, MavenPomEditorPage editorPage, int flags, MavenPomEditor pomEditor) {
		super(composite, flags);
		this.editorPage = editorPage;
		this.pomEditor = pomEditor;
		createComposite();
	}

	private void createComposite() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		Button buttonGo = new Button(this, SWT.NONE);
		buttonGo.setLayoutData(new GridData(GridData.CENTER));
		buttonGo.setText("Generate Stack Report");
		editorBrowser = new Browser(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(editorBrowser);
		buttonGo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent s) {
				IFile currentPomFile = pomEditor.getPomFile();
				if (currentPomFile == null) {
					MessageDialogUtils.displayErrorMessage("Cannot find POM file");
					return;
				}
				GetAnalyticsAuthDataJob getAuthDataJob = new GetAnalyticsAuthDataJob();
				getAuthDataJob.addJobChangeListener(new JobChangeAdapter() {
						
					@Override
					public void done(IJobChangeEvent event) {
						AnalyticsAuthData analyticsAuthData = getAuthDataJob.getAuthData();
						if(analyticsAuthData != null) {
							RecommenderAPIProvider provider = new RecommenderAPIProvider(analyticsAuthData);
							new AnalysesJobHandler(provider, Collections.singleton(currentPomFile), null , EditorComposite.this).analyze();
						}
					}
				});
				getAuthDataJob.schedule();
			}
		});
	}

	public void updateBrowser(String url) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!editorBrowser.isDisposed()) {
					editorBrowser.setUrl(url);
				}
			}
		});

	}

	public void disposeBrowser() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!editorBrowser.isDisposed()) {
					editorBrowser.dispose();
				}
			}
		});

	}
}
