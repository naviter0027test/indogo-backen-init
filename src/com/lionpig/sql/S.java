package com.lionpig.sql;

import com.indogo.model.onlineshopping.StockTransferStatus;
import com.lionpig.language.L;

public class S {
	S() {}
	
	private final static S mysql = new S();
	
	public static S getInstance(L l, String db_type) {
		return mysql;
	}
	
	/**
	 * 
	 * @return select category_name, imei_flag, item_name_prefix from item_category where category_id = ?
	 */
	public String item_category_get() { return "select category_name, imei_flag, item_name_prefix from item_category where category_id = ?"; }
	/**
	 * 
	 * @return insert into item_category (category_id, category_name, imei_flag, item_name_prefix) values (?,?,?,?)
	 */
	public String item_category_insert() { return "insert into item_category (category_id, category_name, imei_flag, item_name_prefix) values (?,?,?,?)"; }
	/**
	 * 
	 * @return update item_category set category_name = ?, imei_flag = ?, item_name_prefix = ? where category_id = ?
	 */
	public String item_category_update() { return "update item_category set category_name = ?, imei_flag = ?, item_name_prefix = ? where category_id = ?"; }
	public String item_category_delete() { return "delete from item_category where category_id = ?"; }
	/**
	 * @return select category_id, category_name, item_name_prefix from item_category
	 */
	public String item_category_get_all() { return "select category_id, category_name, item_name_prefix from item_category"; }
	
	public String item_color_get() { return "select color_name from item_color where color_id = ?"; }
	public String item_color_insert() { return "insert into item_color (color_id, color_name) values (?,?)"; }
	public String item_color_update() { return "update item_color set color_name = ? where color_id = ?"; }
	public String item_color_delete() { return "delete from item_color where color_id = ?"; }
	public String item_color_get_all() { return "select color_id, color_name from item_color"; }

	public String item_size_get() { return "select size_name from item_size where size_id = ?"; }
	public String item_size_insert() { return "insert into item_size (size_id, size_name) values (?,?)"; }
	public String item_size_update() { return "update item_size set size_name = ? where size_id = ?"; }
	public String item_size_delete() { return "delete from item_size where size_id = ?"; }
	public String item_size_get_all() { return "select size_id, size_name from item_size"; }
	
	/**
	 * 
	 * @return select item_name, item_desc, item_filename, category_id, color_id, size_id, item_disabled, price_sale, lm_time, lm_user, location, expired_date, item_point, is_composite from item where item_id = ?
	 */
	public String item_get() { return "select item_name, item_desc, item_filename, category_id, color_id, size_id, item_disabled, price_sale, lm_time, lm_user, location, expired_date, item_point, is_composite from item where item_id = ?"; }
	/**
	 * @return select item_name, price_sale, item_filename, (select category_name from item_category b where b.category_id = a.category_id) as category_name, (select color_name from item_color b where b.color_id = a.color_id) as color_name, (select size_name from item_size b where b.size_id = a.size_id) as size_name, item_desc, item_disabled, item_hide from item a where item_id = ?
	 */
	public String item_get_for_inventory() { return "select item_name, price_sale, item_filename, (select category_name from item_category b where b.category_id = a.category_id) as category_name, (select color_name from item_color b where b.color_id = a.color_id) as color_name, (select size_name from item_size b where b.size_id = a.size_id) as size_name, item_desc, item_disabled, item_hide from item a where item_id = ?"; }
	/**
	 * 
	 * @return insert into item (item_id, item_name, item_desc, item_filename, category_id, color_id, size_id, item_disabled, price_sale, lm_time, lm_user, location, expired_date, item_point) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
	 */
	public String item_insert() { return "insert into item (item_id, item_name, item_desc, item_filename, category_id, color_id, size_id, item_disabled, price_sale, lm_time, lm_user, location, expired_date, item_point, is_composite) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"; }
	/**
	 * 
	 * @return select lm_time, item_filename, is_composite from item where item_id = ? for update
	 */
	public String item_select_for_update() { return "select lm_time, item_filename, is_composite from item where item_id = ? for update"; }
	/**
	 * 
	 * @return update item set item_name = ?, item_desc = ?, item_filename = ?, category_id = ?, color_id = ?, size_id = ?, item_disabled = ?, price_sale = ?, lm_time = ?, lm_user = ?, location = ?, expired_date = ?, item_point = ? where item_id = ?
	 */
	public String item_update() { return "update item set item_name = ?, item_desc = ?, item_filename = ?, category_id = ?, color_id = ?, size_id = ?, item_disabled = ?, price_sale = ?, lm_time = ?, lm_user = ?, location = ?, expired_date = ?, item_point = ? where item_id = ?"; }
	public String item_delete() { return "delete from item where item_id = ?"; }
	
