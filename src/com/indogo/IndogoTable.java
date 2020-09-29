package com.indogo;

import com.lionpig.webui.database.HistoryTable;

public enum IndogoTable implements HistoryTable {
	member(1),
	member_recipient(2),
	bank_code_list(3),
	money_transfer(4),
	member_app_register(5);
	
	private final int id;
	private IndogoTable(int id) {
		this.id = id;
	}
	
	@Override
	public int getId() {
		return id;
	}
}
