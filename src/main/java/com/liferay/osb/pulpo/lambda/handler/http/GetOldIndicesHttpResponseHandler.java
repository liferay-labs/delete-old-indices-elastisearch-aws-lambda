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

package com.liferay.osb.pulpo.lambda.handler.http;

import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.HttpResponseHandler;

import java.nio.charset.StandardCharsets;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cristina González
 */
public class GetOldIndicesHttpResponseHandler
	implements HttpResponseHandler<List<String>> {

	public GetOldIndicesHttpResponseHandler(
		List<Pattern> indexPatterns, LocalDate lastDayToKept) {

		_indexPatterns = indexPatterns;
		_lastDayToKept = lastDayToKept;
	}

	@Override
	public List<String> handle(HttpResponse response) {
		List<String> indicesToDelete = new ArrayList<>();

		try (Scanner scanner =
				new Scanner(
					response.getContent(), StandardCharsets.UTF_8.name())) {

			scanner.useDelimiter("\\n");

			while (scanner.hasNext()) {
				String indexName = scanner.next();

				for (Pattern indexPattern : _indexPatterns) {
					Matcher matcher = indexPattern.matcher(indexName);

					if (matcher.matches()) {
						LocalDate indexDay = LocalDate.parse(
							matcher.group(1), _DATE_FORMAT);

						if (indexDay.isBefore(_lastDayToKept)) {
							indicesToDelete.add(matcher.group(0));
						}
					}
				}
			}
		}

		return indicesToDelete;
	}

	@Override
	public boolean needsConnectionLeftOpen() {
		return false;
	}

	private static final DateTimeFormatter _DATE_FORMAT =
		DateTimeFormatter.ofPattern("yyyy.MM.dd");

	private final List<Pattern> _indexPatterns;
	private final LocalDate _lastDayToKept;

}