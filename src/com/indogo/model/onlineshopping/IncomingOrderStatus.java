package com.indogo.model.onlineshopping;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.lionpig.language.L;

public enum IncomingOrderStatus {
	created(1),
	deleted(2);
	
	private final int id;
	private IncomingOrderStatus(int id) {
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
				return l.deleted();
			default:
				return name();
		}
	}
	
	private static final Map<Integer, IncomingOrderStatus> lookup = new HashMap<>();
	
	static {
		for (IncomingOrderStatus e : EnumSet.allOf(IncomingOrderStatus.class)) {
			lookup.put(e.id, e);
		}
	}
	
	public static IncomingOrderStatus get(int id) {
		return lookup.get(id);
	}
}