	public String shop_insert() { return "insert into shop (shop_id, shop_name, shop_telp, shop_address) values (?,?,?,?)"; }
	public String shop_update() { return "update shop set shop_name = ?, shop_telp = ?, shop_address = ? where shop_id = ?"; }
	public String shop_delete() { return "delete from shop where shop_id = ?"; }
	public String shop_get() { return "select shop_name, shop_telp, shop_address from shop where shop_id = ?"; }
	/**
	 * @return select shop_name from shop where shop_id = ?
	 */
	public String shop_get_for_inventory() { return "select shop_name from shop where shop_id = ?"; }
	/**
	 * @return select a.shop_id, a.shop_name from shop a, shop_user b where a.shop_id = b.shop_id and b.user_row_id = ? order by a.shop_name
	 */
	public String shop_list_for_user() { return "select a.shop_id, a.shop_name from shop a, shop_user b where a.shop_id = b.shop_id and b.user_row_id = ? order by a.shop_name"; }
	/**
	 * @return select shop_id, shop_name from shop order by shop_name
	 */
	public String shop_list_all() { return "select shop_id, shop_name from shop order by shop_name"; }
	/**
	 * 
	 * @return insert into vendor (vendor_id, vendor_name, contact_person, phone_no, fax_no, address, email, lm_time, lm_user, vendor_desc) values (?,?,?,?,?,?,?,?,?,?)
	 */
	public String vendor_insert() { return "insert into vendor (vendor_id, vendor_name, contact_person, phone_no, fax_no, address, email, lm_time, lm_user, vendor_desc) values (?,?,?,?,?,?,?,?,?,?)"; }
	public String vendor_select_for_update() { return "select lm_time from vendor where vendor_id = ? for update"; }
	/**
	 * 
	 * @return update vendor set vendor_name = ?, contact_person = ?, phone_no = ?, fax_no = ?, address = ?, email = ?, lm_time = ?, lm_user = ?, vendor_desc = ? where vendor_id = ?
	 */
	public String vendor_update() { return "update vendor set vendor_name = ?, contact_person = ?, phone_no = ?, fax_no = ?, address = ?, email = ?, lm_time = ?, lm_user = ?, vendor_desc = ? where vendor_id = ?"; }
	public String vendor_delete() { return "delete from vendor where vendor_id = ?"; }
	/**
	 * @return select vendor_name, contact_person, phone_no, fax_no, address, email, lm_time, lm_user, vendor_desc from vendor where vendor_id = ?
	 */
	public String vendor_get() { return "select vendor_name, contact_person, phone_no, fax_no, address, email, lm_time, lm_user, vendor_desc from vendor where vendor_id = ?"; }
	/**
	 * @return select vendor_name from vendor where vendor_id = ?
	 */
	public String vendor_get_for_inventory() { return "select vendor_name from vendor where vendor_id = ?"; }
	/**
	 * @return select item_qty from inventory where shop_id = ? and item_id = ?
	 */
	public String inventory_select() { return "select item_qty from inventory where shop_id = ? and item_id = ?"; }
	/**
	 * @return select item_qty from inventory where shop_id = ? and item_id = ? for update
	 */
	public String inventory_select_for_update() { return "select item_qty from inventory where shop_id = ? and item_id = ? for update"; }
	/**
	 * 
	 * @return insert into inventory (shop_id, item_id, item_qty, lm_time, lm_user) values (?,?,?,?,?)
	 */
	public String inventory_insert() { return "insert into inventory (shop_id, item_id, item_qty, lm_time, lm_user) values (?,?,?,?,?)"; }
	/**
	 * @return update inventory set item_qty = ?, lm_time = ?, lm_user = ? where shop_id = ? and item_id = ?
	 */
	public String inventory_update() { return "update inventory set item_qty = ?, lm_time = ?, lm_user = ? where shop_id = ? and item_id = ?"; }
	
