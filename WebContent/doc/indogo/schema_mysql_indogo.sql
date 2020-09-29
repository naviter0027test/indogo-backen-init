set names big5;

create table member_status (
	status_id tinyint,
	status_name varchar(50)
)
ENGINE=InnoDB;

alter table member_status add (
	constraint member_status_pk
	primary key (status_id)
);

insert into member_status (status_id, status_name) values (0, 'active');
insert into member_status (status_id, status_name) values (1, 'banned');

create table member_sex (
	sex_id tinyint,
	sex_name varchar(50)
)
ENGINE=InnoDB;

alter table member_sex add (
	constraint member_sex_pk
	primary key (sex_id)
);

insert into member_sex (sex_id, sex_name) values (1, 'male');
insert into member_sex (sex_id, sex_name) values (2, 'female');

CREATE TABLE member (
	member_id BIGINT,
	member_name VARCHAR (250),
	member_login_id VARCHAR(50),
	member_password VARCHAR(2000),
	phone_no VARCHAR(100),
	arc_no VARCHAR(20),
	arc_expire_date DATETIME,
	address VARCHAR(300),
	birthday DATETIME,
	lm_time DATETIME,
	lm_user varchar(100),
	arc_photo_basename VARCHAR(100),
	email varchar(100),
	remit_point int default 0,
	signature_photo_basename varchar(100),
	old_phone_no varchar(500),
	old_arc_photo varchar(100),
	old_signature_photo varchar(100),
	is_wait_confirm tinyint default 0,
	app_member_name varchar(300),
	app_phone_no varchar(100),
	app_email varchar(100),
	app_register_time datetime,
	status_id tinyint default 0,
	app_token varchar(32),
	app_sms_code int,
	app_sms_time datetime,
	app_share_id varchar(8),
    friend_id bigint,
    app_share_count int,
    app_remit_count int,
    app_register_followup varchar(200),
    app_arc_image varchar(100),
    app_signature_image varchar(100),
    app_address_image varchar(100),
    app_lm_time datetime,
    signature_need_fix bit(1) default 0,
    sex_id tinyint default 2,
    is_print bit(1) default 0,
    app_verify_time datetime,
    transfer_amount_idr bigint comment 'total transfer amount, cannot exceed quota',
    transfer_amount_ntd int,
    wallet int default 0
)
ENGINE=InnoDB;

ALTER TABLE member ADD (
	CONSTRAINT member_pk
	PRIMARY KEY (member_id)
);

alter table member add (
	constraint member_fk1
	foreign key (status_id)
	references member_status (status_id)
);

alter table member add (
	constraint member_fk2
	foreign key (sex_id)
	references member_sex (sex_id)
);

CREATE UNIQUE INDEX member_u1 ON member (arc_no);
CREATE UNIQUE INDEX member_u2 ON member (member_login_id);
create unique index member_u3 on member (app_token);
create unique index member_u4 on member (app_share_id);
CREATE INDEX member_i2 ON member (member_name);
create index member_i3 on member (is_wait_confirm);
create index member_i4 on member (status_id, phone_no);
create index member_i5 on member (status_id, arc_no);
create index member_i6 on member (status_id, member_name);

create table address_type (
	type_id int,
	type_name varchar(50)
);

alter table address_type add (
	constraint address_type_pk
	primary key (type_id)
);

insert into address_type (type_id, type_name) values (1, 'home');
insert into address_type (type_id, type_name) values (2, '7-11');
insert into address_type (type_id, type_name) values (3, 'family mart');
insert into address_type (type_id, type_name) values (4, 'ok mart');
insert into address_type (type_id, type_name) values (5, 'hilife');

create table member_address (
	member_id bigint,
	address_id int,
	type_id int,
	address_value varchar(300)
)
ENGINE=InnoDB;

alter table member_address add (
	constraint member_address_pk
	primary key (member_id, address_id)
);

alter table member_address add (
	constraint member_address_fk1
	foreign key (type_id)
	references address_type (type_id)
);

alter table member_address add (
	constraint member_address_fk2
	foreign key (member_id)
	references member (member_id)
);

create table bni_transfer_type (
	transfer_type smallint,
	transfer_desc varchar(20)
);

alter table bni_transfer_type add (
	constraint bni_transfer_type_pk
	primary key (transfer_type)
);

insert into bni_transfer_type (transfer_type, transfer_desc) values (1, 'Interbank');
insert into bni_transfer_type (transfer_type, transfer_desc) values (2, 'CreditToAccount');
insert into bni_transfer_type (transfer_type, transfer_desc) values (3, 'CashPickup');
insert into bni_transfer_type (transfer_type, transfer_desc) values (4, 'Clearing');
insert into bni_transfer_type (transfer_type, transfer_desc) values (5, 'RTGS');

CREATE TABLE bank_code_list (
	bank_code VARCHAR(10),
	bank_name VARCHAR(100),
	display_seq integer,
	lm_time DATETIME,
	lm_user varchar(100),
	clr_code varchar(20),
	rtgs_code varchar(20),
	transfer_type smallint,
	transfer_threshold_1 smallint,
	transfer_threshold_2 smallint,
	swift_code varchar(20)
)
ENGINE=InnoDB;

ALTER TABLE bank_code_list ADD (
	CONSTRAINT bank_code_list_pk
	PRIMARY KEY (bank_code)
);

alter table bank_code_list add (
	constraint bank_code_list_fk1
	foreign key (transfer_type)
	references bni_transfer_type (transfer_type)
);

alter table bank_code_list add (
	constraint bank_code_list_fk2
	foreign key (transfer_threshold_1)
	references bni_transfer_type (transfer_type)
);

alter table bank_code_list add (
	constraint bank_code_list_fk3
	foreign key (transfer_threshold_2)
	references bni_transfer_type (transfer_type)
);

create index bank_code_list_i1 on bank_code_list (bank_name);

CREATE TABLE member_recipient (
	member_id BIGINT,
	recipient_id int,
	recipient_name VARCHAR(300),
	bank_code VARCHAR(10),
	bank_acc VARCHAR(300),
	bank_branch varchar(100),
	lm_time DATETIME,
	lm_user varchar(100),
	is_verified bit(1) default 0,
	is_hidden bit(1) default 0,
	birthday datetime,
	id_filename varchar(100),
	recipient_name_2 varchar(300)
)
ENGINE=InnoDB;

ALTER TABLE member_recipient ADD (
	CONSTRAINT member_recipient_pk
	PRIMARY KEY (member_id, recipient_id)
);

ALTER TABLE member_recipient ADD (
	CONSTRAINT member_recipient_fk1
	FOREIGN KEY (member_id)
	REFERENCES member (member_id)
);

ALTER TABLE member_recipient ADD (
	CONSTRAINT member_recipient_fk2
	FOREIGN KEY (bank_code)
	REFERENCES bank_code_list (bank_code)
);

create unique index member_recipient_u2 on member_recipient (member_id, bank_code, bank_acc);

CREATE TABLE address_city (
	city_cname VARCHAR(50) NOT NULL,
	city_ename VARCHAR(100) NOT NULL,
	seq INT
)
ENGINE=InnoDB;

ALTER TABLE address_city ADD
CONSTRAINT address_city_pk
PRIMARY KEY (city_cname);

CREATE TABLE address_area (
	city_cname VARCHAR(50) NOT NULL,
	area_cname VARCHAR(50) NOT NULL,
	area_ename VARCHAR(100) NOT NULL,
	zipcode VARCHAR(10)
)
ENGINE=InnoDB;

ALTER TABLE address_area ADD
CONSTRAINT address_area_pk
PRIMARY KEY (city_cname, area_cname);

ALTER TABLE address_area ADD
CONSTRAINT address_area_fk1
FOREIGN KEY (city_cname)
REFERENCES address_city (city_cname);

CREATE TABLE address_road (
	city_cname VARCHAR(50) NOT NULL,
	area_cname VARCHAR(50) NOT NULL,
	road_cname VARCHAR(50) NOT NULL,
	road_ename VARCHAR(100) NOT NULL,
	zipcode VARCHAR(10),
	seq INT
)
ENGINE=InnoDB;

ALTER TABLE address_road ADD
CONSTRAINT address_road_pk
PRIMARY KEY (city_cname, area_cname, road_cname);

ALTER TABLE address_road ADD
CONSTRAINT address_road_fk1
FOREIGN KEY (city_cname, area_cname)
REFERENCES address_area (city_cname, area_cname);

create table kurs_history (
	lm_time datetime,
	lm_user varchar(100),
	kurs_value decimal(6,2)
)
ENGINE=InnoDB;

alter table kurs_history add (
	constraint kurs_history_pk
	primary key (lm_time)
);

create table payment_type (
	payment_id tinyint,
	payment_name varchar(50)
)
ENGINE=InnoDB;

alter table payment_type add (
	constraint payment_type_pk
	primary key (payment_id)
);

