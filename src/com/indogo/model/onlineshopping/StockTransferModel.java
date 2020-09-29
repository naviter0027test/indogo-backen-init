package com.indogo.model.onlineshopping;

import java.sql.Timestamp;

public class StockTransferModel {
	public long transfer_id;
	public Timestamp create_time;
	public int shop_id_from;
	public int shop_id_to;
	public StockTransferStatus status;
	public String comment;
	public Timestamp lm_time;
	public String lm_user;
	public String qrcode_filename;
	
	public StockTransferItemModel[] items;
}
