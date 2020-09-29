-- create database indogo collate = big5_chinese_ci
-- CREATE USER 'indogo'@'localhost' IDENTIFIED BY 'indogo#go#';
-- GRANT ALL ON indogo.* TO 'indogo'@'localhost';

CREATE TABLE role_name_list (
    role_id INTEGER,
	role_name VARCHAR(32),
	role_desc varchar(2000)
)
ENGINE=InnoDB;

ALTER TABLE role_name_list ADD (
	CONSTRAINT role_name_list_pk
	PRIMARY KEY (role_id)
);

CREATE TABLE relay_type_list (
    relay_type VARCHAR(32)
)
ENGINE=InnoDB;

ALTER TABLE relay_type_list ADD (
    CONSTRAINT relay_type_list_pk
    PRIMARY KEY (relay_type)
);

CREATE TABLE relay_config (
    relay_id VARCHAR(50),
    relay_type VARCHAR(32) DEFAULT 'http' NOT NULL,
    url VARCHAR(255),
    login_name VARCHAR(100),
    login_pass VARCHAR(100),
    class_name VARCHAR(100)
)
ENGINE=InnoDB;

ALTER TABLE relay_config ADD (
    CONSTRAINT relay_config_pk
    PRIMARY KEY (relay_id)
);

ALTER TABLE relay_config ADD (
    CONSTRAINT relay_config_fk1
    FOREIGN KEY (relay_type)
    REFERENCES relay_type_list (relay_type)
);

CREATE TABLE menu_path (
	path_row_id INTEGER,
	path_name VARCHAR(32) NOT NULL,
	display_seq INTEGER
)
ENGINE=InnoDB;

ALTER TABLE menu_path ADD (
	CONSTRAINT menu_path_pk
	PRIMARY KEY (path_row_id)
);

CREATE TABLE menu_type (
    menu_type_id VARCHAR(32)
)
ENGINE=InnoDB;

ALTER TABLE menu_type ADD (
    CONSTRAINT menu_type_pk
    PRIMARY KEY (menu_type_id)
);

CREATE TABLE db_config_type (
	db_type VARCHAR(32)
)
ENGINE=InnoDB;

ALTER TABLE db_config_type ADD (
	CONSTRAINT db_config_type_pk
	PRIMARY KEY (db_type)
);

CREATE TABLE db_config (
    db_name VARCHAR(32),
    db_ip VARCHAR(32) NOT NULL,
    db_port INTEGER DEFAULT 1521 NOT NULL,
    db_instance VARCHAR(32) NOT NULL,
    db_acc VARCHAR(32) NOT NULL,
    db_pass VARCHAR(32) NOT NULL,
    db_type VARCHAR(32) DEFAULT 'MYSQL' NOT NULL
)
ENGINE=InnoDB;

ALTER TABLE db_config ADD (
    CONSTRAINT db_config_pk
    PRIMARY KEY (db_name)
);

ALTER TABLE db_config ADD (
	CONSTRAINT db_config_fk1
	FOREIGN KEY (db_type)
	REFERENCES db_config_type (db_type)
);

CREATE TABLE auto_page_type (
    page_type VARCHAR(32)
)
ENGINE=InnoDB;

ALTER TABLE auto_page_type ADD (
    CONSTRAINT auto_page_type_pk
    PRIMARY KEY (page_type)
);

CREATE TABLE auto_page (
    page_id VARCHAR(32),
    db_name VARCHAR(32),
    page_type VARCHAR(32) NOT NULL,
    cmd_text LONGTEXT NOT NULL
)
ENGINE=InnoDB;

ALTER TABLE auto_page ADD (
    CONSTRAINT auto_page_pk
    PRIMARY KEY (page_id)
    
);

ALTER TABLE auto_page ADD (
    CONSTRAINT auto_page_fk1
    FOREIGN KEY (db_name)
    REFERENCES db_config (db_name)
);

ALTER TABLE auto_page ADD (
    CONSTRAINT auto_page_fk2
    FOREIGN KEY (page_type)
    REFERENCES auto_page_type (page_type)
);

CREATE TABLE menu (
	menu_row_id INTEGER,
	title VARCHAR(250) NOT NULL,
	url VARCHAR(255),
	path_row_id INTEGER NOT NULL,
	display_seq INTEGER,
	menu_type_id VARCHAR(32) DEFAULT 'html' NOT NULL,
	page_id VARCHAR(32)
)
ENGINE=InnoDB;