insert into payment_type (payment_id, payment_name) values (1, 'Family Mart');
insert into payment_type (payment_id, payment_name) values (2, 'Mini Mart');
insert into payment_type (payment_id, payment_name) values (3, 'Cash');
insert into payment_type (payment_id, payment_name) values (99, '黑貓');

create table transfer_status (
	transfer_status_id tinyint,
	transfer_status_name varchar(10)
)
ENGINE=InnoDB;

alter table transfer_status add (
	constraint transfer_status_pk
	primary key (transfer_status_id)
);

insert into transfer_status (transfer_status_id, transfer_status_name) values (1, 'pending');
insert into transfer_status (transfer_status_id, transfer_status_name) values (2, 'paid');
insert into transfer_status (transfer_status_id, transfer_status_name) values (3, 'process');
insert into transfer_status (transfer_status_id, transfer_status_name) values (4, 'transfered');
insert into transfer_status (transfer_status_id, transfer_status_name) values (5, 'failed');
insert into transfer_status (transfer_status_id, transfer_status_name) values (6, 'cancel');

create table mini_mart (
	mini_mart_id tinyint,
	mini_mart_name varchar(20)
)
ENGINE=InnoDB;

alter table mini_mart add (
	constraint mini_mart_pk
	primary key (mini_mart_id)
);

insert into mini_mart (mini_mart_id, mini_mart_name) values (1, '7-11');
insert into mini_mart (mini_mart_id, mini_mart_name) values (2, 'Family Mart');
insert into mini_mart (mini_mart_id, mini_mart_name) values (3, 'OK Mart');
insert into mini_mart (mini_mart_id, mini_mart_name) values (4, 'Hi Life');

create table money_transfer (
	txn_id bigint,
	member_id bigint,
	recipient_id int,
	payment_id tinyint,
	payment_info varchar(255),
	transfer_status_id tinyint,
	kurs_value decimal(6,2),
	transfer_amount_ntd int,
	transfer_amount_idr bigint,
	service_charge int,
	total int,
	is_print bit(1),
	lm_time datetime,
	lm_user varchar(100),
	transfer_through_bank_name varchar(20),
	comment varchar(2000),
	mini_mart_id tinyint,
	lm_time_pending datetime,
	lm_time_paid datetime,
	lm_time_process datetime,
	lm_time_transfer datetime,
	lm_time_failed datetime,
	lm_time_cancel datetime,
	store_id varchar(200),
	is_app bit(1) default 0,
	point_used smallint,
	bni_trx_date varchar(20) comment 'the bni name is misleading, this will store bni and bri response',
	is_notified bit(1) default 0,
	bni_retry tinyint,
	export_time datetime,
	exchange_id bigint,
	lm_time_expire datetime
)
ENGINE=InnoDB;

alter table money_transfer add (
	constraint money_transfer_pk
	primary key (txn_id)
);

alter table money_transfer add (
	constraint money_transfer_fk1
	foreign key (payment_id)
	references payment_type (payment_id)
);

alter table money_transfer add (
	constraint money_transfer_fk2
	foreign key (transfer_status_id)
	references transfer_status (transfer_status_id)
);

alter table money_transfer add (
	constraint money_transfer_fk3
	foreign key (member_id)
	references member (member_id)
);

alter table money_transfer add (
	constraint money_transfer_fk4
	foreign key (member_id, recipient_id)
	references member_recipient (member_id, recipient_id)
);

alter table money_transfer add (
	constraint money_transfer_fk5
	foreign key (mini_mart_id)
	references mini_mart (mini_mart_id)
);

create unique index money_transfer_u1 on money_transfer (payment_info);
create index money_transfer_i2 on money_transfer (member_id, transfer_status_id);
create index money_transfer_i3 on money_transfer (transfer_status_id, txn_id);
create index money_transfer_i4 on money_transfer (is_app, member_id);
create index money_transfer_i5 on money_transfer (is_notified, member_id);
create index money_transfer_i6 on money_transfer (payment_id, transfer_status_id, lm_time_expire);

create table money_transfer_status (
	txn_id bigint,
	lm_time datetime,
	lm_user varchar(100),
	old_status_id tinyint,
	new_status_id tinyint,
	comment varchar(2000),
	bank_name varchar(20)
)
ENGINE=InnoDB;

alter table money_transfer_status add (
	constraint money_transfer_status_pk
	primary key (txn_id, lm_time)
);

alter table money_transfer_status add (
	constraint money_transfer_status_fk1
	foreign key (old_status_id)
	references transfer_status (transfer_status_id)
);

alter table money_transfer_status add (
	constraint money_transfer_status_fk2
	foreign key (new_status_id)
	references transfer_status (transfer_status_id)
);

create table money_transfer_recipient (
	txn_id bigint,
	lm_time datetime,
	lm_user varchar(100),
	old_recipient_name varchar(300),
	old_bank_code varchar(10),
	old_bank_acc varchar(300),
	new_recipient_name varchar(300),
	new_bank_code varchar(10),
	new_bank_acc varchar(300)
)
ENGINE=InnoDB;

alter table money_transfer_recipient add (
	constraint money_transfer_recipient_pk
	primary key (txn_id, lm_time)
);

create table idr_history (
	lm_time datetime,
	lm_user varchar(100),
	amount bigint,
	total bigint,
	comment varchar(2000),
	txn_id bigint,
	kurs_value decimal(6,2),
	amount_ntd int
)
ENGINE=InnoDB;

create index idr_history_i1 on idr_history (lm_time);

create table usd_account (
	lm_time datetime,
	lm_user varchar(100),
	amount_ntd int,
	usd_to_ntd decimal(6, 2),
	amount_usd decimal(12, 2)
)
ENGINE=InnoDB;

alter table usd_account add (
	constraint usd_account_pk
	primary key (lm_time)
);

create table usd_history (
	lm_time datetime,
	seq_no int,
	lm_user varchar(100),
	amount_ntd int,
	usd_to_ntd decimal(8, 4),
	amount_usd decimal(14, 4),
	usd_to_idr decimal(10, 4),
	amount_idr bigint,
	ntd_to_idr decimal(8, 4),
	amount_ntd_used int,
	comment varchar(2000),
	lm_time_used datetime
)
ENGINE=InnoDB;

alter table usd_history add (
	constraint usd_history_pk
	primary key (lm_time, seq_no)
);

create table member_point_reason (
	reason_id tinyint,
	reason_name varchar(100)
);

alter table member_point_reason add (
	constraint member_point_reason_pk
	primary key (reason_id)
);

insert into member_point_reason (reason_id, reason_name) values (1, 'app download bonus');
insert into member_point_reason (reason_id, reason_name) values (2, 'app share bonus');
insert into member_point_reason (reason_id, reason_name) values (3, 'app create invoice bonus');
insert into member_point_reason (reason_id, reason_name) values (4, 'redeem point for free service charge');
insert into member_point_reason (reason_id, reason_name) values (5, 'manual');
insert into member_point_reason (reason_id, reason_name) values (6, 'bonus for each 100 remit transactions');
insert into member_point_reason (reason_id, reason_name) values (7, 'bonus for each 50 friends invited');
insert into member_point_reason (reason_id, reason_name) values (8, 'birthday bonus');
insert into member_point_reason (reason_id, reason_name) values (9, 'special occassion bonus');
insert into member_point_reason (reason_id, reason_name) values (10, 'create invoice');
insert into member_point_reason (reason_id, reason_name) values (11, 'scrap invoice');

create table member_point_birthday (
	member_id bigint,
	bonus_year int
)
ENGINE=InnoDB;

alter table member_point_birthday add (
	constraint member_point_birthday_pk
	primary key (member_id, bonus_year)
);

create table bni_key_store (
	create_date datetime,
	file_name varchar(50),
	expire_date datetime,
	pwd varchar(250)
)
ENGINE=InnoDB;

alter table bni_key_store add (
	constraint bni_key_store_pk
	primary key (create_date)
);

create table member_point_schedule (
	schedule_id int,
	date_of_month tinyint,
	month_id tinyint,
	year_id smallint,
	remit_point int,
	schedule_desc varchar(150)
)
ENGINE=InnoDB;

alter table member_point_schedule add (
	constraint member_point_schedule_pk
	primary key (schedule_id)
);

create table mini_mart_store (
	mini_mart_id tinyint,
	store_id varchar(200),
	store_name varchar(100),
	store_addr varchar(2000),
	store_area varchar(20)
)
ENGINE=InnoDB;

alter table mini_mart_store add (
	constraint mini_mart_store_pk
	primary key (mini_mart_id, store_id)
);

alter table mini_mart_store add (
	constraint mini_mart_store_fk1
	foreign key (mini_mart_id)
	references mini_mart (mini_mart_id)
);

create table send_sms_record (
	phone_no varchar(20),
	send_count smallint
)
ENGINE=InnoDB;

alter table send_sms_record add (
	constraint send_sms_record_pk
	primary key (phone_no)
);

