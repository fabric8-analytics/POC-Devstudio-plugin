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

package com.redhat.fabric8analytics.eclipse.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Fabric8AnalysisCoreActivator extends Plugin {

	public static final String PLUGIN_ID = "com.redhat.fabric8analytics.lsp.eclipse.core";
	
	// The shared instance
	private static Fabric8AnalysisCoreActivator plugin;

	/**
	 * The constructor
	 */
	public Fabric8AnalysisCoreActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Fabric8AnalysisCoreActivator getDefault() {
		return plugin;
	}

	public void logInfo(String msg) {
		IStatus status = new Status(Status.INFO, PLUGIN_ID, msg);
		getLog().log(status);
	}

	public void logError(String msg, Throwable t) {
		IStatus status = new Status(Status.ERROR, PLUGIN_ID, msg, t);
		getLog().log(status);
	}
}
