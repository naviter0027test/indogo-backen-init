package com.indogo.model.onlineshopping;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.lionpig.language.L;

public enum StockTransferStatus {
	created(1),
	collected(2),
	cancel(3);
	
	private final int id;
	private StockTransferStatus(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName(L l) {
		switch (id) {
			case 1:
				return l.created();
			case 2:
				return l.collected();
			case 3:
				return l.cancel();
			default:
				return name();
		}
	}
	
	private static final Map<Integer, StockTransferStatus> lookup = new HashMap<>();
	
	static {
		for (StockTransferStatus e : EnumSet.allOf(StockTransferStatus.class)) {
			lookup.put(e.id, e);
		}
	}
	
	public static StockTransferStatus get(int id) {
		return lookup.get(id);
	}
}
