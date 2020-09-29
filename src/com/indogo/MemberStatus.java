package com.indogo;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum MemberStatus {
	active(0),
	banned(1);
	
	private final int id;
	private MemberStatus(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	private static final Map<Integer, MemberStatus> lookup = new HashMap<>();
	
	static {
		for (MemberStatus e : EnumSet.allOf(MemberStatus.class)) {
			lookup.put(e.id, e);
		}
	}
	
	public static MemberStatus get(int id) {
		return lookup.get(id);
	}
}
