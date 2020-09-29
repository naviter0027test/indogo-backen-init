package com.indogo.relay.onlineshopping;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.indogo.model.onlineshopping.SalesStatus;
import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.AbstractFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Stringify;

public class SalesImportHeimao extends AbstractFunction {

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		if (action.equals(C.upload)) {
			Hashtable<String, FileItem> uploadFiles = fi.getUploadedFiles();
			FileItem file = uploadFiles.get(C.upload);
			if (file != null && file.getName().length() > 0) {
				try (InputStream stream = file.getInputStream()) {
					int insertCount = upload_excel(fi, stream, file.getName());
					fi.getConnection().getConnection().commit();
					return String.valueOf(insertCount);
				}
			}
		}
		return null;
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public class ImportHeimao {
		public int seq;
		public String shipping_date;
		public String office;
		public String shipping_number;
		public String dest_area;
		public String invoice_no;
		public String expected_money;
		public String collected_money;
		public String fee;
		public String comment;
		public String filename;
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		
		List<Long> sales_ids = new ArrayList<>();
		List<ImportHeimao> heimaos = new ArrayList<>();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select a.num1, a.item1, a.item2, a.item3, a.item4, a.item5, a.item6, a.item7, a.item8, a.item9, a.item10, b.sales_id from sql_temp_table a left join sales b on a.item3 = b.ship_no where a.session_id = ? and a.page_id = ?"))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.sales_import_heimao);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					ImportHeimao heimao = new ImportHeimao();
					heimao.seq = r.getInt(1);
					heimao.shipping_date = r.getString(2);
					heimao.office = r.getString(3);
					heimao.shipping_number = r.getString(4);
					heimao.dest_area = r.getString(5);
					heimao.invoice_no = r.getString(6);
					heimao.expected_money = r.getString(7);
					heimao.collected_money = r.getString(8);
					heimao.fee = r.getString(9);
					heimao.comment = r.getString(10);
					heimao.filename = r.getString(11);
					heimaos.add(heimao);
					
					sales_ids.add(r.getLong(12));
				}
			}
		}
		
		Collections.sort(sales_ids);
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select status_id from sales where sales_id = ? for update"))) {
			for (long sales_id : sales_ids) {
				pstmt.setLong(1, sales_id);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						SalesStatus status = SalesStatus.get(r.getInt(1));
						if (status != SalesStatus.shipped) {
							throw new Exception("status must be shipped");
						}
					} else {
						throw new Exception(l.data_not_exist("sales_id = " + sales_id));
					}
				}
			}
		}
		
		Timestamp lm_time = fi.getConnection().getCurrentTime();
		String lm_user = fi.getSessionInfo().getUserName();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update sales set status_id = ?, lm_time = ?, lm_user = ?, lm_time_paid = ?, lm_user_paid = ? where sales_id = ?"))) {
			for (long sales_id : sales_ids) {
				pstmt.setInt(1, SalesStatus.paid.getId());
				pstmt.setTimestamp(2, lm_time);
				pstmt.setString(3, lm_user);
				pstmt.setTimestamp(4, lm_time);
				pstmt.setString(5, lm_user);
				pstmt.setLong(6, sales_id);
				pstmt.executeUpdate();
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into heimao_import (import_date, filename, lm_time, lm_user) values (?,?,?,?)"))) {
			pstmt.setTimestamp(1, lm_time);
			pstmt.setString(2, heimaos.get(0).filename);
			pstmt.setTimestamp(3, lm_time);
			pstmt.setString(4, lm_user);
			pstmt.executeUpdate();
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into heimao_import_list (import_date, seq, shipping_date, office, shipping_number, dest_area, invoice_no, expected_money, collected_money, fee, comment) values (?,?,?,?,?,?,?,?,?,?,?)"))) {
			for (ImportHeimao heimao : heimaos) {
				pstmt.setTimestamp(1, lm_time);
				pstmt.setInt(2, heimao.seq);
				pstmt.setString(3, heimao.shipping_date);
				pstmt.setString(4, heimao.office);
				pstmt.setString(5, heimao.shipping_number);
				pstmt.setString(6, heimao.dest_area);
				pstmt.setString(7, heimao.invoice_no);
				pstmt.setString(8, heimao.expected_money);
				pstmt.setString(9, heimao.collected_money);
				pstmt.setString(10, heimao.fee);
				pstmt.setString(11, heimao.comment);
				pstmt.executeUpdate();
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.sales_import_heimao);
			pstmt.executeUpdate();
		}
		
		conn.commit();
		
		return C.emptyString;
	}

	@Override
	protected String onUpdate(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String onDelete(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select a.num1, a.item1, a.item2, a.item3, a.item4, a.item5, a.item6, a.item7, a.item8, a.item9, a.item10, b.status_id from sql_temp_table a left join sales b on a.item3 = b.ship_no where a.session_id = ? and a.page_id = ?"))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.sales_import_heimao);
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				StringBuilder sb = new StringBuilder();
				while (r.next()) {
					sb.append(r.getString(1));
					for (int i = 2; i <= 11; i++) {
						sb.append(C.char_30).append(r.getString(i));
					}
					Integer status_id = r.unwrap().getInt(12);
					if (status_id == null) {
						sb.append(C.char_30).append(C.emptyString).append(C.char_30).append(C.emptyString);
					} else {
						sb.append(C.char_30).append(SalesStatus.get(status_id).getName(l)).append(C.char_30).append(status_id);
					}
					sb.append(C.char_31);
				}
				if (sb.length() > 0) {
					sb.delete(sb.length() - 1, sb.length());
				}
				return sb.toString();
			}
		}
	}
	
	public int upload_excel(FunctionItem fi, InputStream stream, String filename) throws Exception {
		Workbook wb = WorkbookFactory.create(stream);
		Sheet sheet = wb.getSheetAt(0);
		Row row = sheet.getRow(0);
		Map<String, Integer> columns = new HashMap<>();
		for (int i = 0; i < 9; i++) {
			Cell cell = row.getCell(i);
			String columnName = cell.getStringCellValue().trim();
			columns.put(columnName, i);
		}
		
		Hashtable<String, String> heimao_import_column = fi.getConnection().getGlobalConfig(C.heimao_import_column);
		String column_name_shipping_date = heimao_import_column.get(C.shipping_date);
		String column_name_office = heimao_import_column.get(C.office);
		String column_name_shipping_number = heimao_import_column.get(C.shipping_number);
		String column_name_dest_area = heimao_import_column.get(C.dest_area);
		String column_name_invoice_no = heimao_import_column.get(C.invoice_no);
		String column_name_expected_money = heimao_import_column.get(C.expected_money);
		String column_name_collected_money = heimao_import_column.get(C.collected_money);
		String column_name_fee = heimao_import_column.get(C.fee);
		String column_name_comment = heimao_import_column.get(C.comment);
		
		// check for must be columns
		if (!columns.containsKey(column_name_shipping_date)) {
			throw new Exception("file missing column name [" + column_name_shipping_date + "]");
		}
		if (!columns.containsKey(column_name_shipping_number)) {
			throw new Exception("file missing column name [" + column_name_shipping_number + "]");
		}
		if (!columns.containsKey(column_name_invoice_no)) {
			throw new Exception("file missing column name [" + column_name_invoice_no + "]");
		}
		if (!columns.containsKey(column_name_collected_money)) {
			throw new Exception("file missing column name [" + column_name_collected_money + "]");
		}
		
		int column_index_shipping_date = columns.get(column_name_shipping_date);
		int column_index_office = columns.get(column_name_office);
		int column_index_shipping_number = columns.get(column_name_shipping_number);
		int column_index_dest_area = columns.get(column_name_dest_area);
		int column_index_invoice_no = columns.get(column_name_invoice_no);
		int column_index_expected_money = columns.get(column_name_expected_money);
		int column_index_collected_money = columns.get(column_name_collected_money);
		int column_index_fee = columns.get(column_name_fee);
		int column_index_comment = columns.get(column_name_comment);
		
		Connection conn = fi.getConnection().getConnection();
		S sql = fi.getSql();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.sales_import_heimao);
			pstmt.executeUpdate();
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into sql_temp_table (session_id, page_id, item1, item2, item3, item4, item5, item6, item7, item8, item9, item10, num1) values (?,?,?,?,?,?,?,?,?,?,?,?,?)"))) {
			int lastRowNum = sheet.getLastRowNum();
			int batchCount = 0;
			int insertCount = 0;
			for (int i = 1; i < lastRowNum; i++) {
				row = sheet.getRow(i);
				Cell cell = row.getCell(column_index_shipping_date);
				String shipping_date = getCellValue(cell);
				
				cell = row.getCell(column_index_office);
				String office = getCellValue(cell);
				
				cell = row.getCell(column_index_shipping_number);
				String shipping_number = getCellValue(cell);
				
				cell = row.getCell(column_index_dest_area);
				String dest_area = getCellValue(cell);
				
				cell = row.getCell(column_index_invoice_no);
				String invoice_no = getCellValue(cell);
				
				cell = row.getCell(column_index_expected_money);
				String expected_money = getCellValue(cell);
				
				cell = row.getCell(column_index_collected_money);
				String collected_money = getCellValue(cell);
				
				cell = row.getCell(column_index_fee);
				String fee = getCellValue(cell);
				
				cell = row.getCell(column_index_comment);
				String comment = getCellValue(cell);
				
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.sales_import_heimao);
				pstmt.setString(3, shipping_date);
				pstmt.setString(4, office);
				pstmt.setString(5, shipping_number);
				pstmt.setString(6, dest_area);
				pstmt.setString(7, invoice_no);
				pstmt.setString(8, expected_money);
				pstmt.setString(9, collected_money);
				pstmt.setString(10, fee);
				pstmt.setString(11, comment);
				pstmt.setString(12, filename);
				pstmt.setInt(13, i);
				pstmt.addBatch();
				batchCount++;
				insertCount++;
				
				if (batchCount >= 5000) {
					pstmt.executeBatch();
					batchCount = 0;
				}
			}
			
			if (batchCount > 0) {
				pstmt.executeBatch();
				batchCount = 0;
			}
			
			return insertCount;
		}
	}
	
	private String getCellValue(Cell cell) {
		if (cell == null) {
			return C.emptyString;
		} else {
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_NUMERIC:
					return Stringify.getString(cell.getNumericCellValue());
				case Cell.CELL_TYPE_BOOLEAN:
					return cell.getBooleanCellValue() ? "1" : "0";
				default:
					String s = cell.getStringCellValue();
					if (s == null) {
						return C.emptyString;
					} else {
						return s;
					}
			}
		}
	}

}