create table recipient_transfer_count (
	lm_time date,
	bank_code VARCHAR(10),
	bank_acc VARCHAR(300),
	transfer_count smallint
)
ENGINE=InnoDB;

alter table recipient_transfer_count add (
	constraint recipient_transfer_count_pk
	primary key (lm_time, bank_code, bank_acc)
);

create table item_category (
	category_id smallint,
	category_name varchar(50),
	imei_flag bit(1) default 0,
	item_name_prefix varchar(50)
)
engine=InnoDB;

alter table item_category add (
	constraint item_category_pk
	primary key (category_id)
);

create table item_color (
	color_id smallint,
	color_name varchar(50)
)
engine=InnoDB;

alter table item_color add (
	constraint item_color_pk
	primary key (color_id)
);

create unique index item_color_u1 on item_color (color_name);

create table item_size (
	size_id smallint,
	size_name varchar(50)
)
engine=InnoDB;

alter table item_size add (
	constraint item_size_pk
	primary key (size_id)
);

create unique index item_size_u1 on item_size (size_name);

create table item (
	item_id int,
	item_name varchar(100),
	item_desc varchar(1000),
	item_filename varchar(50),
	category_id smallint,
	color_id smallint,
	size_id smallint,
	item_disabled bit(1) default 0 comment 'cannot restock but can sell',
	price_sale int,
	lm_time datetime,
	lm_user varchar(100),
	location varchar(100),
	expired_date datetime,
	item_point int,
	item_hide bit(1) default 0 comment 'cannot restock and sell',
	is_composite bit(1) default 0,
	has_gift bit(1) default 0
)
engine=InnoDB;

alter table item add (
	constraint item_pk
	primary key (item_id)
);

alter table item add (
	constraint item_fk1
	foreign key (category_id)
	references item_category (category_id)
);

alter table item add (
	constraint item_fk2
	foreign key (color_id)
	references item_color (color_id)
);

alter table item add (
	constraint item_fk3
	foreign key (size_id)
	references item_size (size_id)
);

create unique index item_u1 on item (item_name);

create index item_i1 on item (has_gift);

create table barcode (
	barcode_id varchar(100),
	item_id int
)
engine=InnoDB;

alter table barcode add (
	constraint barcode_pk
	primary key (barcode_id)
);

alter table barcode add (
	constraint barcode_fk1
	foreign key (item_id)
	references item (item_id)
);

create index barcode_i1 on barcode (item_id);

create table item_composite (
	composite_id int,
	item_id int,
	item_qty int
)
engine=InnoDB;

alter table item_composite add (
	constraint item_composite_pk
	primary key (composite_id, item_id)
);

alter table item_composite add (
	constraint item_composite_fk1
	foreign key (composite_id)
	references item (item_id)
);

alter table item_composite add (
	constraint item_composite_fk2
	foreign key (item_id)
	references item (item_id)
);

create table shop (
	shop_id smallint,
	shop_name varchar(50),
	shop_address varchar(200),
	shop_telp varchar(50)
)
engine=InnoDB;

alter table shop add (
	constraint shop_pk
	primary key (shop_id)
);

create table shop_user (
	user_row_id int,
	shop_id smallint
)
engine=InnoDB;

alter table shop_user add (
	constraint shop_user_pk
	primary key (user_row_id, shop_id)
);

alter table shop_user add (
	constraint shop_user_fk1
	foreign key (shop_id)
	references shop (shop_id)
);

alter table shop_user add (
	constraint shop_user_fk2
	foreign key (user_row_id)
	references user_list (user_row_id)
);

create index shop_user_i1 on shop_user (shop_id);

create table inventory_adjust (
	adjust_id bigint,
	shop_id smallint,
	lm_time datetime,
	lm_user varchar(100)
)
engine=InnoDB;

alter table inventory_adjust add (
	constraint inventory_adjust_pk
	primary key (adjust_id)
);

create index inventory_adjust_i1 on inventory_adjust (lm_time, shop_id);

alter table inventory_adjust add (
	constraint inventory_adjust_fk1
	foreign key (shop_id)
	references shop (shop_id)
);

create table inventory_adjust_item (
	adjust_id bigint,
	item_id int,
	old_qty int,
	new_qty int,
	comment varchar(250)
)
engine=InnoDB;

alter table inventory_adjust_item add (
	constraint inventory_adjust_item_pk
	primary key (adjust_id, item_id)
);

alter table inventory_adjust_item add (
	constraint inventory_adjust_item_fk1
	foreign key (item_id)
	references item (item_id)
);

create table inventory (
	shop_id smallint,
	item_id int,
	item_qty int default 0,
	price_sale int,
	lm_time datetime,
	lm_user varchar(100)
)
engine=InnoDB;

alter table inventory add (
	constraint inventory_pk
	primary key (shop_id, item_id)
);

alter table inventory add (
	constraint inventory_fk1
	foreign key (shop_id)
	references shop (shop_id)
);

alter table inventory add (
	constraint inventory_fk2
	foreign key (item_id)
	references item (item_id)
);

create table vendor (
	vendor_id int,
	vendor_name varchar(100),
	contact_person varchar(100),
	phone_no varchar(50),
	fax_no varchar(50),
	address varchar(100),
	email varchar(100),
	lm_time datetime,
	lm_user varchar(100),
	vendor_desc varchar(200)
)
engine=InnoDB;

alter table vendor add (
	constraint vendor_pk
	primary key (vendor_id)
);

create table incoming_order_status (
	status_id tinyint,
	status_name varchar(20)
)
engine=InnoDB;

alter table incoming_order_status add (
	constraint incoming_order_status_pk
	primary key (status_id)
);

insert into incoming_order_status (status_id, status_name) values (1, 'created');
insert into incoming_order_status (status_id, status_name) values (2, 'deleted');

create table incoming_order (
	order_id bigint,
	create_time datetime,
	vendor_id int,
	invoice_no varchar(50),
	total int default 0,
	comment varchar(1000),
	shop_id smallint,
	lm_time datetime,
	lm_user varchar(100),
	status_id tinyint,
	create_user varchar(100)
)
engine=InnoDB;

alter table incoming_order add (
	constraint incoming_order_pk
	primary key (order_id)
);

alter table incoming_order add (
	constraint incoming_order_fk1
	foreign key (vendor_id)
	references vendor (vendor_id)
);

alter table incoming_order add (
	constraint incoming_order_fk2
	foreign key (shop_id)
	references shop (shop_id)
);

alter table incoming_order add (
	constraint incoming_order_fk3
	foreign key (status_id)
	references incoming_order_status (status_id)
);

create table incoming_order_item (
	order_id bigint,
	item_id int,
	qty int default 0,
	price_buy int default 0,
	discount int default 0,
	total int default 0
)
engine=InnoDB;

alter table incoming_order_item add (
	constraint incoming_order_item_pk
	primary key (order_id, item_id)
);

alter table incoming_order_item add (
	constraint incoming_order_item_fk1
	foreign key (item_id)
	references item (item_id)
);

alter table incoming_order_item add (
	constraint incoming_order_item_fk2
	foreign key (order_id)
	references incoming_order (order_id)
);

create table stock_transfer_status (
	status_id tinyint,
	status_name varchar(20)
)
engine=InnoDB;

alter table stock_transfer_status add (
	constraint stock_transfer_status_pk
	primary key (status_id)
);

insert into stock_transfer_status (status_id, status_name) values (1, 'created');
insert into stock_transfer_status (status_id, status_name) values (2, 'collect');
insert into stock_transfer_status (status_id, status_name) values (3, 'cancel');

create table stock_transfer (
	transfer_id bigint,
	create_time datetime,
	shop_id_from smallint,
	shop_id_to smallint,
	status_id tinyint,
	comment varchar(1000),
	lm_time datetime,
	lm_user varchar(100),
	qrcode_filename varchar(40),
	lm_time_1 datetime,
	lm_user_1 varchar(100),
	lm_time_2 datetime,
	lm_user_2 varchar(100),
	lm_time_3 datetime,
	lm_user_3 varchar(100)
)
engine=InnoDB;

alter table stock_transfer add (
	constraint stock_transfer_pk
	primary key (transfer_id)
);

alter table stock_transfer add (
	constraint stock_transfer_fk1
	foreign key (status_id)
	references stock_transfer_status (status_id)
);

alter table stock_transfer add (
	constraint stock_transfer_fk2
	foreign key (shop_id_from)
	references shop (shop_id)
);

alter table stock_transfer add (
	constraint stock_transfer_fk3
	foreign key (shop_id_to)
	references shop (shop_id)
);

create table stock_transfer_item (
	transfer_id bigint,
	item_id int,
	qty int
)
engine=InnoDB;

alter table stock_transfer_item add (
	constraint stock_transfer_item_pk
	primary key (transfer_id, item_id)
);

alter table stock_transfer_item add (
	constraint stock_transfer_item_fk1
	foreign key (transfer_id)
	references stock_transfer (transfer_id)
);

