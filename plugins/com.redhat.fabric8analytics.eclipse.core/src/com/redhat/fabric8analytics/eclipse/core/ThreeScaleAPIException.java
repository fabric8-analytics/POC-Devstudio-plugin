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

import org.json.JSONObject;

public class ThreeScaleAPIException extends Exception {

	private static final long serialVersionUID = -6919737368324609835L;

	private JSONObject jsonObject;
	
	public ThreeScaleAPIException(JSONObject jsonObj) {
		this.jsonObject = jsonObj;
	}
	
	public ThreeScaleAPIException(Exception e) {
		super(e);
	}

	public ThreeScaleAPIException(String msg) {
		super(msg);
	}

	@Override
	public String getMessage() {
		if (jsonObject != null) {
			return jsonObject.toString();
		}
		return super.getMessage();
	}
}
