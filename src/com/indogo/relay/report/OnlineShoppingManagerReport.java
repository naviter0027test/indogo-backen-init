package com.indogo.relay.report;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

public class OnlineShoppingManagerReport extends AbstractFunction implements ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.sql_temp_table;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.date1, C.columnTypeString, C.columnDirectionDefault, true, false, "日期"));
		cols.add(new TablePageColumn(C.long1, C.columnTypeNumber, C.columnDirectionDefault, true, false, "購買金額"));
		cols.add(new TablePageColumn(C.long2, C.columnTypeNumber, C.columnDirectionDefault, true, false, "銷售金額"));
		cols.add(new TablePageColumn(C.long3, C.columnTypeNumber, C.columnDirectionDefault, true, false, "進帳金額"));
		cols.add(new TablePageColumn(C.session_id, C.columnTypeString, C.columnDirectionDefault, true, false, C.session_id, true, true));
		cols.add(new TablePageColumn(C.page_id, C.columnTypeString, C.columnDirectionDefault, true, false, C.page_id, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.date1).setValue(r.getDate(C.date1));
		cols.get(C.long1).setValue(r.getLongCurrency(C.long1));
		cols.get(C.long2).setValue(r.getLongCurrency(C.long2));
		cols.get(C.long3).setValue(r.getLongCurrency(C.long3));
		cols.get(C.session_id).setValue(r.getString(C.session_id));
		cols.get(C.page_id).setValue(r.getString(C.page_id));
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
		// TODO Auto-generated method stub
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
		Timestamp start_time = Helper.getTimestamp(params, C.start_time, true);
		Timestamp end_time = Helper.getTimestamp(params, C.end_time, true);
		try {
			int count = getSummary(fi, start_time, end_time);
			conn.commit();
			return String.valueOf(count);
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}
	
	public class Summary {
		public Timestamp date;
		public long total_incoming_order;
		public long total_sales_created;
		public long total_sales_paid;
	}
	
	public int getSummary(FunctionItem fi, Timestamp start_time, Timestamp end_time) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		S sql = fi.getSql();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.online_shopping_manager_report);
			pstmt.executeUpdate();
		}
		
		Map<Timestamp, Summary> list = new HashMap<>();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select cast(create_time as date), sum(total)\r\n" + 
				"from incoming_order\r\n" + 
				"where status_id = 1\r\n" + 
				"and create_time between ? and ?\r\n" + 
				"group by cast(create_time as date)"))) {
			pstmt.setTimestamp(1, start_time);
			pstmt.setTimestamp(2, end_time);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					Summary s = new Summary();
					s.date = r.getTimestamp(1);
					s.total_incoming_order = r.getLong(2);
					list.put(s.date, s);
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select cast(lm_time_created as date), sum(total_amount)\r\n" + 
				"from sales\r\n" + 
				"where status_id not in (5, 6)\r\n" + 
				"and lm_time_created between ? and ?\r\n" + 
				"group by cast(lm_time_created as date)"))) {
			pstmt.setTimestamp(1, start_time);
			pstmt.setTimestamp(2, end_time);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					Timestamp date = r.getTimestamp(1);
					Summary s = list.get(date);
					if (s == null) {
						s = new Summary();
						s.date = date;
						list.put(date, s);
					}
					s.total_sales_created = r.getLong(2);
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select cast(lm_time_paid as date), sum(total_amount)\r\n" + 
				"from sales\r\n" + 
				"where status_id not in (5, 6)\r\n" + 
				"and lm_time_paid between ? and ?\r\n" + 
				"group by cast(lm_time_paid as date)"))) {
			pstmt.setTimestamp(1, start_time);
			pstmt.setTimestamp(2, end_time);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					Timestamp date = r.getTimestamp(1);
					Summary s = list.get(date);
					if (s == null) {
						s = new Summary();
						s.date = date;
						list.put(date, s);
					}
					s.total_sales_paid = r.getLong(2);
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into sql_temp_table (session_id, page_id, date1, long1, long2, long3) values (?,?,?,?,?,?)"))) {
			for (Summary s : list.values()) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.online_shopping_manager_report);
				pstmt.setTimestamp(3, s.date);
				pstmt.setLong(4, s.total_incoming_order);
				pstmt.setLong(5, s.total_sales_created);
				pstmt.setLong(6, s.total_sales_paid);
				pstmt.executeUpdate();
			}
		}
		
		return list.size();
	}

}