alter table stock_transfer_item add (
	constraint stock_transfer_item_fk2
	foreign key (item_id)
	references item (item_id)
);

create table freight (
	freight_id tinyint,
	freight_name varchar(20)
)
engine=InnoDB;

alter table freight add (
	constraint freight_pk
	primary key (freight_id)
);

insert into freight (freight_id, freight_name) values (1, 'heimao');
insert into freight (freight_id, freight_name) values (2, 'cash');
insert into freight (freight_id, freight_name) values (3, 'post');

create table sales_status (
	status_id tinyint,
	status_name varchar(20)
)
engine=InnoDB;

alter table sales_status add (
	constraint sales_status_pk
	primary key (status_id)
);

insert into sales_status (status_id, status_name) values (1, 'created');
insert into sales_status (status_id, status_name) values (2, 'checkout');
insert into sales_status (status_id, status_name) values (3, 'shipped');
insert into sales_status (status_id, status_name) values (4, 'paid');
insert into sales_status (status_id, status_name) values (5, 'returned');
insert into sales_status (status_id, status_name) values (6, 'scrapped');

create table sales (
	sales_id bigint,
	shop_id smallint,
	status_id tinyint,
	member_id bigint,
	freight_id tinyint,
	ship_address varchar(200),
	total_amount int,
	lm_time datetime,
	lm_user varchar(100),
	invoice_no varchar(20),
	ship_no varchar(100),
	lm_time_created datetime,
	lm_time_checkout datetime,
	lm_time_shipped datetime,
	lm_time_paid datetime,
	lm_time_returned datetime,
	lm_time_scrapped datetime,
	lm_user_created varchar(100),
	lm_user_checkout varchar(100),
	lm_user_shipped varchar(100),
	lm_user_paid varchar(100),
	lm_user_returned varchar(100),
	lm_user_scrapped varchar(100),
	ship_fee int,
	is_print bit(1) default 0,
	comment varchar(200),
	point_used int,
	print_by varchar(100),
	wallet_used int
)
engine=InnoDB;

alter table sales add (
	constraint sales_pk
	primary key (sales_id)
);

alter table sales add (
	constraint sales_fk1
	foreign key (status_id)
	references sales_status (status_id)
);

alter table sales add (
	constraint sales_fk2
	foreign key (member_id)
	references member (member_id)
);

alter table sales add (
	constraint sales_fk3
	foreign key (freight_id)
	references freight (freight_id)
);

alter table sales add (
	constraint sales_fk4
	foreign key (shop_id)
	references shop (shop_id)
);

create index sales_i1 on sales (member_id, lm_time_created);
create index sales_i2 on sales (ship_no);

create table sales_item (
	sales_id bigint,
	item_id int,
	sales_qty int,
	sales_price int,
	sales_discount int,
	sales_total int,
	comment varchar(200)
)
engine=InnoDB;

alter table sales_item add (
	constraint sales_item_pk
	primary key (sales_id, item_id)
);

create table sales_imei (
	imei_id varchar(50),
	sales_id bigint,
	item_id int
);

alter table sales_imei add (
	constraint sales_imei_pk
	primary key (imei_id)
);

create index sales_imei_i1 on sales_imei (sales_id);

create table member_point (
	member_id bigint,
	lm_time datetime,
	remit_point int,
	friend_id bigint,
	txn_id bigint,
	reason_id tinyint,
	reason_desc varchar(200),
	sales_id bigint
)
ENGINE=InnoDB;

alter table member_point add (
	constraint member_point_pk
	primary key (member_id, lm_time)
);

alter table member_point add (
	constraint member_point_fk1
	foreign key (member_id)
	references member (member_id)
);

alter table member_point add (
	constraint member_point_fk2
	foreign key (friend_id)
	references member (member_id)
);

alter table member_point add (
	constraint member_point_fk3
	foreign key (txn_id)
	references money_transfer (txn_id)
);

alter table member_point add (
	constraint member_point_fk4
	foreign key (reason_id)
	references member_point_reason (reason_id)
);

alter table member_point add (
	constraint member_point_fk5
	foreign key (sales_id)
	references sales (sales_id)
);

create table inventory_history_reason (
	reason_id smallint,
	reason_name varchar(50)
)
ENGINE=InnoDB;

alter table inventory_history_reason add (
	constraint inventory_history_reason_pk
	primary key (reason_id)
);

insert into inventory_history_reason (reason_id, reason_name) values (1, 'incoming order create');
insert into inventory_history_reason (reason_id, reason_name) values (2, 'incoming order cancel');
insert into inventory_history_reason (reason_id, reason_name) values (3, 'sales create');
insert into inventory_history_reason (reason_id, reason_name) values (4, 'sales cancel');
insert into inventory_history_reason (reason_id, reason_name) values (5, 'manual edit');
insert into inventory_history_reason (reason_id, reason_name) values (6, 'mass adjust');
insert into inventory_history_reason (reason_id, reason_name) values (7, 'stock transfer create');
insert into inventory_history_reason (reason_id, reason_name) values (8, 'stock transfer collect');
insert into inventory_history_reason (reason_id, reason_name) values (9, 'stock transfer cancel');

create table inventory_history (
	log_id bigint,
	reason_id smallint,
	shop_id smallint,
	item_id int,
	item_old_qty int,
	item_diff_qty int,
	item_new_qty int,
	lm_time datetime,
	lm_user varchar(100),
	sales_id bigint,
	order_id bigint,
	adjust_id bigint,
	transfer_id bigint
)
ENGINE=InnoDB;

alter table inventory_history add (
	constraint inventory_history_pk
	primary key (log_id)
);

alter table inventory_history add (
	constraint inventory_history_fk1
	foreign key (reason_id)
	references inventory_history_reason (reason_id)
);

alter table inventory_history add (
	constraint inventory_history_fk2
	foreign key (shop_id, item_id)
	references inventory (shop_id, item_id)
);

alter table inventory_history add (
	constraint inventory_history_fk3
	foreign key (sales_id)
	references sales (sales_id)
);

alter table inventory_history add (
	constraint inventory_history_fk4
	foreign key (order_id)
	references incoming_order (order_id)
);

alter table inventory_history add (
	constraint inventory_history_fk5
	foreign key (adjust_id)
	references inventory_adjust (adjust_id)
);

alter table inventory_history add (
	constraint inventory_history_fk6
	foreign key (transfer_id)
	references stock_transfer (transfer_id)
);

create table heimao_import (
	import_date datetime,
	filename varchar(100),
	lm_time datetime,
	lm_user varchar(100)
)
ENGINE=InnoDB;

alter table heimao_import add (
	constraint heimao_import_pk
	primary key (import_date)
);

create table heimao_import_list (
	import_date datetime,
	seq int,
	shipping_date varchar(100),
	office varchar(100),
	shipping_number varchar(100),
	dest_area varchar(100),
	invoice_no varchar(100),
	expected_money varchar(100),
	collected_money varchar(100),
	fee varchar(100),
	comment varchar(100)
)
ENGINE=InnoDB;

alter table heimao_import_list add (
	constraint heimao_import_list_pk
	primary key (import_date, seq)
);

create table exchange (
	exchange_id bigint,
	kurs_usd decimal(6,2) comment 'ntd to usd rate',
	kurs_idr decimal(10,2) comment 'usd to idr rate',
	kurs_value decimal(6,2) comment 'ntd to idr rate',
	lm_time datetime,
	lm_user varchar(100)
)
ENGINE=InnoDB;

alter table exchange add (
	constraint exchange_pk
	primary key (exchange_id)
);

create table exchange_item (
	exchange_id bigint,
	txn_id bigint,
	old_kurs_value decimal(6,2),
	old_amount_idr bigint,
	new_kurs_value decimal(6,2),
	new_amount_idr bigint
)
ENGINE=InnoDB;

alter table exchange_item add (
	constraint exchange_item_pk
	primary key (exchange_id, txn_id)
);

create table item_gift (
	item_id int,
	gift_id int,
	gift_qty int
)
ENGINE=InnoDB;

alter table item_gift add (
	constraint item_gift_pk
	primary key (item_id, gift_id)
);

alter table item_gift add (
	constraint item_gift_fk1
	foreign key (item_id)
	references item (item_id)
);

alter table item_gift add (
	constraint item_gift_fk2
	foreign key (gift_id)
	references item (item_id)
);

create table member_history (
	member_id bigint,
	lm_time datetime,
	lm_user varchar(100),
	hst_id int,
	hst_desc varchar(200)
)
engine=InnoDB;

alter table member_history add (
	constraint member_history_pk
	primary key (member_id, hst_id, lm_time)
);

create table member_wallet_hst (
	member_id bigint,
	lm_time datetime,
	wallet int,
	sales_id bigint
)
engine=InnoDB;

alter table member_wallet_hst add (
	constraint member_wallet_hst_pk
	primary key (member_id, lm_time)
);

