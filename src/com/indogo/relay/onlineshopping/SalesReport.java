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

public class SalesReport extends AbstractFunction implements ITablePage {

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
		cols.add(new TablePageColumn(C.long2, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total Price"));
		cols.add(new TablePageColumn(C.num1, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Current Stock"));
		cols.add(new TablePageColumn(C.num2, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Unit Cost"));
		cols.add(new TablePageColumn(C.num3, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Unit Price"));
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
		cols.get(C.num1).setValue(r.getLongCurrency(C.num1));
		cols.get(C.num2).setValue(r.getLongCurrency(C.num2));
		cols.get(C.num3).setValue(r.getLongCurrency(C.num3));
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
			int count = getSummary(fi, start_time, end_time, status_ids, category_ids, sales_ids, SalesStatus.get(lm_time_type));
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
		public long sum_sales_qty;
		public long sum_sales_total;
		public Integer current_stock;
		public Integer unit_cost;
		public Integer unit_price;
	}
	
	public int getSummary(FunctionItem fi, Timestamp start_time, Timestamp end_time, int[] status_ids, int[] category_ids, long[] sales_ids, SalesStatus lm_time_type) throws Exception {
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.sales_report);
			pstmt.executeUpdate();
		}
		
		StringBuilder sb = new StringBuilder();
		if (status_ids.length > 0) {
			sb.append("a.status_id in (").append(StringUtils.join(status_ids, ',')).append(") and ");
		}
		if (category_ids.length > 0) {
			sb.append("d.category_id in (").append(StringUtils.join(category_ids, ',')).append(") and ");
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
		
		List<Summary> rows = new ArrayList<>();
		/*
select d.item_name, d.item_desc, g.category_name, e.color_name, f.size_name, sum(b.sales_qty), sum(b.sales_total),
(select sum(item_qty) from inventory where item_id = b.item_id) as current_stock, h.price_buy, max(d.price_sale) as unit_price
  from sales a
 inner join sales_item b on a.sales_id = b.sales_id
 inner join shop c on a.shop_id = c.shop_id
 inner join item d on b.item_id = d.item_id
  left join item_color e on d.color_id = e.color_id
  left join item_size f on d.size_id = f.size_id
 inner join item_category g on d.category_id = g.category_id
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
       ) h on h.item_id = b.item_id
 where between ? and ?
 group by d.item_name, d.item_desc, g.category_name, e.color_name, f.size_name
		 */
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select d.item_name, d.item_desc, g.category_name, e.color_name, f.size_name, sum(b.sales_qty), sum(b.sales_total),\r\n" + 
				"(select sum(item_qty) from inventory where item_id = b.item_id) as current_stock, h.price_buy, max(d.price_sale) as unit_price\r\n" + 
				"  from sales a\r\n" + 
				" inner join sales_item b on a.sales_id = b.sales_id\r\n" + 
				" inner join shop c on a.shop_id = c.shop_id\r\n" + 
				" inner join item d on b.item_id = d.item_id\r\n" + 
				"  left join item_color e on d.color_id = e.color_id\r\n" + 
				"  left join item_size f on d.size_id = f.size_id\r\n" + 
				" inner join item_category g on d.category_id = g.category_id\r\n" + 
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
				"       ) h on h.item_id = b.item_id\r\n" + 
				" where " + sb + " between ? and ?\r\n" + 
				" group by d.item_name, d.item_desc, g.category_name, e.color_name, f.size_name"))) {
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
					s.sum_sales_qty = r.getLong(6);
					s.sum_sales_total = r.getLong(7);
					s.current_stock = r.getInt(8);
					s.unit_cost = r.getInt(9);
					s.unit_price = r.getInt(10);
					rows.add(s);
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into sql_temp_table (session_id, page_id, item1, item2, item3, item4, item5, long1, long2, num1, num2, num3) values (?,?,?,?,?,?,?,?,?,?,?,?)"))) {
			int count = 0;
			for (Summary s : rows) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.sales_report);
				pstmt.setString(3, s.item_name);
				pstmt.setString(4, s.item_desc);
				pstmt.setString(5, s.category_name);
				pstmt.setString(6, s.color_name);
				pstmt.setString(7, s.size_name);
				pstmt.setLong(8, s.sum_sales_qty);
				pstmt.setLong(9, s.sum_sales_total);
				pstmt.setInt(10, s.current_stock);
				pstmt.setInt(11, s.unit_cost);
				pstmt.setInt(12, s.unit_price);
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
