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

package com.redhat.fabric8analytics.eclipse.lsp.ui;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.redhat.fabric8analytics.eclipse.core.Fabric8AnalysisPreferences;
import com.redhat.fabric8analytics.eclipse.core.internal.AnalyticsAuthService;
import com.redhat.fabric8analytics.eclipse.lsp.ui.Fabric8AnalysisLSUIActivator;// Should this be imported?

public class Fabric8AnalysisPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	static final String PREFERENCE_PAGE_ID = Fabric8AnalysisLSUIActivator.getDefault().getBundle().getSymbolicName()
			+ ".preferences"; //$NON-NLS-1$

	private BooleanFieldEditor enableLSPField;

	public Fabric8AnalysisPreferencePage() {

	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Fabric8AnalysisLSUIActivator.PLUGIN_ID));
		setTitle("Fabric8");
		setDescription("Following services will be enabled");
	}

	@Override
	protected void createFieldEditors() {
		enableLSPField = new BooleanFieldEditor(Fabric8AnalysisPreferences.LSP_SERVER_ENABLED,
				"&Fabric8 Analytics LSP Server", getFieldEditorParent());
		addField(enableLSPField);
	}

	@Override
	protected void performApply() {
		super.performApply();
		if (enableLSPField != null) {
			enableLSPField.load();
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if(enableLSPField.equals(event.getSource()) && (Boolean)event.getNewValue()) {
			// login
			try {
				AnalyticsAuthService.getInstance().login(new NullProgressMonitor());
			} catch (StorageException e) {
				Fabric8AnalysisLSUIActivator.getDefault().logError("Error while logging to openshift.io", e);
			}
		}
		super.propertyChange(event);
	}
}
