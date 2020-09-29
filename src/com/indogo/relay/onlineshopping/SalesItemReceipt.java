package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.AbstractFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class SalesItemReceipt extends AbstractFunction {

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		long sales_id = Helper.getLong(params, C.sales_id, true);
		
		Hashtable<String, String> print_yoho = fi.getConnection().getGlobalConfig(C.print_yoho);
		String company_name = print_yoho.get(C.company_name);
		String company_address = print_yoho.get(C.company_address);
		String customer_service_phone = fi.getConnection().getGlobalConfig(C.money_transfer_ibon, C.customer_service_phone);
		
		Connection conn = fi.getConnection().getConnection();
		String member_name, phone_no, app_phone_no, ship_address, lm_time_created, ship_fee, total_amount, point_used, freight_id, wallet_used;
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select b.member_name, b.phone_no, b.app_phone_no, a.ship_address, a.lm_time_created, a.ship_fee, a.total_amount, a.point_used, a.freight_id, a.wallet_used from sales a, member b where a.member_id = b.member_id and a.sales_id = ?"))) {
			pstmt.setLong(1, sales_id);
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQueryWrapper())) {
				if (r.next()) {
					member_name = r.getString(1);
					phone_no = r.getString(2);
					app_phone_no = r.getString(3);
					ship_address = r.getString(4);
					lm_time_created = r.getTimestamp(5);
					ship_fee = r.getIntCurrency(6);
					total_amount = r.getIntCurrency(7);
					point_used = r.getIntCurrency(8);
					freight_id = r.getString(9);
					wallet_used = r.getIntCurrency(10);
				} else {
					throw new Exception(l.data_not_exist("sales_id = " + sales_id));
				}
			}
		}
		
		String final_phone_no = C.emptyString;
		if (app_phone_no.length() > 0) {
			final_phone_no = app_phone_no;
		} else {
			String[] tokens = StringUtils.split(phone_no, '.');
			for (String token : tokens) {
				if (token.length() > 0) {
					final_phone_no = token;
					break;
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(company_name)
		.append(C.char_31).append(company_address)
		.append(C.char_31).append(customer_service_phone)
		.append(C.char_31).append(member_name)
		.append(C.char_31).append(final_phone_no)
		.append(C.char_31).append(ship_address)
		.append(C.char_31).append(lm_time_created)
		.append(C.char_31).append(ship_fee)
		.append(C.char_31).append(total_amount)
		.append(C.char_31).append(point_used)
		.append(C.char_31).append(freight_id)
		.append(C.char_31).append(wallet_used);
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select b.item_desc, b.item_name, a.sales_qty, a.sales_price, a.sales_total, (select color_name from item_color where color_id = b.color_id) as color_name, (select size_name from item_size where size_id = b.size_id) as size_name from sales_item a, item b where a.item_id = b.item_id and a.sales_id = ?"))) {
			pstmt.setLong(1, sales_id);
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQueryWrapper())) {
				StringBuilder sbb = new StringBuilder();
				int count = 0;
				while (r.next()) {
					String item_desc, item_name, sales_qty, sales_price, sales_total, color_name, size_name;
					
					item_desc = r.getString(1);
					item_name = r.getString(2);
					sales_qty = r.getIntCurrency(3);
					sales_price = r.getIntCurrency(4);
					sales_total = r.getIntCurrency(5);
					color_name = r.getString(6);
					size_name = r.getString(7);
					
					sbb.append(C.char_31).append(item_desc)
					.append(C.char_31).append(item_name)
					.append(C.char_31).append(sales_qty)
					.append(C.char_31).append(sales_price)
					.append(C.char_31).append(sales_total)
					.append(C.char_31).append(color_name)
					.append(C.char_31).append(size_name);
					
					count++;
				}
				
				sb.append(C.char_31).append(count).append(sbb);
			}
		}
		
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
		// TODO Auto-generated method stub
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
