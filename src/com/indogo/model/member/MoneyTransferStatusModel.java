package com.indogo.model.member;

import java.sql.Timestamp;

import com.indogo.TransferStatus;

public class MoneyTransferStatusModel {
	public long txn_id;
	public Timestamp lm_time;
	public String lm_user;
	public TransferStatus old_status;
	public TransferStatus new_status;
	public String comment;
}
