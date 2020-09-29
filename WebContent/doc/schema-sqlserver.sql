--
-- !!! WARNING !!!
--
-- ALTER DATABASE zhige SET READ_COMMITTED_SNAPSHOT ON
-- 

CREATE TABLE ROLE_NAME_LIST (
	ROLE_NAME VARCHAR(32),
	PRIMARY KEY (ROLE_NAME)
);

CREATE TABLE RELAY_TYPE_LIST (
	RELAY_TYPE VARCHAR(32),
	PRIMARY KEY (RELAY_TYPE)
);

CREATE TABLE RELAY_CONFIG (
	RELAY_ID VARCHAR(64),
	RELAY_TYPE VARCHAR(32) DEFAULT 'http' NOT NULL,
	URL VARCHAR(255),
	LOGIN_NAME VARCHAR(100),
	LOGIN_PASS VARCHAR(100),
	CLASS_NAME VARCHAR(100),
	PRIMARY KEY (RELAY_ID)
);

ALTER TABLE RELAY_CONFIG ADD
CONSTRAINT RELAY_CONFIG_FK1
FOREIGN KEY (RELAY_TYPE)
REFERENCES RELAY_TYPE_LIST (RELAY_TYPE);

CREATE TABLE MENU_PATH (
	PATH_ROW_ID INT,
	PATH_NAME VARCHAR(32) NOT NULL,
	DISPLAY_SEQ INT,
	PRIMARY KEY (PATH_ROW_ID)
);

CREATE TABLE MENU_TYPE (
	MENU_TYPE_ID VARCHAR(32),
	PRIMARY KEY (MENU_TYPE_ID)
);

CREATE TABLE DB_CONFIG_TYPE (
	DB_TYPE VARCHAR(32),
	PRIMARY KEY (DB_TYPE)
);

CREATE TABLE DB_CONFIG (
	DB_NAME VARCHAR(32),
	DB_IP VARCHAR(32) NOT NULL,
	DB_PORT INT DEFAULT 1521 NOT NULL,
	DB_INSTANCE VARCHAR(32) NOT NULL,
	DB_ACC VARCHAR(32) NOT NULL,
	DB_PASS VARCHAR(32) NOT NULL,
	DB_TYPE VARCHAR(32) DEFAULT 'ORACLE' NOT NULL,
	PRIMARY KEY (DB_NAME)
);

ALTER TABLE DB_CONFIG ADD
CONSTRAINT DB_CONFIG_FK1
FOREIGN KEY (DB_TYPE)
REFERENCES DB_CONFIG_TYPE (DB_TYPE);

CREATE TABLE AUTO_PAGE_TYPE (
	PAGE_TYPE VARCHAR(32),
	PRIMARY KEY (PAGE_TYPE)
);

CREATE TABLE AUTO_PAGE (
	PAGE_ID VARCHAR(32),
	DB_NAME VARCHAR(32),
	PAGE_TYPE VARCHAR(32) NOT NULL,
	CMD_TEXT TEXT NOT NULL,
	PRIMARY KEY (PAGE_ID)
);

ALTER TABLE AUTO_PAGE ADD
CONSTRAINT AUTO_PAGE_FK1
FOREIGN KEY (DB_NAME)
REFERENCES DB_CONFIG (DB_NAME);

ALTER TABLE AUTO_PAGE ADD
CONSTRAINT AUTO_PAGE_FK2
FOREIGN KEY (PAGE_TYPE)
REFERENCES AUTO_PAGE_TYPE (PAGE_TYPE);

CREATE TABLE MENU (
	MENU_ROW_ID INT,
	TITLE VARCHAR(250) NOT NULL,
	URL VARCHAR(255),
	PATH_ROW_ID INT NOT NULL,
	DISPLAY_SEQ INT,
	MENU_TYPE_ID VARCHAR(32) DEFAULT 'html' NOT NULL,
	PAGE_ID VARCHAR(32),
	PRIMARY KEY (MENU_ROW_ID)
);

ALTER TABLE MENU ADD
CONSTRAINT MENU_FK1
FOREIGN KEY (PATH_ROW_ID)
REFERENCES MENU_PATH (PATH_ROW_ID);

ALTER TABLE MENU ADD
CONSTRAINT MENU_FK2
FOREIGN KEY (MENU_TYPE_ID)
REFERENCES MENU_TYPE (MENU_TYPE_ID);

