package com.indogo.model.onlineshopping;

import java.sql.Timestamp;

public class ItemModel {
	public int item_id;
	public String item_name;
	public String item_desc;
	public String item_filename;
	public int category_id;
	public Integer color_id;
	public Integer size_id;
	public boolean item_disabled;
	public Integer price_sale;
	public Timestamp lm_time;
	public String lm_user;
	public String category_name;
	public String color_name;
	public String size_name;
	public String location;
	public Timestamp expired_date;
	public Integer item_point;
	public boolean item_hide;
	public boolean is_composite;
	public ItemCompositeModel[] item_composites;
}
