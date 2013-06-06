/*
 *   Copyright 2013 Ant Kutschera
 *   
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
DROP VIEW BOOKINGS;
DROP TABLE CONFIG;
DROP TABLE PAYMENT;
DROP TABLE ACCOUNT_BOOKING;
DROP TABLE ORDER_ITEM;
DROP TABLE ORDERS;
DROP TABLE COMMENT;
DROP TABLE RATING_USER_MAPPING;
DROP TABLE RATING;
DROP TABLE EVENT;
DROP TABLE EVENTS_SEQUENCES;
DROP TABLE ROLE;
DROP TABLE USER;
DROP TABLE USER_SEQUENCE;
DROP TABLE RULE;
DROP TABLE RULE_SEQUENCE;

-- ---------------------------------------------------------------
-- USER MANAGEMENT
-- ---------------------------------------------------------------

CREATE TABLE USER (
	ID BIGINT NOT NULL, 
	EMAIL VARCHAR(255) UNIQUE NOT NULL, 
	PASSWORD VARCHAR(255) NOT NULL, 
	NAME VARCHAR(255) NOT NULL, 
	PRIMARY KEY (ID)
)
;

CREATE TABLE USER_SEQUENCE (
	SEQ_NAME VARCHAR(50) NOT NULL, 
	SEQ_COUNT BIGINT NOT NULL, 
	PRIMARY KEY (SEQ_NAME)
)
;

-- role uses a sequence in table user_sequence
CREATE TABLE ROLE (
	ID BIGINT NOT NULL, 
	EMAIL VARCHAR(255) NOT NULL, --not unique, since user can have many roles. this col is here for jee security only! 
	ROLE VARCHAR(255) NOT NULL, 
	USER_ID BIGINT, 
	PRIMARY KEY (ID),
	FOREIGN KEY (USER_ID) REFERENCES USER(ID)
)
;

CREATE INDEX ON ROLE(EMAIL);

-- ---------------------------------------------------------------
-- RULE ENGINE
-- ---------------------------------------------------------------

CREATE TABLE RULE (
	ID BIGINT NOT NULL, 
	NAME VARCHAR(255) NOT NULL, 
	EXPRESSION VARCHAR(2048) NOT NULL, 
	OUTCOME VARCHAR(255) NOT NULL, 
	PRIORITY INT NOT NULL, 
	NAMESPACE VARCHAR(255) NOT NULL, 
	DESCRIPTION VARCHAR(4096) NOT NULL, 
	PRIMARY KEY (ID)
)
;

CREATE TABLE RULE_SEQUENCE (
	SEQ_NAME VARCHAR(50) NOT NULL, 
	SEQ_COUNT BIGINT NOT NULL, 
	PRIMARY KEY (SEQ_NAME)
)
;

-- ---------------------------------------------------------------
-- eEvents.com tables
-- ---------------------------------------------------------------

-- which users have rated an event?
CREATE TABLE EVENT (
	UID VARCHAR(255) NOT NULL, 
	PRIMARY KEY (UID)
)
;

-- which users have rated an event?
CREATE TABLE RATING_USER_MAPPING (
	ID BIGINT NOT NULL, 
	EVENT_UID VARCHAR(255) NOT NULL, 
	USER_ID BIGINT NOT NULL, 
	RATING INT NOT NULL, -- what rating did this user give? 
	PRIMARY KEY (ID),
	FOREIGN KEY (USER_ID) REFERENCES USER(ID),
	FOREIGN KEY (EVENT_UID) REFERENCES EVENT(UID)
)
;

CREATE INDEX ON RATING_USER_MAPPING(EVENT_UID);

--table of average ratings
CREATE TABLE RATING (
	EVENT_UID VARCHAR(255) NOT NULL, 
	SUM_RATINGS BIGINT NOT NULL, 
	NUM_RATINGS INT NOT NULL, 
	PRIMARY KEY (EVENT_UID),
	FOREIGN KEY (EVENT_UID) REFERENCES EVENT(UID)
)
;

--table of comments
CREATE TABLE COMMENT (
	ID BIGINT NOT NULL, 
	EVENT_UID VARCHAR(255) NOT NULL, 
	COMMENT VARCHAR(2048) NOT NULL,
	USER_ID BIGINT NOT NULL,
	WHEN TIMESTAMP NOT NULL,
	PRIMARY KEY (ID),
	FOREIGN KEY (EVENT_UID) REFERENCES EVENT(UID),
	FOREIGN KEY (USER_ID) REFERENCES USER(ID)
)
;

--table of orders
CREATE TABLE ORDERS (
	ID BIGINT NOT NULL, 
	UUID VARCHAR(255) NOT NULL, 
	USER_ID BIGINT NOT NULL,
	WHEN TIMESTAMP NOT NULL,
	STATE VARCHAR(25) NOT NULL,
	PRIMARY KEY (ID),
	FOREIGN KEY (USER_ID) REFERENCES USER(ID)
)
;

--table of order items
--yes, I know these are not normalised! but i dont care in this instance - the requirement is that i make a copy of the data which will be stored for 10 years.
CREATE TABLE ORDER_ITEM (
	ID BIGINT NOT NULL, 
	TARIF_NAME VARCHAR(255) NOT NULL, 
	TARIF_QTY SMALLINT NOT NULL, 
	TARIF_PRICE_CHF DECIMAL(7,2) NOT NULL, 
	BOOKING_REF VARCHAR(255) NOT NULL, 
	EVENT_UID VARCHAR(255) NOT NULL,
	EVENT_NAME VARCHAR(255) NOT NULL,
	EVENT_DATE TIMESTAMP NOT NULL,
	BOOKING_SYSTEM_CODE VARCHAR(20) NOT NULL,
	PARTNER_REFERENCE VARCHAR(255) NOT NULL,
	STATE VARCHAR(25) NOT NULL,
	PRIMARY KEY (ID)
	-- FOREIGN KEY (ORDER_ID) REFERENCES ORDERS(ID) doesnt work, because hibernate has to create an update script after the insert!
	-- FOREIGN KEY (EVENT_UID) REFERENCES EVENT(UID)
)
;

--table of accounts
CREATE TABLE ACCOUNT_BOOKING (
	ID BIGINT NOT NULL,
	BOOKING_SYSTEM_CODE VARCHAR(20) NOT NULL,
	EVENT_ID INT NOT NULL, 
	TARIF_NAME VARCHAR(255) NOT NULL, 
	TARIF_PRICE_CHF DECIMAL(7,2) NOT NULL, 
	REFERENCE_NUMBER VARCHAR(255) UNIQUE,
	PARTNER_REFERENCE VARCHAR(255) UNIQUE,
	STATE VARCHAR(25) NOT NULL,
	CREATED TIMESTAMP NOT NULL,
	LAST_UPDATED TIMESTAMP NOT NULL,
	PRIMARY KEY (ID)
)
;

--table of payments
CREATE TABLE PAYMENT (
	ID BIGINT NOT NULL, 
	ORDER_UUID VARCHAR(255) NOT NULL,
	PRICE_CHF DECIMAL(7,2) NOT NULL, 
	TOKEN_1 VARCHAR(255), -- the one we send to the partner, which they generated for us
	TOKEN_2 VARCHAR(255), -- the one we get when the user returns to our site
	STATE VARCHAR(25) NOT NULL,
	PRIMARY KEY (ID)
)
;

CREATE INDEX ON PAYMENT(ORDER_UUID);

-- sequences for all tables specific to this eevents app
CREATE TABLE EVENTS_SEQUENCES (
	SEQ_NAME VARCHAR(50) NOT NULL, 
	SEQ_COUNT BIGINT NOT NULL, 
	PRIMARY KEY (SEQ_NAME)
)
;

--table of configuration data
CREATE TABLE CONFIG (
	ID VARCHAR(255) NOT NULL,
	VAL VARCHAR(255) NOT NULL,
	PRIMARY KEY (ID)
)
;

-- -----------------------------
-- views
-- -----------------------------

CREATE VIEW BOOKINGS AS 
SELECT 
	U.NAME, 
	OI.BOOKING_REF, 
	OI.TARIF_NAME, 
	OI.TARIF_PRICE_CHF, 
	OI.TARIF_QTY,
	OI.STATE, 
	OI.EVENT_UID
FROM 
	ORDER_ITEM OI, 
	ORDERS O, 
	USER U 
WHERE 
	O.ID = OI.ORDER_ID 
	AND o.USER_ID  = U.ID
;

