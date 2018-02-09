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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

public class StackAnalysesView extends ViewPart {

	public static final String NAME = "com.redhat.fabric8analytics.lsp.eclipse.ui.StackAnalysesView";
	
	private static Browser browser = null;

	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(browser);	
	}

	@Override
	public void setFocus() {
		browser.setFocus();
	}

	public void updatebrowserUrl(String browseUrl) {
		Display.getDefault().asyncExec(new Runnable(){
			public void run(){
				if (!browser.isDisposed()) {
					browser.setUrl(browseUrl);
				}
			}
		});
	}
	public void disposebrowserUrl() {
		Display.getDefault().asyncExec(new Runnable(){
			public void run(){
				if (!browser.isDisposed()) {
					browser.dispose();;
				}
			}
		});
	}
}