ALTER TABLE menu ADD (
	CONSTRAINT menu_pk
	PRIMARY KEY (menu_row_id)
	
);

ALTER TABLE menu ADD (
    CONSTRAINT menu_fk1
    FOREIGN KEY (path_row_id)
    REFERENCES menu_path (path_row_id)
);

ALTER TABLE menu ADD (
    CONSTRAINT menu_fk2
    FOREIGN KEY (menu_type_id)
    REFERENCES menu_type (menu_type_id)
);

ALTER TABLE menu ADD (
    CONSTRAINT menu_fk3
    FOREIGN KEY (page_id)
    REFERENCES auto_page (page_id)
);

CREATE TABLE role_menu (
	role_id INTEGER,
	menu_row_id INTEGER
)
ENGINE=InnoDB;

ALTER TABLE role_menu ADD (
	CONSTRAINT role_menu_pk
	PRIMARY KEY (role_id, menu_row_id)
);

ALTER TABLE role_menu ADD (
    CONSTRAINT role_menu_fk1
    FOREIGN KEY (menu_row_id)
    REFERENCES menu (menu_row_id)
);

ALTER TABLE role_menu ADD (
    CONSTRAINT role_menu_fk2
    FOREIGN KEY (role_id)
    REFERENCES role_name_list (role_id)
);

CREATE TABLE session_list (
	session_id VARCHAR(32),
	last_active_time DATETIME NOT NULL,
	server_ip VARCHAR(32),
	server_name VARCHAR(32),
	server_port INTEGER
)
ENGINE=InnoDB;

ALTER TABLE session_list ADD (
	CONSTRAINT session_list_pk
	PRIMARY KEY (session_id)
);

CREATE TABLE session_param (
	session_id VARCHAR(32),
	param_name VARCHAR(200),
	param_value LONGTEXT
)
ENGINE=InnoDB;

ALTER TABLE session_param ADD (
	CONSTRAINT session_param_pk
	PRIMARY KEY (session_id, param_name)
);

ALTER TABLE session_param ADD (
    CONSTRAINT session_param_fk1
    FOREIGN KEY (session_id)
    REFERENCES session_list (session_id)
    ON DELETE CASCADE
);

CREATE TABLE user_list (
	user_row_id INTEGER,
	user_name VARCHAR(100) NOT NULL,
	password VARCHAR(2000),
	disabled CHAR(1) DEFAULT 'N' NOT NULL,
	session_id VARCHAR(32),
	retry_count INT DEFAULT 0,
	email_address VARCHAR(250),
	alias_id VARCHAR(100) NOT NULL,
	color_id varchar(10)
)
ENGINE=InnoDB;

ALTER TABLE user_list ADD (
	CONSTRAINT user_list_pk
	PRIMARY KEY (user_row_id)
);

CREATE UNIQUE INDEX user_list_u1 ON user_list (user_name);

CREATE UNIQUE INDEX user_list_u2 ON user_list (session_id);

ALTER TABLE user_list ADD (
    CONSTRAINT user_list_fk1
    FOREIGN KEY (session_id)
    REFERENCES session_list (session_id)
    ON DELETE SET NULL
);

CREATE TABLE user_role (
    role_id INTEGER,
    user_row_id INTEGER
)
ENGINE=InnoDB;

ALTER TABLE user_role ADD (
    CONSTRAINT user_role_pk
    PRIMARY KEY (role_id, user_row_id)    
);

ALTER TABLE user_role ADD (
    CONSTRAINT user_role_fk1
    FOREIGN KEY (role_id)
    REFERENCES role_name_list (role_id)
);

ALTER TABLE user_role ADD (
    CONSTRAINT user_role_fk2
    FOREIGN KEY (user_row_id)
    REFERENCES user_list (user_row_id)
);

CREATE TABLE sp_param_direction (
	param_direction VARCHAR(6)
)
ENGINE=InnoDB;

ALTER TABLE sp_param_direction ADD (
	CONSTRAINT sp_param_direction_pk
	PRIMARY KEY (param_direction)
);

CREATE TABLE sp_param_type (
	param_type VARCHAR(32)
)
ENGINE=InnoDB;

ALTER TABLE sp_param_type ADD (
	CONSTRAINT sp_param_type_pk
	PRIMARY KEY (param_type)
);

