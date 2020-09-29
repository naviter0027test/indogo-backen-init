package com.lionpig.webui.http.func;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;

public class Param implements IFunction {

	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi)
			throws Exception {
		IConnection conn = fi.getConnection();
		String SID = fi.getSID();
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = params.get("Action");
		String paramName = params.get("ParamName");
		if (action.equals("get")) {
			String val = conn.getParamValue(SID, paramName);
			if (val == null)
				return "";
			else
				return val;
		}
		else if (action.equals("del")) {
			conn.removeParam(SID, paramName);
		}
		return "";
	}
}
