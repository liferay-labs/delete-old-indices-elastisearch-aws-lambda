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

package com.liferay.osb.pulpo.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.liferay.osb.pulpo.lambda.handler.elasticsearch.ElasticSearchAWSUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Cristina González
 */
public class LambdaHandler
	implements RequestHandler<String, Map<String, String>> {

	public static final Pattern FILEBEAT_INDEX_PATTERN = Pattern.compile(
		"filebeat.*-(\\d{4}.\\d{2}.\\d{2})");

	public static final Pattern METRICBEAT_INDEX_PATTERN = Pattern.compile(
		"metricbeat.*-(\\d{4}.\\d{2}.\\d{2})");

	@Override
	public Map<String, String> handleRequest(String host, Context context) {
		LambdaLogger logger = context.getLogger();

		logger.log("You are going to delete old indices in " + host + "\n");

		List<String> oldIndices = ElasticSearchAWSUtil.getOldIndices(
			host,
			Arrays.asList(FILEBEAT_INDEX_PATTERN, METRICBEAT_INDEX_PATTERN),
			_DAYS_TO_KEEP);

		Map<String, String> indices = ElasticSearchAWSUtil.deleteIndices(
			host, oldIndices);

		return indices;
	}

	private static final int _DAYS_TO_KEEP = 2;

}