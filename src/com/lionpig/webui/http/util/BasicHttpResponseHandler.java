package com.lionpig.webui.http.util;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

public class BasicHttpResponseHandler implements ResponseHandler<String> {
	private BasicHttpResponseHandler() {}
	
	private static BasicHttpResponseHandler singleton = new BasicHttpResponseHandler();
	public static BasicHttpResponseHandler createInstance() {
		return singleton;
	}

	public String handleResponse(HttpResponse r) throws ClientProtocolException, IOException {
		if (r.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			throw new ClientProtocolException(r.getStatusLine().toString());
		}
		HttpEntity entity = r.getEntity();
		if (entity != null)
			return EntityUtils.toString(entity, "UTF-8");
		else
			return null;
	}
}
