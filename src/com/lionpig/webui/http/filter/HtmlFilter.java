package com.lionpig.webui.http.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.webui.database.ConnectionFactory;
import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.http.FunctionException;
import com.lionpig.webui.http.struct.LogInfo;
import com.lionpig.webui.http.struct.SessionInfo;
import com.lionpig.webui.http.util.AjaxMessage;

public class HtmlFilter implements Filter {
	private ServletContext context = null;

	public void destroy() {}

	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain chain) throws IOException, ServletException {
		if (context == null)
			return;
		HttpServletRequest req = (HttpServletRequest)arg0;
		HttpServletResponse resp = (HttpServletResponse)arg1;
		IConnection conn = null;
		LogInfo log = null;
		try {
			resp.setCharacterEncoding("UTF-8");
			resp.setHeader("Pragma", "no-cache");
			resp.setHeader("Cache-Control", "must-revalidate");
			resp.setHeader("Cache-Control", "no-cache");
			resp.setHeader("Cache-Control", "no-store");
			resp.setDateHeader("Expires", 0);
			req.setCharacterEncoding("UTF-8");
			
			String SID = req.getParameter("SID");
			if (SID == null)
				throw new Exception("Please provide SID");
			
			String rowId = req.getParameter("RowId");
			if (rowId == null)
				throw new Exception("Please provide RowId");
			
			int menuRowId = Integer.parseInt(rowId);
			conn = ConnectionFactory.getInstance().createConnection(context);
			if (conn.checkSessionId(SID, true, req.getLocalAddr(), req.getLocalName(), req.getLocalPort())) {
				SessionInfo si = conn.getUserInfo(SID);
				log = new LogInfo(si.getUserRowId(), si.getUserName(), SID, "HtmlFilter");
				
				try {
					if (conn.getLogVerbose() >= LogInfo.LOG_DEBUG) {
						StringBuilder sb = new StringBuilder();
						Enumeration<String> en = req.getParameterNames();
						while (en.hasMoreElements()) {
							String K = en.nextElement().toString();
							String V = req.getParameter(K);
							sb.append(K).append("=").append(V).append("\n");
						}
						
						log.setVerbose(LogInfo.LOG_DEBUG);
						log.setMessage(sb.toString());
						conn.log(log);
					}
				}
				catch (Exception ignore) {}
				
				if (conn.checkMenuAuthorize(SID, menuRowId)) {
					chain.doFilter(arg0, arg1);
				}
				else {
					throw new Exception("You are not authorize to view this page");
				}
			}
			else {
				throw new FunctionException(4, "Session already expired");
			}
		}
		catch (FunctionException E) {
			String msg = AjaxMessage.parseError(E.getErrorCode(), E, true);
			if (conn != null && log != null) {
				try {
					log.setVerbose(LogInfo.LOG_ERROR);
					log.setMessage(msg);
					conn.log(log);
				}
				catch (Exception ignore) {}
			}
			resp.setCharacterEncoding("UTF-8");
			PrintWriter pw = resp.getWriter();
			pw.write(msg);
		}
		catch (Exception E) {
			String msg = AjaxMessage.parseError(1, E, true);
			if (conn != null && log != null) {
				try {
					log.setVerbose(LogInfo.LOG_ERROR);
					log.setMessage(msg);
					conn.log(log);
				}
				catch (Exception ignore) {}
			}
			resp.setCharacterEncoding("UTF-8");
			PrintWriter pw = resp.getWriter();
			pw.write(msg);
		}
		finally {
			try {
				if (conn != null) {
					conn.close();
				}
			}
			catch (Exception ignore) {}
		}
	}

	public void init(FilterConfig arg0) throws ServletException {
		context = arg0.getServletContext();
	}

}