	/**
	 * @return insert into incoming_order (order_id, create_time, vendor_id, invoice_no, total, comment, shop_id, lm_time, lm_user, status_id, create_user) values (?,?,?,?,?,?,?,?,?,1,?)
	 */
	public String incoming_order_insert() { return "insert into incoming_order (order_id, create_time, vendor_id, invoice_no, total, comment, shop_id, lm_time, lm_user, status_id, create_user) values (?,?,?,?,?,?,?,?,?,1,?)"; }
	/**
	 * 
	 * @return insert into incoming_order_item (order_id, item_id, qty, price_buy, discount, total) values (?,?,?,?,?,?)
	 */
	public String incoming_order_item_insert() { return "insert into incoming_order_item (order_id, item_id, qty, price_buy, discount, total) values (?,?,?,?,?,?)"; }
	/**
	 * @return select shop_id, lm_time, lm_user, status_id from incoming_order where order_id = ? for update 
	 */
	public String incoming_order_get_for_update() { return "select shop_id, lm_time, lm_user, status_id from incoming_order where order_id = ? for update"; }
	/**
	 * @return select item_id, qty from incoming_order_item where order_id = ?
	 */
	public String incoming_order_get_all_items() { return "select item_id, qty from incoming_order_item where order_id = ?"; }
	/**
	 * @return update incoming_order set status_id = ?, lm_time = ?, lm_user = ? where order_id = ?
	 */
	public String incoming_order_update() { return "update incoming_order set status_id = ?, lm_time = ?, lm_user = ? where order_id = ?"; }
	
	/**
	 * @return delete from sql_temp_table where session_id = ? and page_id = ?
	 */
	public String clear_temp_table() { return "delete from sql_temp_table where session_id = ? and page_id = ?"; }
	/**
	 * 
	 * @return insert into sql_temp_table (session_id, page_id, long1) values (?,?,?)
	 */
	public String insert_temp_table_long1() { return "insert into sql_temp_table (session_id, page_id, long1) values (?,?,?)"; }
	/**
	 * 
	 * @return insert into sql_temp_table (session_id, page_id, item1) values (?,?,?)
	 */
	public String insert_temp_table_item1() { return "insert into sql_temp_table (session_id, page_id, item1) values (?,?,?)"; }
	
	/**
	 * @return insert into stock_transfer_item (transfer_id, item_id, qty) values (?,?,?)
	 */
	public String stock_transfer_item_insert() { return "insert into stock_transfer_item (transfer_id, item_id, qty) values (?,?,?)"; }
	/**
	 * @return update stock_transfer set status_id = ?, lm_time = ?, lm_user = ?, lm_time_{status_id} = ?, lm_user_{status_id} = ? where transfer_id = ?
	 */
	public String stock_transfer_change_status(StockTransferStatus status) {
		return "update stock_transfer set status_id = ?, lm_time = ?, lm_user = ?, lm_time_" + status.getId() + " = ?, lm_user_" + status.getId() + " = ? where transfer_id = ?";
	}
	/**
	 * @return select lm_time, lm_user, status_id, shop_id_from, shop_id_to from stock_transfer where transfer_id = ? for update
	 */
	public String stock_transfer_select_for_update() { return "select lm_time, lm_user, status_id, shop_id_from, shop_id_to from stock_transfer where transfer_id = ? for update"; }
	/**
	 * @return select item_id, qty from stock_transfer_item where transfer_id = ?
	 */
	public String stock_transfer_item_get_all() { return "select item_id, qty from stock_transfer_item where transfer_id = ?"; }
	/**
	 * @return select shop_id_from, shop_id_to from stock_transfer where transfer_id = ?
	 */
	public String stock_transfer_select_for_print() { return "select shop_id_from, shop_id_to from stock_transfer where transfer_id = ?"; }
	
