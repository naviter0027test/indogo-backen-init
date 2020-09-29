package com.lionpig.webui.http.func;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oracle.jdbc.driver.OracleCallableStatement;
import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.http.FunctionException;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.func.autopage.AutoPageResultOption;
import com.lionpig.webui.http.func.autopage.AutoPageResultSingle;
import com.lionpig.webui.http.func.autopage.AutoPageResultTable;
import com.lionpig.webui.http.func.autopage.IAutoPageResult;
import com.lionpig.webui.http.struct.AutoPageInfo;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.struct.LogInfo;
import com.lionpig.webui.http.struct.SpParamInfo;
import com.lionpig.webui.http.struct.SqlInputInfo;
import com.lionpig.webui.http.struct.SqlOutputInfo;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageResult;
import com.lionpig.webui.http.util.AjaxMessage;
import com.lionpig.webui.http.util.DateFormat;

public class AutoPage implements IFunction {
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		IConnection conn = fi.getConnection();
		Hashtable<String, String> params = fi.getRequestParameters();
		LogInfo log = fi.getLogInfo();
		
		String action = params.get("Action");
		if (action == null)
			throw new Exception("Action cannot be null");
		String pageId = params.get("PageId");
		if (pageId == null)
			throw new Exception("PageId cannot be null");
		AutoPageInfo page = conn.autoPageGetDetail(pageId);
		
