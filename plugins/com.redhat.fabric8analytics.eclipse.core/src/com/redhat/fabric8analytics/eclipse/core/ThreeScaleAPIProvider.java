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

package com.redhat.fabric8analytics.eclipse.core;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.redhat.fabric8analytics.eclipse.core.data.ThreeScaleData;

/**
 * Provides access to 3scale service
 * 
 */
public class ThreeScaleAPIProvider {

	public static final String THREE_SCALE_URL = "https://3scale-connect.api.openshift.io/get-route";

	public static final String SERVICE_ID = "2555417754949";

	private String token;

	public ThreeScaleAPIProvider(String token) {
		checkConstructorArguments(token);
		this.token = token;
	}

	/**
	 * Registers to 3scale. 
	 * 
	 * @author Geetika Batra
	 * @param token
	 * @return 
	 * @throws ThreeScaleAPIException 
	 */
	public ThreeScaleData register3Scale() throws ThreeScaleAPIException {
		CloseableHttpClient client = createClient();
		try {
			JSONObject urlObject = new JSONObject();
			urlObject.put("auth_token", token);
			urlObject.put("service_id" , SERVICE_ID);

			StringEntity se = new StringEntity(urlObject.toString());
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

			HttpPost post = new HttpPost(THREE_SCALE_URL);
			post.setEntity(se);

			HttpResponse response = client.execute(post);

			int responseCode = response.getStatusLine().getStatusCode();

			if (responseCode==HttpStatus.SC_OK) {
				JSONObject jsonObj = new JSONObject(EntityUtils.toString(response.getEntity()));
				return createThreeScaleData(jsonObj);
			} else {
				throw new ThreeScaleAPIException("The 3scale server returned unexpected return code: " + responseCode);				
			}
		} catch (IOException | JSONException e) {
			throw new ThreeScaleAPIException(e);				
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				// do nothing
			}
		}
		
	}

	protected CloseableHttpClient createClient() {
		return HttpClients.createDefault();
	}
	
	private void checkConstructorArguments(String token2) {
		if (token2 == null) {
			throw new IllegalArgumentException("The token was null");
		}
	}

	private ThreeScaleData createThreeScaleData(JSONObject jsonObj) throws ThreeScaleAPIException, JSONException {
		String endpointsString = jsonObj.getString("endpoints");
		JSONObject endpoints = new JSONObject(endpointsString);

		ThreeScaleData data = new ThreeScaleData();
		data.setProd(endpoints.getString("prod"));
		data.setStage(endpoints.getString("stage"));
		data.setUserKey(jsonObj.getString("user_key"));
		return data;
	}
}
