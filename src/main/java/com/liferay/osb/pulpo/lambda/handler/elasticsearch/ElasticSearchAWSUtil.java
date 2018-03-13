/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.osb.pulpo.lambda.handler.elasticsearch;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.http.HttpMethodName;

import com.liferay.osb.pulpo.lambda.handler.http.DeleteIndexHttpResponseHandler;
import com.liferay.osb.pulpo.lambda.handler.http.GetOldIndicesHttpResponseHandler;
import com.liferay.osb.pulpo.lambda.handler.http.SimpleHttpResponseHandler;

import java.net.URI;

import java.time.LocalDate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Cristina González
 */
public class ElasticSearchAWSUtil {

	public static Map<String, String> deleteIndices(
		String host, List<String> indices) {

		Map<String, String> indicesResults = new HashMap<>();

		for (String index : indices) {
			Request<Void> request = _getRequest(
				host, "/" + index, Collections.EMPTY_MAP,
				HttpMethodName.DELETE);

			Response<String> response = new AmazonHttpClient(
				new ClientConfiguration()
			).requestExecutionBuilder(
			).executionContext(
				new ExecutionContext(true)
			).request(
				request
			).errorResponseHandler(
				new SimpleHttpResponseHandler()
			).execute(
				new DeleteIndexHttpResponseHandler()
			);

			indicesResults.put(index, response.getAwsResponse());
		}

		return indicesResults;
	}

	public static List<String> getOldIndices(
		String host, List<Pattern> indexPatterns, int keepIndexDays) {

		Request<Void> request = _getRequest(
			host, "/_cat/indices",
			Collections.unmodifiableMap(
				new HashMap<String, List<String>>() {
					{
						put("h", Arrays.asList("index"));
					}
				}),
			HttpMethodName.GET);

		Response<List<String>> response = new AmazonHttpClient(
			new ClientConfiguration()
		).requestExecutionBuilder(
		).executionContext(
			new ExecutionContext(true)
		).request(request
		).errorResponseHandler(
			new SimpleHttpResponseHandler()
		).execute(
			new GetOldIndicesHttpResponseHandler(
				indexPatterns, LocalDate.now().minusDays(keepIndexDays))
		);

		return response.getAwsResponse();
	}

	private static Request<Void> _getRequest(
		String host, String path, Map<String, List<String>> params,
		HttpMethodName httpMethodName) {

		Request<Void> request = new DefaultRequest<>("es");

		request.setHttpMethod(httpMethodName);

		request.setEndpoint(URI.create(host));

		request.setResourcePath(path);

		request.setParameters(params);

		AWS4Signer aws4Signer = new AWS4Signer();

		aws4Signer.setServiceName(request.getServiceName());
		aws4Signer.setRegionName(_REGION);
		aws4Signer.sign(
			request, new DefaultAWSCredentialsProviderChain().getCredentials());

		return request;
	}

	private static final String _REGION = System.getenv(
		SDKGlobalConfiguration.AWS_REGION_ENV_VAR);

}