		if (action.equals("Query")) {
			List<SqlOutputInfo> listSqlOutputInfo = conn.autoPageGetSqlOutputs(pageId);
			AutoPageResultTable autoPageResult = new AutoPageResultTable(fi, pageId, listSqlOutputInfo);
			autoPageResult.lock();
			try {
				return this.query(conn, log, params, page, autoPageResult, null);
			}
			finally {
				autoPageResult.unlock();
			}
		}
		else if (action.equals("QueryOption")) {
			IAutoPageResult autoPageResult = new AutoPageResultOption();
			return this.query(conn, log, params, page, autoPageResult, null);
		}
		else if (action.equals("SelectionList")) {
			String inputName = params.get("InputName");
			if (inputName == null)
				throw new Exception("InputName cannot be null");
			
			SqlInputInfo in = conn.autoPageGetSqlInput(pageId, inputName);
			if (in == null)
				throw new Exception("Cannot found input name [" + inputName + "]");
			
			String sourceId = in.getInputListSource();
			if (sourceId == null)
				throw new Exception("INPUT_LIST_SOURCE cannot be null for input name [" + inputName + "]");
			
			AutoPageInfo source = conn.autoPageGetDetail(sourceId);
			return this.query(conn, log, params, source, new AutoPageResultOption(), pageId);
		}
		else if (action.equals("col")) {
			List<SqlOutputInfo> cols = conn.autoPageGetSqlOutputs(pageId);
			StringBuilder sb = new StringBuilder();
			sb.append("<tr>");
			String tmp = "<td nowrap CAN_FILTER=\"true\" COLUMN_NAME=\"%1$s\" DATA_TYPE=\"%3$s\" IS_EDITABLE=\"false\" DIRECTION=\"default\">%2$s</td>";
			String outputType;
			int iItem = 1;
			int iDate = 1;
			int iNum = 1;
			for (SqlOutputInfo col : cols) {
				outputType = col.getOutputType();
				if (outputType.equals("text")) {
					sb.append(String.format(tmp, "ITEM" + iItem, col.getOutputName(), "STRING"));
					iItem++;
				}
				else if (outputType.equals("date")) {
					sb.append(String.format(tmp, "DATE" + iDate, col.getOutputName(), "DATE"));
					iDate++;
				}
				else if (outputType.equals("datetime")) {
					sb.append(String.format(tmp, "DATE" + iDate, col.getOutputName(), "DATETIME"));
					iDate++;
				}
				else if (outputType.equals("number")) {
					sb.append(String.format(tmp, "NUM" + iNum, col.getOutputName(), "NUMBER"));
					iNum++;
				}
			}
			sb.append("</tr>");
			return sb.toString();
		}
		else if (action.equals("row")) {
			int pageNo = Integer.parseInt(params.get("Page"));
			int offset = Integer.parseInt(params.get("Offset"));
			
			List<TablePageFilter> filter = TablePage.parseFilter(params.get("Filter"), params.get("FilterSql"), params.get("FilterSqlValues"));
			filter.add(new TablePageFilter("PAGE_ID", "STRING", "=", pageId, null));
			filter.add(new TablePageFilter("SESSION_ID", "STRING", "=", fi.getSID(), null));
			
			TablePageResult result = conn.tablePageBegin(
					null,
					"SQL_TEMP_TABLE",
					pageNo,
					offset,
					TablePage.parseColumn(params.get("Column")),
					TablePage.parseSort(params.get("Sort")),
					filter,
					null, null, null);
			ResultSet r = result.getResultSet();
			try {
				int maxRow = 0;
				String rowCount = conn.getParamValue(fi.getSID(), "row-count");
				if (rowCount != null)
					maxRow = Integer.parseInt(rowCount);
				
				if (params.get("Filter").length() > 0)
					maxRow = 0;
				
				ResultSetMetaData meta = r.getMetaData();
				StringBuilder sb = new StringBuilder();
				DateFormat df = DateFormat.getInstance();
				String val;
				int type;
				while (r.next()) {
					sb.append("<tr maxrow=\"").append(maxRow).append("\">");
					for (int i = 1; i <= meta.getColumnCount(); i++) {
						type = meta.getColumnType(i);
						if (type == Types.TIMESTAMP || type == Types.DATE || type == Types.TIME) {
							val = df.format(r.getTimestamp(i));
						}
						else {
							val = r.getString(i);
						}
						sb.append("<td nowrap>");
						sb.append(val == null ? "" : AjaxMessage.TextToHTML(val));
						sb.append("</td>");
					}
					sb.append("</tr>");
				}
				return sb.toString();
			}
			finally {
				r.close();
				conn.tablePageEnd();
			}
		}
		else if (action.equals("Export")) {
			String type = params.get("Type");
			if (type == null)
				throw new Exception("Type cannot be null");
			String useFilter = params.get("UseFilter");
			if (useFilter == null)
				useFilter = "0";
			return this.export(conn, page, fi.getSID(), fi.getTempFolder(), type, useFilter.equals("1"));
		}
		else if (action.equals("Chart")) {
			return this.chart(fi, page);
		}
		else if (action.equals("ChartSelection")) {
			String chartType = params.get("Type");
			if (chartType == null)
				throw new Exception("Type cannot be null");
			
			if (chartType.equals("Line")) {
				FileInputStream in = new FileInputStream(fi.getServletContext().getRealPath("tool/chart-line.html"));
				try {
					return IOUtils.toString(in, "UTF-8");
				}
				finally {
					in.close();
				}
			}
			else {
				throw new Exception("Chart type [" + chartType + "] currently not supported");
			}
		}
		else {
			throw new Exception("Unknown action [" + action + "]");
		}
	}
	
	public String execute(Hashtable<String, String> params, IConnection conn, LogInfo log) throws Exception {
		String action = params.get("Action");
		if (action == null)
			throw new Exception("Action cannot be null");
		String pageId = params.get("PageId");
		if (pageId == null)
			throw new Exception("PageId cannot be null");
		AutoPageInfo page = conn.autoPageGetDetail(pageId);
		
		if (action.equals("SingleValue")) {
			return this.query(conn, log, params, page, new AutoPageResultSingle(), null);
		}
		else if (action.equals("SelectionList")) {
			String inputName = params.get("InputName");
			if (inputName == null)
				throw new Exception("InputName cannot be null");
			
			SqlInputInfo in = conn.autoPageGetSqlInput(pageId, inputName);
			if (in == null)
				throw new Exception("Cannot found input name [" + inputName + "]");
			
			String sourceId = in.getInputListSource();
			if (sourceId == null)
				throw new Exception("INPUT_LIST_SOURCE cannot be null for input name [" + inputName + "]");
			
			AutoPageInfo source = conn.autoPageGetDetail(sourceId);

			return this.query(conn, log, params, source, new AutoPageResultOption(), pageId);
		}
		else {
			throw new Exception("Unknown action [" + action + "] for internal function");
		}
	}
	
	private String query(IConnection conn, LogInfo log, Hashtable<String, String> inputs, AutoPageInfo page, IAutoPageResult resultEngine, String parentPageId) throws Exception {
		String pageType = page.getPageType();
		boolean isStoredProcedure;
		if (pageType.equals("SQL") || pageType.equals("SQL_DS") || pageType.equals("SQL_RPT"))
			isStoredProcedure = false;
		else if (pageType.equals("SP") || pageType.equals("SP_DS"))
			isStoredProcedure = true;
		else
			throw new Exception("Page type [" + pageType + "] not yet supported");
		
		List<Object> listInputValue = new ArrayList<Object>();
		List<Integer> listOutputSqlTypes = new ArrayList<Integer>();
		List<String> listOutputParamTypes = new ArrayList<String>();
		String cmdText;
		if (isStoredProcedure) {
			List<SpParamInfo> listSpParamInfo = conn.autoPageGetSpParams(page.getPageId());
			if (listSpParamInfo.size() == 0)
				cmdText = "{call " + page.getCmdText() + "}";
			else
				cmdText = "{call " + this.prepareSP(page.getCmdText(), listSpParamInfo, inputs, listInputValue, listOutputSqlTypes, listOutputParamTypes) + "}";
		}
		else {
			List<SqlInputInfo> listSqlInputInfo;
			if (parentPageId != null)
				listSqlInputInfo = conn.autoPageGetSqlInputs(parentPageId);
			else
				listSqlInputInfo = conn.autoPageGetSqlInputs(page.getPageId());
			
			if (listSqlInputInfo.size() == 0)
				cmdText = page.getCmdText();
			else if (pageType.equals("SQL_RPT"))
				cmdText = this.prepareSQLRPT(page.getCmdText(), listSqlInputInfo, inputs);
			else
				cmdText = this.prepareSQL(page.getCmdText(), listSqlInputInfo, inputs, listInputValue);
		}
		
		log.setVerbose(LogInfo.LOG_DEBUG);
		log.setMessage(cmdText);
		conn.log(log);
		
		Connection db;
		if (page.getDbName() != null) {
			db = conn.getConnection(page.getDbName());
		}
		else {
			db = conn.getConnection();
		}
		
		if (isStoredProcedure) {
			CallableStatement cstmt = db.prepareCall(cmdText);
			try {
				int paramIndex = 1;
				List<Integer> listCursorIndex = new ArrayList<Integer>();
				for (int i = 0; i < listInputValue.size(); i++) {
					Object o = listInputValue.get(i);
					if (o instanceof String)
						cstmt.setString(paramIndex, (String)o);
					else if (o instanceof Date)
						cstmt.setTimestamp(paramIndex, new Timestamp(((Date)o).getTime()));
					else if (o instanceof Double)
						cstmt.setDouble(paramIndex, (Double)o);
					else
						cstmt.setString(paramIndex, o.toString());
					paramIndex++;
				}
				for (int i = 0; i < listOutputSqlTypes.size(); i++) {
					cstmt.registerOutParameter(paramIndex, listOutputSqlTypes.get(i));
					if (listOutputParamTypes.get(i).equals("TABLE"))
						listCursorIndex.add(paramIndex);
					paramIndex++;
				}
				
				if (listCursorIndex.size() == 0)
					throw new Exception("At least one param type TABLE needed in page [" + page.getPageId() + "]");
				
				log.setVerbose(LogInfo.LOG_DEBUG);
				log.setMessage("Query Start [" + page.getPageId() + "]");
				conn.log(log);
				
				cstmt.execute();
				
				log.setVerbose(LogInfo.LOG_DEBUG);
				log.setMessage("Query End [" + page.getPageId() + "]");
				conn.log(log);
				
				OracleCallableStatement cstmtOracle;
				if (cstmt instanceof OracleCallableStatement)
					cstmtOracle = (OracleCallableStatement) cstmt;
				else
					throw new Exception("Type of CallableStatement is not yet supported in page [" + page.getPageId() + "]");
				
				ResultSet r = cstmtOracle.getCursor(listCursorIndex.get(0));
				try {
					return resultEngine.execute(r);
				}
				finally {
					r.close();
				}
			}
			finally {
				try {
					cstmt.close();
				}
				catch (Exception ignore) {}
			}
		}
		else {
			PreparedStatement pstmt = db.prepareStatement(cmdText);
			try {
				for (int i = 0; i < listInputValue.size(); i++) {
					Object o = listInputValue.get(i);
					if (o instanceof String)
						pstmt.setString(i+1, (String)o);
					else if (o instanceof Double)
						pstmt.setDouble(i+1, (Double)o);
					else if (o instanceof Date)
						pstmt.setTimestamp(i+1, new Timestamp(((Date)o).getTime()));
					else
						pstmt.setString(i+1, o.toString());
				}
				
				log.setVerbose(LogInfo.LOG_DEBUG);
				log.setMessage("Query Start [" + page.getPageId() + "]");
				conn.log(log);
				
				ResultSet r = pstmt.executeQuery();
				try {
					log.setVerbose(LogInfo.LOG_DEBUG);
					log.setMessage("Query End [" + page.getPageId() + "]");
					conn.log(log);
					
					return resultEngine.execute(r);
				}
				finally {
					r.close();
				}
			}
			finally {
				try {
					pstmt.close();
				}
				catch (Exception ignore) {}
			}
		}
	}
	
	private String prepareSP(String cmdText, List<SpParamInfo> listSpParamInfo, Hashtable<String, String> inputs, List<Object> outListInputValue, List<Integer> outListOutputSqlTypes, List<String> outListOutputParamTypes) throws Exception {
		SpParamInfo sp;
		String paramType;
		String paramDirection;
		String paramName;
		DateFormat df = DateFormat.getInstance();
		for (int i = 0; i < listSpParamInfo.size(); i++) {
			sp = listSpParamInfo.get(i);
			paramType = sp.getParamType();
			paramDirection = sp.getParamDirection();
			paramName = "\"" + sp.getParamName() + "\"";
			if (cmdText.indexOf(paramName) < 0)
				throw new Exception("Cannot find parameter name [" + sp.getParamName() + "]");
			
			if (paramDirection.equals("IN")) {
				if (paramType.equals("STRING")) {
					outListInputValue.add(inputs.get(sp.getParamName()));
				}
				else if (paramType.equals("DATE")) {
					outListInputValue.add(df.parse(inputs.get(sp.getParamName())));
				}
				else if (paramType.equals("DATETIME")) {
					outListInputValue.add(df.parse(inputs.get(sp.getParamName())));
				}
				else if (paramType.equals("NUMBER")) {
					outListInputValue.add(Double.parseDouble(inputs.get(sp.getParamName())));
				}
				else {
					throw new Exception("Param Type [" + paramType + "] not supported for Param Direction [IN]");
				}
				
				cmdText = cmdText.replaceFirst(paramName, "?");
			}
			else if (paramDirection.equals("OUT")) {
				cmdText = cmdText.replaceFirst(paramName, "?");
				
				if (paramType.equals("TABLE")) {
					outListOutputSqlTypes.add(OracleTypes.CURSOR);
				}
				else if (paramType.equals("NUMBER")) {
					outListOutputSqlTypes.add(Types.NUMERIC);
				}
				else if (paramType.equals("STRING")) {
					outListOutputSqlTypes.add(Types.VARCHAR);
				}
				else {
					throw new Exception("Param Type [" + paramType + "] not supported for Param Direction [OUT]");
				}
				
				outListOutputParamTypes.add(paramType);
			}
		}
		
		return cmdText;
	}
	
	private String prepareSQLRPT(String cmdText, List<SqlInputInfo> pageInputs, Hashtable<String, String> inputs) throws Exception {
		String id;
		String type, op, val;
		
		Hashtable<Integer, SqlInputInfo> ht = new Hashtable<Integer, SqlInputInfo>();
		int index = 0;
		for (SqlInputInfo info : pageInputs) {
			id = "\"" + info.getInputName() + "\"";
			while ((index = cmdText.indexOf(id, index)) > -1) {
				ht.put(index, info);
				index++;
			}
		}
		int[] indexes = new int[ht.size()];
		Enumeration<Integer> e = ht.keys();
		index = 0;
		while (e.hasMoreElements()) {
			indexes[index] = e.nextElement();
			index++;
		}
		Arrays.sort(indexes);
		
		for (int i : indexes) {
			SqlInputInfo info = ht.get(i);
			id = "\"" + info.getInputName() + "\"";
			val = inputs.get(info.getInputName());
			type = info.getInputType();
			op = info.getInputOp();
			if (type.equals("list"))
				op = "IN";

			if (val == null || val.length() == 0) {
				if (info.isOptional()) {
					if (info.getInputOptional().equals("T"))
						cmdText = cmdText.replaceFirst(id, "1");
					else if (info.getInputOptional().equals("F"))
						cmdText = cmdText.replaceFirst(id, "0");
					else
						throw new Exception("Input [" + info.getInputName() + "] is an optional, but the value type [" + info.getInputOptional() + "] currently not supported");
				}
				else {
					throw new FunctionException(2000, "Value of [" + info.getInputName() + "] must be provided");
				}
			}
			else {
				if (op.equals("IN")) {
					if (info.getInputCustomFlag() == 1) {
						cmdText = cmdText.replaceFirst(id, "'" + val + "'");
					}
					else {
						String[] tokens = val.split((char)31 + "");
						if (tokens.length > 0) {
							StringBuilder sb = new StringBuilder();
							for (String token : tokens) {
								sb.append("'").append(token).append("',");
							}
							sb = sb.delete(sb.length() - 1, sb.length());
							cmdText = cmdText.replaceFirst(id, sb.toString());
						}
						else {
							throw new Exception("This should not happens for input [" + info.getInputName() + "]");
						}
					}
				}
				else {
					cmdText = cmdText.replaceFirst(id, "'" + val + "'");
				}
			}
		}
		
		return cmdText;
	}
	
	private String prepareSQL(String cmdText, List<SqlInputInfo> pageInputs, Hashtable<String, String> inputs, List<Object> outListInputValue) throws Exception {
		String id;
		String type, op, val;
		
		Hashtable<Integer, SqlInputInfo> ht = new Hashtable<Integer, SqlInputInfo>();
		int index = 0;
		for (SqlInputInfo info : pageInputs) {
			id = "\"" + info.getInputName() + "\"";
			while ((index = cmdText.indexOf(id, index)) > -1) {
				ht.put(index, info);
				index++;
			}
		}
		int[] indexes = new int[ht.size()];
		Enumeration<Integer> e = ht.keys();
		index = 0;
		while (e.hasMoreElements()) {
			indexes[index] = e.nextElement();
			index++;
		}
		Arrays.sort(indexes);
		
		DateFormat df = DateFormat.getInstance();
		
		for (int i : indexes) {
			SqlInputInfo info = ht.get(i);
			id = "\"" + info.getInputName() + "\"";
			val = inputs.get(info.getInputName());
			type = info.getInputType();
			op = info.getInputOp();
			if (type.equals("list"))
				op = "IN";

			if (val == null || val.length() == 0) {
				if (info.isOptional()) {
					if (info.getInputOptional().equals("T"))
						cmdText = cmdText.replaceFirst(id, info.getInputId() + " = " + info.getInputId());
					else if (info.getInputOptional().equals("F"))
						cmdText = cmdText.replaceFirst(id, info.getInputId() + " = ''");
					else
						throw new Exception("Input [" + info.getInputName() + "] is an optional, but the value type [" + info.getInputOptional() + "] currently not supported");
				}
				else {
					throw new FunctionException(2000, "Value of [" + info.getInputName() + "] must be provided");
				}
			}
			else {
				if (op.equals("IN")) {
					String[] tokens = val.split((char)31 + "");
					if (tokens.length > 0) {
						StringBuilder sb = new StringBuilder();
						sb.append(info.getInputId()).append(" IN (");
						for (String token : tokens) {
							sb.append("?,");
							outListInputValue.add(token);
						}
						sb = sb.replace(sb.length() - 1, sb.length(), ")");
						cmdText = cmdText.replaceFirst(id, sb.toString());
					}
					else {
						throw new Exception("This should not happens for input [" + info.getInputName() + "]");
					}
				}
				else {
					String replacement = info.getInputId() + " " + op + " ?";
					
					if (type.equals("date")) {
						outListInputValue.add(df.parse(val));
					}
					else if (type.equals("datetime")) {
						outListInputValue.add(df.parse(val));
					}
					else if (type.equals("text") || type.equals("combobox")) {
						outListInputValue.add(val);
					}
					else {
						throw new Exception("Input [" + info.getInputName() + "] type [" + type + "] currently not supported");
					}
					
					if (op.equals("LIKE")) {
						replacement += " ESCAPE '\\'";
					}
					
					cmdText = cmdText.replaceFirst(id, replacement);
				}
			}
		}
		
		return cmdText;
	}
	
	private String export(IConnection conn, AutoPageInfo page, String SID, File tempFolder, String type, boolean useFilter) throws Exception {
		String pageId = page.getPageId();
		List<SqlOutputInfo> cols = conn.autoPageGetSqlOutputs(pageId);
		if (cols.size() == 0)
			throw new Exception("Cannot find output column name definition for page [" + pageId + "]");
		String outputType;
		StringBuilder sb = new StringBuilder();
		int iItem = 1, iDate = 1, iNum = 1;
		for (SqlOutputInfo col : cols) {
			outputType = col.getOutputType();
			if (outputType.equals("text")) {
				sb.append("ITEM").append(iItem).append(",");
				iItem++;
			}
			else if (outputType.equals("date") || outputType.equals("datetime")) {
				sb.append("DATE").append(iDate).append(",");
				iDate++;
			}
			else if (outputType.equals("number")) {
				sb.append("NUM").append(iNum).append(",");
				iNum++;
			}
			else {
				throw new Exception("Output Type [" + outputType + "] currently not supported for excel");
			}
		}
		String s = sb.deleteCharAt(sb.length() - 1).toString();
		
		String filter;
		if (useFilter) {
			filter = conn.getParamValue(SID, "filter-string");
			if (filter == null)
				filter = "";
			else
				filter = " AND " + filter;
		}
		else {
			filter = "";
		}
		
		PreparedStatement pstmt = conn.getConnection().prepareStatement("SELECT " + s + " FROM SQL_TEMP_TABLE WHERE SESSION_ID = ? AND PAGE_ID = ?" + filter);
		try {
			pstmt.setString(1, SID);
			pstmt.setString(2, pageId);
			ResultSet r = pstmt.executeQuery();
			if (type.equals("Excel")) {
				File file = new File(tempFolder, "export.xls");
				this.excel(file, cols, r);
				return "export.xls";
			}
			else if (type.equals("CSV")) {
				File file = new File(tempFolder, "export.csv");
				FileOutputStream fos = new FileOutputStream(file, false);
				try {
					PrintWriter pw = new PrintWriter(fos);
					try {
						pw.print(cols.get(0).getOutputName());
						for (int i = 1; i < cols.size(); i++)
							pw.print("," + cols.get(i).getOutputName());
						pw.println();

						SimpleDateFormat formatDateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd");
						String val;
						while (r.next()) {
							outputType = cols.get(0).getOutputType();
							if (outputType.equals("text") || outputType.equals("number"))
								val = r.getString(1) == null ? "" : r.getString(1);
							else if (outputType.equals("date"))
								val = r.getDate(1) == null ? "" : formatDate.format(r.getDate(1));
							else if (outputType.equals("datetime"))
								val = r.getTimestamp(1) == null ? "" : formatDateTime.format(r.getTimestamp(1));
							else
								throw new Exception("Output Type [" + outputType + "] currently not supported for CSV data");
							pw.print(val);
							
							for (int i = 1; i < cols.size(); i++) {
								outputType = cols.get(i).getOutputType();
								if (outputType.equals("text") || outputType.equals("number"))
									val = r.getString(i+1) == null ? "" : r.getString(i+1);
								else if (outputType.equals("date"))
									val = r.getDate(i+1) == null ? "" : formatDate.format(r.getDate(i+1));
								else if (outputType.equals("datetime"))
									val = r.getTimestamp(i+1) == null ? "" : formatDateTime.format(r.getTimestamp(i+1));
								else
									throw new Exception("Output Type [" + outputType + "] currently not supported for CSV data");
								pw.print("," + val);
							}
							pw.println();
						}
					}
					finally {
						pw.close();
					}
				}
				finally {
					fos.close();
				}
				return "export.csv";
			}
			else {
				throw new Exception("Unknown Export Type [" + type + "]");
			}
		}
		finally {
			pstmt.close();
		}
	}
	
	private void excel(File file, List<SqlOutputInfo> cols, ResultSet r) throws Exception {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");
		int rowIndex = 0;
		Row row = sheet.createRow(rowIndex);
		rowIndex++;
		CellStyle style = wb.createCellStyle();
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Cell cell;
		for (int i = 0; i < cols.size(); i++) {
			cell = row.createCell(i);
			cell.setCellValue(cols.get(i).getOutputName());
			cell.setCellStyle(style);
		}
		sheet.createFreezePane(0, 1, 0, 1);
		
		String val, outputType;
		SimpleDateFormat formatDateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd");
		while (r.next()) {
			if (rowIndex > 65000)
				break;
			row = sheet.createRow(rowIndex);
			rowIndex++;
			for (int i = 1; i <= cols.size(); i++) {
				outputType = cols.get(i-1).getOutputType();
				if (outputType.equals("number")) {
					val = r.getString(i);
					if (val == null)
						row.createCell(i-1);
					else
						row.createCell(i-1).setCellValue(Double.parseDouble(val));
				}
				else {
					if (outputType.equals("text"))
						val = r.getString(i) == null ? "" : r.getString(i);
					else if (outputType.equals("date"))
						val = r.getDate(i) == null ? "" : formatDate.format(r.getDate(i));
					else if (outputType.equals("datetime"))
						val = r.getTimestamp(i) == null ? "" : formatDateTime.format(r.getTimestamp(i));
					else
						throw new Exception("Output Type [" + outputType + "] currently not supported for excel data");
					row.createCell(i-1).setCellValue(val);
				}
			}
		}
		
		try {
			for (int i = 0; i < cols.size(); i++) {
				sheet.autoSizeColumn(i);
			}
		}
		catch (Exception ignore) {}
		
		FileOutputStream fos = new FileOutputStream(file, false);
		try {
			wb.write(fos);
		}
		finally {
			fos.close();
		}
	}
	
	private String chart(FunctionItem fi, AutoPageInfo page) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String x = params.get("X");
		String y = params.get("Y");
		String category = params.get("Category");
		String title = params.get("Title");
		String col1, col2;
		
		if (x.startsWith("DATE"))
			col1 = "TO_CHAR(" + x + ", 'YYYY/MM/DD HH24:MI:SS')";
		else if (x.startsWith("NUM"))
			col1 = "TO_CHAR(" + x + ")";
		else
			col1 = x;
		
		col2 = y;
		
		String filename = UUID.randomUUID().toString().replaceAll("-", "") + ".csv";
		PrintWriter pw = new PrintWriter(new FileOutputStream(new File(fi.getTempFolder(), filename), false));
		try {
			List<SqlOutputInfo> listSqlOutputInfo = fi.getConnection().autoPageGetSqlOutputs(page.getPageId());
			String titleX = "", titleY = "";
			String outputType;
			int iItem = 1;
			int iDate = 1;
			int iNum = 1;
			for (SqlOutputInfo col : listSqlOutputInfo) {
				outputType = col.getOutputType();
				if (outputType.equals("text")) {
					if (x.equals("ITEM" + iItem))
						titleX = col.getOutputName();
					if (y.equals("ITEM" + iItem))
						titleY = col.getOutputName();
					iItem++;
				}
				else if (outputType.equals("date") || outputType.equals("datetime")) {
					if (x.equals("DATE" + iDate))
						titleX = col.getOutputName();
					if (y.equals("DATE" + iDate))
						titleY = col.getOutputName();
					iDate++;
				}
				else if (outputType.equals("number")) {
					if (x.equals("NUM" + iNum))
						titleX = col.getOutputName();
					if (y.equals("NUM" + iNum))
						titleY = col.getOutputName();
					iNum++;
				}
			}
			
			pw.println(titleY + "," + category + "," + titleX);
			
			Connection conn = fi.getConnection().getConnection();
			
			String cmd;
			if (col1.equals(category))
				cmd = "SELECT " + col1 + " AS C1, " + col2 + " AS C2, ' ' AS C3 FROM SQL_TEMP_TABLE WHERE SESSION_ID = ? AND PAGE_ID = ? GROUP BY " + col1 + " ORDER BY " + col1;
			else
				cmd = "SELECT " + col1 + " AS C1, " + col2 + " AS C2, " + category + " AS C3 FROM SQL_TEMP_TABLE WHERE SESSION_ID = ? AND PAGE_ID = ? GROUP BY " + col1 + ", " + category + " ORDER BY " + col1 + ", " + category;
			
			LogInfo log = fi.getLogInfo();
			log.setMessage(cmd);
			log.setVerbose(LogInfo.LOG_DEBUG);
			fi.getConnection().log(log);
			
			PreparedStatement pstmt = conn.prepareStatement(cmd);
			try {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, page.getPageId());
				
				ResultSet r = pstmt.executeQuery();
				try {
					while (r.next()) {
						pw.println(r.getDouble(2) + "," + r.getString(3) + "," + r.getString(1));
					}
				}
				finally {
					r.close();
				}
			}
			finally {
				pstmt.close();
			}
		}
		finally {
			pw.flush();
			pw.close();
		}
		return "servlet/Chart?SID=" + fi.getSID() + "&ChartType=LINE&DataSourceFilename=" + filename + "&DataSourceType=DEFAULT&Title=" + title;
	}
}