alter table member_wallet_hst add (
	constraint member_wallet_hst_fk1
	foreign key (member_id)
	references member (member_id)
);

alter table member_wallet_hst add (
	constraint member_wallet_hst_fk2
	foreign key (sales_id)
	references sales (sales_id)
);

INSERT INTO global_config (group_name, config_name, config_value) VALUES ('member', 'arc_photo_directory', 'sftp://tomcat:tomcat#cat#@61.63.47.154/images/arc');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('member', 'arc_photo_directory_php', '/data/tomcat/images/arc');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('member', 'arc_photo_url', 'http://61.63.47.154/tomcat_images/arc');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('member', 'signature_photo_directory', 'sftp://tomcat:tomcat#cat#@61.63.47.154/images/signature');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('member', 'signature_photo_directory_php', '/data/tomcat/images/signature');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('member', 'signature_photo_url', 'http://61.63.47.154/tomcat_images/signature');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('member', 'address_photo_directory', 'sftp://tomcat:tomcat#cat#@61.63.47.154/images/address');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('member', 'address_photo_directory_php', '/data/tomcat/images/address');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('member', 'address_photo_url', 'http://61.63.47.154/tomcat_images/address');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('member', 'recipient_photo_directory', 'sftp://tomcat:tomcat#cat#@61.63.47.154/images/recipient');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('member', 'recipient_photo_url', 'http://61.63.47.154/tomcat_images/recipient');

INSERT INTO global_config (group_name, config_name, config_value) VALUES ('money_transfer', 'service_charge', '150');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('money_transfer', 'print_label_default', 'print_yoho');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'mini_mart_max_amount', '20000');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'bri_fee_idr', '5000');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'bri_fee_to_non_bri', '10000');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'bni_fee_idr', '0');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'bni_fee_to_non_bni', '0');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'font_arial', '/usr/local/apache-tomcat-8.5.12/ARIALUNI.TTF');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'font_alger', '/usr/local/apache-tomcat-8.5.12/ALGER.TTF');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'change_status_allowed_role_ids', '1,2');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'print_image_arc_width', '280');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'print_image_arc_height', '140');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'print_image_signature_width', '100');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'print_image_signature_height', '40');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'recipient_transfer_count', '10');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'allow_console_from_time', '07:01:00');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'allow_console_to_time', '22:59:00');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'enable_create_invoice', '1');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'enable_create_invoice_console', '1');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'total_transfer_amount_ntd', '0');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'max_create_count_0', '0');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'max_create_count_1', '0');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'max_create_count_2', '0');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'max_create_count_3', '0');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'max_create_count_4', '0');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'max_create_count_5', '0');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'max_create_count_6', '0');
insert into global_config (group_name, config_name, config_value) values ('money_transfer', 'max_recipient_count', '0');

insert into global_config (group_name, config_name, config_value) values ('import_bri', 'success_status_desc', '0001 : TRANSACTION SUCCESS');

INSERT INTO global_config (group_name, config_name, config_value) VALUES ('print_yoho', 'company_name', '數位點子多媒體股份有限公司');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('print_yoho', 'company_bank_account', 'BANK RAKYAT INDONESIA - BRI REMITTANCE, 帳號 020602001359307');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('print_yoho', 'company_phone_no', '02-2955959 #2217');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('print_yoho', 'company_uid', '27294437');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('print_yoho', 'company_address', '臺北市內湖區安美街181號5樓');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'seal_1_path', 'sftp://tomcat:tomcat#cat#@61.63.47.154/images/seal_1.png');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'seal_1_x', '460');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'seal_1_y', '210');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'seal_1_w', '40');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'seal_1_h', '40');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'seal_2_path', 'sftp://tomcat:tomcat#cat#@61.63.47.154/images/seal_2.png');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'seal_2_x', '380');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'seal_2_y', '190');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'seal_2_w', '80');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'seal_2_h', '80');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'bank_stamp_path', 'sftp://tomcat:tomcat#cat#@61.63.47.154/images/bank_stamp.png');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'bank_stamp_x', '90');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'bank_stamp_y', '60');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'bank_stamp_w', '140');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'bank_stamp_h', '60');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'arc_x', '160');
insert into global_config (group_name, config_name, config_value) values ('print_yoho', 'arc_y', '40');

INSERT INTO global_config (group_name, config_name, config_value) VALUES ('print_royal', 'company_name', '遞億行有限公司');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('print_royal', 'company_bank_account', 'PAN INDONESIA BANK-CHRISTIANA SETIJAWATI,帳號4424081555,BANK CENTRAL ASIA-CHRISTIANA SETIJAWATI,帳號6670240108');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('print_royal', 'company_phone_no', '02-2311-5080');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('print_royal', 'company_uid', '24326034');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('print_royal', 'company_address', '台北市中山區新生北路三段61號3樓之22');

insert into global_config (group_name, config_name, config_value) values ('money_transfer_family', 'family_expire', '1');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_family', 'family_url', 'http://test.familynet.com.tw/pin/webec.asmx');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_family', 'family_cuid', '27294437');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_family', 'family_termino', 'KK1ZKK1');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_family', 'family_paytype', 'cash');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_family', 'family_prddesc', 'remit');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_family', 'family_paycompany', 'IndoGO');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_family', 'family_system_id', 'indogotest');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_family', 'family_system_pwd', '2qd3rf4g5j');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_family', 'family_xml_ver', '05.01');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_family', 'family_xml_to', '99027');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_family', 'http_so_timeout', '3000');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_family', 'http_connection_timeout', '3000');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_family', 'http_request_timeout', '3000');

insert into global_config (group_name, config_name, config_value) values ('money_transfer_ibon', 'customer_service_phone', '(02)87928773');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_ibon', 'expire_date', '1');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_ibon', 'expire_date_console', '1');
insert into global_config (group_name, config_name, config_value) values ('money_transfer_ibon', 'total_create_count', '0');

insert into global_config (group_name, config_name, config_value) values ('usd_history', 'lock', 'lock');

insert into global_config (group_name, config_name, config_value) values ('kotsms', 'account', 'INDOGO88');
insert into global_config (group_name, config_name, config_value) values ('kotsms', 'password', 'indogo279588');
insert into global_config (group_name, config_name, config_value) values ('kotsms', 'message', '[sms_code] masukan kode ini untuk registrasi aplikasi Indogo');
insert into global_config (group_name, config_name, config_value) values ('kotsms', 'resend_threshold_seconds', '300');
insert into global_config (group_name, config_name, config_value) values ('kotsms', 'msg_expire_seconds', '300');
insert into global_config (group_name, config_name, config_value) values ('kotsms', 'register_complete_message', 'Data member anda sudah diverifikasi, silahkan login aplikasi Indogo');
insert into global_config (group_name, config_name, config_value) values ('kotsms', 'http_so_timeout', '3000');
insert into global_config (group_name, config_name, config_value) values ('kotsms', 'http_connection_timeout', '3000');
insert into global_config (group_name, config_name, config_value) values ('kotsms', 'http_request_timeout', '3000');
insert into global_config (group_name, config_name, config_value) values ('kotsms', 'msg_footnote', '[this is footnote]');

insert into global_config (group_name, config_name, config_value) values ('app_share', 'download_url', 'http://liontrix.asuscomm.com/work/indogo_app.apk');
insert into global_config (group_name, config_name, config_value) values ('app_share', 'sms_message', '[download_url] Anda mendapat undangan dari [member_name], silahkan download aplikasi Indogo dan masukkan kode point in [app_share_id] untuk mendapatkan point');
insert into global_config (group_name, config_name, config_value) values ('app_share', 'google_map_url', 'geo:0,0?q=7-eleven | familymart');
insert into global_config (group_name, config_name, config_value) values ('app_share', 'app_version', '1.11');
insert into global_config (group_name, config_name, config_value) values ('app_share', 'app_market', 'market://details?id=tw.com.indogo.indogoremit');
insert into global_config (group_name, config_name, config_value) values ('app_share', 'line_url', 'http://line.me/ti/p/%40indogo');

insert into global_config (group_name, config_name, config_value) values ('remit_point', 'free_service_charge', '150');
insert into global_config (group_name, config_name, config_value) values ('remit_point', 'app_register_bonus', '10');
insert into global_config (group_name, config_name, config_value) values ('remit_point', 'app_share_bonus', '20');
insert into global_config (group_name, config_name, config_value) values ('remit_point', 'app_share_50th_bonus', '150');
insert into global_config (group_name, config_name, config_value) values ('remit_point', 'app_share_50th_threshold', '50');
insert into global_config (group_name, config_name, config_value) values ('remit_point', 'app_create_invoice_bonus', '10');
insert into global_config (group_name, config_name, config_value) values ('remit_point', 'app_create_invoice_threshold', '150');
insert into global_config (group_name, config_name, config_value) values ('remit_point', 'app_remit_100th_bonus', '300');
insert into global_config (group_name, config_name, config_value) values ('remit_point', 'app_remit_100th_threshold', '100');
insert into global_config (group_name, config_name, config_value) values ('remit_point', 'birthday_bonus', '30');

