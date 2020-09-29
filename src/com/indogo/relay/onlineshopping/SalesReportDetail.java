package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

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

public class SalesReportDetail extends AbstractFunction implements ITablePage {

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
		cols.add(new TablePageColumn(C.item1, C.columnTypeString, C.columnDirectionDefault, true, false, "Status"));
		cols.add(new TablePageColumn(C.item2, C.columnTypeString, C.columnDirectionDefault, true, false, "Invoice Number"));
		cols.add(new TablePageColumn(C.item3, C.columnTypeString, C.columnDirectionDefault, true, false, "Shipping Number"));
		cols.add(new TablePageColumn(C.date1, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Date"));
		cols.add(new TablePageColumn(C.item4, C.columnTypeString, C.columnDirectionDefault, true, false, "Member Name"));
		cols.add(new TablePageColumn(C.item5, C.columnTypeString, C.columnDirectionDefault, true, false, "Category Name"));
		cols.add(new TablePageColumn(C.item6, C.columnTypeString, C.columnDirectionDefault, true, false, "Product Name"));
		cols.add(new TablePageColumn(C.num1, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Quantity"));
		cols.add(new TablePageColumn(C.num2, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Unit Cost"));
		cols.add(new TablePageColumn(C.num3, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Unit Price"));
		cols.add(new TablePageColumn(C.long1, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total Cost"));
		cols.add(new TablePageColumn(C.long2, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total Price"));
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
		cols.get(C.date1).setValue(r.getTimestamp(C.date1));
		cols.get(C.item4).setValue(r.getString(C.item4));
		cols.get(C.item5).setValue(r.getString(C.item5));
		cols.get(C.item6).setValue(r.getString(C.item6));
		cols.get(C.num1).setValue(r.getIntCurrency(C.num1));
		cols.get(C.num2).setValue(r.getIntCurrency(C.num2));
		cols.get(C.num3).setValue(r.getIntCurrency(C.num3));
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
		int[] status_ids = Helper.getIntArray(params, C.status_ids, false);
		int[] category_ids = Helper.getIntArray(params, C.category_ids, false);
		long[] sales_ids = Helper.getLongArray(params, C.sales_ids, false);
		int lm_time_type = Helper.getInt(params, C.lm_time_type, true);
		try {
			int count = generateReport(fi, start_time, end_time, status_ids, category_ids, sales_ids, SalesStatus.get(lm_time_type));
			conn.commit();
			return String.valueOf(count);
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}
	
	private class Report {
		public String status;
		public String invoice_number;
		public String shipping_number;
		public Timestamp date;
		public String member_name;
		public String category_name;
		public String product_name;
		public Integer quantity;
		public Integer unit_cost;
		public Integer unit_price;
		public Long total_cost;
		public Long total_price;
	}
	
	public int generateReport(FunctionItem fi, Timestamp start_time, Timestamp end_time, int[] status_ids, int[] category_ids, long[] sales_ids, SalesStatus lm_time_type) throws Exception {
		S sql = fi.getSql();
		L l = fi.getLanguage();
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.sales_report_detail);
			pstmt.executeUpdate();
		}
		
		StringBuilder sb = new StringBuilder();
		if (status_ids.length > 0) {
			sb.append("a.status_id in (").append(StringUtils.join(status_ids, ',')).append(") and ");
		}
		if (category_ids.length > 0) {
			sb.append("e.category_id in (").append(StringUtils.join(category_ids, ',')).append(") and ");
		}
		if (sales_ids.length > 0) {
			sb.append("a.sales_id in (").append(StringUtils.join(sales_ids, ',')).append(") and ");
		}
		
		switch (lm_time_type) {
			case created:
				sb.append("a.lm_time_created");
				break;
			case checkout:
				sb.append("a.lm_time_checkout");
				break;
			case shipped:
				sb.append("a.lm_time_shipped");
				break;
			case paid:
				sb.append("a.lm_time_paid");
				break;
			case returned:
				sb.append("a.lm_time_returned");
				break;
			case scrapped:
				sb.append("a.lm_time_scrapped");
				break;
		}
		
		List<Report> rows = new ArrayList<>();
		/*
select a.status_id, a.sales_id, a.ship_no, a.lm_time_created, c.member_name, e.category_name, d.item_desc, b.sales_qty, f.price_buy as unit_cost, b.sales_price as unit_price, (b.sales_qty * f.price_buy) as total_cost, b.sales_total as total_price
  from sales a
 inner join sales_item b on b.sales_id = a.sales_id
 inner join member c on c.member_id = a.member_id
 inner join item d on d.item_id = b.item_id
 inner join item_category e on e.category_id = d.category_id
  left join (
       select b.item_id, b.price_buy
         from incoming_order a
        inner join incoming_order_item b on b.order_id = a.order_id
        inner join (
              select b.item_id, max(a.order_id) as order_id
                from incoming_order a
               inner join incoming_order_item b on b.order_id = a.order_id
               where a.status_id = 1
               group by b.item_id
              ) c on c.item_id = b.item_id and a.order_id = c.order_id
       ) f on f.item_id = b.item_id
 where a.lm_time_created between ? and ?
		 */
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select a.status_id, a.sales_id, a.ship_no, a.lm_time_created, c.member_name, e.category_name, d.item_desc, b.sales_qty, f.price_buy as unit_cost, b.sales_price as unit_price, (b.sales_qty * f.price_buy) as total_cost, b.sales_total as total_price\r\n" + 
				"  from sales a\r\n" + 
				" inner join sales_item b on b.sales_id = a.sales_id\r\n" + 
				" inner join member c on c.member_id = a.member_id\r\n" + 
				" inner join item d on d.item_id = b.item_id\r\n" + 
				" inner join item_category e on e.category_id = d.category_id\r\n" + 
				"  left join (\r\n" + 
				"       select b.item_id, b.price_buy\r\n" + 
				"         from incoming_order a\r\n" + 
				"        inner join incoming_order_item b on b.order_id = a.order_id\r\n" + 
				"        inner join (\r\n" + 
				"              select b.item_id, max(a.order_id) as order_id\r\n" + 
				"                from incoming_order a\r\n" + 
				"               inner join incoming_order_item b on b.order_id = a.order_id\r\n" + 
				"               where a.status_id = 1\r\n" + 
				"               group by b.item_id\r\n" + 
				"              ) c on c.item_id = b.item_id and a.order_id = c.order_id\r\n" + 
				"       ) f on f.item_id = b.item_id\r\n" + 
				" where " + sb + " between ? and ?"))) {
			pstmt.setTimestamp(1, start_time);
			pstmt.setTimestamp(2, end_time);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					Report s = new Report();
					s.status = SalesStatus.get(r.getInt(1)).getName(l);
					s.invoice_number = r.getString(2);
					s.shipping_number = r.getString(3);
					s.date = r.getTimestamp(4);
					s.member_name = r.getString(5);
					s.category_name = r.getString(6);
					s.product_name = r.getString(7);
					s.quantity = r.getInt(8);
					s.unit_cost = r.getInt(9);
					s.unit_price = r.getInt(10);
					s.total_cost = r.getLong(11);
					s.total_price = r.getLong(12);
					rows.add(s);
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into sql_temp_table (session_id, page_id, item1, item2, item3, date1, item4, item5, item6, num1, num2, num3, long1, long2) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"))) {
			int count = 0;
			for (Report s : rows) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.sales_report_detail);
				pstmt.setString(3, s.status);
				pstmt.setString(4, s.invoice_number);
				pstmt.setString(5, s.shipping_number);
				pstmt.setTimestamp(6, s.date);
				pstmt.setString(7, s.member_name);
				pstmt.setString(8, s.category_name);
				pstmt.setString(9, s.product_name);
				pstmt.setInt(10, s.quantity);
				pstmt.setInt(11, s.unit_cost);
				pstmt.setInt(12, s.unit_price);
				pstmt.setLong(13, s.total_cost);
				pstmt.setLong(14, s.total_price);
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
