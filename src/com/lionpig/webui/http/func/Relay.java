package com.lionpig.webui.http.func;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.http.FunctionException;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.RelayException;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.struct.LogInfo;
import com.lionpig.webui.http.struct.RelayInfo;
import com.lionpig.webui.http.util.BasicHttpResponseHandler;

// error code = 11xx
public class Relay implements IFunction {

	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi)
			throws Exception {
		IConnection conn = fi.getConnection();
		LogInfo log = fi.getLogInfo();
		String relayId = fi.getRequestParameters().get("RelayId");
		if (relayId == null)
			throw new Exception("Need to provide RelayId when using Relay function");
		RelayInfo ri = conn.getRelayInfo(relayId);
		if (ri == null)
			throw new Exception("Cannot find relay configuration for RelayId [" + relayId + "]");
		
		log.setRelayId(ri.getRelayId());
		log.setRelayUrl(ri.getUrl());
		log.setClassName(ri.getClassName());
		
		if (ri.getRelayType().equals("http")) {
			return executeHttp(ri, fi);
		}
		else if (ri.getRelayType().equals("class")) {
			return executeClass(ri, req, resp, fi);
		}
		else {
			throw new FunctionException(1100, "Unknown relay type [" + ri.getRelayType() + "] for relay id [" + ri.getRelayId() + "]");
		}
	}

	private String executeHttp(RelayInfo ri, FunctionItem fi) throws Exception {
		List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
		queryParams.add(new BasicNameValuePair("UID", fi.getSessionInfo().getUserName()));
		String n, v;
		Hashtable<String, String> params = fi.getRequestParameters();
		Enumeration<String> en = params.keys();
		while (en.hasMoreElements()) {
			n = en.nextElement();
			v = params.get(n);
			queryParams.add(new BasicNameValuePair(n, v));
		}
		String queryString = URLEncodedUtils.format(queryParams, "UTF-8");
		
		LogInfo log = fi.getLogInfo();
		IConnection conn = fi.getConnection();
		log.setVerbose(LogInfo.LOG_DEBUG);
		log.setMessage(queryString);
		conn.log(log);
		
		String result;
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			if (ri.getLoginName() != null && ri.getLoginPass() != null)
				client.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(ri.getLoginName(), ri.getLoginPass()));
			HttpPost post = new HttpPost(ri.getUrl() + "?" + queryString);
			result = client.execute(post, BasicHttpResponseHandler.createInstance());
		}
		finally {
			client.getConnectionManager().shutdown();
		}
		
		if (result.startsWith("<relayerror>")) {
			int beginIndex = "<relayerror>".length();
			int endIndex = result.indexOf("</relayerror>");
			String msg = result.substring(beginIndex, endIndex);
			
			beginIndex = result.indexOf("<relaystack>") + "<relaystack>".length();
			endIndex = result.indexOf("</relaystack>");
			String stack = result.substring(beginIndex, endIndex);
			
			throw new RelayException(1101, msg, stack);
		}
		return result;
	}
	
	@SuppressWarnings("rawtypes")
	private String executeClass(RelayInfo ri, HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		String className = ri.getClassName();
		if (className == null)
			throw new FunctionException(1102, "Please provide ClassName");
		Class c = Class.forName(className);
		Object o = c.newInstance();
		if (o instanceof IFunction) {
			return ((IFunction)o).execute(req, resp, fi);
		}
		else
			throw new FunctionException(1103, "ClassName [" + className + "] not implementing IFunction");
	}
	
}