ALTER TABLE MENU ADD
CONSTRAINT MENU_FK3
FOREIGN KEY (PAGE_ID)
REFERENCES AUTO_PAGE (PAGE_ID);

CREATE TABLE ROLE_MENU (
	ROLE_NAME VARCHAR(32),
	MENU_ROW_ID INT
	PRIMARY KEY (ROLE_NAME, MENU_ROW_ID)
);

ALTER TABLE ROLE_MENU ADD
CONSTRAINT ROLE_MENU_FK1
FOREIGN KEY (MENU_ROW_ID)
REFERENCES MENU (MENU_ROW_ID);

ALTER TABLE ROLE_MENU ADD
CONSTRAINT ROLE_MENU_FK2
FOREIGN KEY (ROLE_NAME)
REFERENCES ROLE_NAME_LIST (ROLE_NAME);

CREATE TABLE SESSION_LIST (
	SESSION_ID VARCHAR(32),
	LAST_ACTIVE_TIME DATETIME NOT NULL,
	SERVER_IP VARCHAR(32),
	SERVER_NAME VARCHAR(32),
	SERVER_PORT INT,
	PRIMARY KEY (SESSION_ID)
);

CREATE TABLE SESSION_PARAM (
	SESSION_ID VARCHAR(32),
	PARAM_NAME VARCHAR(200),
	PARAM_VALUE TEXT,
	PRIMARY KEY (SESSION_ID, PARAM_NAME)
);

ALTER TABLE SESSION_PARAM ADD
CONSTRAINT SESSION_PARAM_FK1
FOREIGN KEY (SESSION_ID)
REFERENCES SESSION_LIST (SESSION_ID)
ON DELETE CASCADE;

CREATE TABLE USER_LIST (
	USER_ROW_ID INT,
	USER_NAME VARCHAR(100) NOT NULL,
	PASSWORD VARCHAR(2000),
	DISABLED CHAR(1) DEFAULT 'N' NOT NULL,
	SESSION_ID VARCHAR(32),
	RETRY_COUNT INT DEFAULT 0,
	EMAIL_ADDRESS VARCHAR(250) NOT NULL,
	PRIMARY KEY (USER_ROW_ID)
);

CREATE UNIQUE INDEX USER_LIST_U1 ON USER_LIST (USER_NAME);

CREATE INDEX USER_LIST_I1 ON USER_LIST (SESSION_ID);

ALTER TABLE USER_LIST ADD
CONSTRAINT USER_LIST_FK1
FOREIGN KEY (SESSION_ID)
REFERENCES SESSION_LIST (SESSION_ID)
ON DELETE SET NULL;

CREATE TABLE USER_ROLE (
	USER_ROW_ID INT,
	ROLE_NAME VARCHAR(32),
	PRIMARY KEY (USER_ROW_ID, ROLE_NAME)
);

ALTER TABLE USER_ROLE ADD
CONSTRAINT USER_ROLE_FK1
FOREIGN KEY (ROLE_NAME)
REFERENCES ROLE_NAME_LIST (ROLE_NAME);

ALTER TABLE USER_ROLE ADD
CONSTRAINT USER_ROLE_FK2
FOREIGN KEY (USER_ROW_ID)
REFERENCES USER_LIST (USER_ROW_ID);

CREATE TABLE SP_PARAM_DIRECTION (
	PARAM_DIRECTION VARCHAR(6),
	PRIMARY KEY (PARAM_DIRECTION)
);

CREATE TABLE SP_PARAM_TYPE (
	PARAM_TYPE VARCHAR(32),
	PRIMARY KEY (PARAM_TYPE)
);

CREATE TABLE SP_PARAM (
	PAGE_ID VARCHAR(32),
	PARAM_NAME VARCHAR(32),
	PARAM_TYPE VARCHAR(32) NOT NULL,
	PARAM_DIRECTION VARCHAR(6) NOT NULL,
	PARAM_SEQ INT,
	PRIMARY KEY (PAGE_ID, PARAM_NAME)
);

ALTER TABLE SP_PARAM ADD
CONSTRAINT SP_PARAM_FK1
FOREIGN KEY (PAGE_ID)
REFERENCES AUTO_PAGE (PAGE_ID);

ALTER TABLE SP_PARAM ADD
CONSTRAINT SP_PARAM_FK2
FOREIGN KEY (PARAM_DIRECTION)
REFERENCES SP_PARAM_DIRECTION (PARAM_DIRECTION);

