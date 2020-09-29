package com.indogo.relay.onlineshopping;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.indogo.model.onlineshopping.SalesStatus;
import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.AbstractFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class SalesExportHeimao extends AbstractFunction implements ITablePage {
	
	private PreparedStatementWrapper pstmtMember = null;

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.sales;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.member_name, C.columnTypeString, C.columnDirectionNone, false, true, "Customer Name"));
		cols.add(new TablePageColumn(C.phone_no_1, C.columnTypeString, C.columnDirectionNone, false, true, "Telp 1"));
		cols.add(new TablePageColumn(C.phone_no_2, C.columnTypeString, C.columnDirectionNone, false, true, "Telp 2"));
		cols.add(new TablePageColumn(C.ship_address, C.columnTypeString, C.columnDirectionDefault, true, false, "Shipping Address"));
		cols.add(new TablePageColumn(C.action, C.columnTypeString, C.columnDirectionNone, false, true, "C1"));
		cols.add(new TablePageColumn(C.action_2, C.columnTypeString, C.columnDirectionNone, false, true, "C2"));
		cols.add(new TablePageColumn(C.total_amount, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total"));
		cols.add(new TablePageColumn(C.lm_time_created, C.columnTypeDateTime, C.columnDirectionDefault, true, false, l.lm_time_created()));
		cols.add(new TablePageColumn(C.comment, C.columnTypeString, C.columnDirectionDefault, true, false, "Comment"));
		cols.add(new TablePageColumn(C.sales_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.sales_id));
		cols.add(new TablePageColumn(C.ship_no, C.columnTypeString, C.columnDirectionDefault, true, false, "Shipping Number"));
		cols.add(new TablePageColumn(C.shop_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.shop_id, true, true));
		cols.add(new TablePageColumn(C.status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.status_id, true, true));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, true));
		cols.add(new TablePageColumn(C.freight_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.freight_id, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		long member_id = r.unwrap().unwrap().getInt(C.member_id);
		
		pstmtMember.setLong(1, member_id);
		try (ResultSetWrapperStringify rr = new ResultSetWrapperStringify(pstmtMember.executeQueryWrapper())) {
			if (rr.next()) {
				cols.get(C.member_name).setValue(rr.getString(C.member_name));
				String[] phone_nos = StringUtils.split(rr.getString(C.phone_no), '.');
				String app_phone_no = rr.getString(C.app_phone_no);
				
				Set<String> phones = new HashSet<>();
				if (app_phone_no.length() > 0) {
					phones.add(app_phone_no);
				}
				for (String phone_no : phone_nos) {
					if (phone_no.length() > 0) {
						phones.add(phone_no);
					}
				}
				
				phone_nos = phones.toArray(new String[0]);
				if (phone_nos.length > 0) {
					cols.get(C.phone_no_1).setValue(phone_nos[0]);
				} else {
					cols.get(C.phone_no_1).setValue(C.emptyString);
				}
				
				if (phone_nos.length > 1) {
					cols.get(C.phone_no_2).setValue(phone_nos[1]);
				} else {
					cols.get(C.phone_no_2).setValue(C.emptyString);
				}
			} else {
				cols.get(C.member_name).setValue(C.emptyString);
				cols.get(C.phone_no_1).setValue(C.emptyString);
				cols.get(C.phone_no_2).setValue(C.emptyString);
			}
		}
		
		cols.get(C.ship_address).setValue(r.getString(C.ship_address));
		cols.get(C.action).setValue("1");
		cols.get(C.action_2).setValue(C.emptyString);
		cols.get(C.total_amount).setValue(r.getInt(C.total_amount));
		cols.get(C.lm_time_created).setValue(r.getTimestamp(C.lm_time_created));
		cols.get(C.comment).setValue(r.getString(C.comment));
		cols.get(C.sales_id).setValue(r.getLong(C.sales_id));
		cols.get(C.ship_no).setValue(r.getString(C.ship_no));
		cols.get(C.shop_id).setValue(r.getInt(C.shop_id));
		cols.get(C.status_id).setValue(r.getInt(C.status_id));
		cols.get(C.member_id).setValue(r.getLong(C.member_id));
		cols.get(C.freight_id).setValue(r.getInt(C.freight_id));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		S sql = fi.getSql();
		pstmtMember = new PreparedStatementWrapper(fi.getConnection().getConnection().prepareStatement(sql.member_get_for_sales_export_heimao()));
		filter.add(new TablePageFilter(C.status_id, C.columnTypeNumber, C.operationIn, "1,2", null));
		filter.add(new TablePageFilter(C.freight_id, C.columnTypeNumber, C.operationIn, "1", null));
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
		pstmtMember.close();
	}
	
	private static final String[] PRINT_COLUMN_NAMES = new String[] { "NAME", "ADDRESS", "PHONE", "AMOUNT", "INVOICE" };

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		if (action.equals(C.print)) {
			long[] sales_ids = Helper.getLongArray(params, C.sales_ids, true);
			
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.clear_temp_table()))) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.sales_export_heimao);
				pstmt.executeUpdate();
			}
			
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.insert_temp_table_long1()))) {
				for (int i = 0; i < sales_ids.length; i++) {
					pstmt.setString(1, fi.getSID());
					pstmt.setString(2, C.sales_export_heimao);
					pstmt.setLong(3, sales_ids[i]);
					pstmt.executeUpdate();
				}
			}
			
			List<Long> sales_ids_sorted = new ArrayList<>();
			for (long sales_id : sales_ids) {
				sales_ids_sorted.add(sales_id);
			}
			Collections.sort(sales_ids_sorted);
			
			List<Long> sales_ids_update = new ArrayList<>();
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select status_id from sales where sales_id = ? for update"))) {
				for (long sales_id : sales_ids_sorted) {
					pstmt.setLong(1, sales_id);
					try (ResultSet r = pstmt.executeQuery()) {
						if (r.next()) {
							SalesStatus status = SalesStatus.get(r.getInt(1));
							if (status != SalesStatus.created && status != SalesStatus.checkout) {
								throw new Exception("sales_id = " + sales_id + " : " + l.status_not_allowed());
							}
							
							if (status == SalesStatus.created) {
								sales_ids_update.add(sales_id);
							}
						} else {
							throw new Exception(l.data_not_exist("sales_id = " + sales_id));
						}
					}
				}
			}

			Timestamp lm_time = fi.getConnection().getCurrentTime();
			String lm_user = fi.getSessionInfo().getUserName();
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update sales set status_id = ?, lm_user = ?, lm_time = ?, lm_user_checkout = ?, lm_time_checkout = ? where sales_id = ?"))) {
				for (long sales_id : sales_ids_update) {
					pstmt.setInt(1, SalesStatus.checkout.getId());
					pstmt.setString(2, lm_user);
					pstmt.setTimestamp(3, lm_time);
					pstmt.setString(4, lm_user);
					pstmt.setTimestamp(5, lm_time);
					pstmt.setLong(6, sales_id);
					pstmt.executeUpdate();
				}
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String filenamePrefix = sdf.format(lm_time);
			long filenameSeq = fi.getConnection().getSeq("export_heimao_" + filenamePrefix, true);
			
			conn.commit();
			
			String filename = "export_heimao_" + filenamePrefix + "_" + StringUtils.leftPad(String.valueOf(filenameSeq), 3, '0') + ".xls";
			File file = new File(fi.getTempFolder(), filename);
			
			Workbook workbook;
			Row row;
			Cell cell;
			CellStyle style;
			int rowIndex = 0;
			
			workbook = new HSSFWorkbook();
			Sheet sheet = workbook.createSheet("Sheet1");
			
			row = sheet.createRow(rowIndex);
			rowIndex++;
			
			style = workbook.createCellStyle();
			style.setAlignment(CellStyle.ALIGN_CENTER);
			
			for (int i = 0; i < PRINT_COLUMN_NAMES.length; i++) {
				cell = row.createCell(i);
				cell.setCellValue(PRINT_COLUMN_NAMES[i]);
				cell.setCellStyle(style);
			}
			
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.print_sales_export_heimao()))) {
				pstmt.setString(1, fi.getSID());
				try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
					while (r.next()) {
						row = sheet.createRow(rowIndex);
						rowIndex++;
						
						String member_name = r.getString(1);
						String[] phone_nos = StringUtils.split(r.getString(2), '.');
						String app_phone_no = r.getString(3);
						String ship_address = r.getString(4);
						String total_amount = r.getInt(5);
						String sales_id = r.getLong(6);
						
						Set<String> phones = new HashSet<>();
						if (app_phone_no.length() > 0) {
							phones.add(app_phone_no);
						}
						for (String phone_no : phone_nos) {
							if (phone_no.length() > 0) {
								phones.add(phone_no);
							}
						}
						
						String phone_no_1;
						phone_nos = phones.toArray(new String[0]);
						if (phone_nos.length > 0) {
							phone_no_1 = StringUtils.join(phone_nos, ',');
						} else {
							phone_no_1 = C.emptyString;
						}
						
						row.createCell(0).setCellValue(member_name);
						row.createCell(1).setCellValue(ship_address);
						row.createCell(2).setCellValue(phone_no_1);
						row.createCell(3).setCellValue(total_amount);
						row.createCell(4).setCellValue(sales_id);
					}
				}
			}

			FileOutputStream fos = new FileOutputStream(file, false);
			try {
				workbook.write(fos);
			}
			finally {
				fos.close();
			}
			
			return filename;
		} else if (action.equals(C.sql_temp_table)) {
			String member_name = Helper.getString(params, C.member_name, false);
			String phone_no = Helper.getString(params, C.phone_no, false);
			String item_desc = Helper.getString(params, C.item_desc, false);
			try {
				int count = prepareTempTable(fi, member_name, phone_no, item_desc);
				conn.commit();
				return String.valueOf(count);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else {
			throw new Exception(l.unknown_action(action));
		}
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		return Shop.getShopListForUser(fi);
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}
	
	public int prepareTempTable(FunctionItem fi, String member_name, String phone_no, String item_desc) throws Exception {
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.sales_export_heimao);
			pstmt.executeUpdate();
		}
		
		StringBuilder sb = new StringBuilder();
		
		if (item_desc == null) {
			sb.append("select sales_id from sales where status_id in (1,2) and freight_id = 1");
			if (member_name != null && phone_no != null) {
				sb.append(" and member_id in (select member_id from member where member_name like ? and (phone_no like ? or app_phone_no like ?))");
			} else if (member_name != null) {
				sb.append(" and member_id in (select member_id from member where member_name like ?)");
			} else if (phone_no != null) {
				sb.append(" and member_id in (select member_id from member where phone_no like ? or app_phone_no like ?)");
			}
		} else {
			sb.append("select sales_id from sales a inner join sales_item b on a.sales_id = b.sales_id where a.status_id in (1,2) and a.freight_id = 1 and b.item_id in (select item_id from item where item_desc like ?)");
			if (member_name != null && phone_no != null) {
				sb.append(" and a.member_id in (select member_id from member where member_name like ? and (phone_no like ? or app_phone_no like ?))");
			} else if (member_name != null) {
				sb.append(" and a.member_id in (select member_id from member where member_name like ?)");
			} else if (phone_no != null) {
				sb.append(" and a.member_id in (select member_id from member where phone_no like ? or app_phone_no like ?)");
			}
		}
		
		List<Long> sales_ids = new ArrayList<>();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sb.toString()))) {
			int pos = 1;
			if (item_desc == null) {
				if (member_name != null && phone_no != null) {
					pstmt.setString(pos++, "%" + member_name + "%");
					pstmt.setString(pos++, "%." + phone_no + "%");
					pstmt.setString(pos++, phone_no + "%");
				} else if (member_name != null) {
					pstmt.setString(pos++, "%" + member_name + "%");
				} else if (phone_no != null) {
					pstmt.setString(pos++, "%." + phone_no + "%");
					pstmt.setString(pos++, phone_no + "%");
				}
			} else {
				pstmt.setString(pos++, "%" + item_desc + "%");
				if (member_name != null && phone_no != null) {
					pstmt.setString(pos++, "%" + member_name + "%");
					pstmt.setString(pos++, "%." + phone_no + "%");
					pstmt.setString(pos++, phone_no + "%");
				} else if (member_name != null) {
					pstmt.setString(pos++, "%" + member_name + "%");
				} else if (phone_no != null) {
					pstmt.setString(pos++, "%." + phone_no + "%");
					pstmt.setString(pos++, phone_no + "%");
				}
			}
			
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					sales_ids.add(r.getLong(1));
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.insert_temp_table_long1()))) {
			for (Long sales_id : sales_ids) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.sales_export_heimao);
				pstmt.setLong(3, sales_id);
				pstmt.executeUpdate();
			}
		}
		
		return sales_ids.size();
	}

}
