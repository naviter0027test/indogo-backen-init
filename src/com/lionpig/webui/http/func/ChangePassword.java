package com.lionpig.webui.http.func;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.webui.http.FunctionException;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;

public class ChangePassword implements IFunction {

	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi)
			throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String userName = params.get("UserName");
		if (userName == null)
			throw new FunctionException(1001, "Please provide UserName");
		String oldPassword = params.get("OldPassword");
		if (oldPassword == null)
			throw new FunctionException(1002, "Please provide OldPassword");
		String newPassword = params.get("NewPassword");
		if (newPassword == null)
			throw new FunctionException(1003, "Please provide NewPassword");
		fi.getConnection().adminUserChangePassword(userName, oldPassword, newPassword);
		return null;
	}

}
