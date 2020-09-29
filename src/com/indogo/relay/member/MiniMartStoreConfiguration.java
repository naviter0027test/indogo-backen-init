package com.indogo.relay.member;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indogo.MiniMart;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class MiniMartStoreConfiguration implements IFunction, ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.mini_mart_store;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<TablePageColumn>();
		cols.add(new TablePageColumn(C.mini_mart_name, C.columnTypeString, C.columnDirectionNone, false, true, "通路"));
		cols.add(new TablePageColumn(C.store_id, C.columnTypeString, C.columnDirectionDefault, true, false, "店鋪名稱"));
		cols.add(new TablePageColumn(C.store_name, C.columnTypeString, C.columnDirectionDefault, true, false, "店名"));
		cols.add(new TablePageColumn(C.store_addr, C.columnTypeString, C.columnDirectionDefault, true, false, "地址"));
		cols.add(new TablePageColumn(C.store_area, C.columnTypeString, C.columnDirectionDefault, true, false, "縣市"));
		cols.add(new TablePageColumn(C.mini_mart_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.mini_mart_id, true, false));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		int mini_mart_id = r.unwrap().unwrap().getInt(C.mini_mart_id);
		MiniMart miniMart = MiniMart.get(mini_mart_id);
		cols.get(C.mini_mart_name).setValue(miniMart.name());
		cols.get(C.store_id).setValue(r.getString(C.store_id));
		cols.get(C.store_name).setValue(r.getString(C.store_name));
		cols.get(C.store_addr).setValue(r.getString(C.store_addr));
		cols.get(C.store_area).setValue(r.getString(C.store_area));
		cols.get(C.mini_mart_id).setValue(String.valueOf(mini_mart_id));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
	}

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		if (action.equals(C.insert)) {
			return C.emptyString;
		} else {
			throw new Exception(String.format(C.unknown_action, action));
		}
	}

}