ALTER TABLE SP_PARAM ADD
CONSTRAINT SP_PARAM_FK3
FOREIGN KEY (PARAM_TYPE)
REFERENCES SP_PARAM_TYPE (PARAM_TYPE);

CREATE TABLE SQL_INPUT_TYPE (
	INPUT_TYPE VARCHAR(32),
	PRIMARY KEY (INPUT_TYPE)
);

CREATE TABLE SQL_INPUT_OP (
	INPUT_OP VARCHAR(32),
	PRIMARY KEY (INPUT_OP)
);

CREATE TABLE SQL_INPUT_LOGIC (
    INPUT_LOGIC VARCHAR(32),
	PRIMARY KEY (INPUT_LOGIC)
);

CREATE TABLE SQL_INPUT_OPTIONAL (
	INPUT_OPTIONAL VARCHAR(1),
	PRIMARY KEY (INPUT_OPTIONAL)
);

CREATE TABLE SQL_INPUT_CUSTOM_FLAG (
	INPUT_CUSTOM_FLAG INT,
	DESCRIPTION VARCHAR(100),
	PRIMARY KEY (INPUT_CUSTOM_FLAG)
);

CREATE TABLE SQL_INPUT (
	PAGE_ID VARCHAR(32),
	INPUT_NAME VARCHAR(32),
	INPUT_ID VARCHAR(250) NOT NULL,
	INPUT_TYPE VARCHAR(32) NOT NULL,
	INPUT_SEQ INT,
	INPUT_OP VARCHAR(32) NOT NULL,
	INPUT_LOGIC VARCHAR(32),
	INPUT_OPTIONAL VARCHAR(1) DEFAULT 'Y' NOT NULL,
	INPUT_LIST_SOURCE VARCHAR(32),
	INPUT_DEFAULT_VALUE VARCHAR(32),
	INPUT_CUSTOM_FLAG INT,
	PRIMARY KEY (PAGE_ID, INPUT_NAME)
);

ALTER TABLE SQL_INPUT ADD
CONSTRAINT SQL_INPUT_FK1
FOREIGN KEY (PAGE_ID)
REFERENCES AUTO_PAGE (PAGE_ID);

ALTER TABLE SQL_INPUT ADD
CONSTRAINT SQL_INPUT_FK2
FOREIGN KEY (INPUT_TYPE)
REFERENCES SQL_INPUT_TYPE (INPUT_TYPE);

ALTER TABLE SQL_INPUT ADD
CONSTRAINT SQL_INPUT_FK3
FOREIGN KEY (INPUT_OP)
REFERENCES SQL_INPUT_OP (INPUT_OP);

ALTER TABLE SQL_INPUT ADD
CONSTRAINT SQL_INPUT_FK4
FOREIGN KEY (INPUT_LOGIC)
REFERENCES SQL_INPUT_LOGIC (INPUT_LOGIC);

ALTER TABLE SQL_INPUT ADD
CONSTRAINT SQL_INPUT_FK5
FOREIGN KEY (INPUT_OPTIONAL)
REFERENCES SQL_INPUT_OPTIONAL (INPUT_OPTIONAL);

ALTER TABLE SQL_INPUT ADD
CONSTRAINT SQL_INPUT_FK6
FOREIGN KEY (INPUT_LIST_SOURCE)
REFERENCES AUTO_PAGE (PAGE_ID);

ALTER TABLE SQL_INPUT ADD
CONSTRAINT SQL_INPUT_FK7
FOREIGN KEY (INPUT_DEFAULT_VALUE)
REFERENCES AUTO_PAGE (PAGE_ID);

ALTER TABLE SQL_INPUT ADD
CONSTRAINT SQL_INPUT_FK8
FOREIGN KEY (INPUT_CUSTOM_FLAG)
REFERENCES SQL_INPUT_CUSTOM_FLAG (INPUT_CUSTOM_FLAG);

CREATE TABLE SQL_OUTPUT_TYPE (
	OUTPUT_TYPE VARCHAR(32),
	PRIMARY KEY (OUTPUT_TYPE)
);

