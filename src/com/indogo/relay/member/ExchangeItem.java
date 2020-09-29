package com.indogo.relay.member;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;

public class ExchangeItem implements ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.exchange_item;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.txn_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.txn_id));
		cols.add(new TablePageColumn(C.old_kurs_value, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Previous Rate"));
		cols.add(new TablePageColumn(C.old_amount_idr, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Previous Rupiah"));
		cols.add(new TablePageColumn(C.new_kurs_value, C.columnTypeNumber, C.columnDirectionDefault, true, false, "New Rate"));
		cols.add(new TablePageColumn(C.new_amount_idr, C.columnTypeNumber, C.columnDirectionDefault, true, false, "New Rupiah"));
		cols.add(new TablePageColumn(C.exchange_id, C.columnTypeNumber, C.columnDirectionDesc, true, false, "No.", true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.txn_id).setValue(r.getLong(C.txn_id));
		cols.get(C.old_kurs_value).setValue(r.getDoubleCurrency(C.old_kurs_value));
		cols.get(C.old_amount_idr).setValue(r.getLongCurrency(C.old_amount_idr));
		cols.get(C.new_kurs_value).setValue(r.getDoubleCurrency(C.new_kurs_value));
		cols.get(C.new_amount_idr).setValue(r.getLongCurrency(C.new_amount_idr));
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

}