CREATE TABLE sp_param (
	page_id VARCHAR(32),
	param_name VARCHAR(32),
	param_type VARCHAR(32) NOT NULL,
	param_direction VARCHAR(6) NOT NULL,
	param_seq INTEGER
)
ENGINE=InnoDB;

ALTER TABLE sp_param ADD (
	CONSTRAINT sp_param_pk
	PRIMARY KEY (page_id, param_name)
);

ALTER TABLE sp_param ADD (
	CONSTRAINT sp_param_fk1
	FOREIGN KEY (page_id)
	REFERENCES auto_page (page_id)
);

ALTER TABLE sp_param ADD (
	CONSTRAINT sp_param_fk2
	FOREIGN KEY (param_direction)
	REFERENCES sp_param_direction (param_direction)
);

ALTER TABLE sp_param ADD (
	CONSTRAINT sp_param_fk3
	FOREIGN KEY (param_type)
	REFERENCES sp_param_type (param_type)
);

CREATE TABLE sql_input_type (
    input_type VARCHAR(32)
)
ENGINE=InnoDB;

ALTER TABLE sql_input_type ADD (
    CONSTRAINT sql_input_type_pk
    PRIMARY KEY (input_type)
);

CREATE TABLE sql_input_op (
    input_op VARCHAR(32)
)
ENGINE=InnoDB;

ALTER TABLE sql_input_op ADD (
    CONSTRAINT sql_input_op_pk
    PRIMARY KEY (input_op)
);

CREATE TABLE sql_input_logic (
    input_logic VARCHAR(32)
)
ENGINE=InnoDB;

ALTER TABLE sql_input_logic ADD (
    CONSTRAINT sql_input_logic_pk
    PRIMARY KEY (input_logic)
);

CREATE TABLE sql_input_optional (
    input_optional VARCHAR(1)
)
ENGINE=InnoDB;

ALTER TABLE sql_input_optional ADD (
    CONSTRAINT sql_input_optional_pk
    PRIMARY KEY (input_optional)
);

CREATE TABLE sql_input_custom_flag (
	input_custom_flag INTEGER,
	description VARCHAR(100)
)
ENGINE=InnoDB;

ALTER TABLE sql_input_custom_flag ADD (
	CONSTRAINT sql_input_custom_flag_pk
	PRIMARY KEY (input_custom_flag)
);

CREATE TABLE sql_input (
    page_id VARCHAR(32),
    input_name VARCHAR(32),
    input_id VARCHAR(250) NOT NULL,
    input_type VARCHAR(32) NOT NULL,
    input_seq INTEGER,
    input_op VARCHAR(32) NOT NULL,
    input_logic VARCHAR(32),
    input_optional VARCHAR(1) DEFAULT 'Y' NOT NULL,
    input_list_source VARCHAR(32),
    input_default_value VARCHAR(32),
    input_custom_flag INTEGER
)
ENGINE=InnoDB;

ALTER TABLE sql_input ADD (
    CONSTRAINT sql_input_pk
    PRIMARY KEY (page_id, input_name)
);

ALTER TABLE sql_input ADD (
    CONSTRAINT sql_input_fk1
    FOREIGN KEY (page_id)
    REFERENCES auto_page (page_id)
);

ALTER TABLE sql_input ADD (
    CONSTRAINT sql_input_fk2
    FOREIGN KEY (input_type)
    REFERENCES sql_input_type (input_type)
);

ALTER TABLE sql_input ADD (
    CONSTRAINT sql_input_fk3
    FOREIGN KEY (input_op)
    REFERENCES sql_input_op (input_op)
);

ALTER TABLE sql_input ADD (
    CONSTRAINT sql_input_fk4
    FOREIGN KEY (input_logic)
    REFERENCES sql_input_logic (input_logic)
);

ALTER TABLE sql_input ADD (
    CONSTRAINT sql_input_fk5
    FOREIGN KEY (input_optional)
    REFERENCES sql_input_optional (input_optional)
);

ALTER TABLE sql_input ADD (
	CONSTRAINT sql_input_fk6
	FOREIGN KEY (input_list_source)
	REFERENCES auto_page (page_id)
);

ALTER TABLE sql_input ADD (
	CONSTRAINT sql_input_fk7
	FOREIGN KEY (input_default_value)
	REFERENCES auto_page (page_id)
);