insert into global_config (group_name, config_name, config_value) values ('bni_h2h', 'jks_directory', 'c:\\work');
insert into global_config (group_name, config_name, config_value) values ('bni_h2h', 'keytool_location', 'c:\\java\\jdk1.7.0_79\\bin\\keytool.exe');
insert into global_config (group_name, config_name, config_value) values ('bni_h2h', 'client_id', 'INDOGO');
insert into global_config (group_name, config_name, config_value) values ('bni_h2h', 'private_key_alias_id', 'indogo_bni_h2h');
insert into global_config (group_name, config_name, config_value) values ('bni_h2h', 'url', 'https://remitapi.bni.co.id:{port}/remittance/incoming/indogo');
insert into global_config (group_name, config_name, config_value) values ('bni_h2h', 'port', '55001');
insert into global_config (group_name, config_name, config_value) values ('bni_h2h', 'default_key_store_filename', 'indogo_dev.jks');
insert into global_config (group_name, config_name, config_value) values ('bni_h2h', 'transfer_threshold_1', '25000000');
insert into global_config (group_name, config_name, config_value) values ('bni_h2h', 'transfer_threshold_2', '500000000');
insert into global_config (group_name, config_name, config_value) values ('bni_h2h', 'http_so_timeout', '120000');
insert into global_config (group_name, config_name, config_value) values ('bni_h2h', 'http_connection_timeout', '5000');
insert into global_config (group_name, config_name, config_value) values ('bni_h2h', 'http_request_timeout', '120000');
insert into global_config (group_name, config_name, config_value) values ('bni_h2h', 'verify_recipient', 'false');

INSERT INTO global_config (group_name, config_name, config_value) VALUES ('bri_h2h', 'agent_code', 'BRC 04');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('bri_h2h', 'http_connection_timeout', '5000');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('bri_h2h', 'http_request_timeout', '120000');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('bri_h2h', 'http_so_timeout', '120000');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('bri_h2h', 'ip_address', '127.0.0.1');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('bri_h2h', 'username', 'BRC04WS');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('bri_h2h', 'password', 'AOGgNBUdnS3AM9sDnYD_CQ');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('bri_h2h', 'url', 'http://trx.dev.brifast.co.id/Webservice/brifastService');

INSERT INTO global_config (group_name, config_name, config_value) VALUES ('employee_performance', 'score_create_invoice', '40');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('employee_performance', 'score_create_member', '60');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('employee_performance', 'score_verify_old_member', '15');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('employee_performance', 'score_verify_new_member', '90');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('employee_performance', 'score_create_signature', '20');

insert into global_config (group_name, config_name, config_value) values ('h2h', 'default_acc_verification', 'BNI');

insert into global_config (group_name, config_name, config_value) values ('item', 'image_base_directory', 'sftp://tomcat:tomcat#cat#@61.63.47.154/images/item');
insert into global_config (group_name, config_name, config_value) values ('item', 'image_base_url', 'http://61.63.47.154/tomcat_images/item');
insert into global_config (group_name, config_name, config_value) values ('item', 'image_temp_directory', 'sftp://tomcat:tomcat#cat#@61.63.47.154/images/item_temp');
insert into global_config (group_name, config_name, config_value) values ('item', 'image_temp_url', 'http://61.63.47.154/tomcat_images/item_temp');

insert into global_config (group_name, config_name, config_value) values ('sales', 'ship_fee_heimao', '150');
insert into global_config (group_name, config_name, config_value) values ('sales', 'ship_fee_post', '200');

insert into global_config (group_name, config_name, config_value) values ('inventory', 'allow_edit_qty_role_ids', '1,2');

insert into global_config (group_name, config_name, config_value) values ('heimao_import_column', 'shipping_date', '出貨日期');
insert into global_config (group_name, config_name, config_value) values ('heimao_import_column', 'office', '集貨營業所');
insert into global_config (group_name, config_name, config_value) values ('heimao_import_column', 'shipping_number', '託運單號');
insert into global_config (group_name, config_name, config_value) values ('heimao_import_column', 'dest_area', '到貨區域');
insert into global_config (group_name, config_name, config_value) values ('heimao_import_column', 'invoice_no', '訂單號碼');
insert into global_config (group_name, config_name, config_value) values ('heimao_import_column', 'expected_money', '應收金額');
insert into global_config (group_name, config_name, config_value) values ('heimao_import_column', 'collected_money', '實收金額');
insert into global_config (group_name, config_name, config_value) values ('heimao_import_column', 'fee', '手續費');
insert into global_config (group_name, config_name, config_value) values ('heimao_import_column', 'comment', '備註');

insert into global_config (group_name, config_name, config_value) values ('indogo_http', 'url', 'http://prod.indogo.tw/console/');
insert into global_config (group_name, config_name, config_value) values ('indogo_http', 'login_name', 'ythung1');
insert into global_config (group_name, config_name, config_value) values ('indogo_http', 'login_pass', 'f122e12039b79a9b6b41d6fb06cd3bc9dcaeb992017f64f9ee6692237184ac03');
insert into global_config (group_name, config_name, config_value) values ('indogo_http', 'connection_timeout', '120000');

insert into history_table (table_id, table_name) values (1, 'member');
insert into history_table (table_id, table_name) values (2, 'member_recipient');
insert into history_table (table_id, table_name) values (3, 'bank_code_list');
insert into history_table (table_id, table_name) values (4, 'money_transfer');
insert into history_table (table_id, table_name) values (5, 'member_app_register');

