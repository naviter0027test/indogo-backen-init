package com.lionpig.webui.database;

public enum HistoryAction {
	add(1),
	update(2),
	delete(3);

	private final int id;
	private HistoryAction(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
}
