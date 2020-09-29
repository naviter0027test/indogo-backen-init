package com.indogo.model.onlineshopping;

import java.sql.Timestamp;

public class IncomingOrderModel {
	public long order_id;
	public Timestamp create_time;
	public int vendor_id;
	public String invoice_no;
	public int total;
	public String comment;
	public int shop_id;
	public Timestamp lm_time;
	public String lm_user;
	public IncomingOrderStatus status;
	
	public IncomingOrderItemModel[] items;
}
