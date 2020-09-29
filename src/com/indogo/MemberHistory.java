package com.indogo;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum MemberHistory {
	reset_password(1, "Reset Password"),
	change_app_phone_no(2, "Change APP Phone No"),
	change_app_login_id(3, "Change APP Login Id"),
	accept_member(4, "Accept Membership"),
	ask_for_followup(5, "Followup");
	
	private final int id;
	private final String display;
	private MemberHistory(int id, String display) {
		this.id = id;
		this.display = display;
	}
	
	public int getId() {
		return id;
	}
	
	public String getDisplay() {
		return display;
	}
	
	private static final Map<Integer, MemberHistory> lookup = new HashMap<>();
	
	static {
		for (MemberHistory e : EnumSet.allOf(MemberHistory.class)) {
			lookup.put(e.id, e);
		}
	}
	
	public static MemberHistory get(int id) {
		return lookup.get(id);
	}
}
