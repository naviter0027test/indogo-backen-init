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

public class IncomingOrderReportDetail extends AbstractFunction implements ITablePage {

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
		cols.add(new TablePageColumn(C.date1, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Date"));
		cols.add(new TablePageColumn(C.item1, C.columnTypeString, C.columnDirectionDefault, true, false, "Category"));
		cols.add(new TablePageColumn(C.item2, C.columnTypeString, C.columnDirectionDefault, true, false, "Product Name"));
		cols.add(new TablePageColumn(C.num1, C.columnTypeNumber, C.columnDirectionDefault, true, false, "QTY"));
		cols.add(new TablePageColumn(C.num2, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Buy Price"));
		cols.add(new TablePageColumn(C.num3, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Discount"));
		cols.add(new TablePageColumn(C.num4, C.columnTypeNumber, C.columnDirectionDefault, true, false, "TOTAL"));
		cols.add(new TablePageColumn(C.long1, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Order Id"));
		cols.add(new TablePageColumn(C.item3, C.columnTypeString, C.columnDirectionDefault, true, false, "Purchase By"));
		cols.add(new TablePageColumn(C.session_id, C.columnTypeString, C.columnDirectionDefault, true, false, C.session_id, true, true));
		cols.add(new TablePageColumn(C.page_id, C.columnTypeString, C.columnDirectionDefault, true, false, C.page_id, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.date1).setValue(r.getTimestamp(C.date1));
		cols.get(C.item1).setValue(r.getString(C.item1));
		cols.get(C.item2).setValue(r.getString(C.item2));
		cols.get(C.num1).setValue(r.getIntCurrency(C.num1));
		cols.get(C.num2).setValue(r.getIntCurrency(C.num2));
		cols.get(C.num3).setValue(r.getIntCurrency(C.num3));
		cols.get(C.num4).setValue(r.getIntCurrency(C.num4));
		cols.get(C.long1).setValue(r.getLongCurrency(C.long1));
		cols.get(C.item3).setValue(r.getString(C.item3));
		cols.get(C.session_id).setValue(r.getString(C.session_id));
		cols.get(C.page_id).setValue(r.getString(C.page_id));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		return ItemCategory.getAll(fi);
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
		String item_desc = Helper.getString(params, C.item_desc, false);
		String create_user = Helper.getString(params, C.create_user, false);
		try {
			int count = generateReport(fi, start_time, end_time, category_ids, item_desc, create_user);
			conn.commit();
			return String.valueOf(count);
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}
	
	private class Report {
		public Timestamp date;
		public String category;
		public String product_name;
		public int qty;
		public int buy_price;
		public int discount;
		public int total;
		public long order_id;
		public String purchase_by;
	}

	public int generateReport(FunctionItem fi, Timestamp start_time, Timestamp end_time, int[] category_ids, String item_desc, String create_user) throws Exception {
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.incoming_order_report_detail);
			pstmt.executeUpdate();
		}
		
		StringBuilder sb = new StringBuilder();
		if (category_ids.length > 0) {
			sb.append("d.category_id in (").append(StringUtils.join(category_ids, ',')).append(") and ");
		}
		if (!Helper.isNullOrEmpty(item_desc)) {
			sb.append("c.item_desc like '%").append(item_desc).append("%' and ");
		}
		if (!Helper.isNullOrEmpty(create_user)) {
			sb.append("a.create_user like '").append(create_user).append("' and ");
		}
		
		/*
select a.create_time, d.category_name, c.item_desc, b.qty, b.price_buy, b.discount, b.total, a.order_id, a.create_user
  from incoming_order a
 inner join incoming_order_item b on b.order_id = a.order_id
 inner join item c on c.item_id = b.item_id
 inner join item_category d on d.category_id = c.category_id
 where between ? and ?
		 */
		
		List<Report> rows = new ArrayList<>();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select a.create_time, d.category_name, c.item_desc, b.qty, b.price_buy, b.discount, b.total, a.order_id, a.create_user\r\n" + 
				"  from incoming_order a\r\n" + 
				" inner join incoming_order_item b on b.order_id = a.order_id\r\n" + 
				" inner join item c on c.item_id = b.item_id\r\n" + 
				" inner join item_category d on d.category_id = c.category_id\r\n" + 
				" where " + sb + " a.create_time between ? and ?"))) {
			pstmt.setTimestamp(1, start_time);
			pstmt.setTimestamp(2, end_time);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					Report s = new Report();
					s.date = r.getTimestamp(1);
					s.category = r.getString(2);
					s.product_name = r.getString(3);
					s.qty = r.getInt(4);
					s.buy_price = r.getInt(5);
					s.discount = r.getInt(6);
					s.total = r.getInt(7);
					s.order_id = r.getLong(8);
					s.purchase_by = r.getString(9);
					rows.add(s);
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into sql_temp_table (session_id, page_id, date1, item1, item2, num1, num2, num3, num4, long1, item3) values (?,?,?,?,?,?,?,?,?,?,?)"))) {
			int count = 0;
			for (Report s : rows) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.incoming_order_report_detail);
				pstmt.setTimestamp(3, s.date);
				pstmt.setString(4, s.category);
				pstmt.setString(5, s.product_name);
				pstmt.setInt(6, s.qty);
				pstmt.setInt(7, s.buy_price);
				pstmt.setInt(8, s.discount);
				pstmt.setInt(9, s.total);
				pstmt.setLong(10, s.order_id);
				pstmt.setString(11, s.purchase_by);
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
