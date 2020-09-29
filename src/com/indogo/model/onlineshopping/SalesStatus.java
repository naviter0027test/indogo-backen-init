package com.indogo.model.onlineshopping;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.lionpig.language.L;

public enum SalesStatus {
	created(1),
	checkout(2),
	shipped(3),
	paid(4),
	returned(5),
	scrapped(6);
	
	private final int id;
	private SalesStatus(int id) {
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
				return l.checkout();
			case 3:
				return "Shipped";
			case 4:
				return "Paid";
			case 5:
				return "Returned";
			case 6:
				return "Scrapped";
			default:
				return name();
		}
	}
	
	private static final Map<Integer, SalesStatus> lookup = new HashMap<>();
	
	static {
		for (SalesStatus e : EnumSet.allOf(SalesStatus.class)) {
			lookup.put(e.id, e);
		}
	}
	
	public static SalesStatus get(int id) {
		return lookup.get(id);
	}
}
