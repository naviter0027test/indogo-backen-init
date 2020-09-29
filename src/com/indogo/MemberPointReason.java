package com.indogo;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum MemberPointReason {
	app_download_bonus(1, "APP Download Bonus"),
	app_share_bonus(2, "APP Shared Bonus"),
	app_create_invoice(3, "Remit Transaction Bonus"),
	redeem_point(4, "Use for Free Service Charge"),
	manual(5, "Edit Manually"),
	bonus_100th(6, "Bonus for each 100 Transactions"),
	bonus_50_friends(7, "Bonus for 50 friends"),
	birthday(8, "Birthday"),
	special_ocassion(9, "Special Event"),
	sales_created(10, "Online Shopping Created"),
	sales_scrapped(11, "Online Shopping Scrapped");
	
	private final int id;
	private final String display;
	private MemberPointReason(int id, String display) {
		this.id = id;
		this.display = display;
	}
	
	public int getId() {
		return id;
	}
	
	public String getDisplay() {
		return display;
	}
	
	private static final Map<Integer, MemberPointReason> lookup = new HashMap<>();
	
	static {
		for (MemberPointReason e : EnumSet.allOf(MemberPointReason.class)) {
			lookup.put(e.id, e);
		}
	}
	
	public static MemberPointReason get(int id) {
		return lookup.get(id);
	}
}