ALTER TABLE sql_input ADD (
	CONSTRAINT sql_input_fk8
	FOREIGN KEY (input_custom_flag)
	REFERENCES sql_input_custom_flag (input_custom_flag)
);

CREATE TABLE sql_output_type (
    output_type VARCHAR(32)
)
ENGINE=InnoDB;

ALTER TABLE sql_output_type ADD (
    CONSTRAINT sql_output_type_pk
    PRIMARY KEY (output_type)
);

CREATE TABLE sql_output (
    page_id VARCHAR(32),
    output_name VARCHAR(32),
    output_id VARCHAR(32) NOT NULL,
    output_seq INTEGER,
    output_type VARCHAR(32) NOT NULL
)
ENGINE=InnoDB;

ALTER TABLE sql_output ADD (
    CONSTRAINT sql_output_pk
    PRIMARY KEY (page_id, output_name)
);

ALTER TABLE sql_output ADD (
    CONSTRAINT sql_output_fk1
    FOREIGN KEY (page_id)
    REFERENCES auto_page (page_id)
);

ALTER TABLE sql_output ADD (
    CONSTRAINT sql_output_fk2
    FOREIGN KEY (output_type)
    REFERENCES sql_output_type (output_type)
);

CREATE TABLE sql_temp_table (
	session_id VARCHAR(32),
	page_id VARCHAR(32),
	long1 BIGINT,
	long2 bigint,
	long3 bigint,
	item1 varchar(200),
	item2 varchar(200),
	item3 varchar(200),
	item4 varchar(200),
	item5 varchar(200),
	item6 varchar(200),
	item7 varchar(200),
	item8 varchar(200),
	item9 varchar(200),
	item10 varchar(200),
	num1 integer,
	num2 integer,
	num3 integer,
	num4 integer,
	date1 datetime
)
ENGINE=InnoDB;

CREATE INDEX sql_temp_table_i1 ON sql_temp_table (session_id, page_id);

ALTER TABLE sql_temp_table ADD (
	CONSTRAINT sql_temp_table_fk1
	FOREIGN KEY (session_id)
	REFERENCES session_list (session_id)
	ON DELETE CASCADE
);

CREATE TABLE debug_log (
    log_seq INTEGER AUTO_INCREMENT,
    log_time DATETIME,
    log_verbose INTEGER,
    log_value LONGTEXT,
    user_row_id INTEGER,
    user_name VARCHAR(100),
    session_id VARCHAR(50),
    relay_id VARCHAR(32),
    relay_url VARCHAR(255),
    function_name VARCHAR(32),
    class_name VARCHAR(100),
    PRIMARY KEY (log_seq)
)
ENGINE=InnoDB;

CREATE INDEX debug_log_i1 ON debug_log (log_time, user_row_id, session_id);

CREATE TABLE global_config (
    group_name VARCHAR(32),
    config_name VARCHAR(32),
    config_value LONGTEXT
)
ENGINE=InnoDB;

ALTER TABLE global_config ADD (
    CONSTRAINT global_config_PK
    PRIMARY KEY (group_name, config_name)
);

CREATE TABLE history_table (
	table_id TINYINT,
	table_name VARCHAR(100)
)
ENGINE=InnoDB;

ALTER TABLE history_table ADD (
	CONSTRAINT history_table_pk
	PRIMARY KEY (table_id)
);

CREATE TABLE history_action (
	action_id TINYINT,
	action_name VARCHAR(10)
)
ENGINE=InnoDB;

ALTER TABLE history_action ADD (
	CONSTRAINT history_action_pk
	PRIMARY KEY (action_id)
);

CREATE TABLE history (
	log_id BIGINT,
	table_id TINYINT,
	row_id BIGINT,
	action_time DATETIME,
	action_id TINYINT,
	action_desc VARCHAR(300),
	action_user varchar(100)
)
ENGINE=InnoDB;

ALTER TABLE history ADD (
	CONSTRAINT history_pk
	PRIMARY KEY (log_id)
);

ALTER TABLE history ADD (
	CONSTRAINT history_fk1
	FOREIGN KEY (table_id)
	REFERENCES history_table (table_id)
);

CREATE INDEX history_i1 ON history (table_id, action_time);

CREATE TABLE history_data (
	log_id BIGINT,
	data_seq SMALLINT,
	attr_name VARCHAR(50),
	old_attr_value VARCHAR(2000),
	new_attr_value VARCHAR(2000)
)
ENGINE=InnoDB;

