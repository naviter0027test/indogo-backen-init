CREATE OR REPLACE VIEW member_recipient_v AS
SELECT a.member_id, a.recipient_id, a.recipient_name, a.bank_code, a.bank_acc, a.lm_time, b.bank_name, a.lm_user, a.is_hidden, a.is_verified, b.swift_code, a.id_filename, a.birthday, a.recipient_name_2
  FROM member_recipient a, bank_code_list b
 WHERE a.bank_code = b.bank_code;

CREATE OR REPLACE VIEW inventory_v AS
SELECT a.item_qty, IFNULL(a.price_sale, b.price_sale) AS price_sale, a.lm_time, a.lm_user, a.shop_id, a.item_id,
b.item_name, b.item_filename, b.item_desc, b.item_disabled, b.item_hide, b.is_composite, b.item_point,
c.shop_name,
d.category_name,
e.color_name,
f.size_name,
i.pending_qty
FROM inventory a
INNER JOIN item b ON a.item_id = b.item_id
INNER JOIN shop c ON c.shop_id = a.shop_id
INNER JOIN item_category d ON d.category_id = b.category_id
LEFT JOIN item_color e ON e.color_id = b.color_id
LEFT JOIN item_size f ON f.size_id = b.size_id
left join (select g.shop_id, h.item_id, sum(h.sales_qty) as pending_qty
from sales g
inner join sales_item h on h.sales_id = g.sales_id
where g.status_id = 1
group by g.shop_id, h.item_id) i on i.shop_id = a.shop_id and i.item_id = a.item_id;

CREATE OR REPLACE VIEW item_v AS
select a.*, b.category_name, c.color_name, d.size_name
from item a
inner join item_category b on b.category_id = a.category_id
left join item_color c on c.color_id = a.color_id
left join item_size d on d.size_id = a.size_id;

create or replace view sales_returned_v as
select a.sales_id, a.ship_address, a.total_amount, a.invoice_no, a.ship_no, a.lm_time_created, a.comment, a.lm_time, a.lm_user, a.shop_id, a.status_id, a.member_id, a.freight_id, b.member_name, b.phone_no
from sales a
inner join member b on a.member_id = b.member_id;

create or replace view item_with_inventory_v as
select a.item_name, a.item_desc, a.price_sale, b.category_name, c.color_name, d.size_name, a.location, a.expired_date,
       a.item_point, a.item_filename, a.item_disabled, a.item_hide, a.lm_time, a.lm_user,
       a.item_id, b.category_id, c.color_id, d.size_id, a.is_composite,
       (select sum(item_qty) from inventory where item_id = a.item_id) as inventory_qty
  from item a
 inner join item_category b on b.category_id = a.category_id
  left join item_color c on c.color_id = a.color_id
  left join item_size d on d.size_id = a.size_id;

create or replace view money_transfer_v as
select a.payment_info, a.`comment`, a.is_print, a.txn_id,
b.member_name, b.phone_no, b.arc_no, b.arc_expire_date,
c.recipient_name, c.bank_code, d.bank_name, d.swift_code, c.bank_acc,
a.kurs_value, a.transfer_amount_ntd, a.transfer_amount_idr, a.service_charge,
a.total, a.member_id, a.recipient_id, a.payment_id, a.transfer_status_id,
a.lm_time, a.lm_user, c.is_verified, a.transfer_through_bank_name, a.is_app, a.bni_trx_date,
a.lm_time_paid, a.lm_time_transfer, b.birthday, b.status_id, a.mini_mart_id, a.export_time,
c.birthday as recipient_birthday, c.id_filename, c.recipient_name_2
from money_transfer a
inner join member b on b.member_id = a.member_id
inner join member_recipient c on c.member_id = a.member_id and c.recipient_id = a.recipient_id
inner join bank_code_list d on d.bank_code = c.bank_code;
