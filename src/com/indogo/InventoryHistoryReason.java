package com.indogo;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum InventoryHistoryReason {
	incoming_order_create(1),
	incoming_order_cancel(2),
	sales_create(3),
	sales_cancel(4),
	manual_edit(5),
	mass_adjust(6),
	stock_transfer_create(7),
	stock_transfer_collect(8),
	stock_transfer_cancel(9);
	
	private final int id;
	private InventoryHistoryReason(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	private static final Map<Integer, InventoryHistoryReason> lookup = new HashMap<>();
	
	static {
		for (InventoryHistoryReason e : EnumSet.allOf(InventoryHistoryReason.class)) {
			lookup.put(e.id, e);
		}
	}
	
	public static InventoryHistoryReason get(int id) {
		return lookup.get(id);
	}
}
