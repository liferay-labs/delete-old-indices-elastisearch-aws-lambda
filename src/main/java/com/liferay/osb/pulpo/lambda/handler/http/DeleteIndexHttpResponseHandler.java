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

import java.util.Scanner;

/**
 * @author Cristina González
 */
public class DeleteIndexHttpResponseHandler
	implements HttpResponseHandler<String> {

	@Override
	public String handle(HttpResponse response) {
		String responseString = null;

		try (Scanner scanner = new Scanner(
			response.getContent(), StandardCharsets.UTF_8.name())) {

			scanner.useDelimiter("\\A");

			responseString = scanner.next();
		}

		return responseString;
	}

	@Override
	public boolean needsConnectionLeftOpen() {
		return false;
	}

}