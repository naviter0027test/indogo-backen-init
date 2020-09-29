package com.indogo.model.member;

import java.sql.Timestamp;

import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Stringify;

public class RecipientModel {
	public Long member_id;
	public Integer recipient_id;
	public String recipient_name;
	public String bank_code;
	public String bank_name;
	public String bank_acc;
	public Timestamp lm_time;
	public String lm_user;
	public boolean is_verified;
	public boolean is_hidden;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(50);
		sb.append(member_id).append(C.char_31)
		.append(recipient_id).append(C.char_31)
		.append(recipient_name).append(C.char_31)
		.append(bank_code).append(C.char_31)
		.append(Stringify.getString(bank_name)).append(C.char_31)
		.append(bank_acc).append(C.char_31)
		.append(Stringify.getTimestamp(lm_time)).append(C.char_31)
		.append(lm_user).append(C.char_31)
		.append(is_verified ? 1 : 0).append(C.char_31)
		.append(is_hidden ? 1 : 0);
		return sb.toString();
	}
}
