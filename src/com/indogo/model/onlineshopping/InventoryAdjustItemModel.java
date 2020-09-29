package com.indogo.model.onlineshopping;

public class InventoryAdjustItemModel {
	public int item_id;
	public int old_qty;
	public int new_qty;
	public String comment;
	public boolean is_composite;
	public boolean is_new_item;
	public Integer qty_taken_for_composite = null;
	public Integer composite_id = null;
}