ALTER TABLE history_data ADD (
	CONSTRAINT history_data_pk
	PRIMARY KEY (log_id, data_seq)
);

create table chart (
	chart_id int,
	chart_name varchar(50),
	chart_seq int
)
ENGINE=InnoDB;

alter table chart add (
	constraint chart_pk
	primary key (chart_id)
);

create table chart_series (
	chart_id int,
	series_id int,
	series_name varchar(50)
)
ENGINE=InnoDB;

alter table chart_series add (
	constraint chart_series_pk
	primary key (chart_id, series_id)
);

create table chart_daily (
	chart_id int,
	series_id int,
	point_id datetime,
	point_value double
)
ENGINE=InnoDB;

alter table chart_daily add (
	constraint chart_daily_pk
	primary key (chart_id, series_id, point_id)
);

create table chart_weekly (
	chart_id int,
	series_id int,
	year_id smallint,
	week_id tinyint,
	point_value double
)
ENGINE=InnoDB;

alter table chart_weekly add (
	constraint chart_weekly_pk
	primary key (chart_id, series_id, year_id, week_id)
);

create table chart_monthly (
	chart_id int,
	series_id int,
	year_id smallint,
	month_id tinyint,
	point_value double
)
ENGINE=InnoDB;

alter table chart_monthly add (
	constraint chart_monthly_pk
	primary key (chart_id, series_id, year_id, month_id)
);

create table table_page (
	table_page_id int,
	class_name varchar(100),
	table_name varchar(100)
)
ENGINE=InnoDB;

alter table table_page add (
	constraint table_page_pk
	primary key (table_page_id)
);

create unique index table_page_u1 on table_page (class_name);

create table role_table_page (
	role_id int,
	table_page_id int,
	allow_export bit(1)
)
ENGINE=InnoDB;

alter table role_table_page add (
	constraint role_table_page_pk
	primary key (role_id, table_page_id)
);

alter table role_table_page add (
	constraint role_table_page_fk1
	foreign key (role_id)
	references role_name_list (role_id)
);

alter table role_table_page add (
	constraint role_table_page_fk2
	foreign key (table_page_id)
	references table_page (table_page_id)
);

-- PREPARED DATA --
INSERT INTO menu_type (menu_type_id) VALUES ('html');
INSERT INTO menu_type (menu_type_id) VALUES ('auto');
INSERT INTO sql_input_type (input_type) VALUES ('text');
INSERT INTO sql_input_type (input_type) VALUES ('date');
INSERT INTO sql_input_type (input_type) VALUES ('datetime');
INSERT INTO sql_input_type (input_type) VALUES ('list');
INSERT INTO sql_input_type (input_type) VALUES ('combobox');
INSERT INTO sql_input_op VALUES ('=');
INSERT INTO sql_input_op VALUES ('<>');
INSERT INTO sql_input_op VALUES ('>');
INSERT INTO sql_input_op VALUES ('>=');
INSERT INTO sql_input_op VALUES ('<');
INSERT INTO sql_input_op VALUES ('<=');
INSERT INTO sql_input_op VALUES ('LIKE');
INSERT INTO sql_input_logic VALUES ('AND');
INSERT INTO sql_input_logic VALUES ('OR');
INSERT INTO sql_input_logic VALUES ('AND (');
INSERT INTO sql_input_logic VALUES ('OR (');
INSERT INTO sql_input_logic VALUES (')');
INSERT INTO sql_input_logic VALUES (') AND');
INSERT INTO sql_input_logic VALUES (') OR');
INSERT INTO sql_input_logic VALUES (') AND (');
INSERT INTO sql_input_logic VALUES (') OR (');
INSERT INTO relay_type_list VALUES ('http');
INSERT INTO relay_type_list VALUES ('class');
INSERT INTO auto_page_type VALUES ('SQL');
INSERT INTO auto_page_type VALUES ('SQL_DS');
INSERT INTO auto_page_type VALUES ('SP');
INSERT INTO auto_page_type VALUES ('SP_DS');
INSERT INTO auto_page_type VALUES ('SQL_RPT');
INSERT INTO sql_output_type VALUES ('text');
INSERT INTO sql_output_type VALUES ('date');
INSERT INTO sql_output_type VALUES ('datetime');
INSERT INTO sql_output_type VALUES ('number');
INSERT INTO sql_input_optional VALUES ('N');
INSERT INTO sql_input_optional VALUES ('T');
INSERT INTO sql_input_optional VALUES ('F');

