package com.indogo.relay.member;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.indogo.MemberPointReason;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;

public class MemberPointView implements ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.member_point;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<TablePageColumn>();
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDesc, true, false, C.lm_time));
		cols.add(new TablePageColumn(C.remit_point, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.remit_point));
		cols.add(new TablePageColumn(C.reason_name, C.columnTypeString, C.columnDirectionNone, false, true, C.reason_name));
		cols.add(new TablePageColumn(C.reason_desc, C.columnTypeString, C.columnDirectionDefault, true, false, C.reason_desc));
		cols.add(new TablePageColumn(C.txn_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.txn_id));
		cols.add(new TablePageColumn(C.sales_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.sales_id));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, true));
		cols.add(new TablePageColumn(C.reason_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.reason_id, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		MemberPointReason reason = MemberPointReason.get(r.unwrap().getInt(C.reason_id));
		
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.remit_point).setValue(r.getInt(C.remit_point));
		cols.get(C.reason_name).setValue(reason.getDisplay());
		cols.get(C.reason_desc).setValue(r.getString(C.reason_desc));
		cols.get(C.txn_id).setValue(r.getLong(C.txn_id));
		cols.get(C.sales_id).setValue(r.getLong(C.sales_id));
		cols.get(C.member_id).setValue(r.getLong(C.member_id));
		cols.get(C.reason_id).setValue(String.valueOf(reason.getId()));
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
