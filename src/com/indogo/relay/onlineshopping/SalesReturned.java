package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indogo.InventoryHistoryReason;
import com.indogo.model.onlineshopping.Freight;
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
import com.lionpig.webui.http.util.Stringify;

public class SalesReturned extends AbstractFunction implements ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.sales_returned_v;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.action, C.columnTypeString, C.columnDirectionNone, false, true, C.emptyString));
		cols.add(new TablePageColumn(C.status_name, C.columnTypeString, C.columnDirectionNone, false, true, "Status"));
		cols.add(new TablePageColumn(C.sales_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.sales_id));
		cols.add(new TablePageColumn(C.ship_address, C.columnTypeString, C.columnDirectionDefault, true, false, "Shipping Address"));
		cols.add(new TablePageColumn(C.total_amount, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total"));
		cols.add(new TablePageColumn(C.invoice_no, C.columnTypeString, C.columnDirectionDefault, true, false, "發票號碼"));
		cols.add(new TablePageColumn(C.ship_no, C.columnTypeString, C.columnDirectionDefault, true, false, "Shipping Number"));
		cols.add(new TablePageColumn(C.freight_name, C.columnTypeString, C.columnDirectionNone, false, true, "Shipping Type"));
		cols.add(new TablePageColumn(C.member_name, C.columnTypeString, C.columnDirectionDefault, true, false, "Member Name"));
		cols.add(new TablePageColumn(C.phone_no, C.columnTypeString, C.columnDirectionDefault, true, false, "Phone No"));
		cols.add(new TablePageColumn(C.lm_time_created, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Create Time"));
		cols.add(new TablePageColumn(C.comment, C.columnTypeString, C.columnDirectionDefault, true, false, "Comment"));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, l.lm_time()));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, l.lm_user()));
		cols.add(new TablePageColumn(C.shop_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.shop_id, true, true));
		cols.add(new TablePageColumn(C.status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.status_id, true, true));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, true));
		cols.add(new TablePageColumn(C.freight_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.freight_id, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		L l = fi.getLanguage();
		SalesStatus status = SalesStatus.get(r.unwrap().unwrap().getInt(C.status_id));
		Freight freight = Freight.get(r.unwrap().unwrap().getInt(C.freight_id));
		
		cols.get(C.action).setValue(C.emptyString);
		cols.get(C.status_name).setValue(status.getName(l));
		cols.get(C.sales_id).setValue(r.getLong(C.sales_id));
		cols.get(C.ship_address).setValue(r.getString(C.ship_address));
		cols.get(C.total_amount).setValue(r.getIntCurrency(C.total_amount));
		cols.get(C.invoice_no).setValue(r.getString(C.invoice_no));
		cols.get(C.ship_no).setValue(r.getString(C.ship_no));
		cols.get(C.freight_name).setValue(freight.getName(l));
		cols.get(C.member_name).setValue(r.getString(C.member_name));
		cols.get(C.phone_no).setValue(r.getString(C.phone_no));
		cols.get(C.lm_time_created).setValue(r.getTimestamp(C.lm_time_created));
		cols.get(C.comment).setValue(r.getString(C.comment));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.shop_id).setValue(r.getInt(C.shop_id));
		cols.get(C.status_id).setValue(String.valueOf(status.getId()));
		cols.get(C.member_id).setValue(r.getLong(C.member_id));
		cols.get(C.freight_id).setValue(String.valueOf(freight.getId()));
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
		// TODO Auto-generated method stub
		return null;
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
		long sales_id = Helper.getLong(params, C.sales_id, true);
		Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
		String comment = Helper.getString(params, C.comment, true);
		Integer money_returned = Helper.getIntNullable(params, C.money_returned, false);
		Connection conn = fi.getConnection().getConnection();
		try {
			DeleteInfo info = delete(fi, sales_id, lm_time, comment, money_returned);
			conn.commit();
			return Stringify.getTimestamp(info.lm_time) + C.char_31 + info.lm_user + C.char_31 + SalesStatus.returned.getName(l) + C.char_31 + SalesStatus.returned.getId();
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public class DeleteInfo {
		public Timestamp lm_time;
		public String lm_user;
	}

	public DeleteInfo delete(FunctionItem fi, long sales_id, Timestamp lm_time, String comment, Integer money_returned) throws Exception {
		L l = fi.getLanguage();
		Connection conn = fi.getConnection().getConnection();
		Timestamp old_lm_time;
		int shop_id, status_id;
		long member_id;
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select lm_time, shop_id, status_id, member_id from sales where sales_id = ? for update"))) {
			pstmt.setLong(1, sales_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				if (r.next()) {
					old_lm_time = r.getTimestamp(1);
					shop_id = r.getInt(2);
					status_id = r.getInt(3);
					member_id = r.getLong(4);
				} else {
					throw new Exception(l.data_not_exist("sales_id = " + sales_id));
				}
			}
		}
		
		SalesStatus status = SalesStatus.get(status_id);
		if (status != SalesStatus.shipped) {
			if (status == SalesStatus.paid) {
				if (money_returned == null) {
					throw new Exception(l.status_not_allowed());
				}
			} else {
				throw new Exception(l.status_not_allowed());
			}
		}
		
		if (lm_time.getTime() != old_lm_time.getTime()) {
			throw new Exception(l.data_already_updated_by_another_user("sales_id = " + sales_id));
		}
		
		List<Entry<Integer, Integer>> items = new ArrayList<>();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select item_id, sales_qty from sales_item where sales_id = ? order by item_id"))) {
			pstmt.setLong(1, sales_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					Entry<Integer, Integer> item = new AbstractMap.SimpleEntry<Integer, Integer>(r.getInt(1), r.getInt(2));
					items.add(item);
				}
			}
		}
		
		Timestamp new_lm_time = fi.getConnection().getCurrentTime();
		String new_lm_user = fi.getSessionInfo().getUserName();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update sales set status_id = ?, lm_time = ?, lm_user = ?, lm_time_returned = ?, lm_user_returned = ?, comment = ? where sales_id = ?"))) {
			pstmt.setInt(1, SalesStatus.returned.getId());
			pstmt.setTimestamp(2, new_lm_time);
			pstmt.setString(3, new_lm_user);
			pstmt.setTimestamp(4, new_lm_time);
			pstmt.setString(5, new_lm_user);
			pstmt.setString(6, comment);
			pstmt.setLong(7, sales_id);
			pstmt.executeUpdate();
		}
		
		// update inventories
		S sql = fi.getSql();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_select_for_update()))) {
			try (PreparedStatementWrapper pstmtUpd = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_update()))) {
				try (PreparedStatementWrapper pstmtHistory = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_history_insert()))) {
					for (Entry<Integer, Integer> item : items) {
						int item_id = item.getKey();
						int sales_qty = item.getValue();
						
						pstmt.setInt(1, shop_id);
						pstmt.setInt(2, item_id);
						int item_old_qty;
						try (ResultSet r = pstmt.executeQuery()) {
							if (r.next()) {
								item_old_qty = r.getInt(1);
							} else {
								throw new Exception("inventory for item id [" + item_id + "] not exist");
							}
						}
						
						int item_diff_qty = sales_qty;
						int item_new_qty = item_old_qty + item_diff_qty;
						
						pstmtUpd.setInt(1, item_new_qty);
						pstmtUpd.setTimestamp(2, new_lm_time);
						pstmtUpd.setString(3, new_lm_user);
						pstmtUpd.setInt(4, shop_id);
						pstmtUpd.setInt(5, item_id);
						pstmtUpd.executeUpdate();
						
						pstmtHistory.setLong(1, Helper.randomSeq());
						pstmtHistory.setInt(2, InventoryHistoryReason.sales_cancel.getId());
						pstmtHistory.setInt(3, shop_id);
						pstmtHistory.setInt(4, item_id);
						pstmtHistory.setInt(5, item_old_qty);
						pstmtHistory.setInt(6, item_diff_qty);
						pstmtHistory.setInt(7, item_new_qty);
						pstmtHistory.setTimestamp(8, new_lm_time);
						pstmtHistory.setString(9, new_lm_user);
						pstmtHistory.setLong(10, sales_id);
						pstmtHistory.setLong(11, null);
						pstmtHistory.setLong(12, null);
						pstmtHistory.setLong(13, null);
						pstmtHistory.executeUpdate();
					}
				}
			}
		}
		
		if (money_returned != null) {
			int wallet;
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select wallet from member where member_id = ? for update"))) {
				pstmt.setLong(1, member_id);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						wallet = r.getInt(1);
					} else {
						throw new Exception(l.data_not_exist(C.member_id, member_id));
					}
				}
			}
			
			wallet += money_returned.intValue();
			
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update member set wallet = ? where member_id = ?"))) {
				pstmt.setInt(1, wallet);
				pstmt.setLong(2, member_id);
				pstmt.executeUpdate();
			}
			
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into member_wallet_hst (member_id, lm_time, wallet, sales_id) values (?,?,?,?)"))) {
				pstmt.setLong(1, member_id);
				pstmt.setTimestamp(2, lm_time);
				pstmt.setInt(3, money_returned);
				pstmt.setLong(4, sales_id);
				pstmt.executeUpdate();
			}
		}
		
		DeleteInfo info = new DeleteInfo();
		info.lm_time = new_lm_time;
		info.lm_user = new_lm_user;
		return info;
	}
}
