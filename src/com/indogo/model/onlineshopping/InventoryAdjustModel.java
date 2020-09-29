package com.indogo.model.onlineshopping;

import java.sql.Timestamp;

public class InventoryAdjustModel {
	public long adjust_id;
	public int shop_id;
	public Timestamp lm_time;
	public String lm_user;
	public InventoryAdjustItemModel[] items;
}
