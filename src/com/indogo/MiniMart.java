package com.indogo;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum MiniMart {
	SevenEleven(1, "7-11"),
	FamilyMart(2, "全家"),
	OkMart(3, "OK"),
	HiLife(4, "萊爾富");

	private final int id;
	private final String display;
	private MiniMart(int id, String display) {
		this.id = id;
		this.display = display;
	}
	
	public int getId() {
		return id;
	}
	
	public String getDisplay() {
		return display;
	}
	
	private static final Map<Integer, MiniMart> lookup = new HashMap<>();
	
	static {
		for (MiniMart e : EnumSet.allOf(MiniMart.class)) {
			lookup.put(e.id, e);
		}
	}
	
	public static MiniMart get(int id) {
		return lookup.get(id);
	}
}
