package com.indogo.relay.member;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.indogo.TransferStatus;
import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.AbstractFunction;
import com.lionpig.webui.http.func.TablePage;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;
import com.lionpig.webui.http.util.Stringify;

public class Exchange extends AbstractFunction implements ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.exchange;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.action, C.columnTypeString, C.columnDirectionNone, false, true, C.emptyString));
		cols.add(new TablePageColumn(C.exchange_id, C.columnTypeNumber, C.columnDirectionDesc, true, false, "No."));
		cols.add(new TablePageColumn(C.kurs_usd, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.kurs_usd()));
		cols.add(new TablePageColumn(C.kurs_idr, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.kurs_idr()));
		cols.add(new TablePageColumn(C.kurs_value, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.real_kurs_value()));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, l.lm_time()));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, l.lm_user()));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.action).setValue(C.emptyString);
		cols.get(C.exchange_id).setValue(r.getLong(C.exchange_id));
		cols.get(C.kurs_usd).setValue(r.getDoubleCurrency(C.kurs_usd));
		cols.get(C.kurs_idr).setValue(r.getDoubleCurrency(C.kurs_idr));
		cols.get(C.kurs_value).setValue(r.getDoubleCurrency(C.kurs_value));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		if (action.equals(C.upload)) {
			Hashtable<String, FileItem> uploadFiles = fi.getUploadedFiles();
			FileItem file = uploadFiles.get(C.upload);
			if (file != null && file.getName().length() > 0) {
				try (InputStream stream = file.getInputStream()) {
					int insertCount = upload(fi, stream);
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

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String onUpdate(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		double kurs_usd = Helper.getDouble(params, C.kurs_usd, true);
		double kurs_idr = Helper.getDouble(params, C.kurs_idr, true);
		double kurs_value = Helper.getDouble(params, C.kurs_value, true);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			long exchange_id = update(fi, kurs_usd, kurs_idr, kurs_value);
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.exchange_id, C.columnTypeNumber, C.operationEqual, String.valueOf(exchange_id), null));
			TablePage p = new TablePage();
			String row = p.getRows(this, fi, 1, 1, this.getColumns(fi), null, filter, null, null, null);
			
			conn.commit();
			
			return row;
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
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
		Double kurs_value = Helper.getDoubleNullable(params, C.kurs_value, false);
		return get(fi, kurs_value);
	}

	public int upload(FunctionItem fi, InputStream inputStream) throws Exception {
		Workbook wb = WorkbookFactory.create(inputStream);
		Sheet sheet = wb.getSheetAt(0);
		Row row = sheet.getRow(0);
		
		int txnIdColumnIndex = -1;
		for (Cell cell : row) {
			if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				String value = cell.getStringCellValue();
				if (value.equals("廠商訂單編號")) {
					txnIdColumnIndex = cell.getColumnIndex();
					break;
				}
			}
		}
		
		if (txnIdColumnIndex < 0) {
			throw new Exception("column name [廠商訂單編號] not found");
		}
		
		Connection conn = fi.getConnection().getConnection();
		S sql = fi.getSql();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.exchange);
			pstmt.executeUpdate();
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into sql_temp_table (session_id, page_id, long1) values (?,?,?)"))) {
			int lastRowNum = sheet.getLastRowNum();
			int batchCount = 0;
			int insertCount = 0;
			for (int i = 1; i <= lastRowNum; i++) {
				row = sheet.getRow(i);
				Cell cell = row.getCell(txnIdColumnIndex);
				String txnIdString = getCellValue(cell);
				if (Helper.isNullOrEmpty(txnIdString))
					continue;
				
				long txnId = Long.parseLong(txnIdString);
				
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.exchange);
				pstmt.setLong(3, txnId);
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
	
	public String get(FunctionItem fi, Double input_kurs_value) throws Exception {
		StringBuilder sb = new StringBuilder();

		long total_ntd = 0, total_idr = 0, total_idr_new = 0;
		
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select a.long1, b.transfer_status_id, b.transfer_amount_ntd, b.kurs_value, b.transfer_amount_idr from sql_temp_table a left join money_transfer b on a.long1 = b.txn_id where a.session_id = ? and a.page_id = ?"))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.exchange);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				int count = 0;
				StringBuilder rows = new StringBuilder();
				while (r.next()) {
					TransferStatus status = TransferStatus.get(r.getInt(2));
					int amount_ntd = r.getInt(3);
					double kurs_value = r.getDouble(4);
					long amount_idr = r.getLong(5);
					long new_amount_idr;
					double new_kurs_value = input_kurs_value == null ? kurs_value : input_kurs_value.doubleValue();
					
					switch (status) {
						case pending:
						case paid:
						case process:
							if (kurs_value < new_kurs_value) {
								new_amount_idr = (long)Math.ceil((double)amount_ntd * new_kurs_value);
							} else {
								new_kurs_value = kurs_value;
								new_amount_idr = amount_idr;
							}
							break;
						default:
							new_kurs_value = kurs_value;
							new_amount_idr = amount_idr;
							break;
					}
					
					total_ntd += amount_ntd;
					total_idr += amount_idr;
					total_idr_new += new_amount_idr;
					
					rows.append(C.char_31).append(r.getLong(1))
					.append(C.char_31).append(status.getId())
					.append(C.char_31).append(status.name())
					.append(C.char_31).append(Stringify.getCurrency(amount_ntd))
					.append(C.char_31).append(Stringify.getCurrency(kurs_value))
					.append(C.char_31).append(Stringify.getCurrency(amount_idr))
					.append(C.char_31).append(Stringify.getCurrency(new_kurs_value))
					.append(C.char_31).append(Stringify.getCurrency(new_amount_idr));
					count++;
				}
				sb.append(count).append(rows);
			}
		}
		
		sb.append(C.char_31).append(Stringify.getCurrency(total_ntd))
		.append(C.char_31).append(Stringify.getCurrency(total_idr))
		.append(C.char_31).append(Stringify.getCurrency(total_idr_new));
		return sb.toString();
	}
	
	private class M {
		public long txn_id;
		public int transfer_amount_ntd;
		public double kurs_value;
		public long transfer_amount_idr;
		public Long new_transfer_amount_idr;
	}
	
	public long update(FunctionItem fi, double kurs_usd, double kurs_idr, double kurs_value) throws Exception {
		L l = fi.getLanguage();
		S sql = fi.getSql();
		
		Connection conn = fi.getConnection().getConnection();
		List<M> list = new ArrayList<>();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select a.long1, b.transfer_amount_ntd, b.kurs_value, b.transfer_amount_idr from sql_temp_table a inner join money_transfer b on a.long1 = b.txn_id where a.session_id = ? and a.page_id = ? order by a.long1"))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.exchange);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					M m = new M();
					m.txn_id = r.getLong(1);
					m.transfer_amount_ntd = r.getInt(2);
					m.kurs_value = r.getDouble(3);
					m.transfer_amount_idr = r.getLong(4);
					
					if (m.kurs_value < kurs_value) {
						m.new_transfer_amount_idr = (long)Math.ceil((double)m.transfer_amount_ntd * kurs_value);
					} else {
						m.new_transfer_amount_idr = null;
					}
					list.add(m);
				}
			}
		}
		
		if (list.size() == 0) {
			throw new Exception(l.nothing_to_update());
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.exchange);
			pstmt.executeUpdate();
		}
		
		Timestamp lm_time = fi.getConnection().getCurrentTime();
		String lm_user = fi.getSessionInfo().getUserName();
		
		long exchange_id = fi.getConnection().getSeq(C.exchange_id, true);
		
		List<M> updated_list = new ArrayList<>();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select transfer_amount_idr, transfer_status_id from money_transfer where txn_id = ? for update"))) {
			for (M m : list) {
				pstmt.setLong(1, m.txn_id);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						long transfer_amount_idr = r.getLong(1);
						if (transfer_amount_idr != m.transfer_amount_idr) {
							throw new Exception(l.data_already_updated_by_another_user("txn_id = " + m.txn_id));
						}
						TransferStatus status = TransferStatus.get(r.getInt(2));
						switch (status) {
							case paid:
							case pending:
							case process:
								updated_list.add(m);
								break;
							default:
								break;
						}
					} else {
						throw new Exception(l.data_not_exist("txn_id = " + m.txn_id));
					}
				}
			}
		}
		
		if (updated_list.size() == 0) {
			throw new Exception(l.nothing_to_update());
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into exchange (exchange_id, kurs_usd, kurs_idr, kurs_value, lm_time, lm_user) values (?,?,?,?,?,?)"))) {
			pstmt.setLong(1, exchange_id);
			pstmt.setDouble(2, kurs_usd);
			pstmt.setDouble(3, kurs_idr);
			pstmt.setDouble(4, kurs_value);
			pstmt.setTimestamp(5, lm_time);
			pstmt.setString(6, lm_user);
			pstmt.executeUpdate();
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update money_transfer set kurs_value = ?, transfer_amount_idr = ?, lm_time = ?, lm_user = ?, exchange_id = ? where txn_id = ?"))) {
			for (M m : updated_list) {
				if (m.new_transfer_amount_idr != null) {
					pstmt.setDouble(1, kurs_value);
					pstmt.setLong(2, m.new_transfer_amount_idr);
					pstmt.setTimestamp(3, lm_time);
					pstmt.setString(4, lm_user);
					pstmt.setLong(5, exchange_id);
					pstmt.setLong(6, m.txn_id);
					pstmt.executeUpdate();
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update money_transfer set lm_time = ?, lm_user = ?, exchange_id = ? where txn_id = ?"))) {
			for (M m : updated_list) {
				if (m.new_transfer_amount_idr == null) {
					pstmt.setTimestamp(1, lm_time);
					pstmt.setString(2, lm_user);
					pstmt.setLong(3, exchange_id);
					pstmt.setLong(4, m.txn_id);
					pstmt.executeUpdate();
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into exchange_item (exchange_id, txn_id, old_kurs_value, old_amount_idr, new_kurs_value, new_amount_idr) values (?,?,?,?,?,?)"))) {
			for (M m : updated_list) {
				pstmt.setLong(1, exchange_id);
				pstmt.setLong(2, m.txn_id);
				pstmt.setDouble(3, m.kurs_value);
				pstmt.setLong(4, m.transfer_amount_idr);
				if (m.new_transfer_amount_idr == null) {
					pstmt.setDouble(5, m.kurs_value);
					pstmt.setLong(6, m.transfer_amount_idr);
				} else {
					pstmt.setDouble(5, kurs_value);
					pstmt.setLong(6, m.new_transfer_amount_idr);
				}
				pstmt.executeUpdate();
			}
		}
		
		return exchange_id;
	}
}
