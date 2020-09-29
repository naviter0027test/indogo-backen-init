package com.indogo.relay.member;

import java.util.ArrayList;
import java.util.List;

import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.util.C;

public class MoneyTransferBRIAuto extends MoneyTransferBRI {
	
	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.bri_reply_message, C.columnTypeString, C.columnDirectionNone, false, true, "BRI Reply", false));
		cols.addAll(super.getColumns(fi));
		return cols;
	}

}
