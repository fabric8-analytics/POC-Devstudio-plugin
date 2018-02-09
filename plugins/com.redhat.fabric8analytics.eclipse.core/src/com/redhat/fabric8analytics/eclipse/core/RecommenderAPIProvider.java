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


import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.json.JSONException;
import org.json.JSONObject;

import com.redhat.fabric8analytics.eclipse.core.data.AnalyticsAuthData;
import com.redhat.fabric8analytics.eclipse.core.internal.PomContentBody;

/**
 * Provides access to Recommender API server
 * 
 * @author ljelinko
 *
 */
public class RecommenderAPIProvider {

	private static final String RECOMMENDER_API_URL_POSTFIX = "/api/v1";

	private static final String RECOMMENDER_API_URL_STACK_ANALYSES_POSTFIX = RECOMMENDER_API_URL_POSTFIX
			+ "/stack-analyses/";

	private static final String RECOMMENDER_API_URL_POLL_ANALYSES_POSTFIX = RECOMMENDER_API_URL_POSTFIX
			+ "/stack-analyses/";

	private static final String ANALYSES_REPORT_URL = "https://stack-analytics-report.openshift.io/#/analyze/";

	private static final String POST_ANALYSES_REPORT_URL = "?api_data={\"access_token\":\"%s\",\"route_config\":{\"api_url\":\"%s\"},\"user_key\":\"%s\"}";

	private static final String SERVICE_ID = "2555417754949";

	private AnalyticsAuthData analyticsAuthData;

	public RecommenderAPIProvider(AnalyticsAuthData analyticsAuthData) {
		checkAuthData(analyticsAuthData);
		this.analyticsAuthData = analyticsAuthData;
	}

	/**
	 * Request analysis from the recommender API server.
	 * 
	 * @param pomFiles
	 * @return jobID
	 */
	public String requestAnalyses(Map<String, String> files, IFile license) throws RecommenderAPIException {

		checkFiles(files);// check if this is none
		HttpPost post = new HttpPost(
				analyticsAuthData.getThreeScaleData().getProd() + RECOMMENDER_API_URL_STACK_ANALYSES_POSTFIX
						+ String.format("?user_key=%s", analyticsAuthData.getThreeScaleData().getUserKey()));
		post.addHeader("Authorization", analyticsAuthData.getToken());

		MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		
		for (Map.Entry<String, String> fileObject : files.entrySet()) {
			builder.addPart("manifest[]", new PomContentBody(fileObject.getValue())).addTextBody("filePath[]",
					fileObject.getKey());
		}
		if(license!=null) {
			IPath licenseFile = license.getLocation();

			FileBody licenseBody = new FileBody(new File(licenseFile.toString()));
			builder.addPart("license[]", licenseBody);
		}
		HttpEntity multipart = builder.build();
		post.setEntity(multipart);

		CloseableHttpClient client = createClient();
		try {
			HttpResponse response = client.execute(post);
			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode == HttpStatus.SC_OK) {
				JSONObject jsonObj = new JSONObject(EntityUtils.toString(response.getEntity()));
				return jsonObj.getString("id");
			} else {
				throw new RecommenderAPIException(
						"The recommender server returned unexpected return code: " + responseCode);
			}
		} catch (IOException | JSONException e) {
			throw new RecommenderAPIException(e);
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	public boolean analysesFinished(String jobId) throws RecommenderAPIException {
		checkJobID(jobId);
		String RECOMMENDER_API_TOKEN = "Bearer ";
		if (!RECOMMENDER_API_TOKEN.equals("Bearer " + analyticsAuthData.getToken())) {
			RECOMMENDER_API_TOKEN = "Bearer " + analyticsAuthData.getToken();
		}
		HttpGet get = new HttpGet(
				analyticsAuthData.getThreeScaleData().getProd() + RECOMMENDER_API_URL_POLL_ANALYSES_POSTFIX + jobId
						+ String.format("?user_key=%s", analyticsAuthData.getThreeScaleData().getUserKey()));
		get.addHeader("Authorization", RECOMMENDER_API_TOKEN);
		CloseableHttpClient client = createClient();

		try {
			HttpResponse response = client.execute(get);
			int responseCode = response.getStatusLine().getStatusCode();

			// TODO - for debug purposes - should be removed later
			Fabric8AnalysisCoreActivator.getDefault().logInfo("F8 server response code: " + responseCode);

			if (responseCode == HttpStatus.SC_OK) {
				return true;
			} else if (responseCode == HttpStatus.SC_ACCEPTED) {
				return false;
			} else {
				throw new RecommenderAPIException(
						"The recommender server returned unexpected return code: " + responseCode);
			}
		} catch (Exception e) {
			throw new RecommenderAPIException(e);
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				// do nothing
			}
		}

	}

	public String getAnalysesURL(String jobID) {
		String postURLFormat = String.format(POST_ANALYSES_REPORT_URL, analyticsAuthData.getToken(),
				analyticsAuthData.getThreeScaleData().getProd(), analyticsAuthData.getThreeScaleData().getUserKey());

		String url = ANALYSES_REPORT_URL + jobID + postURLFormat;
		return url;
	}

	protected CloseableHttpClient createClient() {
		return HttpClients.createDefault();
	}

	private void checkAuthData(AnalyticsAuthData authData) {
		if (authData.getThreeScaleData().getProd() == null) {
			throw new IllegalArgumentException("The URL was null");
		}

		if (authData.getThreeScaleData().getProd().isEmpty()) {
			throw new IllegalArgumentException("The URL was empty");
		}

		if (authData.getThreeScaleData().getUserKey() == null) {
			throw new IllegalArgumentException("The user key was null");
		}

		if (authData.getToken() == null) {
			throw new IllegalArgumentException("The token was null");
		}
	}

	private void checkFiles(Map<String, String> files) {
		if (files == null) {
			throw new IllegalArgumentException("Files for analyses were null");
		}

		if (files.size() == 0) {
			throw new IllegalArgumentException("Files for analyses were empty");
		}
	}

	private void checkJobID(String jobId) {
		if (jobId == null) {
			throw new IllegalArgumentException("Job ID was null");
		}

		if ("".equals(jobId)) {
			throw new IllegalArgumentException("Job ID was empty string");
		}
	}
}