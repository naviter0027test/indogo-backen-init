package com.indogo.relay.onlineshopping;

import java.sql.Connection;
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

public class InventoryReport extends AbstractFunction implements ITablePage {

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
		cols.add(new TablePageColumn(C.num1, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Unit Cost"));
		cols.add(new TablePageColumn(C.num2, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Unit Sales"));
		cols.add(new TablePageColumn(C.num3, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Current Qty"));
		cols.add(new TablePageColumn(C.long1, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total Cost"));
		cols.add(new TablePageColumn(C.long2, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total Sales"));
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
		cols.get(C.num1).setValue(r.getLongCurrency(C.num1));
		cols.get(C.num2).setValue(r.getLongCurrency(C.num2));
		cols.get(C.num3).setValue(r.getLongCurrency(C.num3));
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
		int[] category_ids = Helper.getIntArray(params, C.category_ids, false);
		try {
			int count = getSummary(fi, category_ids);
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
		public Integer unit_cost;
		public Integer unit_sales;
		public Integer current_qty;
		public Long total_cost;
		public Long total_sales;
	}
	
	public int getSummary(FunctionItem fi, int[] category_ids) throws Exception {
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.inventory_report);
			pstmt.executeUpdate();
		}
		
		StringBuilder sb = new StringBuilder();
		if (category_ids.length > 0) {
			sb.append("where b.category_id in (").append(StringUtils.join(category_ids, ',')).append(")\r\n");
		}
		
		List<Summary> rows = new ArrayList<>();
		/*
select b.item_name, b.item_desc, e.category_name, c.color_name, d.size_name, max(f.price_buy), max(b.price_sale), sum(a.item_qty), max(f.price_buy) * sum(a.item_qty), max(b.price_sale) * sum(a.item_qty)
  from inventory a
 inner join item b on b.item_id = a.item_id
  left join item_color c on c.color_id = b.color_id
  left join item_size d on d.size_id = b.size_id
 inner join item_category e on e.category_id = b.category_id
  left join (
       select b.item_id, b.price_buy
         from incoming_order a
        inner join incoming_order_item b on b.order_id = a.order_id
        inner join (
              select b.item_id, max(a.order_id) as order_id
                from incoming_order a
               inner join incoming_order_item b on b.order_id = a.order_id
               where a.status_id = 1
                 and b.price_buy > 0
               group by b.item_id
              ) c on c.item_id = b.item_id and a.order_id = c.order_id
       ) f on f.item_id = a.item_id
group by b.item_name, b.item_desc, e.category_name, c.color_name, d.size_name
		 */
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select b.item_name, b.item_desc, e.category_name, c.color_name, d.size_name, max(f.price_buy), max(b.price_sale), sum(a.item_qty), max(f.price_buy) * sum(a.item_qty), max(b.price_sale) * sum(a.item_qty)\r\n" + 
				"  from inventory a\r\n" + 
				" inner join item b on b.item_id = a.item_id\r\n" + 
				"  left join item_color c on c.color_id = b.color_id\r\n" + 
				"  left join item_size d on d.size_id = b.size_id\r\n" + 
				" inner join item_category e on e.category_id = b.category_id\r\n" + 
				"  left join (\r\n" + 
				"       select b.item_id, b.price_buy\r\n" + 
				"         from incoming_order a\r\n" + 
				"        inner join incoming_order_item b on b.order_id = a.order_id\r\n" + 
				"        inner join (\r\n" + 
				"              select b.item_id, max(a.order_id) as order_id\r\n" + 
				"                from incoming_order a\r\n" + 
				"               inner join incoming_order_item b on b.order_id = a.order_id\r\n" + 
				"               where a.status_id = 1\r\n" + 
				"                 and b.price_buy > 0\r\n" + 
				"               group by b.item_id\r\n" + 
				"              ) c on c.item_id = b.item_id and a.order_id = c.order_id\r\n" + 
				"       ) f on f.item_id = a.item_id\r\n" + 
				sb +
				"group by b.item_name, b.item_desc, e.category_name, c.color_name, d.size_name"))) {
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					Summary s = new Summary();
					s.item_name = r.getString(1);
					s.item_desc = r.getString(2);
					s.category_name = r.getString(3);
					s.color_name = r.getString(4);
					s.size_name = r.getString(5);
					s.unit_cost = r.getInt(6);
					s.unit_sales = r.getInt(7);
					s.current_qty = r.getInt(8);
					s.total_cost = r.getLong(9);
					s.total_sales = r.getLong(10);
					rows.add(s);
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into sql_temp_table (session_id, page_id, item1, item2, item3, item4, item5, num1, num2, num3, long1, long2) values (?,?,?,?,?,?,?,?,?,?,?,?)"))) {
			int count = 0;
			for (Summary s : rows) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.inventory_report);
				pstmt.setString(3, s.item_name);
				pstmt.setString(4, s.item_desc);
				pstmt.setString(5, s.category_name);
				pstmt.setString(6, s.color_name);
				pstmt.setString(7, s.size_name);
				pstmt.setInt(8, s.unit_cost);
				pstmt.setInt(9, s.unit_sales);
				pstmt.setInt(10, s.current_qty);
				pstmt.setLong(11, s.total_cost);
				pstmt.setLong(12, s.total_sales);
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