insert into table_page (table_page_id, class_name, table_name) values (1, 'com.indogo.relay.bookkeeping.IdrBookkeeping', 'IDR Book');
insert into table_page (table_page_id, class_name, table_name) values (2, 'com.indogo.relay.bookkeeping.UsdBookkeeping', 'USD Book');
insert into table_page (table_page_id, class_name, table_name) values (3, 'com.indogo.relay.config.AccountConfiguration', 'User Account');
insert into table_page (table_page_id, class_name, table_name) values (4, 'com.indogo.relay.member.BankCodeConfiguration', 'Bank List');
insert into table_page (table_page_id, class_name, table_name) values (5, 'com.indogo.relay.member.Exchange', 'Exchange');
insert into table_page (table_page_id, class_name, table_name) values (6, 'com.indogo.relay.member.ExchangeItem', 'Exchange Details');
insert into table_page (table_page_id, class_name, table_name) values (7, 'com.indogo.relay.member.KursConfiguration', 'Kurs History');
insert into table_page (table_page_id, class_name, table_name) values (8, 'com.indogo.relay.member.MemberAppRegister', 'App Register');
insert into table_page (table_page_id, class_name, table_name) values (9, 'com.indogo.relay.member.MemberPointSchedule', 'Point Schedule');
insert into table_page (table_page_id, class_name, table_name) values (10, 'com.indogo.relay.member.MemberPoint', 'Member Point');
insert into table_page (table_page_id, class_name, table_name) values (11, 'com.indogo.relay.member.MemberPointHistory', 'Point History');
insert into table_page (table_page_id, class_name, table_name) values (12, 'com.indogo.relay.member.MemberConfiguration', 'Member');
insert into table_page (table_page_id, class_name, table_name) values (13, 'com.indogo.relay.member.MoneyTransferBNI', 'BNI Transfer');
insert into table_page (table_page_id, class_name, table_name) values (14, 'com.indogo.relay.member.RecipientView', 'Recipient');
insert into table_page (table_page_id, class_name, table_name) values (15, 'com.indogo.relay.member.MoneyTransferBRIAuto', 'BRI Auto Transfer');
insert into table_page (table_page_id, class_name, table_name) values (16, 'com.indogo.relay.member.MoneyTransferBRI', 'BRI Transfer');
insert into table_page (table_page_id, class_name, table_name) values (17, 'com.indogo.relay.member.MemberView', 'Member View');
insert into table_page (table_page_id, class_name, table_name) values (18, 'com.indogo.relay.member.MoneyTransferReport', 'Summary Report');
insert into table_page (table_page_id, class_name, table_name) values (19, 'com.indogo.relay.member.MoneyTransferSummary', 'Bank Report');
insert into table_page (table_page_id, class_name, table_name) values (20, 'com.indogo.relay.member.MoneyTransfer', 'Remit Invoice');
insert into table_page (table_page_id, class_name, table_name) values (21, 'com.indogo.relay.onlineshopping.Item', 'Product');
insert into table_page (table_page_id, class_name, table_name) values (22, 'com.indogo.relay.onlineshopping.Vendor', 'Supplier');
insert into table_page (table_page_id, class_name, table_name) values (23, 'com.indogo.relay.onlineshopping.IncomingOrder', 'Incoming Order');
insert into table_page (table_page_id, class_name, table_name) values (24, 'com.indogo.relay.onlineshopping.IncomingOrderItem', 'Incoming Order Item');
insert into table_page (table_page_id, class_name, table_name) values (25, 'com.indogo.relay.onlineshopping.InventoryAdjust', 'Inventory Adjust');
insert into table_page (table_page_id, class_name, table_name) values (26, 'com.indogo.relay.onlineshopping.Inventory', 'Inventory');
insert into table_page (table_page_id, class_name, table_name) values (27, 'com.indogo.relay.onlineshopping.InventoryHistory', 'Inventory History');
insert into table_page (table_page_id, class_name, table_name) values (28, 'com.indogo.relay.onlineshopping.ItemCategory', 'Product Category');
insert into table_page (table_page_id, class_name, table_name) values (29, 'com.indogo.relay.onlineshopping.ItemColor', 'Product Color');
insert into table_page (table_page_id, class_name, table_name) values (30, 'com.indogo.relay.onlineshopping.ItemSize', 'Product Size');
insert into table_page (table_page_id, class_name, table_name) values (31, 'com.indogo.relay.onlineshopping.SalesCheckout', 'Checkout');
insert into table_page (table_page_id, class_name, table_name) values (32, 'com.indogo.relay.onlineshopping.SalesExportHeimao', 'Export Heimao');
insert into table_page (table_page_id, class_name, table_name) values (33, 'com.indogo.relay.onlineshopping.SalesScrap', 'Sales Scrap');
insert into table_page (table_page_id, class_name, table_name) values (34, 'com.indogo.relay.onlineshopping.Sales', 'Sales View');
insert into table_page (table_page_id, class_name, table_name) values (35, 'com.indogo.relay.onlineshopping.SalesItem', 'Sales Items');
insert into table_page (table_page_id, class_name, table_name) values (36, 'com.indogo.relay.onlineshopping.Shop', 'Outlet');
insert into table_page (table_page_id, class_name, table_name) values (37, 'com.indogo.relay.onlineshopping.StockTransfer', 'Stock Transfer');
insert into table_page (table_page_id, class_name, table_name) values (38, 'com.indogo.relay.onlineshopping.SalesReport', 'Sales Report');
insert into table_page (table_page_id, class_name, table_name) values (39, 'com.indogo.relay.onlineshopping.ItemViewForIncomingOrder', 'Product View For Incoming Order');
insert into table_page (table_page_id, class_name, table_name) values (40, 'com.indogo.relay.report.OnlineShoppingManagerReport', 'Online Shop Manager Report');
insert into table_page (table_page_id, class_name, table_name) values (41, 'com.indogo.relay.onlineshopping.IncomingOrderReport', 'Incoming Order Report');
insert into table_page (table_page_id, class_name, table_name) values (42, 'com.indogo.relay.member.MemberRecipientBatchVerification', 'Member Recipient Batch Verify');
insert into table_page (table_page_id, class_name, table_name) values (43, 'com.indogo.relay.onlineshopping.SalesReportDetail', 'Sales Report Detail');
insert into table_page (table_page_id, class_name, table_name) values (44, 'com.indogo.relay.onlineshopping.IncomingOrderReportDetail', 'Incoming Order Report Detail');
insert into table_page (table_page_id, class_name, table_name) values (45, 'com.indogo.relay.onlineshopping.SalesReturned', 'Sales Returned');
insert into table_page (table_page_id, class_name, table_name) values (46, 'com.indogo.relay.onlineshopping.InventoryReport', 'Inventory Report');
insert into table_page (table_page_id, class_name, table_name) values (47, 'com.indogo.relay.member.MoneyTransferBOT', 'BOT Transfer');
insert into table_page (table_page_id, class_name, table_name) values (48, 'com.indogo.relay.member.MemberAddress', 'Member Address');
insert into table_page (table_page_id, class_name, table_name) values (49, 'com.indogo.relay.onlineshopping.ItemGift', 'Item Gift');
insert into table_page (table_page_id, class_name, table_name) values (50, 'com.indogo.relay.member.MemberPointView', 'Member Point History View');
insert into table_page (table_page_id, class_name, table_name) values (51, 'com.indogo.relay.member.MemberWalletHistory', 'Member Wallet History');

INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.bank_code_config', 'class', 'com.indogo.relay.member.BankCodeConfiguration');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.member_config', 'class', 'com.indogo.relay.member.MemberConfiguration');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.member_app_register', 'class', 'com.indogo.relay.member.MemberAppRegister');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.money_transfer', 'class', 'com.indogo.relay.member.MoneyTransfer');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.money_transfer_bri', 'class', 'com.indogo.relay.member.MoneyTransferBRI');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.kurs_config', 'class', 'com.indogo.relay.member.KursConfiguration');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('book.idr', 'class', 'com.indogo.relay.bookkeeping.IdrBookkeeping');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('book.usd', 'class', 'com.indogo.relay.bookkeeping.UsdBookkeeping');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('book.revenue', 'class', 'com.indogo.relay.bookkeeping.RevenueReport');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.money_transfer_report', 'class', 'com.indogo.relay.member.MoneyTransferReport');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.money_transfer_summary', 'class', 'com.indogo.relay.member.MoneyTransferSummary');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.money_transfer_bni', 'class', 'com.indogo.relay.member.MoneyTransferBNI');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.member_point', 'class', 'com.indogo.relay.member.MemberPoint');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.member_point_history', 'class', 'com.indogo.relay.member.MemberPointHistory');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.member_point_schedule', 'class', 'com.indogo.relay.member.MemberPointSchedule');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.member_point_report', 'class', 'com.indogo.relay.member.MemberPointReport');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('report.employee_performance', 'class', 'com.indogo.relay.report.EmployeePerformance');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.item_category', 'class', 'com.indogo.relay.onlineshopping.ItemCategory');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.item_color', 'class', 'com.indogo.relay.onlineshopping.ItemColor');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.item_size', 'class', 'com.indogo.relay.onlineshopping.ItemSize');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.item', 'class', 'com.indogo.relay.onlineshopping.Item');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.shop', 'class', 'com.indogo.relay.onlineshopping.Shop');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.vendor', 'class', 'com.indogo.relay.onlineshopping.Vendor');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.inventory', 'class', 'com.indogo.relay.onlineshopping.Inventory');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.stock_transfer', 'class', 'com.indogo.relay.onlineshopping.StockTransfer');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.stock_transfer_create', 'class', 'com.indogo.relay.onlineshopping.StockTransferCreate');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.incoming_order_create', 'class', 'com.indogo.relay.onlineshopping.IncomingOrderCreate');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.incoming_order', 'class', 'com.indogo.relay.onlineshopping.IncomingOrder');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.sales_create', 'class', 'com.indogo.relay.onlineshopping.SalesCreate');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.sales_checkout', 'class', 'com.indogo.relay.onlineshopping.SalesCheckout');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.sales', 'class', 'com.indogo.relay.onlineshopping.Sales');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.sales_export_heimao', 'class', 'com.indogo.relay.onlineshopping.SalesExportHeimao');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.sales_item_receipt', 'class', 'com.indogo.relay.onlineshopping.SalesItemReceipt');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.sales_scrap', 'class', 'com.indogo.relay.onlineshopping.SalesScrap');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.sales_import_heimao', 'class', 'com.indogo.relay.onlineshopping.SalesImportHeimao');
insert into relay_config (relay_id, relay_type, class_name) values ('member.exchange', 'class', 'com.indogo.relay.member.Exchange');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.inventory_adjust', 'class', 'com.indogo.relay.onlineshopping.InventoryAdjust');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.sales_report', 'class', 'com.indogo.relay.onlineshopping.SalesReport');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.item_view_for_incoming_order', 'class', 'com.indogo.relay.onlineshopping.ItemViewForIncomingOrder');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.online_shopping_manager_report', 'class', 'com.indogo.relay.report.OnlineShoppingManagerReport');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.incoming_order_report', 'class', 'com.indogo.relay.onlineshopping.IncomingOrderReport');
insert into relay_config (relay_id, relay_type, class_name) values ('member.recipient_batch_verification', 'class', 'com.indogo.relay.member.MemberRecipientBatchVerification');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.sales_report_detail', 'class', 'com.indogo.relay.onlineshopping.SalesReportDetail');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.incoming_order_report_detail', 'class', 'com.indogo.relay.onlineshopping.IncomingOrderReportDetail');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.sales_returned', 'class', 'com.indogo.relay.onlineshopping.SalesReturned');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.inventory_report', 'class', 'com.indogo.relay.onlineshopping.InventoryReport');
insert into relay_config (relay_id, relay_type, class_name) values ('member.money_transfer_bot', 'class', 'com.indogo.relay.member.MoneyTransferBOT');
insert into relay_config (relay_id, relay_type, class_name) values ('member.member_address', 'class', 'com.indogo.relay.member.MemberAddress');
insert into relay_config (relay_id, relay_type, class_name) values ('shop.item_gift', 'class', 'com.indogo.relay.onlineshopping.ItemGift');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.money_transfer_config', 'class', 'com.indogo.relay.member.MoneyTransferConfig');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.recipient_view', 'class', 'com.indogo.relay.member.RecipientView');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.member_point_view', 'class', 'com.indogo.relay.member.MemberPointView');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('member.member_wallet_history', 'class', 'com.indogo.relay.member.MemberWalletHistory');

