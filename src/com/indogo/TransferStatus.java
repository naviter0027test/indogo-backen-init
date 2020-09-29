package com.indogo;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum TransferStatus {
	pending(1),
	paid(2),
	process(3),
	transfered(4),
	failed(5),
	cancel(6);

	private final int id;
	private TransferStatus(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	private static final Map<Integer, TransferStatus> lookup = new HashMap<>();
	
	static {
		for (TransferStatus e : EnumSet.allOf(TransferStatus.class)) {
			lookup.put(e.id, e);
		}
	}
	
	public static TransferStatus get(int id) {
		return lookup.get(id);
	}
}
