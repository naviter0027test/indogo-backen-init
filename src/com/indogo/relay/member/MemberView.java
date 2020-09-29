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

public class MemberView implements ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.member;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<TablePageColumn>();
		cols.add(new TablePageColumn(C.member_name, C.columnTypeString, C.columnDirectionDefault, true, false, "Member Name"));
		cols.add(new TablePageColumn(C.member_login_id, C.columnTypeString, C.columnDirectionDefault, true, false, "App Login Id"));
		cols.add(new TablePageColumn(C.phone_no, C.columnTypeString, C.columnDirectionDefault, true, false, "Phone No"));
		cols.add(new TablePageColumn(C.arc_no, C.columnTypeString, C.columnDirectionDefault, true, false, "ARC"));
		cols.add(new TablePageColumn(C.arc_expire_date, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "ARC Expire"));
		cols.add(new TablePageColumn(C.address, C.columnTypeString, C.columnDirectionDefault, true, false, "Address"));
		cols.add(new TablePageColumn(C.birthday, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Birthday"));
		cols.add(new TablePageColumn(C.email, C.columnTypeString, C.columnDirectionDefault, true, false, "Email"));
		cols.add(new TablePageColumn(C.wallet, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Wallet"));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, false));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.member_name).setValue(r.getString(C.member_name));
		cols.get(C.member_login_id).setValue(r.getString(C.member_login_id));
		cols.get(C.phone_no).setValue(r.getString(C.phone_no));
		cols.get(C.arc_no).setValue(r.getString(C.arc_no));
		cols.get(C.arc_expire_date).setValue(r.getDate(C.arc_expire_date));
		cols.get(C.address).setValue(r.getString(C.address));
		cols.get(C.birthday).setValue(r.getDate(C.birthday));
		cols.get(C.email).setValue(r.getString(C.email));
		cols.get(C.wallet).setValue(r.getIntCurrency(C.wallet));
		cols.get(C.member_id).setValue(r.getLong(C.member_id));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		filter.add(new TablePageFilter(C.status_id, C.columnTypeNumber, "=", "0", null));
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
	}

}
