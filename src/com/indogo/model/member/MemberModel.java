package com.indogo.model.member;

import java.sql.Timestamp;

import com.indogo.MemberSex;
import com.indogo.MemberStatus;

public class MemberModel {
	public long member_id;
	public String member_name;
	public String member_login_id;
	public String phone_no;
	public String arc_no;
	public Timestamp arc_expire_date;
	public String address;
	public Timestamp birthday;
	public Timestamp lm_time;
	public String lm_user;
	public String arc_photo_basename;
	public String signature_photo_basename;
	public String email;
	public MemberStatus status;
	public boolean signature_need_fix;
	public MemberSex sex;
	public String app_phone_no;
}
