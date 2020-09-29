package com.indogo;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum PaymentType {
	family_mart(1, "Family Mart"),
	mini_mart(2, "Mini Mart"),
	cash(3, "Cash"),
	heimao(99, "黑貓");
	
	private final int id;
	private final String display;
	private PaymentType(int id, String display) {
		this.id = id;
		this.display = display;
	}
	
	public int getId() {
		return id;
	}
	
	public String getDisplay() {
		return display;
	}
	
	private static final Map<Integer, PaymentType> lookup = new HashMap<>();
	
	static {
		for (PaymentType e : EnumSet.allOf(PaymentType.class)) {
			lookup.put(e.id, e);
		}
	}
	
	public static PaymentType get(int id) {
		return lookup.get(id);
	}
}
