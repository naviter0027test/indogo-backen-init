package com.lionpig.webui.http.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.lionpig.webui.database.ConnectionFactory;
import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.http.FunctionException;
import com.lionpig.webui.http.struct.AutoPageInfo;
import com.lionpig.webui.http.struct.LogInfo;
import com.lionpig.webui.http.struct.SessionInfo;
import com.lionpig.webui.http.struct.SqlInputInfo;
import com.lionpig.webui.http.util.AjaxMessage;

// ErrorCode: 2xx
public class AutoPage extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7948940154420363746L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		
		String SID  = req.getParameter("SID");
		if (SID == null)
			return;
		String RowId = req.getParameter("RowId");
		if (RowId == null)
			return;
		String id = req.getParameter("id");
		if (id == null)
			return;
		
		PrintWriter pw = resp.getWriter();
		IConnection conn = null;
		LogInfo logInfo = null;
		try {
			conn = ConnectionFactory.getInstance().createConnection(getServletContext());
			
			if (!conn.checkSessionId(SID, true, req.getLocalAddr(), req.getLocalName(), req.getLocalPort()))
				throw new FunctionException(201, "Session not exist");
			
			SessionInfo SIDInfo = conn.getUserInfo(SID);
			logInfo = new LogInfo(SIDInfo.getUserRowId(), SIDInfo.getUserName(), SID, "AutoPage");
			
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

			String reply = getPage(conn, logInfo, SID, RowId, id);
			
			logInfo.setMessage(reply);
			logInfo.setVerbose(LogInfo.LOG_TRACE);
			conn.log(logInfo);
			
			pw.write(reply);
		}
		catch (FunctionException E) {
			String msg = AjaxMessage.parseError(E.getErrorCode(), E, true);
			if (conn != null && logInfo != null) {
				logInfo.setVerbose(LogInfo.LOG_ERROR);
				logInfo.setMessage(msg);
				conn.log(logInfo);
			}
			pw.write(msg);
		}
		catch (Exception E) {
			String msg = AjaxMessage.parseError(200, E, true);
			if (conn != null && logInfo != null) {
				logInfo.setVerbose(LogInfo.LOG_ERROR);
				logInfo.setMessage(msg);
				conn.log(logInfo);
			}
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
	
	private String getPage(IConnection conn, LogInfo logInfo, String SID, String RowId, String pageId) throws Exception {
		if (!conn.checkMenuAuthorize(SID, Integer.parseInt(RowId)))
			throw new FunctionException(202, "You are not authorized to view this page");
		
		AutoPageInfo page = conn.autoPageGetDetail(pageId);
		String pageType = page.getPageType();
		if (pageType.equals("SQL") || pageType.equals("SP") || pageType.equals("SQL_RPT")) {
			return this.getPageSQL(conn, logInfo, SID, RowId, pageId, page);
		}
		else if (pageType.equals("SQL_DS") || pageType.equals("SP_DS")) {
			return this.getPageSQLDS(conn, logInfo, SID, RowId, pageId, page);
		}
		else {
			throw new Exception("Page type [" + pageType + "] not supported");
		}
	}
	
	private String getPageSQL(IConnection conn, LogInfo logInfo, String SID, String RowId, String pageId, AutoPageInfo page) throws Exception {
		com.lionpig.webui.http.func.AutoPage p = new com.lionpig.webui.http.func.AutoPage();
		Hashtable<String, String> pHt = new Hashtable<String, String>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("<table class=\"ui-widget ui-widget-content\" id=\"tableSelection\" pageid=\"" + pageId + "\"><thead><tr class=\"ui-widget-header\"><td colspan=5><input type=\"button\" value=\"Hide\" id=\"btnToggleView\"/><input type=\"button\" value=\"Query\" id=\"btnQuery\"/><input type=\"button\" value=\"Export\" id=\"btnExport\"/></td></tr></thead><tbody>\n");
		
		List<SqlInputInfo> listInput = conn.autoPageGetSqlInputs(pageId);
		String inputType;
		for (SqlInputInfo info : listInput) {
			if (info.isOptional())
				sb.append("<tr class=\"optional\">");
			else
				sb.append("<tr class=\"mustbe\">");
			sb.append("<td class=\"auto-page-label\" nowrap align=\"right\">").append(info.getInputName()).append("</td>")
			.append("<td nowrap align=\"center\" style=\"font-weight: normal\">").append(info.getInputOp()).append("</td>");
			
			inputType = info.getInputType();
			sb.append("<td class=\"auto-page-input\" nowrap>");
			if (inputType.equals("text")) {
				sb.append("<input class=\"input input-type-text\" id=\"").append(info.getInputName()).append("\"");
				if (info.getInputDefaultValue() != null) {
					pHt.put("Action", "SingleValue");
					pHt.put("PageId", info.getInputDefaultValue());
					String v = p.execute(pHt, conn, logInfo);
					if (v != null) {
						sb.append(" value=\"").append(v).append("\"");
					}
				}
				sb.append("/>");
			}
			else if (inputType.equals("datetime")) {
				sb.append("<div class=\"input input-type-datetime\" id=\"").append(info.getInputName()).append("\"");
				if (info.getInputDefaultValue() != null) {
					pHt.put("Action", "SingleValue");
					pHt.put("PageId", info.getInputDefaultValue());
					String v = p.execute(pHt, conn, logInfo);
					if (v != null) {
						sb.append(" value=\"").append(v).append("\"");
					}
				}
				sb.append("></div>");
			}
			else if (inputType.equals("date")) {
				sb.append("<div class=\"input input-type-date\" id=\"").append(info.getInputName()).append("\"");
				if (info.getInputDefaultValue() != null) {
					pHt.put("Action", "SingleValue");
					pHt.put("PageId", info.getInputDefaultValue());
					String v = p.execute(pHt, conn, logInfo);
					if (v != null) {
						sb.append(" value=\"").append(v).append("\"");
					}
				}
				sb.append("></div>");
			}
			else if (inputType.equals("list")) {
				sb.append("<div class=\"input input-type-list\" id=\"").append(info.getInputName()).append("\"></div>");
			}
			else if (inputType.equals("combobox")) {
				pHt.put("Action", "SelectionList");
				pHt.put("PageId", pageId);
				pHt.put("InputName", info.getInputName());
				String comboBoxOptions = p.execute(pHt, conn, logInfo);
				sb.append("<select class=\"input input-type-combobox\" id=\"").append(info.getInputName()).append("\">").append(comboBoxOptions).append("</select>");
			}
			else {
				throw new Exception("Unknown input type [" + inputType + "]");
			}
			sb.append("</td><td nowrap style=\"font-weight: normal\">");
			if (info.getInputOp().equals("LIKE"))
				sb.append("ESCAPE '\\' ");
			sb.append(info.getInputLogic() == null ? "&nbsp;" : info.getInputLogic()).append("</td>");
			sb.append("<td width=100%>&nbsp;</td>");
			sb.append("</tr>\n");
		}
		sb.append("</tbody></table>\n");
		
		sb.append("<table id=\"result\"></table>");
		
		FileInputStream in = new FileInputStream(getServletContext().getRealPath("tool") + File.separator + "auto-page.html");
		try {
			byte[] buffer = new byte[in.available()];
			in.read(buffer);
			sb.append(new String(buffer, "big5"));
		}
		finally {
			in.close();
		}
		
		return sb.toString();
	}
	
	private String getPageSQLDS(IConnection conn, LogInfo logInfo, String SID, String RowId, String pageId, AutoPageInfo page) throws Exception {
		com.lionpig.webui.http.func.AutoPage p = new com.lionpig.webui.http.func.AutoPage();
		Hashtable<String, String> pHt = new Hashtable<String, String>();
		List<SqlInputInfo> listSqlInputInfos = conn.autoPageGetSqlInputs(pageId);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<table id=\"ds-table\" totalstep=\"").append(listSqlInputInfos.size()).append("\" pageid=\"").append(pageId).append("\" >\n")
		.append("<tr><td colspan=3>").append(conn.getMenu(SID, Integer.parseInt(RowId)).getTitle()).append("</td></tr>\n");
		
		SqlInputInfo info;
		String inputType;
		String component;
		for (int i = 0; i < listSqlInputInfos.size(); i++) {
			info = listSqlInputInfos.get(i);
			inputType = info.getInputType();

			if (inputType.equals("text")) {
				component = "<input class=\"component text\" id=\"input-" + i + "\" name=\"" + info.getInputName() + "\" />";
			}
			else if (inputType.equals("datetime")) {
				component = "<div class=\"component datetime\" id=\"input-" + i + "\" name=\"" + info.getInputName() + "\" ></div>";
			}
			else if (inputType.equals("date")) {
				String v = null;
				if (info.getInputDefaultValue() != null) {
					pHt.put("Action", "SingleValue");
					pHt.put("PageId", info.getInputDefaultValue());
					v = p.execute(pHt, conn, logInfo);
				}
				if (v == null)
					component = "<div class=\"component date\" id=\"input-" + i + "\" name=\"" + info.getInputName() + "\" ></div>";
				else
					component = "<div class=\"component date\" id=\"input-" + i + "\" name=\"" + info.getInputName() + "\" value=\"" + v + "\" ></div>";
			}
			else if (inputType.equals("list")) {
				component = "<div class=\"component list\" id=\"input-" + i + "\" name=\"" + info.getInputName() + "\" ></div>";
			}
			else if (inputType.equals("combobox")) {
				component = "<select class=\"component combobox\" id=\"input-" + i + "\" name=\"" + info.getInputName() + "\" ></select>";
			}
			else {
				throw new Exception("Unknown input type [" + inputType + "]");
			}
			
			sb.append("<tr>\n")
			.append(String.format("<td><input class=\"component button step\" type=\"button\" value=\"%1$s.\" step=\"%2$s\" id=\"goto-%2$s\" disabled/></td>\n", (i+1), i))
			.append("<td nowrap>").append(info.getInputName()).append("</td>\n")
			.append(String.format("<td><div id=\"area-%1$s\">%2$s\n<hr/>\n<input class=\"component button ok\" type=\"button\" value=\"Ok\" step=\"%1$s\"/><input class=\"component button cancel\" type=\"button\" value=\"Cancel\" step=\"%1$s\"/></div>\n", i, component))
			.append("<div class=\"show\" id=\"show-").append(i).append("\"></div></td>")
			.append("</tr>");
		}
		
		sb.append("<tr><td colspan=\"3\"><input type=\"button\" value=\"Query\" id=\"btnQuery\"/></td></tr>\n")
		.append("</table>");
		
		FileInputStream in = new FileInputStream(getServletContext().getRealPath("tool") + File.separator + "auto-page-ds.html");
		try {
			return IOUtils.toString(in, "big5").replaceAll("%auto-page%", sb.toString());
		}
		finally {
			in.close();
		}
	}
}
