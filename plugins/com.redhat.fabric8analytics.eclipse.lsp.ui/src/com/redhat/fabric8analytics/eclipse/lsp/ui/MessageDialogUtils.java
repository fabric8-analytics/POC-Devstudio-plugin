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
package com.redhat.fabric8analytics.eclipse.lsp.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.redhat.fabric8analytics.eclipse.core.data.AnalyticsAuthData;
import com.redhat.fabric8analytics.eclipse.core.internal.AnalyticsAuthService;
import com.redhat.fabric8analytics.eclipse.lsp.ui.Fabric8AnalysisLSUIActivator;

public class MessageDialogUtils {

	public static void displayInfoMessage(String message) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Fabric8 info message", message);
			}
		});
	}
	
	public static AnalyticsAuthData proptForLogin(IProgressMonitor monitor) {
		MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "Fabric8Analytics login", 
				null, "Please login to Openshift.io", MessageDialog.CONFIRM, new String[] {"Login", "Cancel"}, 0);
		if(dialog.open() == Window.OK) {
			try {
				return AnalyticsAuthService.getInstance().login(monitor);
			} catch (StorageException e) {
				Fabric8AnalysisLSUIActivator.getDefault().logError("Error while storing Fabric8Analytics data", e);
			}
		}
		return null;
	}

	public static void displayErrorMessage(String message) {
		Fabric8AnalysisLSUIActivator.getDefault().logError(message);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Fabric8 error message", message);
			}
		});
	}
	
	public static void displayErrorMessage(String message, Throwable t) {
		Fabric8AnalysisLSUIActivator.getDefault().logError(message, t);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Fabric8 error message", message);
			}
		});
	}
}
