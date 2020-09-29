package com.lionpig.webui.http;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import com.lionpig.webui.database.ConnectionFactory;
import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.struct.LogInfo;
import com.lionpig.webui.http.struct.SessionInfo;
import com.lionpig.webui.http.util.AjaxMessage;
import com.lionpig.webui.http.util.License;

// ErrorCode: 3xx
public class FunctionManager {
	
	public static void execute(HttpServletRequest req, HttpServletResponse resp, ServletContext sc, Hashtable<String, String> params, Hashtable<String, FileItem> uploadedFiles) throws ServletException, IOException {
		PrintWriter pw = resp.getWriter();
		IConnection conn = null;
		LogInfo logInfo = null;
		try {
			conn = ConnectionFactory.getInstance().createConnection(sc);
			
			String function = params.get("F");
			if (function == null){
				throw new FunctionException(2, "Incorrect message format: Function must be defined");
			}
			
			License license = License.getInstance();
			license.init(conn.getGlobalConfig("SERVER", "LICENSE"));
			
//			Date currentTime = Calendar.getInstance().getTime();
//			if (currentTime.after(license.getExpireDate()))
//				throw new Exception("Your license already expire, please contact software vendor to get a new license");
			
			if (function.equals(FunctionFactory.LOGIN) ||
				function.equals(FunctionFactory.CHANGE_PASSWORD) ||
				function.equals(FunctionFactory.CONFIRM_ACCOUNT)) {
				String reply = FunctionFactory.createInstance().getFunction(function).execute(req, resp, new FunctionItem(conn, null, null, null, params, null, sc, null));
				pw.write(AjaxMessage.parseOk(reply, null));
				return;
			}
			
			String SID = params.get("SID");
			
			if (SID == null)
				throw new FunctionException(3, "SID not provided");
			
			String updateLastActiveTime = params.get("UpdateSessionLastActiveTime");
			if (updateLastActiveTime != null)
				if (!updateLastActiveTime.equals("N"))
					updateLastActiveTime = null;
			
			if (!conn.checkSessionId(SID, updateLastActiveTime == null, req.getLocalAddr(), req.getLocalName(), req.getLocalPort())) {
				pw.write(AjaxMessage.parseError(4, "Session already expired", "Session already expired"));
				return;
			}
			
			SessionInfo SIDInfo = conn.getUserInfo(SID);
			if (SIDInfo == null)
				throw new Exception("Cannot get SessionInfo from SID [" + SID + "]");
			SIDInfo.setServerName(req.getServerName());
			
//			if (currentTime.after(license.getWarningDate())) {
//				SIDInfo.setWarningMessage("Your license will expire at [" + DateFormat.getInstance().formatShort(license.getExpireDate()) + "]");
//			}
			
			logInfo = new LogInfo(SIDInfo.getUserRowId(), SIDInfo.getUserName(), SID, function);
			
			try {
				if (conn.getLogVerbose() >= LogInfo.LOG_DEBUG) {
					Enumeration<String> en = params.keys();
					StringBuilder sb = new StringBuilder();
					while (en.hasMoreElements()) {
						String K = en.nextElement().toString();
						String V = params.get(K);
						sb.append(K).append("=").append(V).append("\n");
					}
					logInfo.setVerbose(LogInfo.LOG_DEBUG);
					logInfo.setMessage(sb.toString());
					conn.log(logInfo);
				}
			}
			catch (Exception E) {}
			
			File tempFolder = new File(sc.getRealPath(SIDInfo.getTempFolderPath()));
			if (!tempFolder.exists()) {
				tempFolder.mkdir();
			}
			else if (!tempFolder.isDirectory()) {
				throw new FunctionException(8, "Cannot create temp folder because file path [" + tempFolder.getAbsolutePath() + "] is not a directory");
			}
			
			FunctionFactory factory = FunctionFactory.createInstance();
			IFunction f = factory.getFunction(function);
			String reply = f.execute(req, resp, new FunctionItem(conn, SID, SIDInfo, logInfo, params, uploadedFiles, sc, tempFolder));
			String msg = AjaxMessage.parseOk(reply, SIDInfo);
			
			logInfo.setVerbose(LogInfo.LOG_TRACE);
			logInfo.setMessage(reply);
			conn.log(logInfo);
			
			pw.write(msg);
		}
		catch (RelayException E) {
			if (logInfo != null && conn != null) {
				try {
					logInfo.setVerbose(LogInfo.LOG_ERROR);
					logInfo.setMessage(AjaxMessage.parseError(E.getCode(), E.getMessage(), E.getStack(), true));
					conn.log(logInfo);
				}
				catch (Exception ignore) {}
			}
			pw.write(AjaxMessage.parseError(E.getCode(), E.getMessage(), E.getStack()));
		}
		catch (FunctionException E) {
			if (logInfo != null && conn != null) {
				try {
					logInfo.setVerbose(LogInfo.LOG_ERROR);
					logInfo.setMessage(AjaxMessage.parseError(E.getErrorCode(), E, true));
					conn.log(logInfo);
				}
				catch (Exception ignore) {}
			}
			pw.write(AjaxMessage.parseError(E.getErrorCode(), E));
		}
		catch (Exception E) {
			if (logInfo != null && conn != null) {
				try {
					logInfo.setVerbose(LogInfo.LOG_ERROR);
					logInfo.setMessage(AjaxMessage.parseError(300, E, true));
					conn.log(logInfo);
				}
				catch (Exception ignore) {}
			}
			pw.write(AjaxMessage.parseError(300, E));
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				}
				catch (Exception E) {}
			}
		}
	}
}