CREATE TABLE SQL_OUTPUT (
	PAGE_ID VARCHAR(32),
	OUTPUT_NAME VARCHAR(32),
	OUTPUT_ID VARCHAR(32) NOT NULL,
	OUTPUT_SEQ INT,
	OUTPUT_TYPE VARCHAR(32) NOT NULL,
	PRIMARY KEY (PAGE_ID, OUTPUT_NAME)
);

ALTER TABLE SQL_OUTPUT ADD
CONSTRAINT SQL_OUTPUT_FK1
FOREIGN KEY (PAGE_ID)
REFERENCES AUTO_PAGE (PAGE_ID);

ALTER TABLE SQL_OUTPUT ADD
CONSTRAINT SQL_OUTPUT_FK2
FOREIGN KEY (OUTPUT_TYPE)
REFERENCES SQL_OUTPUT_TYPE (OUTPUT_TYPE);

CREATE TABLE SQL_TEMP_TABLE (
	SESSION_ID VARCHAR(32),
	PAGE_ID VARCHAR(32),
	ITEM1 VARCHAR(250),
	ITEM2 VARCHAR(250),
	ITEM3 VARCHAR(250),
	ITEM4 VARCHAR(250),
	ITEM5 VARCHAR(250),
	ITEM6 VARCHAR(250),
	ITEM7 VARCHAR(250),
	ITEM8 VARCHAR(250),
	ITEM9 VARCHAR(250),
	ITEM10 VARCHAR(250),
	ITEM11 VARCHAR(250),
	ITEM12 VARCHAR(250),
	ITEM13 VARCHAR(250),
	ITEM14 VARCHAR(250),
	ITEM15 VARCHAR(250),
	ITEM16 VARCHAR(250),
	ITEM17 VARCHAR(250),
	ITEM18 VARCHAR(250),
	ITEM19 VARCHAR(250),
	ITEM20 VARCHAR(250),
	ITEM21 VARCHAR(250),
	ITEM22 VARCHAR(250),
	ITEM23 VARCHAR(250),
	ITEM24 VARCHAR(250),
	ITEM25 VARCHAR(250),
	ITEM26 VARCHAR(250),
	ITEM27 VARCHAR(250),
	ITEM28 VARCHAR(250),
	ITEM29 VARCHAR(250),
	ITEM30 VARCHAR(250),
	DATE1 DATETIME,
	DATE2 DATETIME,
	DATE3 DATETIME,
	DATE4 DATETIME,
	DATE5 DATETIME,
	DATE6 DATETIME,
	DATE7 DATETIME,
	DATE8 DATETIME,
	DATE9 DATETIME,
	DATE10 DATETIME,
	NUM1 NUMERIC,
	NUM2 NUMERIC,
	NUM3 NUMERIC,
	NUM4 NUMERIC,
	NUM5 NUMERIC,
	NUM6 NUMERIC,
	NUM7 NUMERIC,
	NUM8 NUMERIC,
	NUM9 NUMERIC,
	NUM10 NUMERIC
);

CREATE INDEX SQL_TEMP_TABLE_I1 ON SQL_TEMP_TABLE (SESSION_ID, PAGE_ID);

ALTER TABLE SQL_TEMP_TABLE ADD
CONSTRAINT SQL_TEMP_TABLE_FK1
FOREIGN KEY (SESSION_ID)
REFERENCES SESSION_LIST (SESSION_ID)
ON DELETE CASCADE;

CREATE TABLE DEBUG_LOG (
	LOG_SEQ NUMERIC IDENTITY(0, 1),
	LOG_TIME DATETIME,
	LOG_VERBOSE INT,
	LOG_VALUE TEXT,
	USER_ROW_ID INT,
	USER_NAME VARCHAR(100),
	SESSION_ID VARCHAR(50),
	RELAY_ID VARCHAR(64),
	RELAY_URL VARCHAR(255),
	FUNCTION_NAME VARCHAR(32),
	CLASS_NAME VARCHAR(100),
	PRIMARY KEY (LOG_SEQ)
);

CREATE INDEX DEBUG_LOG_I1 ON DEBUG_LOG (LOG_TIME, USER_ROW_ID, SESSION_ID);

CREATE TABLE GLOBAL_CONFIG (
	GROUP_NAME VARCHAR(32),
	CONFIG_NAME VARCHAR(32),
	CONFIG_VALUE TEXT,
	PRIMARY KEY (GROUP_NAME, CONFIG_NAME)
);

