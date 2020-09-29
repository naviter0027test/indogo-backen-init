package com.indogo.model.onlineshopping;

import java.sql.Timestamp;

public class SalesModel {
	public long sales_id;
	public int shop_id;
	public SalesStatus status;
	public long member_id;
	public Freight freight;
	public String ship_address;
	public int total_amount;
	public Timestamp lm_time;
	public String lm_user;
	public String invoice_no;
	public String ship_no;
	public SalesItemModel[] items;
	public Timestamp lm_time_created;
	public int ship_fee;
	public String comment;
	public Integer point_used;
	public String member_phone_no;
	public String member_login_id;
	public String member_name;
	public Integer wallet_used;
}
