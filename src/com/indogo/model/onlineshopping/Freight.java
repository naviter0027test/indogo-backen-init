package com.indogo.model.onlineshopping;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.lionpig.language.L;

public enum Freight {
	heimao(1),
	cash(2),
	post(3);
	
	private final int id;
	private Freight(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName(L l) {
		switch (id) {
			case 1:
				return "黑貓";
			case 3:
				return "郵局";
			default:
				return name();
		}
	}
	
	private static final Map<Integer, Freight> lookup = new HashMap<>();
	
	static {
		for (Freight e : EnumSet.allOf(Freight.class)) {
			lookup.put(e.id, e);
		}
	}
	
	public static Freight get(int id) {
		return lookup.get(id);
	}
}
