package com.lionpig.webui.admin;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.License;

public class LicenseMaintain implements IFunction {

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp,
			FunctionItem fi) throws Exception {
		String oldLicense = fi.getConnection().getGlobalConfig("SERVER", "LICENSE");
		
		Hashtable<String, String> param = fi.getRequestParameters();
		String license = param.get("License");
		License l = License.getInstance();
		
		try {
			l.init(license);
			fi.getConnection().setGlobalConfig("SERVER", "LICENSE", license);
			return "0";
		}
		catch (Exception e) {
			l.init(oldLicense);
			throw e;
		}
	}

}
