package com.indogo;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum MemberSex {
	male(1, "M"),
	female(2, "F");
	
	private final int id;
	private final String shortName;
	private MemberSex(int id, String shortName) {
		this.id = id;
		this.shortName = shortName;
	}
	
	public int getId() {
		return id;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	private static final Map<Integer, MemberSex> lookup = new HashMap<>();
	
	static {
		for (MemberSex e : EnumSet.allOf(MemberSex.class)) {
			lookup.put(e.id, e);
		}
	}
	
	public static MemberSex get(int id) {
		return lookup.get(id);
	}
}
