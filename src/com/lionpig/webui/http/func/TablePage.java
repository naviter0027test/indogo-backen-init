package com.lionpig.webui.http.func;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.FunctionException;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageResult;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class TablePage implements IFunction {
	private final static String StringColumns = "StringColumns";
	private final static String SummaryColumns = "SummaryColumns";
	private final static String NUMBER = "NUMBER";
	private final static String EXCEL_XLS = "export.xls";
	private final static String COL = "col";
	private final static String ROW = "row";
	private final static String PAGE = "Page";
	private final static String OFFSET = "Offset";
	private final static String SORT = "Sort";
	private final static String FILTER = "Filter";
	private final static String FILTER_SQL = "FilterSql";
	private final static String FILTER_SQL_VALUES = "FilterSqlValues";
	private final static String SESSION_ID = "SessionId";
	private final static String PAGE_ID = "PageId";
	private final static String JOIN_SQL = "JoinSql";
	private final static String TR_CLASS = "<tr class=\"";
	private final static String SPACE = " ";
	private final static String QUOTE_CLOSE = "\">";
	private final static String TR = "<tr>";
	private final static String TD_NOWRAP_CLASS = "<td nowrap class=\"";
	private final static String COL_TYPE = " col_type_";
	private final static String TD_CLOSE = "</td>";
	private final static String TR_CLOSE = "</tr>";
	private final static String QUOTE = "\"";
	private final static String MAXROW = " maxrow=\"";
	private final static String EXPORT = "export";
	private final static String TD_COLUMN_NAME = "<td COLUMN_NAME=\"";
	private final static String DIRECTION = "\" DIRECTION=\"";
	private final static String DATA_TYPE = "\" DATA_TYPE=\"";
	private final static String CAN_FILTER = "\" CAN_FILTER=\"";
	private final static String VIRTUAL = "\" VIRTUAL=\"";
	private final static String TRUE = "true";
	private final static String FALSE = "false";
	private final static String ONE = "1";
	private final static String ZERO = "0";
	private final static String ACTION = "Action";
	private final static String CLASS_NAME = "ClassName";
	private final static String COMMA = ",";
	private final static String CHAR_31 = String.valueOf((char)31);
	private final static String BETWEEN = "BETWEEN";
	private final static String DOLLAR_2_DOLLAR = "$2$";
	private final static String SHEET1 = "Sheet1";
	private final static String HIDDEN = "\" style=\"display: none;";

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = params.get(ACTION);
		String className = params.get(CLASS_NAME);
		@SuppressWarnings("rawtypes")
		Class c = Class.forName(className);
		Object o = c.newInstance();
		ITablePage page;
		if (o instanceof ITablePage) {
			page = (ITablePage)o;
		}
		else
			throw new FunctionException(5001, "ClassName [" + className + "] not implementing ITablePage");
		
		if (action.equals(COL)) {
			List<TablePageColumn> cols = page.getColumns(fi);
			StringBuilder sb = new StringBuilder();
			sb.append(TR);
			for (TablePageColumn col : cols) {
				sb.append(TD_COLUMN_NAME)
				.append(col.getColumnName())
				.append(DIRECTION)
				.append(col.getDirection())
				.append(DATA_TYPE)
				.append(col.getColumnType())
				.append(CAN_FILTER)
				.append(col.getCanFilter() ? TRUE : FALSE)
				.append(VIRTUAL)
				.append(col.isVirtualColumn() ? ONE : ZERO);
				
				if (col.isHidden()) {
					sb.append(HIDDEN);
				}
				
				sb.append(QUOTE_CLOSE)
				.append(col.getDisplayName())
				.append(TD_CLOSE);
			}
			sb.append(TR_CLOSE);
			return sb.toString();
		}
		else if (action.equals(ROW)) {
			int pageNo = Integer.parseInt(params.get(PAGE));
			int offset = Integer.parseInt(params.get(OFFSET));
			//List<TablePageColumn> column = parseColumn(params.get(COLUMN));
			List<TablePageColumn> column = page.getColumns(fi);
			List<TablePageSort> sort = parseSort(params.get(SORT));
			List<TablePageFilter> filter = parseFilter(params.get(FILTER), params.get(FILTER_SQL), params.get(FILTER_SQL_VALUES));
			String sessionId = Helper.getString(params, SESSION_ID, false);
			String pageId = Helper.getString(params, PAGE_ID, false);
			String joinSql = Helper.getString(params, JOIN_SQL, false);
			return this.getRows(page, fi, pageNo, offset, column, sort, filter, sessionId, pageId, joinSql);
		}
		else if (action.equals(EXPORT)) {
			if (!fi.getConnection().isAllowedToExportTablePage(fi.getSessionInfo().getUserRowId(), className)) {
				throw new Exception(fi.getLanguage().not_allowed_to_export_table_page());
			}
			
			String[] summaryColumns = Helper.getStringArray(params, SummaryColumns, false, COMMA);
			String[] stringColumns = Helper.getStringArray(params, StringColumns, false, COMMA);
			
			List<TablePageColumn> columns = page.getColumns(fi);
			List<TablePageSort> sorts = parseSort(params.get(SORT));
			List<TablePageFilter> filters = parseFilter(params.get(FILTER), params.get(FILTER_SQL), params.get(FILTER_SQL_VALUES));
			String sessionId = Helper.getString(params, SESSION_ID, false);
			String pageId = Helper.getString(params, PAGE_ID, false);
			String joinSql = Helper.getString(params, JOIN_SQL, false);
			
			Map<String, TablePageColumn> columnsIndex = new HashMap<>();
			for (TablePageColumn column : columns) {
				columnsIndex.put(column.getColumnName(), column);
			}
			
			for (String columnName : summaryColumns) {
				TablePageColumn column = columnsIndex.get(columnName);
				if (column != null) {
					column.CalculateSummary = true;
				}
			}
			
			for (String columnName : stringColumns) {
				TablePageColumn column = columnsIndex.get(columnName);
				if (column != null) {
					column.NumberAsString = true;
				}
			}
			
			return this.export(page, fi, columns, sorts, filters, sessionId, pageId, joinSql);
		}
		else
			throw new Exception("Unknown action [" + action + "]");
	}
	
	public void printRows(FunctionItem fi, ITablePage page, Hashtable<String, String> params, TablePageRowPrinter rowPrinter) throws Exception {
		List<TablePageColumn> columns = page.getColumns(fi);
		List<TablePageSort> sorts = parseSort(params.get(SORT));
		List<TablePageFilter> filters = parseFilter(params.get(FILTER), params.get(FILTER_SQL), params.get(FILTER_SQL_VALUES));
		String sessionId = Helper.getString(params, SESSION_ID, false);
		String pageId = Helper.getString(params, PAGE_ID, false);
		String joinSql = Helper.getString(params, JOIN_SQL, false);
		
		boolean isCancel = page.populateRowDataBefore(fi, columns, sorts, filters);
		if (isCancel)
			return;
		
		Hashtable<String, TablePageColumn> htColumn = new Hashtable<String, TablePageColumn>();
		for (TablePageColumn col : columns) {
			htColumn.put(col.getColumnName(), col);
		}
		
		try {
			IConnection conn = fi.getConnection();
			TablePageResult result = conn.tablePageBegin(page.getTableOwner(), page.getTableName(), -1, 0, columns, sorts, filters, sessionId, pageId, joinSql);
			try {
				try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(result.getResultSet())) {
					TablePageRowAttribute rowAttr = new TablePageRowAttribute();
					while (r.next()) {
						page.populateRowData(fi, htColumn, r, false, rowAttr);
						rowPrinter.print(htColumn);
					}
				}
			}
			finally {
				try {
					conn.tablePageEnd();
				}
				catch (Exception ignore) {}
			}
		}
		finally {
			page.populateRowDataAfter(fi);
		}
	}
	
	public String getRows(ITablePage page, FunctionItem fi, int pageNo, int offset, List<TablePageColumn> column, List<TablePageSort> sort, List<TablePageFilter> filter, String sessionId, String pageId, String joinSql) throws Exception {
		if (sort == null)
			sort = new ArrayList<TablePageSort>();
		if (filter == null)
			filter = new ArrayList<TablePageFilter>();
		if (column == null)
			column = new ArrayList<TablePageColumn>();
		
		boolean isCancel = page.populateRowDataBefore(fi, column, sort, filter);
		if (isCancel)
			return null;
		
		try {
			boolean isQueryAll = pageNo <= 0;
			
			IConnection conn = fi.getConnection();
			TablePageResult result = conn.tablePageBegin(page.getTableOwner(), page.getTableName(), pageNo, offset, column, sort, filter, sessionId, pageId, joinSql);
			ResultSetWrapperStringify r = new ResultSetWrapperStringify(result.getResultSet());
			int totalRecord = result.getTotalRecord();
			try {
				Hashtable<String, TablePageColumn> htColumn = new Hashtable<String, TablePageColumn>();
				for (TablePageColumn col : column) {
					htColumn.put(col.getColumnName(), col);
				}
				
				StringBuilder sb = new StringBuilder();
				int rowCount = 0;
				TablePageRowAttribute rowAttr = new TablePageRowAttribute();
				while (r.next()) {
					rowCount++;
					rowAttr.Reset();
					page.populateRowData(fi, htColumn, r, true, rowAttr);
					if (rowAttr.HtmlClass.size() > 0) {
						sb.append(TR_CLASS).append(rowAttr.HtmlClass.get(0));
						for (int i = 1; i < rowAttr.HtmlClass.size(); i++) {
							sb.append(SPACE).append(rowAttr.HtmlClass.get(i));
						}
						sb.append(QUOTE_CLOSE);
					}
					else
						sb.append(TR);
					for (TablePageColumn col : column) {
						sb.append(TD_NOWRAP_CLASS).append(col.getColumnName()).append(COL_TYPE).append(col.getColumnType());
						if (col.isHidden())
							sb.append(HIDDEN);
						sb.append(QUOTE_CLOSE).append(col.getValue()).append(TD_CLOSE);
					}
					sb.append(TR_CLOSE);
				}
				
				if (rowCount > 0) {
					StringBuilder maxrow = new StringBuilder();
					maxrow.append(MAXROW);
					if (isQueryAll)
						maxrow.append(rowCount);
					else
						maxrow.append(totalRecord);
					maxrow.append(QUOTE);
					sb.insert(3, maxrow);
				}
				
				return sb.toString();
			}
			finally {
				try {
					r.close();
				}
				catch (Exception ignore) {}
				try {
					conn.tablePageEnd();
				}
				catch (Exception ignore) {}
			}
		}
		finally {
			page.populateRowDataAfter(fi);
		}
	}
	
	public String export(ITablePage page, FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort, List<TablePageFilter> filter, String sessionId, String pageId, String joinSql) throws Exception {
		if (sort == null)
			sort = new ArrayList<TablePageSort>();
		if (filter == null)
			filter = new ArrayList<TablePageFilter>();
		if (column == null)
			column = new ArrayList<TablePageColumn>();
		
		boolean isCancel = page.populateRowDataBefore(fi, column, sort, filter);
		if (isCancel)
			return C.emptyString;
		
		try {
			IConnection conn = fi.getConnection();
			TablePageResult result = conn.tablePageBegin(page.getTableOwner(), page.getTableName(), -1, 0, column, sort, filter, sessionId, pageId, joinSql);
			ResultSetWrapperStringify r = new ResultSetWrapperStringify(result.getResultSet());
			try {
				Hashtable<String, TablePageColumn> htColumn = new Hashtable<String, TablePageColumn>();
				for (TablePageColumn col : column) {
					htColumn.put(col.getColumnName(), col);
				}
				
				File file = new File(fi.getTempFolder(), EXCEL_XLS);
				this.excel(file, column, r, fi, htColumn, page);
				return EXCEL_XLS;
			}
			finally {
				try {
					r.close();
				}
				catch (Exception ignore) {}
				try {
					conn.tablePageEnd();
				}
				catch (Exception ignore) {}
			}
		}
		finally {
			page.populateRowDataAfter(fi);
		}
	}
	
	private void excel(File file, List<TablePageColumn> cols, ResultSetWrapperStringify r, FunctionItem fi, Hashtable<String, TablePageColumn> htColumn, ITablePage page) throws Exception {
		HSSFWorkbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet(SHEET1);
		int rowIndex = 0;
		Row row = sheet.createRow(rowIndex);
		rowIndex++;
		CellStyle style = wb.createCellStyle();
		HSSFFont font = wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Cell cell;
		for (int i = 0, j = 0; i < cols.size(); i++) {
			if (cols.get(i).getDoNotExport())
				continue;
			cell = row.createCell(j);
			cell.setCellValue(cols.get(i).getDisplayName());
			cell.setCellStyle(style);
			j++;
		}
		sheet.createFreezePane(0, 1, 0, 1);
		
		String val;
		TablePageRowAttribute rowAttr = new TablePageRowAttribute();
		while (r.next()) {
			if (rowIndex > 65000)
				break;
			rowAttr.Reset();
			page.populateRowData(fi, htColumn, r, false, rowAttr);
			row = sheet.createRow(rowIndex);
			rowIndex++;
			for (int i = 0, j = 0; i < cols.size(); i++) {
				TablePageColumn col = cols.get(i);
				if (col.getDoNotExport())
					continue;
				val = col.getValue();
				if (col.getColumnType().equals(NUMBER)) {
					try {
						if (col.NumberAsString)
							row.createCell(j).setCellValue(val);
						else
							row.createCell(j).setCellValue(Double.parseDouble(val.replaceAll(COMMA, C.emptyString)));
					}
					catch (Exception e) {
						row.createCell(j).setCellValue(val);
					}
				}
				else
					row.createCell(j).setCellValue(val);
				j++;
			}
		}
		
		row = sheet.createRow(rowIndex);
		rowIndex++;
		style = wb.createCellStyle();
		font = wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setUnderline(HSSFFont.U_SINGLE);
		style.setFont(font);
		for (int i = 0, j = 0; i < cols.size(); i++) {
			if (cols.get(i).getDoNotExport())
				continue;
			
			if (cols.get(i).CalculateSummary) {
				CellReference crStart = new CellReference(1, j);
				CellReference crEnd = new CellReference(rowIndex-2, j);
				
				cell = row.createCell(j);
				cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
				cell.setCellFormula("SUM(" + crStart.formatAsString() + ".." + crEnd.formatAsString() + ")");
				cell.setCellStyle(style);
			}
			
			j++;
		}
		HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
		
		try {
			for (int i = 0, j = 0; i < cols.size(); i++) {
				if (cols.get(i).getDoNotExport())
					continue;
				sheet.autoSizeColumn(j);
				j++;
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
	
	public static List<TablePageColumn> parseColumn(String column) {
		if (column == null || column.length() == 0)
			return new ArrayList<TablePageColumn>();

		String[] tokens = column.split(CHAR_31);
		if (tokens[0].equals(DOLLAR_2_DOLLAR)) {
			// version 2
			ArrayList<TablePageColumn> list = new ArrayList<TablePageColumn>();
			for (int i = 1; i < tokens.length; i += 3) {
				TablePageColumn c = new TablePageColumn(tokens[i], tokens[i+1], tokens[i+2].equals(ONE));
				list.add(c);
			}
			return list;
		}
		else {
			// version 1
			ArrayList<TablePageColumn> list = new ArrayList<TablePageColumn>();
			for (int i = 0; i < tokens.length; i += 2) {
				TablePageColumn c = new TablePageColumn(tokens[i], tokens[i+1], false);
				list.add(c);
			}
			return list;
		}
	}
	
	public static List<TablePageFilter> parseFilter(String filter, String filterSql, String filterSqlValues) {
		ArrayList<TablePageFilter> list = new ArrayList<TablePageFilter>();
		
		if (filter != null && filter.length() > 0) {
			String[] tokens = filter.split(CHAR_31);
			int i = 0;
			String n, t, o, v1, v2;
			while (i < tokens.length) {
				n = tokens[i++];
				t = tokens[i++];
				o = tokens[i++];
				v1 = tokens[i++];
				if (o.equals(BETWEEN))
					v2 = tokens[i++];
				else
					v2 = null;
				
				TablePageFilter f = new TablePageFilter(n, t, o, v1, v2);
				list.add(f);
			}
		}
		
		if (filterSql != null && filterSql.length() > 0) {
			TablePageFilter f;
			if (filterSqlValues != null && filterSqlValues.length() > 0)
				f = new TablePageFilter(filterSql, filterSqlValues.split(CHAR_31));
			else
				f = new TablePageFilter(filterSql, null);
			list.add(f);
		}
		
		return list;
	}
	
	public static List<TablePageSort> parseSort(String sort) {
		if (sort == null || sort.length() == 0)
			return new ArrayList<TablePageSort>();
		
		String[] tokens = sort.split(CHAR_31);
		ArrayList<TablePageSort> list = new ArrayList<TablePageSort>();
		for (int i = 0; i < tokens.length; i += 2) {
			TablePageSort s = new TablePageSort(tokens[i], tokens[i+1]);
			list.add(s);
		}
		return list;
	}
}