INSERT INTO role_name_list (role_id, role_name, role_desc) values (1, 'ADMIN', 'IndoGO admin, has access to all functions');
INSERT INTO role_name_list (role_id, role_name, role_desc) values (2, 'SUPERVISOR', 'IndoGO SUPERVISOR');
INSERT INTO role_name_list (role_id, role_name, role_desc) values (3, 'MANAGER', 'IndoGO MANAGER');
INSERT INTO role_name_list (role_id, role_name, role_desc) values (4, 'CUSTOMER SERVICE', 'IndoGO CUSTOMER SERVICE');
INSERT INTO role_name_list (role_id, role_name, role_desc) values (5, 'FINANCE', 'IndoGO FINANCE');

INSERT INTO menu_path (path_row_id, path_name, display_seq) VALUES (1, 'Member', 1);
INSERT INTO menu_path (path_row_id, path_name, display_seq) VALUES (2, 'Book Keeping', 3);
INSERT INTO menu_path (path_row_id, path_name, display_seq) VALUES (3, 'Invoice', 2);
INSERT INTO menu_path (path_row_id, path_name, display_seq) VALUES (4, 'Report', 10);
INSERT INTO menu_path (path_row_id, path_name, display_seq) VALUES (5, 'Online Shopping', 5);
INSERT INTO menu_path (path_row_id, path_name, display_seq) VALUES (6, 'Product', 6);
INSERT INTO menu_path (path_row_id, path_name, display_seq) VALUES (7, 'Inventory', 7);
INSERT INTO menu_path (path_row_id, path_name, display_seq) VALUES (8, 'Stock Order', 8);
INSERT INTO menu_path (path_row_id, path_name, display_seq) VALUES (9, 'Sales', 9);

INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (2, 'Bank Code Configuration', 'html/member/bank_code.html', 1, 0);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (3, 'Member Configuration', 'html/member/member.html', 1, 1);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (4, 'View Invoice', 'html/member/money_transfer.html', 3, 2);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (5, 'Kurs', 'html/member/kurs_value.html', 1, 3);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (6, 'Create Invoice', 'html/member/money_transfer_create.html', 3, 1);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (7, 'IDR Book Keeping', 'html/bookkeeping/idr_bookkeeping.html', 2, 1);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (8, 'USD Book Keeping', 'html/bookkeeping/usd_bookkeeping.html', 2, 2);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (9, 'Export/Import Invoice for BRI', 'html/member/money_transfer_bri.html', 3, 3);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (10, 'Revenue Report', 'html/bookkeeping/revenue_report.html', 2, 3);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (11, 'App Register', 'html/member/member_app_register.html', 1, 4);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (12, 'Invoice Summary Report', 'html/member/money_transfer_report.html', 3, 4);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (13, 'Invoice Bank Report', 'html/member/money_transfer_summary.html', 3, 5);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (14, 'BNI Auto Transfer', 'html/member/money_transfer_bni.html', 3, 6);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (15, 'Member Point', 'html/member/member_point.html', 1, 5);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (16, 'Member Point Schedule', 'html/member/member_point_schedule.html', 1, 6);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (17, 'Member Point Report', 'html/member/member_point_report.html', 1, 7);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (18, 'BRI Auto Transfer', 'html/member/money_transfer_bri_auto.html', 3, 7);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (19, 'Employee Performance', 'html/report/employee_performance.html', 4, 1);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (20, 'Category', 'html/online_shopping/item_category.html', 6, 2);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (21, 'Color', 'html/online_shopping/item_color.html', 6, 3);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (22, 'Size', 'html/online_shopping/item_size.html', 6, 4);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (23, 'View', 'html/online_shopping/item.html', 6, 1);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (24, 'Outlet', 'html/online_shopping/shop.html', 7, 2);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (25, 'Supplier', 'html/online_shopping/vendor.html', 7, 3);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (26, 'View', 'html/online_shopping/inventory.html', 7, 1);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (27, 'Stock Transfer Request', 'html/online_shopping/stock_transfer_create.html', 8, 4);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (28, 'Stock Transfer View', 'html/online_shopping/stock_transfer.html', 8, 3);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (29, 'Create Incoming Order', 'html/online_shopping/incoming_order_create.html', 8, 2);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (30, 'View Incoming Order', 'html/online_shopping/incoming_order.html', 8, 1);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (31, 'Create Invoice', 'html/online_shopping/sales_create.html', 9, 2);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (32, 'Check Out', 'html/online_shopping/sales_checkout.html', 9, 5);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (33, 'View Invoice', 'html/online_shopping/sales.html', 9, 1);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (34, 'Export Heimao', 'html/online_shopping/sales_export_heimao.html', 9, 6);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (35, 'Item Receipt', 'html/online_shopping/sales_item_receipt.html', 9, 3);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (36, 'Sales Scrap', 'html/online_shopping/sales_scrap.html', 9, 4);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (37, 'Import Heimao', 'html/online_shopping/sales_import_heimao.html', 9, 7);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (38, 'Exchange', 'html/member/exchange.html', 3, 18);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (39, 'Inventory Adjust', 'html/online_shopping/inventory_adjust.html', 7, 4);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (40, 'Report', 'html/online_shopping/sales_report.html', 9, 8);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (41, 'Product View For Incoming Order', 'html/online_shopping/item_view_for_incoming_order.html', 6, 5);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (42, 'Online Shop Manager Report', 'html/report/online_shopping_manager_report.html', 4, 2);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (43, 'Incoming Order Report', 'html/online_shopping/incoming_order_report.html', 8, 5);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (44, 'Recipient Batch Verify', 'html/member/member_recipient_batch_verification.html', 1, 10);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (45, 'Report (Detail)', 'html/online_shopping/sales_report_detail.html', 9, 9);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (46, 'Incoming Order Report (Detail)', 'html/online_shopping/incoming_order_report_detail.html', 8, 6);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (47, 'Sales Returned', 'html/online_shopping/sales_returned.html', 9, 9);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (48, 'Inventory Report', 'html/online_shopping/inventory_report.html', 7, 5);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (49, 'Export/Import Invoice for BOT', 'html/member/money_transfer_bot.html', 3, 8);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (50, 'Member Address', 'html/member/member_address.html', 1, 11);
insert into menu (menu_row_id, title, url, path_row_id, display_seq) values (51, 'Gift', 'html/online_shopping/item_gift.html', 6, 6);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (52, 'Money Transfer Configuration', 'html/member/money_transfer_config.html', 3, 9);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (53, 'Recipient View', 'html/member/recipient_view.html', 1, 12);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (54, 'Member Point History View', 'html/member/member_point_view.html', 1, 13);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (55, 'Member Wallet History', 'html/member/member_wallet_history.html', 1, 14);

INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 2);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 3);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 4);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 5);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 6);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 7);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 8);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 9);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 10);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 11);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 12);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 13);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 0);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 1);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 992);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 14);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 15);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 16);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 17);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 18);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 19);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 20);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 21);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 22);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 23);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 24);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 25);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 26);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 27);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 28);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 29);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 30);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 31);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 32);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 33);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 34);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 35);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 36);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 37);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 38);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 39);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 40);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 41);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 42);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 43);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 44);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 45);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 46);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 47);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 48);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 49);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 50);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 51);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 52);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 53);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 54);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (1, 55);

INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 2);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 3);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 4);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 5);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 6);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 7);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 8);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 9);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 10);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 11);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 12);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 13);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 15);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 17);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (2, 38);

INSERT INTO role_menu (role_id, menu_row_id) VALUES (3, 2);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (3, 3);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (3, 4);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (3, 5);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (3, 6);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (3, 9);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (3, 11);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (3, 12);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (3, 13);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (3, 15);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (3, 17);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (3, 38);

INSERT INTO role_menu (role_id, menu_row_id) VALUES (4, 2);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (4, 3);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (4, 4);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (4, 5);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (4, 6);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (4, 9);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (4, 11);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (4, 12);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (4, 15);

INSERT INTO role_menu (role_id, menu_row_id) VALUES (5, 2);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (5, 3);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (5, 4);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (5, 5);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (5, 12);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (5, 13);

insert into chart (chart_id, chart_name) values (1, 'Create Invoice');
insert into chart (chart_id, chart_name) values (2, 'Create Member');
insert into chart (chart_id, chart_name) values (3, 'New Member Verification');
insert into chart (chart_id, chart_name) values (4, 'Old Member Verification');
insert into chart (chart_id, chart_name) values (5, 'Upload Signature');
insert into chart (chart_id, chart_name) values (6, 'Overall');
