package com.indogo;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum AddressType {
	home(1),
	seven_eleven(2),
	family_mart(3),
	ok_mart(4),
	hilife(5);
	
	private final int id;
	private AddressType(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		switch (id) {
			case 1:
				return "home";
			case 2:
				return "7-11";
			case 3:
				return "Family Mart";
			case 4:
				return "OK Mart";
			case 5:
				return "HiLife";
			default:
				return name();
		}
	}
	
	private static final Map<Integer, AddressType> lookup = new HashMap<>();
	
	static {
		for (AddressType e : EnumSet.allOf(AddressType.class)) {
			lookup.put(e.id, e);
		}
	}
	
	public static AddressType get(int id) {
		return lookup.get(id);
	}
}
