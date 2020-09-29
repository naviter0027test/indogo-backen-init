package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indogo.InventoryHistoryReason;
import com.indogo.model.onlineshopping.StockTransferItemModel;
import com.indogo.model.onlineshopping.StockTransferModel;
import com.indogo.model.onlineshopping.StockTransferStatus;
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

public class StockTransfer extends AbstractFunction implements ITablePage {
	
	private PreparedStatementWrapper pstmtShop;
	private Map<Integer, String> shopNames;

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.stock_transfer;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.action, C.columnTypeString, C.columnDirectionNone, false, true, C.emptyString));
		cols.add(new TablePageColumn(C.create_time, C.columnTypeDateTime, C.columnDirectionAsc, true, false, l.create_time()));
		cols.add(new TablePageColumn(C.transfer_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.transfer_id()));
		cols.add(new TablePageColumn(C.status_name, C.columnTypeString, C.columnDirectionNone, false, true, l.status_name()));
		cols.add(new TablePageColumn(C.shop_name_from, C.columnTypeString, C.columnDirectionNone, false, true, l.shop_name_from()));
		cols.add(new TablePageColumn(C.shop_name_to, C.columnTypeString, C.columnDirectionNone, false, true, l.shop_name_to()));
		cols.add(new TablePageColumn(C.comment, C.columnTypeString, C.columnDirectionDefault, true, false, l.comment()));
		cols.add(new TablePageColumn(C.lm_time_1, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Create Time"));
		cols.add(new TablePageColumn(C.lm_user_1, C.columnTypeString, C.columnDirectionDefault, true, false, "Created By"));
		cols.add(new TablePageColumn(C.lm_time_2, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Collect Time"));
		cols.add(new TablePageColumn(C.lm_user_2, C.columnTypeString, C.columnDirectionDefault, true, false, "Collected By"));
		cols.add(new TablePageColumn(C.lm_time_3, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Cancel Time"));
		cols.add(new TablePageColumn(C.lm_user_3, C.columnTypeString, C.columnDirectionDefault, true, false, "Canceled By"));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, l.lm_time()));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, l.lm_user()));
		cols.add(new TablePageColumn(C.shop_id_from, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.shop_id_from(), true, true));
		cols.add(new TablePageColumn(C.shop_id_to, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.shop_id_to(), true, true));
		cols.add(new TablePageColumn(C.status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.status_id(), true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		int shop_id_from = r.unwrap().getInt(C.shop_id_from);
		int shop_id_to = r.unwrap().getInt(C.shop_id_to);
		int status_id = r.unwrap().getInt(C.status_id);
		
		String shop_name_from = checkShopName(shop_id_from);
		String shop_name_to = checkShopName(shop_id_to);
		StockTransferStatus status = StockTransferStatus.get(status_id);
		
		cols.get(C.create_time).setValue(r.getTimestamp(C.create_time));
		cols.get(C.transfer_id).setValue(r.getLong(C.transfer_id));
		cols.get(C.status_name).setValue(status.getName(fi.getLanguage()));
		cols.get(C.shop_name_from).setValue(shop_name_from);
		cols.get(C.shop_name_to).setValue(shop_name_to);
		cols.get(C.comment).setValue(r.getString(C.comment));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.shop_id_from).setValue(String.valueOf(shop_id_from));
		cols.get(C.shop_id_to).setValue(String.valueOf(shop_id_to));
		cols.get(C.status_id).setValue(String.valueOf(status_id));
		cols.get(C.lm_time_1).setValue(r.getTimestamp(C.lm_time_1));
		cols.get(C.lm_user_1).setValue(r.getString(C.lm_user_1));
		cols.get(C.lm_time_2).setValue(r.getTimestamp(C.lm_time_2));
		cols.get(C.lm_user_2).setValue(r.getString(C.lm_user_2));
		cols.get(C.lm_time_3).setValue(r.getTimestamp(C.lm_time_3));
		cols.get(C.lm_user_3).setValue(r.getString(C.lm_user_3));
	}
	
	private String checkShopName(int shop_id) throws Exception {
		String shop_name = shopNames.get(shop_id);
		if (shop_name == null) {
			pstmtShop.setInt(1, shop_id);
			try (ResultSetWrapper rs = pstmtShop.executeQueryWrapper()) {
				if (rs.next()) {
					shop_name = rs.getString(1);
				} else {
					shop_name = C.emptyString;
				}
			}
			shopNames.put(shop_id, shop_name);
		}
		return shop_name;
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		shopNames = new HashMap<>();
		
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		pstmtShop = new PreparedStatementWrapper(conn.prepareStatement(sql.shop_get_for_inventory()));
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
		pstmtShop.close();
	}

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		if (action.equals(C.change_status)) {
			long transfer_id = Helper.getLong(params, C.transfer_id, true);
			Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
			String lm_user = Helper.getString(params, C.lm_user, true);
			StockTransferStatus to_status = StockTransferStatus.get(Helper.getInt(params, C.status_id, true));
			try {
				Timestamp new_time = changeStatus(fi, transfer_id, lm_time, lm_user, to_status);
				conn.commit();
				
				StringBuilder sb = new StringBuilder();
				sb.append(Stringify.getTimestamp(new_time))
				.append(C.char_31).append(fi.getSessionInfo().getUserName());
				return sb.toString();
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.sql_temp_table)) {
			int[] status_ids = Helper.getIntArray(params, C.status_ids, false);
			String item_desc = Helper.getString(params, C.item_desc, false);
			
			List<StockTransferStatus> statuses = new ArrayList<>();
			for (int status_id : status_ids) {
				statuses.add(StockTransferStatus.get(status_id));
			}
			try {
				int count = prepareTempTable(fi, statuses, item_desc);
				conn.commit();
				return String.valueOf(count);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		}
		return null;
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(l.create_time())
		.append(C.char_31).append(l.status_name())
		.append(C.char_31).append(StockTransferStatus.created.getName(l))
		.append(C.char_31).append(StockTransferStatus.collected.getName(l))
		.append(C.char_31).append(StockTransferStatus.cancel.getName(l))
		.append(C.char_31).append(l.button_search())
		.append(C.char_31).append(l.button_cancel())
		.append(C.char_31).append(l.button_accept())
		.append(C.char_31).append(l.shop_name())
		.append(C.char_31).append(l.button_print())
		.append(C.char_31).append(l.shop_name_from())
		.append(C.char_31).append(l.shop_name_to())
		.append(C.char_31).append(l.transfer_id())
		.append(C.char_31).append(l.transfer_qty())
		.append(C.char_31).append(l.item_name())
		.append(C.char_31).append(l.item_desc())
		.append(C.char_31).append(l.category_name())
		.append(C.char_31).append(l.color_name())
		.append(C.char_31).append(l.size_name());
		
		sb.append(C.char_31).append(Shop.getShopListForUser(fi));
		sb.append(C.char_31).append(Shop.getShopList(fi));
		
		return sb.toString();
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		return null;
	}

	@Override
	protected String onUpdate(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		return null;
	}

	@Override
	protected String onDelete(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		return null;
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		return null;
	}
	
	/**
	 * shop owner can accept or cancel stock transfer request that has shop_id_to equals to his shop.<br/>
	 * shop owner can only cancel stock transfer request that has shop_id_from equals to his shop.
	 * @param fi
	 * @param transfer_id
	 * @param lm_time
	 * @param lm_user
	 * @param to_status
	 * @return new lm_time
	 * @throws Exception
	 */
	public Timestamp changeStatus(FunctionItem fi, long transfer_id, Timestamp lm_time, String lm_user, StockTransferStatus to_status) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		L l = fi.getLanguage();
		S sql = fi.getSql();
		
		StockTransferModel stockTransferModel = new StockTransferModel();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.stock_transfer_select_for_update()))) {
			pstmt.setLong(1, transfer_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				if (r.next()) {
					Timestamp old_lm_time = r.getTimestamp(1);
					String old_lm_user = r.getString(2);
					if (lm_time.getTime() != old_lm_time.getTime() || !lm_user.equals(old_lm_user)) {
						throw new Exception(l.data_already_updated_by_another_user(C.transfer_id, String.valueOf(transfer_id)));
					}
					StockTransferStatus old_status = StockTransferStatus.get(r.getInt(3));
					if (old_status != StockTransferStatus.created) {
						throw new Exception(l.status_not_allowed());
					}
					
					stockTransferModel.transfer_id = transfer_id;
					stockTransferModel.shop_id_from = r.getInt(4);
					stockTransferModel.shop_id_to = r.getInt(5);
				} else {
					throw new Exception(l.data_not_exist(C.transfer_id, String.valueOf(transfer_id)));
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.shop_user_check()))) {
			pstmt.setInt(1, stockTransferModel.shop_id_from);
			pstmt.setInt(2, fi.getSessionInfo().getUserRowId());
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				if (r.next()) {
					int count = r.getInt(1);
					if (count == 0) {
						throw new Exception(l.exception_not_shop_owner());
					}
				} else {
					throw new Exception(l.exception_not_shop_owner());
				}
			}
			
			if (to_status == StockTransferStatus.collected) {
				pstmt.setInt(1, stockTransferModel.shop_id_to);
				pstmt.setInt(2, fi.getSessionInfo().getUserRowId());
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						int count = r.getInt(1);
						if (count == 0) {
							throw new Exception(l.exception_not_shop_owner());
						}
					} else {
						throw new Exception(l.exception_not_shop_owner());
					}
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.stock_transfer_item_get_all()))) {
			pstmt.setLong(1, transfer_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				List<StockTransferItemModel> items = new ArrayList<>();
				while (r.next()) {
					StockTransferItemModel m = new StockTransferItemModel();
					m.item_id = r.getInt(1);
					m.qty = r.getInt(2);
					items.add(m);
				}
				
				stockTransferModel.items = new StockTransferItemModel[items.size()];
				for (int i = 0; i < items.size(); i++) {
					stockTransferModel.items[i] = items.get(i);
				}
			}
		}
		
		Timestamp current_time = fi.getConnection().getCurrentTime();
		
		switch (to_status) {
			case collected:
				updateInventoryQty(fi, stockTransferModel.transfer_id, stockTransferModel.shop_id_to, stockTransferModel.items, current_time, to_status);
				break;
			case cancel:
				updateInventoryQty(fi, stockTransferModel.transfer_id, stockTransferModel.shop_id_from, stockTransferModel.items, current_time, to_status);
				break;
			default:
				throw new Exception(l.status_not_allowed());
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.stock_transfer_change_status(to_status)))) {
			pstmt.setInt(1, to_status.getId());
			pstmt.setTimestamp(2, current_time);
			pstmt.setString(3, fi.getSessionInfo().getUserName());
			pstmt.setTimestamp(4, current_time);
			pstmt.setString(5, fi.getSessionInfo().getUserName());
			pstmt.setLong(6, transfer_id);
			pstmt.executeUpdate();
		}
		
		return current_time;
	}
	
	private void updateInventoryQty(FunctionItem fi, long transfer_id, int shop_id, StockTransferItemModel[] items, Timestamp current_time, StockTransferStatus status) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		S sql = fi.getSql();
		InventoryHistoryReason historyReason;
		if (status == StockTransferStatus.collected) {
			historyReason = InventoryHistoryReason.stock_transfer_collect;
		} else {
			historyReason = InventoryHistoryReason.stock_transfer_cancel;
		}
		
		int[] item_ids = new int[items.length];
		for (int i = 0; i < item_ids.length; i++) {
			item_ids[i] = items[i].item_id;
		}
		Arrays.sort(item_ids);
		
		List<StockTransferItemModel> listUpdate = new ArrayList<>();
		List<StockTransferItemModel> listInsert = new ArrayList<>();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_select_for_update()))) {
			for (StockTransferItemModel item : items) {
				pstmt.setInt(1, shop_id);
				pstmt.setInt(2, item.item_id);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						item.current_qty = r.getInt(1);
						listUpdate.add(item);
					} else {
						listInsert.add(item);
					}
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_update()))) {
			try (PreparedStatementWrapper pstmtHistory = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_history_insert()))) {
				for (StockTransferItemModel item : listUpdate) {
					int old_qty = item.current_qty;
					int diff_qty = item.qty;
					int new_qty = old_qty + diff_qty;
					
					pstmt.setInt(1, new_qty);
					pstmt.setTimestamp(2, current_time);
					pstmt.setString(3, fi.getSessionInfo().getUserName());
					pstmt.setInt(4, shop_id);
					pstmt.setInt(5, item.item_id);
					pstmt.executeUpdate();
					
					pstmtHistory.setLong(1, Helper.randomSeq());
					pstmtHistory.setInt(2, historyReason.getId());
					pstmtHistory.setInt(3, shop_id);
					pstmtHistory.setInt(4, item.item_id);
					pstmtHistory.setInt(5, old_qty);
					pstmtHistory.setInt(6, diff_qty);
					pstmtHistory.setInt(7, new_qty);
					pstmtHistory.setTimestamp(8, current_time);
					pstmtHistory.setString(9, fi.getSessionInfo().getUserName());
					pstmtHistory.setLong(10, null);
					pstmtHistory.setLong(11, null);
					pstmtHistory.setLong(12, null);
					pstmtHistory.setLong(13, transfer_id);
					pstmtHistory.executeUpdate();
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_insert()))) {
			try (PreparedStatementWrapper pstmtHistory = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_history_insert()))) {
				for (StockTransferItemModel item : listInsert) {
					pstmt.setInt(1, shop_id);
					pstmt.setInt(2, item.item_id);
					pstmt.setInt(3, item.qty);
					pstmt.setTimestamp(4, current_time);
					pstmt.setString(5, fi.getSessionInfo().getUserName());
					pstmt.executeUpdate();
					
					pstmtHistory.setLong(1, Helper.randomSeq());
					pstmtHistory.setInt(2, historyReason.getId());
					pstmtHistory.setInt(3, shop_id);
					pstmtHistory.setInt(4, item.item_id);
					pstmtHistory.setInt(5, 0);
					pstmtHistory.setInt(6, item.qty);
					pstmtHistory.setInt(7, item.qty);
					pstmtHistory.setTimestamp(8, current_time);
					pstmtHistory.setString(9, fi.getSessionInfo().getUserName());
					pstmtHistory.setLong(10, null);
					pstmtHistory.setLong(11, null);
					pstmtHistory.setLong(12, null);
					pstmtHistory.setLong(13, transfer_id);
					pstmtHistory.executeUpdate();
				}
			}
		}
	}
	
	public int prepareTempTable(FunctionItem fi, List<StockTransferStatus> statuses, String item_desc) throws Exception {
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.stock_transfer);
			pstmt.executeUpdate();
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("select a.transfer_id from stock_transfer a inner join stock_transfer_item b on a.transfer_id = b.transfer_id where a.status_id in (");
		for (int i = 0; i < statuses.size(); i++) {
			sb.append("?,");
		}
		sb.delete(sb.length() - 1, sb.length()).append(") and b.item_id in (select item_id from item where item_desc like ?)");
		
		List<Long> transfer_ids = new ArrayList<>();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sb.toString()))) {
			int pos = 1;
			for (StockTransferStatus status: statuses) {
				pstmt.setInt(pos++, status.getId());
			}
			pstmt.setString(pos++, "%" + item_desc + "%");
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					transfer_ids.add(r.getLong(1));
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.insert_temp_table_long1()))) {
			for (Long transfer_id : transfer_ids) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.stock_transfer);
				pstmt.setLong(3, transfer_id);
				pstmt.executeUpdate();
			}
		}
		
		return transfer_ids.size();
	}

}
