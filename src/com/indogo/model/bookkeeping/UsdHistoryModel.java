package com.indogo.model.bookkeeping;

import java.sql.Timestamp;

public class UsdHistoryModel {
	public Timestamp lm_time;
	public int seq_no;
	public String lm_user;
	public Integer amount_ntd;
	public Double usd_to_ntd;
	public Double amount_usd;
	public Double usd_to_idr;
	public Long amount_idr;
	public Double ntd_to_idr;
	public Integer amount_ntd_used;
	public String comment;
	public Timestamp lm_time_used;
}
