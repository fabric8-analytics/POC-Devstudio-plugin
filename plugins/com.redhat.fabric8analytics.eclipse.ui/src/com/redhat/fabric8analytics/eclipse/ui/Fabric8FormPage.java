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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.editor.pom.MavenPomEditor;
import org.eclipse.m2e.editor.pom.MavenPomEditorPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm; 


/**
 * Class to create contents of page Fabric8Analyses.
 * 
 * @author Geetika Batra
 *
 */
class Fabric8FormPage extends MavenPomEditorPage implements IMavenProjectChangedListener  {

	private EditorComposite dependenciesComposite;
	public Fabric8FormPage(MavenPomEditor pomEditor) {
		super(pomEditor, "fabric8.analysis ", "Fabric8 Analysis");

	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();

		ScrolledForm form = managedForm.getForm();
		form.setText("This plugin analyzes the stack of your current manifest file");

		form.getBody().setLayout(new GridLayout(1, true));

		EditorComposite dependenciesComposite = new EditorComposite(form.getBody(), this, SWT.NONE, pomEditor);
		dependenciesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		toolkit.adapt(dependenciesComposite);


		super.createFormContent(managedForm);

	}

	@Override
	public void mavenProjectChanged(MavenProjectChangedEvent[] arg0, IProgressMonitor arg1) {

	}

	@Override
	public void loadData() {

	}
}
