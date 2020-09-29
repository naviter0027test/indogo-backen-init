package com.lionpig.webui.http.func;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.http.FunctionFactory;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.struct.LogInfo;
import com.lionpig.webui.http.struct.SessionInfo;

public class Login implements IFunction {

	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi)
			throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String userName = params.get("U");
		String password = params.get("P");
		if (userName == null)
			throw new Exception("UserName not provided");
		if (password == null)
			throw new Exception("Password not provided");
		
		IConnection conn = fi.getConnection();
		String SID = fi.getSID();
		SessionInfo info = fi.getSessionInfo();
		SID = conn.createSession(userName, password);
		info = conn.getUserInfo(SID);
		LogInfo log = new LogInfo(info.getUserRowId(), info.getUserName(), SID, FunctionFactory.LOGIN);
		log.setVerbose(LogInfo.LOG_LOGIN);
		StringBuilder msg = new StringBuilder();
		try {
			msg.append("RemoteAddr:").append(req.getRemoteAddr()).append("\n");
		}
		catch (Exception ignore) {}
		try {
			msg.append("RemoteHost:").append(req.getRemoteHost()).append("\n");
		}
		catch (Exception ignore) {}
		try {
			msg.append("ServerName:").append(req.getServerName()).append("\n");
		}
		catch (Exception ignore) {}
		try {
			msg.append("ServerPort:").append(req.getServerPort() + "").append("\n");
		}
		catch (Exception ignore) {}
		try {
			msg.append("IsSecure:").append(req.isSecure()).append("\n");
		}
		catch (Exception ignore) {}
		try {
			msg.append("User-Agent:").append(req.getHeader("User-Agent"));
		}
		catch (Exception ignore) {}
		log.setMessage(msg.toString());
		conn.log(log);
		return SID;
	}
}