-- PREPARED DATA --
INSERT INTO MENU_TYPE (MENU_TYPE_ID) VALUES ('html');
INSERT INTO MENU_TYPE (MENU_TYPE_ID) VALUES ('auto');
INSERT INTO SQL_INPUT_TYPE (INPUT_TYPE) VALUES ('text');
INSERT INTO SQL_INPUT_TYPE (INPUT_TYPE) VALUES ('date');
INSERT INTO SQL_INPUT_TYPE (INPUT_TYPE) VALUES ('datetime');
INSERT INTO SQL_INPUT_TYPE (INPUT_TYPE) VALUES ('list');
INSERT INTO SQL_INPUT_TYPE (INPUT_TYPE) VALUES ('combobox');
INSERT INTO SQL_INPUT_OP VALUES ('=');
INSERT INTO SQL_INPUT_OP VALUES ('<>');
INSERT INTO SQL_INPUT_OP VALUES ('>');
INSERT INTO SQL_INPUT_OP VALUES ('>=');
INSERT INTO SQL_INPUT_OP VALUES ('<');
INSERT INTO SQL_INPUT_OP VALUES ('<=');
INSERT INTO SQL_INPUT_OP VALUES ('LIKE');
INSERT INTO SQL_INPUT_LOGIC VALUES ('AND');
INSERT INTO SQL_INPUT_LOGIC VALUES ('OR');
INSERT INTO SQL_INPUT_LOGIC VALUES ('AND (');
INSERT INTO SQL_INPUT_LOGIC VALUES ('OR (');
INSERT INTO SQL_INPUT_LOGIC VALUES (')');
INSERT INTO SQL_INPUT_LOGIC VALUES (') AND');
INSERT INTO SQL_INPUT_LOGIC VALUES (') OR');
INSERT INTO SQL_INPUT_LOGIC VALUES (') AND (');
INSERT INTO SQL_INPUT_LOGIC VALUES (') OR (');
INSERT INTO RELAY_TYPE_LIST VALUES ('http');
INSERT INTO RELAY_TYPE_LIST VALUES ('class');
INSERT INTO AUTO_PAGE_TYPE VALUES ('SQL');
INSERT INTO AUTO_PAGE_TYPE VALUES ('SQL_DS');
INSERT INTO AUTO_PAGE_TYPE VALUES ('SP');
INSERT INTO AUTO_PAGE_TYPE VALUES ('SP_DS');
INSERT INTO AUTO_PAGE_TYPE VALUES ('SQL_RPT');
INSERT INTO SQL_OUTPUT_TYPE VALUES ('text');
INSERT INTO SQL_OUTPUT_TYPE VALUES ('date');
INSERT INTO SQL_OUTPUT_TYPE VALUES ('datetime');
INSERT INTO SQL_OUTPUT_TYPE VALUES ('number');
INSERT INTO SQL_INPUT_OPTIONAL VALUES ('N');
INSERT INTO SQL_INPUT_OPTIONAL VALUES ('T');
INSERT INTO SQL_INPUT_OPTIONAL VALUES ('F');

INSERT INTO SP_PARAM_DIRECTION VALUES ('IN');
INSERT INTO SP_PARAM_DIRECTION VALUES ('OUT');
INSERT INTO SP_PARAM_TYPE VALUES ('STRING');
INSERT INTO SP_PARAM_TYPE VALUES ('NUMBER');
INSERT INTO SP_PARAM_TYPE VALUES ('DATETIME');
INSERT INTO SP_PARAM_TYPE VALUES ('TABLE');

INSERT INTO ROLE_NAME_LIST VALUES ('ADMIN');
INSERT INTO ROLE_NAME_LIST VALUES ('EXPORT_EXCEL');
INSERT INTO USER_LIST (USER_ROW_ID, USER_NAME, PASSWORD, DISABLED, EMAIL_ADDRESS) VALUES (0, 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'N', 'admin@localhost');
INSERT INTO USER_ROLE (ROLE_NAME, USER_ROW_ID) VALUES ('ADMIN', 0);
INSERT INTO USER_ROLE (ROLE_NAME, USER_ROW_ID) VALUES ('EXPORT_EXCEL', 0);
INSERT INTO MENU_PATH (PATH_ROW_ID, PATH_NAME, DISPLAY_SEQ) VALUES (0, 'ADMIN', 0);

