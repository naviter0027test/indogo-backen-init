package com.lionpig.webui.http.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.net.io.Util;

import com.lionpig.webui.database.ConnectionFactory;
import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.http.struct.LogInfo;
import com.lionpig.webui.http.struct.SessionInfo;
import com.lionpig.webui.http.util.AjaxMessage;

public class DownloadServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1659456827673993042L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String SID = req.getParameter("SID");
		if (SID == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot found parameter \"SID\"");
			return;
		}
		
		String contentType, contentDisposition;
		String filename = req.getParameter("Filename");
		String pdf = req.getParameter("pdf");
		if (filename == null || filename.length() == 0) {
			if (pdf == null || pdf.length() == 0) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot found parameter \"Filename\"");
				return;
			}
			else {
				filename = pdf;
				contentType = "application/pdf";
				contentDisposition = "inline; filename=\"" + filename + "\"";
			}
		}
		else {
			contentType = "APPLICATION/OCTET-STREAM";
			contentDisposition = "Attachment;Filename=" + filename;
		}
		
		String keepFile = req.getParameter("KeepFile");
		if (keepFile == null)
			keepFile = "N";
		else if (!keepFile.equals("Y") && !keepFile.equals("N"))
			keepFile = "N";
		
		SessionInfo si;
		IConnection conn = null;
		LogInfo logInfo = null;
		try {
			conn = ConnectionFactory.getInstance().createConnection(getServletContext());
			if (!conn.checkSessionId(SID, true, req.getLocalAddr(), req.getLocalName(), req.getLocalPort())) {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired");
				return;
			}
			si = conn.getUserInfo(SID);
			logInfo = new LogInfo(si.getUserRowId(), si.getUserName(), SID, "DownloadServlet");
		
			String tool = req.getParameter("Tool");
			if (tool == null)
				tool = "N";
			else if (!tool.equals("Y") && !tool.equals("N"))
				tool = "N";
			
			File f;
			if (tool.equals("Y")) {
				f = new File(getServletContext().getRealPath("tool"), filename);
				keepFile = "Y";
			}
			else {
				f = new File(getServletContext().getRealPath(si.getTempFolderPath()), filename);
			}
			
			try {
				if (conn.getLogVerbose() >= LogInfo.LOG_DEBUG) {
					StringBuilder sb = new StringBuilder();
					@SuppressWarnings("rawtypes")
					Enumeration en = req.getParameterNames();
					while (en.hasMoreElements()) {
						String K = en.nextElement().toString();
						String V = req.getParameter(K);
						sb.append(K).append("=").append(V).append("\n");
					}
					
					logInfo.setVerbose(LogInfo.LOG_DEBUG);
					logInfo.setMessage(sb.toString());
					conn.log(logInfo);
				}
			}
			catch (Exception ignore) {}
			
			resp.setContentType(contentType);
			resp.setHeader("Content-Disposition", contentDisposition);
			resp.setHeader("Content-Length", String.valueOf(f.length()));
			ServletOutputStream out = resp.getOutputStream();
			try {
				FileInputStream in = new FileInputStream(f);
				try {
					Util.copyStream(in, out);
				}
				finally {
					try {
						in.close();
					}
					catch (Exception ignore) {}
				}
			}
			finally {
				out.flush();
				out.close();
				
				if (keepFile.equals("N")) {
					logInfo.setVerbose(LogInfo.LOG_DEBUG);
					logInfo.setMessage("Delete file");
					conn.log(logInfo);
					try {
						boolean b = f.delete();
						logInfo.setMessage("Delete file result: " + b);
						conn.log(logInfo);
					}
					catch (Exception ignore) {
						logInfo.setMessage(ignore.getMessage());
						conn.log(logInfo);
					}
				}
			}
		}
		catch (Exception e) {
			if (logInfo != null && conn != null) {
				String msg = AjaxMessage.parseError(1400, e, true);
				logInfo.setVerbose(LogInfo.LOG_ERROR);
				logInfo.setMessage(msg);
				conn.log(logInfo);
			}
			throw new ServletException(e.getMessage(), e);
		}
		finally {
			try {
				if (conn != null)
					conn.close();
			}
			catch (Exception ignore) {}
		}
	}
}
