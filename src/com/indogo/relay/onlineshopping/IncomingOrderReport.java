package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

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

public class IncomingOrderReport extends AbstractFunction implements ITablePage {

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
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.item1, C.columnTypeString, C.columnDirectionDefault, true, false, l.item_name()));
		cols.add(new TablePageColumn(C.item2, C.columnTypeString, C.columnDirectionDefault, true, false, l.item_desc()));
		cols.add(new TablePageColumn(C.item3, C.columnTypeString, C.columnDirectionDefault, true, false, l.category_name()));
		cols.add(new TablePageColumn(C.item4, C.columnTypeString, C.columnDirectionDefault, true, false, l.color_name()));
		cols.add(new TablePageColumn(C.item5, C.columnTypeString, C.columnDirectionDefault, true, false, l.size_name()));
		cols.add(new TablePageColumn(C.long1, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total Qty"));
		cols.add(new TablePageColumn(C.long2, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total Buy"));
		cols.add(new TablePageColumn(C.session_id, C.columnTypeString, C.columnDirectionDefault, true, false, C.session_id, true, true));
		cols.add(new TablePageColumn(C.page_id, C.columnTypeString, C.columnDirectionDefault, true, false, C.page_id, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.item1).setValue(r.getString(C.item1));
		cols.get(C.item2).setValue(r.getString(C.item2));
		cols.get(C.item3).setValue(r.getString(C.item3));
		cols.get(C.item4).setValue(r.getString(C.item4));
		cols.get(C.item5).setValue(r.getString(C.item5));
		cols.get(C.long1).setValue(r.getLongCurrency(C.long1));
		cols.get(C.long2).setValue(r.getLongCurrency(C.long2));
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
		return ItemCategory.getAll(fi);
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
		int[] category_ids = Helper.getIntArray(params, C.category_ids, false);
		long[] order_ids = Helper.getLongArray(params, C.order_ids, false);
		try {
			int count = getSummary(fi, start_time, end_time, category_ids, order_ids);
			conn.commit();
			return String.valueOf(count);
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}
	
	private class Summary {
		public String item_name;
		public String item_desc;
		public String category_name;
		public String color_name;
		public String size_name;
		public long sum_qty;
		public long sum_total;
	}
	
	public int getSummary(FunctionItem fi, Timestamp start_time, Timestamp end_time, int[] category_ids, long[] order_ids) throws Exception {
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.incoming_order_report);
			pstmt.executeUpdate();
		}
		
		StringBuilder sb = new StringBuilder();
		if (category_ids.length > 0) {
			sb.append("d.category_id in (").append(StringUtils.join(category_ids, ',')).append(") and ");
		}
		if (order_ids.length > 0) {
			sb.append("a.order_id in (").append(StringUtils.join(order_ids, ',')).append(") and ");
		}
		
		List<Summary> rows = new ArrayList<>();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select d.item_name, d.item_desc, g.category_name, e.color_name, f.size_name, sum(b.qty), sum(b.total)\r\n" + 
				"from incoming_order a inner join incoming_order_item b on a.order_id = b.order_id\r\n" + 
				"inner join item d on b.item_id = d.item_id\r\n" + 
				"left join item_color e on d.color_id = e.color_id\r\n" + 
				"left join item_size f on d.size_id = f.size_id\r\n" +
				"inner join item_category g on d.category_id = g.category_id\r\n" +
				"where " + sb + "a.create_time between ? and ?\r\n" +
				"and a.status_id = 1\r\n" +
				"group by d.item_name, d.item_desc, g.category_name, e.color_name, f.size_name"))) {
			pstmt.setTimestamp(1, start_time);
			pstmt.setTimestamp(2, end_time);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					Summary s = new Summary();
					s.item_name = r.getString(1);
					s.item_desc = r.getString(2);
					s.category_name = r.getString(3);
					s.color_name = r.getString(4);
					s.size_name = r.getString(5);
					s.sum_qty = r.getLong(6);
					s.sum_total = r.getLong(7);
					rows.add(s);
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into sql_temp_table (session_id, page_id, item1, item2, item3, item4, item5, long1, long2) values (?,?,?,?,?,?,?,?,?)"))) {
			int count = 0;
			for (Summary s : rows) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.incoming_order_report);
				pstmt.setString(3, s.item_name);
				pstmt.setString(4, s.item_desc);
				pstmt.setString(5, s.category_name);
				pstmt.setString(6, s.color_name);
				pstmt.setString(7, s.size_name);
				pstmt.setLong(8, s.sum_qty);
				pstmt.setLong(9, s.sum_total);
				pstmt.addBatch();
				count++;
				
				if (count > 5000) {
					pstmt.executeBatch();
					count = 0;
				}
			}
			
			if (count > 0) {
				pstmt.executeBatch();
				count = 0;
			}
		}
		
		return rows.size();
	}

}
