package com.indogo.bni;

import java.util.EnumSet;
import java.util.HashMap;

public enum BniTransferType {
	Interbank(1),
	CreditToAccount(2),
	CashPickup(3),
	Clearing(4),
	RTGS(5);
	
	private int id;
	
	private BniTransferType(int id) {
		this.id = id;
	}
	
	public int getId() { return id; }
	
	private static HashMap<Integer, BniTransferType> cache = new HashMap<Integer, BniTransferType>();
	
	static {
		for (BniTransferType e : EnumSet.allOf(BniTransferType.class)) {
			cache.put(e.getId(), e);
		}
	}
	
	public static BniTransferType get(int id) {
		return cache.get(id);
	}
}
