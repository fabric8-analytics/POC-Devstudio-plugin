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

package com.redhat.fabric8analytics.eclipse.lsp.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.osgi.framework.Bundle;

import com.redhat.fabric8analytics.eclipse.core.data.AnalyticsAuthData;
import com.redhat.fabric8analytics.eclipse.core.internal.AnalyticsAuthService;
import com.redhat.fabric8analytics.eclipse.lsp.core.Fabric8AnalysisLSCoreActivator;
import com.redhat.fabric8analytics.eclipse.core.Fabric8AnalysisPreferences;
import com.redhat.fabric8analytics.eclipse.lsp.ui.MessageDialogUtils;

public class Fabric8AnalyticsStreamConnectionProvider extends ProcessStreamConnectionProvider
		implements StreamConnectionProvider {

	public static final String SERVER_ID = "com.redhat.fabric8analytics.lsp.eclipse.server";
	
	private static final String LSP_SERVER_ID_ATTRIBUTE = "languageServerId";
	
	public static final String RECOMMENDER_API_TOKEN = "RECOMMENDER_API_TOKEN";

	public static final String RECOMMENDER_API_URL = "RECOMMENDER_API_URL";

	private AnalyticsAuthService osioApiProvider;

	public static final String THREE_SCALE_USER_TOKEN = "THREE_SCALE_USER_TOKEN";

	public static final String VERSION_ROUTE = "/api/v1";

	private AnalyticsAuthData analyticsAuthData;

	public Fabric8AnalyticsStreamConnectionProvider() {
		this(AnalyticsAuthService.getInstance());
	}

	public Fabric8AnalyticsStreamConnectionProvider(AnalyticsAuthService tokenCheck) {
		super();
		this.osioApiProvider = tokenCheck;

		File nodeJsLocation = getNodeJsLocation();
		if (nodeJsLocation == null) {
			return;
		}

		File serverLocation = getServerLocation();
		Fabric8AnalysisLSCoreActivator.getDefault().logInfo("server location" + serverLocation);
		if (serverLocation == null) {
			return;
		}

		setCommands(Arrays.asList(
				new String[] { nodeJsLocation.getAbsolutePath(), serverLocation.getAbsolutePath(), "--stdio" }));

		setWorkingDirectory(System.getProperty("user.dir"));
		addPreferencesListener();
	}

	@Override
	public void start() throws IOException {
		if (!Fabric8AnalysisPreferences.getInstance().isLSPServerEnabled()) {
			throw new IOException("Fabric8Analytics LSP Server is not enabled");
		}

		try {
			analyticsAuthData = osioApiProvider.getAnalyticsAuthData(new NullProgressMonitor());
		} catch (StorageException e) {
			throw new IOException("Error while retrieving stored data", e);
		}
		if (analyticsAuthData == null) {
			throw new IOException("User is not logged in Openshift.io");
		}

		super.start();
		// if super.start() does not throw exception, we're started
		Fabric8AnalysisLSCoreActivator.getDefault().logInfo("The Fabric8 analyses server is started ");
	}

	@Override
	public void stop() {
		super.stop();
		removeMarkers();
		Fabric8AnalysisLSCoreActivator.getDefault().logInfo("The Fabric8 analyses server is stopped");
	}
	
	private void removeMarkers() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = workspaceRoot.getProjects();
		for (IProject project : projects) {
			try {
				List<IMarker> markersToDelete = new ArrayList<>();
				IMarker[] markers = project.findMarkers(null, true, IResource.DEPTH_INFINITE);
				for (IMarker marker : markers) {
					if (marker.exists()) {
						Object serverId = marker.getAttribute(LSP_SERVER_ID_ATTRIBUTE);
						if (SERVER_ID.equals(serverId)) {
							markersToDelete.add(marker);
						}
					}
				}
				ResourcesPlugin.getWorkspace()
						.deleteMarkers(markersToDelete.toArray(new IMarker[markersToDelete.size()]));
			} catch (CoreException e) {
				Fabric8AnalysisLSCoreActivator.getDefault().logError("Error occured while removing LSP markers", e);
			}
		}
	}

	@Override
	protected ProcessBuilder createProcessBuilder() {
		ProcessBuilder res = super.createProcessBuilder();
		String serverUrl = getAuthData().getThreeScaleData().getProd() + VERSION_ROUTE;
		res.environment().put(RECOMMENDER_API_TOKEN, getAuthData().getToken());
		res.environment().put(RECOMMENDER_API_URL, serverUrl);
		res.environment().put(THREE_SCALE_USER_TOKEN, getAuthData().getThreeScaleData().getUserKey());
		return res;
	}

	private void addPreferencesListener() {
		InstanceScope.INSTANCE.getNode(Fabric8AnalysisLSCoreActivator.PLUGIN_ID)
				.addPreferenceChangeListener(new IPreferenceChangeListener() {

					@Override
					public void preferenceChange(PreferenceChangeEvent event) {
						if (Fabric8AnalysisPreferences.LSP_SERVER_ENABLED.equals(event.getKey())) {
							if(Fabric8AnalysisPreferences.getInstance().isLSPServerEnabled()) {
								try {
									// not sure if this actually works
									start();
								} catch (IOException e) {
									// // nothing to do
								}
							} else {
								stop();
							}
						}
					}
				});
	}

	private static File getServerLocation() {
		try {
			Bundle bundle = Platform.getBundle(Fabric8AnalysisLSCoreActivator.PLUGIN_ID);
			return new File(FileLocator.getBundleFile(bundle), "server/server.js");
		} catch (IOException e) {
			Fabric8AnalysisLSCoreActivator.getDefault().logError("Cannot find the Fabric8 analyses server location", e);
			return null;
		}
	}

	private static File getNodeJsLocation() {
		String location = null;
		String[] command = new String[] { "/bin/bash", "-c", "which node" };
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			command = new String[] { "cmd", "/c", "where node" };
		}
		BufferedReader reader = null;
		try {
			Process p = Runtime.getRuntime().exec(command);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			location = reader.readLine();
		} catch (IOException e) {
			// no problem, try default install path
		} finally {
			IOUtils.closeQuietly(reader);
		}

		// Try default install path as last resort
		if (location == null && Platform.getOS().equals(Platform.OS_MACOSX)) {
			location = "/usr/local/bin/node";
		}

		if (location != null && Files.exists(Paths.get(location))) {
			return new File(location);
		}

		String message = "`node` is missing in your PATH, Fabric8 Fabric8 analyses server won't work.\n"
				+ "Please install `node` and make it available in your PATH";

		MessageDialogUtils.displayErrorMessage(message);
		return null;
	}

	@Override
	public List<String> getCommands() {
		return super.getCommands();
	}

	@Override
	public InputStream getInputStream() {
		return super.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() {
		return super.getOutputStream();
	}
	
	public AnalyticsAuthData getAuthData() {
		return analyticsAuthData;
	}
}
