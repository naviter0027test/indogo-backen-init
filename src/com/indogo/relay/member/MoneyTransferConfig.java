package com.indogo.relay.member;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.http.AbstractFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class MoneyTransferConfig extends AbstractFunction {

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		IConnection iconn = fi.getConnection();
		if (action.equals(C.default_acc_verification)) {
			String value = Helper.getString(params, C.value, true);
			iconn.setGlobalConfig(C.h2h, C.default_acc_verification, value);
		} else if (action.equals(C.max_create_count)) {
			String max_create_count_0 = Helper.getString(params, C.max_create_count_0, true);
			String max_create_count_1 = Helper.getString(params, C.max_create_count_1, true);
			String max_create_count_2 = Helper.getString(params, C.max_create_count_2, true);
			String max_create_count_3 = Helper.getString(params, C.max_create_count_3, true);
			String max_create_count_4 = Helper.getString(params, C.max_create_count_4, true);
			String max_create_count_5 = Helper.getString(params, C.max_create_count_5, true);
			String max_create_count_6 = Helper.getString(params, C.max_create_count_6, true);
			
			try {
				iconn.setGlobalConfig(C.money_transfer, C.max_create_count_0, max_create_count_0, true);
				iconn.setGlobalConfig(C.money_transfer, C.max_create_count_1, max_create_count_1, true);
				iconn.setGlobalConfig(C.money_transfer, C.max_create_count_2, max_create_count_2, true);
				iconn.setGlobalConfig(C.money_transfer, C.max_create_count_3, max_create_count_3, true);
				iconn.setGlobalConfig(C.money_transfer, C.max_create_count_4, max_create_count_4, true);
				iconn.setGlobalConfig(C.money_transfer, C.max_create_count_5, max_create_count_5, true);
				iconn.setGlobalConfig(C.money_transfer, C.max_create_count_6, max_create_count_6, true);
				iconn.getConnection().commit();
			} catch (Exception e) {
				iconn.getConnection().rollback();
				throw e;
			}
		} else if (action.equals(C.max_recipient_count)) {
			String max_recipient_count = Helper.getString(params, C.max_recipient_count, true);
			
			try {
				iconn.setGlobalConfig(C.money_transfer, C.max_recipient_count, max_recipient_count, true);
				iconn.getConnection().commit();
			} catch (Exception e) {
				iconn.getConnection().rollback();
				throw e;
			}
		}
		return null;
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		IConnection iconn = fi.getConnection();
		String default_acc_verification = iconn.getGlobalConfig(C.h2h, C.default_acc_verification);
		String max_create_count_0 = iconn.getGlobalConfig(C.money_transfer, C.max_create_count_0);
		String max_create_count_1 = iconn.getGlobalConfig(C.money_transfer, C.max_create_count_1);
		String max_create_count_2 = iconn.getGlobalConfig(C.money_transfer, C.max_create_count_2);
		String max_create_count_3 = iconn.getGlobalConfig(C.money_transfer, C.max_create_count_3);
		String max_create_count_4 = iconn.getGlobalConfig(C.money_transfer, C.max_create_count_4);
		String max_create_count_5 = iconn.getGlobalConfig(C.money_transfer, C.max_create_count_5);
		String max_create_count_6 = iconn.getGlobalConfig(C.money_transfer, C.max_create_count_6);
		String max_recipient_count = iconn.getGlobalConfig(C.money_transfer, C.max_recipient_count);
		
		StringBuilder sb = new StringBuilder();
		sb.append(default_acc_verification)
		.append(C.char_31).append(max_create_count_0)
		.append(C.char_31).append(max_create_count_1)
		.append(C.char_31).append(max_create_count_2)
		.append(C.char_31).append(max_create_count_3)
		.append(C.char_31).append(max_create_count_4)
		.append(C.char_31).append(max_create_count_5)
		.append(C.char_31).append(max_create_count_6)
		.append(C.char_31).append(max_recipient_count);
		return sb.toString();
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String onUpdate(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		return null;
	}

	@Override
	protected String onDelete(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
