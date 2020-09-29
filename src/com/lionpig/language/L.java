package com.lionpig.language;

import java.util.Hashtable;

public class L {
	
	L() {}
	
	private final static L english = new L();
	
	public static L getInstance(Hashtable<String, String> params) {
		return english;
	}
	
	public String insert_not_allowed() { return "not allowed to do insert"; }
	public String update_not_allowed() { return "not allowed to do update"; }
	public String delete_not_allowed() { return "not allowed to do delete"; }
	public String data_already_updated_by_another_user(String data) { return new StringBuilder().append("record [").append(data).append("] has been updated by another user, please refresh and try again").toString(); }
	public String data_already_updated_by_another_user(String tag, String value) { return new StringBuilder().append("record [").append(tag).append(" = ").append(value).append("] has been updated by another user, please refresh and try again").toString(); }
	public String data_already_updated_by_another_user(String tag, long value) { return new StringBuilder().append("record [").append(tag).append(" = ").append(value).append("] has been updated by another user, please refresh and try again").toString(); }
	public String unknown_action(String action) { return new StringBuilder().append("unknown action [").append(action).append("]").toString(); }
	public String data_not_exist(String data) { return new StringBuilder().append("data [").append(data).append("] not exist").toString(); }
	public String data_not_exist(String tag, String value) { return new StringBuilder().append("data [").append(tag).append(" = ").append(value).append("] not exist").toString(); }
	public String data_not_exist(String tag, long value) { return new StringBuilder().append("data [").append(tag).append(" = ").append(value).append("] not exist").toString(); }
	public String input_must_be_provided(String inputName) { return new StringBuilder().append("input [").append(inputName).append("] must be provided").toString(); }
	public String create_directory_failed(String path) { return new StringBuilder().append("failed to create directory [").append(path).append("]").toString(); }
	public String save_image_failed() { return "save image failed"; };
	public String nothing_to_update() { return "no new data to update"; }
	public String status_not_allowed() { return "status not allowed"; }
	public String not_allowed_to_export_table_page() { return "you are not allowed to export table"; }
	
	public String exception_inventory_item_not_found(int shop_id, int item_id) { return new StringBuilder().append("inventory for item_id [").append(item_id).append("] in shop_id [").append(shop_id).append("] not exist").toString(); }
	public String exception_cannot_transfer_to_same_shop() { return "cannot do stock transfer to the same shop"; }
	public String exception_not_shop_owner() { return "you aren't the shop owner"; }
	
	public String user_name() { return "user_name"; }
	public String alias_id() { return "alias_id"; }
	public String email_address() { return "email_address"; }
	public String role_list() { return "role_list"; }
	public String disabled() { return "disabled"; }
	public String user_row_id() { return "user_row_id"; }
	public String password() { return "password"; }
	public String category_maintenance_title() { return "Product Category Configuration"; }
	public String category_id() { return "category_id"; }
	public String category_name() { return "Category Name"; }
	public String imei_flag() { return "IMEI"; }
	public String database_not_supported(String db_type) { return new StringBuilder().append("database [").append(db_type).append("] not supported").toString(); }
	public String color_maintenance_title() { return "Product Color Configuration"; }
	public String color_id() { return "color_id"; }
	public String color_name() { return "Color Name"; }
	public String size_maintenance_title() { return "Product Size Configuration"; }
	public String size_id() { return "size_id"; }
	public String size_name() { return "Size Name"; }
	public String item_id() { return "item_id"; }
	public String item_name() { return "Barcode"; }
	public String item_desc() { return "Product Name"; }
	public String item_filename() { return "item_filename"; }
	public String item_disabled() { return "Product Discontinued"; }
	public String price_sale() { return "Sale Price"; }
	public String lm_time() { return "lm_time"; }
	public String lm_user() { return "lm_user"; }
	public String item_maintenance_title() { return "Product Configuration"; }
	public String item_image() { return "Product Picture"; }
	public String shop_id() { return "shop_id"; }
	public String shop_name() { return "Outlet Name"; }
	public String shop_address() { return "Outlet Address"; }
	public String shop_telp() { return "Outlet Telp"; }
	public String item_qty() { return "Product Quantity"; }
	public String vendor_maintenance_title() { return "Supplier Configuration"; }
	public String vendor_id() { return "vendor_id"; }
	public String vendor_name() { return "Supplier Name"; }
	public String contact_person() { return "Contact Person"; }
	public String phone_no() { return "Phone"; }
	public String fax_no() { return "Fax"; }
	public String address() { return "Address"; }
	public String email() { return "Email"; }
	public String button_title_add_checked_item_to_list() { return "Add Checked Product to List"; }
	public String invoice_no() { return "發票號碼"; }
	public String item_list() { return "Products"; }
	public String price_buy() { return "Buy Price"; }
	public String total() { return "Total"; }
	public String qty() { return "Quantity"; }
	public String incoming_order_title() { return "Incoming Order"; }
	public String button_search() { return "Search"; }
	public String button_cancel() { return "Cancel"; }
	public String button_accept() { return "Accept"; }
	public String button_print() { return "Print"; }
	public String button_search_vendor() { return "Search Supplier"; }
	public String button_browse_vendor() { return "Browse Supplier"; }
	public String button_add_item() { return "Add Product"; }
	public String button_browse_item() { return "Browse Product"; }
	public String button_show_incoming_order_items() { return "Show Products"; }
	public String button_close() { return "Close"; }
	public String stock_transfer_title() { return "Stock Transfer Request"; }
	public String shop_name_from() { return "From Outlet"; }
	public String shop_name_to() { return "To Outlet"; }
	public String transfer_qty() { return "Transfer Quantity"; }
	public String button_create() { return "Create"; }
	public String button_confirm() { return "Confirm"; }
	public String comment() { return "Comment"; }
	public String button_add_inventory() { return "Add Inventory"; }
	public String button_browse_inventory() { return "Browse Inventory"; }
	public String create_time() { return "Create Time"; }
	public String shop_id_from() { return "shop_id_from"; }
	public String shop_id_to() { return "shop_id_to"; }
	public String transfer_id() { return "Transfer Id"; }
	public String status_id() { return "status_id"; }
	public String status_name() { return "Status"; }
	public String pending() { return "Pending"; }
	public String complete() { return "Complete"; }
	public String collected() { return "Collected"; }
	public String cancel() { return "Cancel"; }
	public String current_qty() { return "Current Quantity"; }
	public String adjust_qty() { return "Adjust Quantity"; }
	public String button_add_shop_user() { return "Add Outlet Employee"; }
	public String order_id() { return "Order Id"; }
	public String created() { return "Created"; }
	public String deleted() { return "Deleted"; }
	public String item_name_prefix() { return "Barcode Prefix"; }
	public String checkout() { return "Checkout"; }
	public String location() { return "Location"; }
	public String vendor_desc() { return "Note"; }
	public String expired_date() { return "Expired Date"; }
	public String item_point() { return "Product Point"; }
	public String item_hide() { return "Product Hidden"; }
	public String amount_ntd() { return "NTD"; }
	public String amount_usd() { return "USD"; }
	public String amount_idr() { return "Rupiah"; }
	public String kurs_value() { return "Rate"; }
	public String real_kurs_value() { return "Real Rate"; }
	public String kurs_usd() { return "NTD to USD Rate"; }
	public String kurs_idr() { return "USD to IDR Rate"; }
	public String lm_time_created() { return "Create Time"; }
	public String discount() { return "Discount"; }
}