	/**
	 * @return select a.user_row_id, b.user_name, b.alias_id from shop_user a, user_list b where a.user_row_id = b.user_row_id and a.shop_id = ?
	 */
	public String shop_user_get() { return "select a.user_row_id, b.user_name, b.alias_id from shop_user a, user_list b where a.user_row_id = b.user_row_id and a.shop_id = ?"; }
	/**
	 * @return delete from shop_user where shop_id = ?
	 */
	public String shop_user_delete() { return "delete from shop_user where shop_id = ?"; }
	/**
	 * @return insert into shop_user (shop_id, user_row_id) values (?,?)
	 */
	public String shop_user_add() { return "insert into shop_user (shop_id, user_row_id) values (?,?)"; }
	/**
	 * @return select count(*) from shop_user where shop_id = ? and user_row_id = ?
	 */
	public String shop_user_check() { return "select count(*) from shop_user where shop_id = ? and user_row_id = ?"; }
	
	/**
	 * @return select user_row_id, user_name, alias_id from user_list order by user_name
	 */
	public String user_list_get() { return "select user_row_id, user_name, alias_id from user_list order by user_name"; }
	
	/**
	 * 
	 * @return select b.member_name, b.arc_no, b.birthday, b.sex_id, b.status_id from money_transfer a, member b, sql_temp_table c where a.member_id = b.member_id and a.txn_id = c.long1 and c.session_id = ? and c.page_id = ?
	 */
	public String money_transfer_get_for_export_bank_format() { return "select b.member_name, b.arc_no, b.birthday, b.sex_id, b.status_id from money_transfer a, member b, sql_temp_table c where a.member_id = b.member_id and a.txn_id = c.long1 and c.session_id = ? and c.page_id = ?"; }
	/**
	 * 
	 * @return select member_name, address, phone_no, remit_point, member_login_id, is_wait_confirm, wallet from member where member_id = ?
	 */
	public String member_get_for_sales_create() { return "select member_name, address, phone_no, remit_point, member_login_id, is_wait_confirm, wallet from member where member_id = ?"; }
	/**
	 * 
	 * @return select member_name, phone_no, app_phone_no from member where member_id = ?
	 */
	public String member_get_for_sales_export_heimao() { return "select member_name, phone_no, app_phone_no from member where member_id = ?"; }
	/**
	 * 
	 * @return insert into inventory_history (log_id, reason_id, shop_id, item_id, item_old_qty, item_diff_qty, item_new_qty, lm_time, lm_user, sales_id, order_id, adjust_id, transfer_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)
	 */
	public String inventory_history_insert() { return "insert into inventory_history (log_id, reason_id, shop_id, item_id, item_old_qty, item_diff_qty, item_new_qty, lm_time, lm_user, sales_id, order_id, adjust_id, transfer_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)"; }
	
	/**
	 * 
	 * @return select b.item_name, b.item_desc, c.category_name, d.color_name, e.size_name, a.item_qty, a.item_id, b.item_filename
from item_composite a inner join item b on a.item_id = b.item_id
inner join item_category c on c.category_id = b.category_id
left join item_color d on d.color_id = b.color_id
left join item_size e on e.size_id = b.size_id
where a.composite_id = ?
	 */
	public String get_item_composites() { 
		return "select b.item_name, b.item_desc, c.category_name, d.color_name, e.size_name, a.item_qty, a.item_id, b.item_filename\r\n" + 
				"from item_composite a inner join item b on a.item_id = b.item_id\r\n" + 
				"inner join item_category c on c.category_id = b.category_id\r\n" + 
				"left join item_color d on d.color_id = b.color_id\r\n" + 
				"left join item_size e on e.size_id = b.size_id\r\n" + 
				"where a.composite_id = ?";
	}
	/**
	 * 
	 * @return SELECT b.member_name, b.phone_no, b.app_phone_no, a.ship_address, a.total_amount, a.sales_id
FROM sales a inner join member b on a.member_id = b.member_id
inner join sql_temp_table c on a.sales_id = c.long1
WHERE c.session_id = ? and c.page_id = 'sales_export_heimao'
	 */
	public String print_sales_export_heimao() {
		return "SELECT b.member_name, b.phone_no, b.app_phone_no, a.ship_address, a.total_amount, a.sales_id\r\n" + 
				"FROM sales a inner join member b on a.member_id = b.member_id\r\n" + 
				"inner join sql_temp_table c on a.sales_id = c.long1\r\n" + 
				"WHERE c.session_id = ? and c.page_id = 'sales_export_heimao'";
	}
}
