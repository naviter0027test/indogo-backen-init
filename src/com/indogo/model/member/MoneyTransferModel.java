package com.indogo.model.member;

import java.sql.Timestamp;

import com.indogo.PaymentType;
import com.indogo.TransferStatus;

public class MoneyTransferModel {
	public long txn_id;
	public long member_id;
	public int recipient_id;
	public PaymentType payment_id;
	public String payment_info;
	public TransferStatus transfer_status_id;
	public double kurs_value;
	public int transfer_amount_ntd;
	public long transfer_amount_idr;
	public int service_charge;
	public int total;
	public boolean is_print;
	public Timestamp lm_time;
	public String lm_user;
	public String family_mart_error_message;
}