INSERT INTO sp_param_direction VALUES ('IN');
INSERT INTO sp_param_direction VALUES ('OUT');
INSERT INTO sp_param_type VALUES ('STRING');
INSERT INTO sp_param_type VALUES ('NUMBER');
INSERT INTO sp_param_type VALUES ('DATETIME');
INSERT INTO sp_param_type VALUES ('TABLE');

INSERT INTO role_name_list (role_id, role_name, role_desc) values (0, 'SYSTEM', 'default role with access to account and email configuration');
INSERT INTO user_list (user_row_id, user_name, password, disabled, email_address, alias_id) VALUES (0, 'root', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'N', 'root@localhost', 'root');
INSERT INTO user_role (role_id, user_row_id) VALUES (0, 0);
INSERT INTO menu_path (path_row_id, path_name, display_seq) VALUES (0, 'Configuration', 0);

INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (0, 'Account', 'html/config/config_account.html', 0, 0);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (1, 'Email', 'html/config/email_configuration.html', 0, 1);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (992, 'Global Configuration', 'html/config/global_config.html', 0, 2);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (993, 'Chart Tool', 'html/admin/chart_tool.html', 0, 3);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (994, 'Role Configuration', 'html/admin/role_config.html', 0, 4);
INSERT INTO menu (menu_row_id, title, url, path_row_id, display_seq) VALUES (995, 'TablePage Configuration', 'html/admin/tablepage_config.html', 0, 5);

INSERT INTO role_menu (role_id, menu_row_id) VALUES (0, 0);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (0, 1);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (0, 992);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (0, 993);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (0, 994);
INSERT INTO role_menu (role_id, menu_row_id) VALUES (0, 995);

INSERT INTO global_config (group_name, config_name, config_value) VALUES ('ADMIN', 'MAX_IDLE_SECOND', '0');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('ADMIN', 'SSO_URL', NULL);
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('ADMIN', 'LOG_VERBOSE', '9');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('ADMIN', 'PASSWORD_RETRY_COUNT', '0');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('ADMIN', 'USER_ROW_ID', '2');

INSERT INTO global_config (group_name, config_name, config_value) VALUES ('ADMIN_EMAIL', 'SMTP_PORT', '587');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('ADMIN_EMAIL', 'USER_NAME', 'ythung1@gmail.com');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('ADMIN_EMAIL', 'PASSWORD', NULL);
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('ADMIN_EMAIL', 'WITH_TLS', 'true');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('ADMIN_EMAIL', 'HOST_NAME', 'smtp.gmail.com');
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('ADMIN_EMAIL', 'FROM', 'ythung1@gmail.com');

INSERT INTO global_config (group_name, config_name, config_value) VALUES ('SERVER', 'LOCK', '1');
# prod, expire: 2017-05-12
INSERT INTO global_config (group_name, config_name, config_value) VALUES ('SERVER', 'LICENSE', 'PIUGnM2fZmx-RjsPOpqkdA824_rNiRpWBt0A1uXLNEWXUgS0tulxRujsGe7nUwMfpnR7UX07_zO81a_prnfupw');

INSERT INTO db_config_type VALUES ('ORACLE');
INSERT INTO db_config_type VALUES ('SQLSERVER');
INSERT INTO db_config_type VALUES ('MYSQL');

INSERT INTO sql_input_custom_flag VALUES (1, 'Do not replace char(20) when processing auto page SQL_RPT');

INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('CONFIG_ACCOUNT', 'class', 'com.indogo.relay.config.AccountConfiguration');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('CONFIG_EMAIL', 'class', 'com.indogo.relay.config.EmailConfiguration');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('admin.global_config', 'class', 'com.lionpig.webui.admin.GlobalConfig');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('admin.chart_tool', 'class', 'com.lionpig.webui.admin.ChartTool');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('admin.role_config', 'class', 'com.lionpig.webui.admin.RoleConfig');
INSERT INTO relay_config (relay_id, relay_type, class_name) VALUES ('admin.table_page_config', 'class', 'com.lionpig.webui.admin.TablePageConfig');

insert into history_action (action_id, action_name) values (1, 'create');
insert into history_action (action_id, action_name) values (2, 'update');
insert into history_action (action_id, action_name) values (3, 'delete');

COMMIT;

