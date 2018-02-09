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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.editor.pom.MavenPomEditor;
import org.eclipse.m2e.editor.pom.MavenPomEditorPageFactory;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * Class to create add a page with pom editor.
 * 
 * @author Geetika Batra
 *
 */
public class Fabric8PomEditorFactory extends MavenPomEditorPageFactory {

	@Override
	public void addPages(MavenPomEditor pomEditor) {
		IFormPage page = new Fabric8FormPage(pomEditor);
		try {
			pomEditor.addPage(page);
		} catch (PartInitException e) {
			Fabric8AnalysisLSUIActivator.getDefault().getLog().log(new Status(IStatus.ERROR, Fabric8AnalysisLSUIActivator.getDefault().getBundle().getSymbolicName(), e.getLocalizedMessage(), e));

		}
	}

}