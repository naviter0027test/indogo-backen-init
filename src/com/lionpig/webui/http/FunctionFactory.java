package com.lionpig.webui.http;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.webui.http.func.AutoPage;
import com.lionpig.webui.http.func.ChangePassword;
import com.lionpig.webui.http.func.Login;
import com.lionpig.webui.http.func.Menu;
import com.lionpig.webui.http.func.Param;
import com.lionpig.webui.http.func.Relay;
import com.lionpig.webui.http.func.TablePage;
import com.lionpig.webui.http.struct.FunctionItem;

public class FunctionFactory {
	private static FunctionFactory singleton = new FunctionFactory();
	public static FunctionFactory createInstance() {
		return singleton;
	}
	
	public static final String LOGIN = "Login";
	public static final String LOGOUT = "Logout";
	public static final String MENU = "Menu";
	public static final String RELAY = "Relay";
	public static final String PARAM = "Param";
	public static final String AUTO_PAGE = "AutoPage";
	public static final String TABLE_PAGE = "TablePage";
	public static final String CONFIRM_ACCOUNT = "ConfirmAccount";
	public static final String CHANGE_PASSWORD = "ChangePassword";
	
	private Hashtable<String, IFunction> ht;
	private FunctionFactory() {
		ht = new Hashtable<String, IFunction>();
		
		ht.put(LOGIN, new Login());
		ht.put(LOGOUT, new IFunction() {
			public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi)
					throws Exception {
				fi.getConnection().logout(fi.getSID());
				return null;
			}
		});
		ht.put(MENU, new Menu());
		ht.put(RELAY, new Relay());
		ht.put(PARAM, new Param());
		ht.put(AUTO_PAGE, new AutoPage());
		ht.put(TABLE_PAGE, new TablePage());
		ht.put(CONFIRM_ACCOUNT, new IFunction() {
			@Override
			public String execute(HttpServletRequest req, HttpServletResponse resp,
					FunctionItem fi) throws Exception {
				Hashtable<String, String> param = fi.getRequestParameters();
				String userName = param.get("UserName");
				String confirmId = param.get("ConfirmId");
				String newPassword = param.get("NewPassword");
				fi.getConnection().adminUserConfirmAccount(userName, confirmId, newPassword);
				return null;
			}
		});
		ht.put(CHANGE_PASSWORD, new ChangePassword());
	}
	
	public IFunction getFunction(String functionName) throws FunctionException {
		if (ht.containsKey(functionName))
			return ht.get(functionName);
		else
			throw new FunctionException(100, "Unknown function [" + functionName + "]");
	}
}
