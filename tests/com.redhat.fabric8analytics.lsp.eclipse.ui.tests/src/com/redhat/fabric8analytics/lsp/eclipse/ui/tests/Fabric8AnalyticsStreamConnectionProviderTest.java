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
package com.redhat.fabric8analytics.lsp.eclipse.ui.tests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Matchers.any;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.security.storage.StorageException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.redhat.fabric8analytics.eclipse.core.data.AnalyticsAuthData;
import com.redhat.fabric8analytics.eclipse.core.data.ThreeScaleData;
import com.redhat.fabric8analytics.eclipse.core.internal.AnalyticsAuthService;
import com.redhat.fabric8analytics.eclipse.core.Fabric8AnalysisPreferences;
import com.redhat.fabric8analytics.eclipse.lsp.core.Fabric8AnalyticsStreamConnectionProvider;

public class Fabric8AnalyticsStreamConnectionProviderTest {
	
	private static final String TOKEN = "mytoken";
	
	private AnalyticsAuthService authService;

	private Fabric8AnalyticsStreamConnectionProviderWrapper provider;
	
	@Before
	public void setup() throws StorageException {
		Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(true);
		
		ThreeScaleData scaleData = new ThreeScaleData("www.myurl.com", "www.myurl.com", "userKey");
		AnalyticsAuthData data = new AnalyticsAuthData(scaleData, TOKEN);
		
		authService = mock(AnalyticsAuthService.class);
		when(authService.getAnalyticsAuthData(any(IProgressMonitor.class))).thenReturn(data);
		
		provider = new Fabric8AnalyticsStreamConnectionProviderWrapper(authService);
	}
	
	@After
	public void after() {
		Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(true);
	}
	
	@Test
	public void createProcessBuilder() throws StorageException {
		ThreeScaleData scaleData = new ThreeScaleData("www.myurl.com", "www.myurl.com", "userKey");
		AnalyticsAuthData data = new AnalyticsAuthData(scaleData, TOKEN);
		
		provider = new Fabric8AnalyticsStreamConnectionProviderWrapper(authService);
		Fabric8AnalyticsStreamConnectionProviderWrapper providerSpy = spy(provider);
		doReturn(data).when(providerSpy).getAuthData();
		
		ProcessBuilder builder = providerSpy.createProcessBuilder();
		List<String> commands = builder.command();
		
		assertThat(commands.size(), is(3));
		assertThat(commands.get(0), containsString("node"));
		assertThat(commands.get(1), containsString("server.js"));
		assertThat(commands.get(2), containsString("--stdio"));
		
		assertEquals(System.getProperty("user.dir"), builder.directory().getAbsolutePath());
		
		Map<String, String> params = builder.environment();
		
		assertThat(params, hasEntry(Fabric8AnalyticsStreamConnectionProvider.RECOMMENDER_API_TOKEN, TOKEN));
		assertThat(params, hasEntry(Fabric8AnalyticsStreamConnectionProvider.RECOMMENDER_API_URL, "www.myurl.com" + Fabric8AnalyticsStreamConnectionProvider.VERSION_ROUTE));
	}
	
	@Test(expected=IOException.class)
	public void start_noToken() throws IOException, StorageException {
		authService = mock(AnalyticsAuthService.class);
		when(authService.getAnalyticsAuthData(any(IProgressMonitor.class))).thenReturn(null);
		
		provider = new Fabric8AnalyticsStreamConnectionProviderWrapper(authService);
		provider.start();
	}
	
	@Test(expected=IOException.class)
	public void start_analysesDisabled() throws IOException {
		Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(false);
		provider.start();
	}
	
	@Test
	public void start() throws IOException {
		provider.start();
		
		assertThat(provider.getInputStream(), is(not(nullValue())));
		assertThat(provider.getOutputStream(), is(not(nullValue())));
	}
	
	@Test
	public void stop() {
		provider.stop();

		assertTrue("No exception expected", true);
	}
	
	@Test
	public void startAndStop() throws IOException {
		provider.start();
		provider.stop();
		
		assertTrue("No exception expected", true);
	}
	
	@Test
	public void propertyEnabled() throws IOException {
		Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(false);
		
		assertThat(provider.getInputStream(), is(nullValue()));
		assertThat(provider.getOutputStream(), is(nullValue()));
		
		Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(true);
		
		assertThat(provider.getInputStream(), is(not(nullValue())));
		assertThat(provider.getOutputStream(), is(not(nullValue())));
	}
	
	@Test
	public void propertyDisabled() throws IOException {
		Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(false);
		
		assertTrue("No exception expected", true);
		// there is no way of knowing if the server is stopped
	}
}
