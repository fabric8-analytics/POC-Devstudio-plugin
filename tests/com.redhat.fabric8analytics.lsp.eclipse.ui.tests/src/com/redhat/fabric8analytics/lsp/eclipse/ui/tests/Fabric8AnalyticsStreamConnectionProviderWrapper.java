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
package com.redhat.fabric8analytics.lsp.eclipse.ui.tests;

import com.redhat.fabric8analytics.eclipse.core.internal.AnalyticsAuthService;
import com.redhat.fabric8analytics.eclipse.lsp.core.Fabric8AnalyticsStreamConnectionProvider;

//wrapper class for Fabric8AnalyticsStreamConnectionProvider to be able to access protected methods
public class Fabric8AnalyticsStreamConnectionProviderWrapper extends Fabric8AnalyticsStreamConnectionProvider{
	
	public Fabric8AnalyticsStreamConnectionProviderWrapper(AnalyticsAuthService authService) {
		super(authService);
	}
	
	@Override
	public ProcessBuilder createProcessBuilder() {
		// TODO Auto-generated method stub
		return super.createProcessBuilder();
	}

}