INSERT INTO MENU (MENU_ROW_ID, TITLE, URL, PATH_ROW_ID, DISPLAY_SEQ) VALUES (0, 'User Account Maintenance', 'html/admin/user_maintain.html', 0, 0);
INSERT INTO MENU (MENU_ROW_ID, TITLE, URL, PATH_ROW_ID, DISPLAY_SEQ) VALUES (-1, 'License', 'html/admin/license.html', 0, 1);

INSERT INTO ROLE_MENU (ROLE_NAME, MENU_ROW_ID) VALUES ('ADMIN', 0);
INSERT INTO ROLE_MENU (ROLE_NAME, MENU_ROW_ID) VALUES ('ADMIN', -1);

INSERT INTO GLOBAL_CONFIG (GROUP_NAME, CONFIG_NAME, CONFIG_VALUE) VALUES ('ADMIN', 'MAX_IDLE_SECOND', '300');
INSERT INTO GLOBAL_CONFIG (GROUP_NAME, CONFIG_NAME, CONFIG_VALUE) VALUES ('ADMIN', 'SSO_URL', NULL);
INSERT INTO GLOBAL_CONFIG (GROUP_NAME, CONFIG_NAME, CONFIG_VALUE) VALUES ('ADMIN', 'LOG_VERBOSE', '9');
INSERT INTO GLOBAL_CONFIG (GROUP_NAME, CONFIG_NAME, CONFIG_VALUE) VALUES ('ADMIN', 'PASSWORD_RETRY_COUNT', '3');
INSERT INTO GLOBAL_CONFIG (GROUP_NAME, CONFIG_NAME, CONFIG_VALUE) VALUES ('ADMIN', 'USER_ROW_ID', '1');

INSERT INTO GLOBAL_CONFIG (GROUP_NAME, CONFIG_NAME, CONFIG_VALUE) VALUES ('ADMIN_EMAIL', 'SMTP_PORT', '587');
INSERT INTO GLOBAL_CONFIG (GROUP_NAME, CONFIG_NAME, CONFIG_VALUE) VALUES ('ADMIN_EMAIL', 'USER_NAME', 'ythung1@gmail.com');
INSERT INTO GLOBAL_CONFIG (GROUP_NAME, CONFIG_NAME, CONFIG_VALUE) VALUES ('ADMIN_EMAIL', 'PASSWORD', NULL);
INSERT INTO GLOBAL_CONFIG (GROUP_NAME, CONFIG_NAME, CONFIG_VALUE) VALUES ('ADMIN_EMAIL', 'WITH_TLS', 'true');
INSERT INTO GLOBAL_CONFIG (GROUP_NAME, CONFIG_NAME, CONFIG_VALUE) VALUES ('ADMIN_EMAIL', 'HOST_NAME', 'smtp.gmail.com');
INSERT INTO GLOBAL_CONFIG (GROUP_NAME, CONFIG_NAME, CONFIG_VALUE) VALUES ('ADMIN_EMAIL', 'FROM', 'ythung1@gmail.com');

INSERT INTO GLOBAL_CONFIG (GROUP_NAME, CONFIG_NAME, CONFIG_VALUE) VALUES ('SERVER', 'LOCK', '1');
INSERT INTO GLOBAL_CONFIG (GROUP_NAME, CONFIG_NAME, CONFIG_VALUE) VALUES ('SERVER', 'LICENSE', 'WJGtuit+qPa8TbIQbwPSxvZNwaJCY9o3/t1vzL2MZKSxvgtQn7BGAgAhhK7qSKD0ENvI8O5WCcM0VH1y91/kKg==');

INSERT INTO DB_CONFIG_TYPE VALUES ('ORACLE');
INSERT INTO DB_CONFIG_TYPE VALUES ('SQLSERVER');
INSERT INTO DB_CONFIG_TYPE VALUES ('MYSQL');

INSERT INTO SQL_INPUT_CUSTOM_FLAG VALUES (1, 'Do not replace char(20) when processing auto page SQL_RPT');

INSERT INTO RELAY_CONFIG (RELAY_ID, RELAY_TYPE, CLASS_NAME)
VALUES ('ADMIN-USER-MAINTAIN', 'class', 'com.lionpig.webui.admin.UserMaintain');
INSERT INTO RELAY_CONFIG (RELAY_ID, RELAY_TYPE, CLASS_NAME)
VALUES ('ADMIN-LICENSE-MAINTAIN', 'class', 'com.lionpig.webui.admin.LicenseMaintain');