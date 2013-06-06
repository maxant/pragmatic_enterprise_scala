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
USE SCALABOOK_APP;

DELETE FROM SESSION_CACHE;
DELETE FROM PAYMENT;
DELETE FROM ACCOUNT_ENTRY;
DELETE FROM ORDER_ITEM;
DELETE FROM ORDERS;
DELETE FROM COMMENT;
DELETE FROM RATING_USER_MAPPING;
DELETE FROM RATING;
DELETE FROM EVENT;
DELETE FROM EVENTS_SEQUENCES;
DELETE FROM ROLE;
DELETE FROM USER;
DELETE FROM USER_SEQUENCE;
DELETE FROM RULE;
DELETE FROM RULE_SEQUENCE;

-- ---------------------------------------------------------------
-- USER MANAGEMENT
-- ---------------------------------------------------------------

INSERT INTO USER_SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('USER', 10);

INSERT INTO USER_SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('ROLE', 10);

INSERT INTO USER(ID, EMAIL, PASSWORD, NAME)
VALUES (1, 'john@maxant.ch', 'kS7IA7LOSeSlQQaNSVq1cA==', 'John'); -- password is asdf

INSERT INTO USER(ID, EMAIL, PASSWORD, NAME)
VALUES (2, 'jane@maxant.ch', 'kS7IA7LOSeSlQQaNSVq1cA==', 'Jane'); -- password is asdf

INSERT INTO ROLE(ID, EMAIL, ROLE, USER_ID)
VALUES (1, 'john@maxant.ch', 'registered', 1);

INSERT INTO ROLE(ID, EMAIL, ROLE, USER_ID)
VALUES (2, 'jane@maxant.ch', 'registered', 2);

INSERT INTO ROLE(ID, EMAIL, ROLE, USER_ID)
VALUES (3, 'jane@maxant.ch', 'admin', 2);

-- ---------------------------------------------------------------
-- RULE ENGINE
-- ---------------------------------------------------------------

INSERT INTO RULE_SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('rule', 10);

INSERT INTO RULE(ID, NAME, EXPRESSION, OUTCOME, PRIORITY, NAMESPACE, DESCRIPTION)
VALUES (1, 'orderValueGreaterThan50', 'input.totalPrice > 50', '5.00', 100, 'events.insurance', 'If an order is worth more than 50 CHF, then we add insurance for 5.00 CHF to it, which a customer can remove if they wish.');

INSERT INTO RULE(ID, NAME, EXPRESSION, OUTCOME, PRIORITY, NAMESPACE, DESCRIPTION)
VALUES (2, 'orderValueGreaterThan100', 'input.totalPrice > 100', '7.50', 110, 'events.insurance', 'If an order is worth more than 100 CHF, then we add insurance for 7.50 CHF to it, which a customer can remove if they wish.');

-- ---------------------------------------------------------------
-- eEvents.com tables
-- ---------------------------------------------------------------

INSERT INTO EVENT(UID, NAME, CITY, THE_TIME, TEASER_PRICE_CHF, DESCRIPTION, BOOKING_SYSTEMS, IS_TEASER, IMG_NAME)
VALUES (
    'LKJSC-W1',
	'Mama Mojo!',
	'Lausanne',
	'2013-03-01 20:00:00',
	14.95,
	'Tu quod Google nunc vertit ad et a Latine? Ago quod paucis tantum inveni, sed tamen puto vere frigus! Nunc si operatus ex hoc puzzle, committitur admirans quod alia has hoc libro continet. Esset sortem. Esset sortem. Suspendisse at tortor odio, quis blandit sapien. Quis blandit sapien. Quis blandit sapien.',
	'GEDE-1,ADFF-1',
	TRUE,
	'LKJSC-W1'
);

INSERT INTO EVENT(UID, NAME, CITY, THE_TIME, TEASER_PRICE_CHF, DESCRIPTION, BOOKING_SYSTEMS, IS_TEASER, IMG_NAME)
VALUES (
    'BWEIY-A4',
	'The Lights, live',
	'Geneva',
	'2013-03-04 21:00:00',
	12.40,
	'Sensus autem est bonum quod libet proximo sit maior subtilitate clamat. Cum vigilantes in penis ostende nocte, sunt ostendit a dildo excogitatoris. Posuit quam ampla probatio processus est ut iustus ius figura et magnitudine enim vultu et sentire omnes variis preferences habent.',
	'ADFF-2',
	TRUE,
	'BWEIY-A4'
);

INSERT INTO EVENT(UID, NAME, CITY, THE_TIME, TEASER_PRICE_CHF, DESCRIPTION, BOOKING_SYSTEMS, IS_TEASER, IMG_NAME)
VALUES (
    'BWEIY-A4-1',
	'The Lights, live',
	'Basel',
	'2013-03-05 21:00:00',
	12.40,
	'Sensus autem est bonum quod libet proximo sit maior subtilitate clamat. Cum vigilantes in penis ostende nocte, sunt ostendit a dildo excogitatoris. Posuit quam ampla probatio processus est ut iustus ius figura et magnitudine enim vultu et sentire omnes variis preferences habent.',
	'ADFF-2',
	TRUE,
	'BWEIY-A4'
);

INSERT INTO EVENT(UID, NAME, CITY, THE_TIME, TEASER_PRICE_CHF, DESCRIPTION, BOOKING_SYSTEMS, IS_TEASER, IMG_NAME)
VALUES (
    'BWEIY-A4-2',
	'The Lights, live',
	'London',
	'2013-03-15 21:00:00',
	12.40,
	'Sensus autem est bonum quod libet proximo sit maior subtilitate clamat. Cum vigilantes in penis ostende nocte, sunt ostendit a dildo excogitatoris. Posuit quam ampla probatio processus est ut iustus ius figura et magnitudine enim vultu et sentire omnes variis preferences habent.',
	'ADFF-2',
	TRUE,
	'BWEIY-A4'
);

INSERT INTO EVENT(UID, NAME, CITY, THE_TIME, TEASER_PRICE_CHF, DESCRIPTION, BOOKING_SYSTEMS, IS_TEASER, IMG_NAME)
VALUES (
    'INS-087',
	'Insurance',
	'-',
	'2099-12-31 23:59:59',
	5.00,
	' Maecenas vel lectus ac mauris elementum eleifend. Curabitur et eros lectus, vitae gravida quam.',
	'EEVT-1',
	FALSE,
	'-'
);

INSERT INTO EVENT(UID, NAME, CITY, THE_TIME, TEASER_PRICE_CHF, DESCRIPTION, BOOKING_SYSTEMS, IS_TEASER, IMG_NAME)
VALUES (
    'HWJK-7K',
	'Big DZ',
	'Geneva',
	'2013-03-04 21:00:00',
	45.30,
	'Donec ut consectetur massa. Proin ullamcorper risus eget neque condimentum quis elementum risus volutpat. Vivamus a neque augue. Vivamus metus nulla, aliquam ut feugiat eu, posuere sit amet sem. Aliquam diam sem, euismod non auctor sed, tempus non augue.',
	'ADFF-2',
	TRUE,
	'HWJK-7K'
);

INSERT INTO EVENT(UID, NAME, CITY, THE_TIME, TEASER_PRICE_CHF, DESCRIPTION, BOOKING_SYSTEMS, IS_TEASER, IMG_NAME)
VALUES (
    'RJGK-3M',
	'Rocknrollers',
	'Geneva',
	'2013-03-04 21:00:00',
	32.00,
	'Vestibulum at odio orci, non ullamcorper nibh. Phasellus scelerisque dictum blandit. Aliquam erat volutpat. Nam dui dolor, eleifend porta bibendum vitae, interdum non augue. Aliquam aliquam pellentesque porttitor. Nam sed dolor arcu, et rhoncus quam. Suspendisse at tortor odio, quis blandit sapien.',
	'ADFF-1',
	TRUE,
    'RJGK-3M'
);


INSERT INTO RATING_USER_MAPPING (ID, EVENT_UID, USER_ID, RATING)
VALUES (1, 'LKJSC-W1', 2, 4);

INSERT INTO RATING (EVENT_UID, SUM_RATINGS, NUM_RATINGS)
VALUES ('LKJSC-W1', 44, 11);

INSERT INTO RATING (EVENT_UID, SUM_RATINGS, NUM_RATINGS)
VALUES ('BWEIY-A4', 3, 1);

INSERT INTO EVENTS_SEQUENCES(SEQ_NAME, SEQ_COUNT) VALUES ('RATING', 10);
INSERT INTO EVENTS_SEQUENCES(SEQ_NAME, SEQ_COUNT) VALUES ('RATING_USER_MAPPING', 10);

INSERT INTO COMMENT(ID, EVENT_UID, COMMENT, USER_ID, THE_TIME)
VALUES (1, 'BWEIY-A4', 'I cant wait to go to this concert, Ive been waiting years for them to come back to town!', 1, '2012-09-16 10:50:01');

INSERT INTO COMMENT(ID, EVENT_UID, COMMENT, USER_ID, THE_TIME)
VALUES (2, 'BWEIY-A4', 'Yeah, me neither.', 2, '2012-09-16 10:50:31');

INSERT INTO EVENTS_SEQUENCES(SEQ_NAME, SEQ_COUNT) VALUES ('COMMENT', 10);

INSERT INTO ORDERS(ID, UUID, USER_ID, THE_TIME, STATE)
VALUES (1, '81b2746e-9558-4df5-b8d7-c02e49cb3d3e', 1, '2012-09-16 10:50:31', 'Paid');

INSERT INTO ORDER_ITEM(ID, ORDER_ID, TARIF_NAME, TARIF_DESCRIPTION, TARIF_CONDITIONS, TARIF_QTY, TARIF_PRICE_CHF, BOOKING_REF, EVENT_UID, EVENT_NAME, EVENT_DATE, BOOKING_SYSTEM_CODE, PARTNER_REFERENCE, STATE)
VALUES (1, 1, 'Last Minute Saver R14-20', 'Exchangeable, any row', 'Some conditions...', 3, 12.99, 'F32-FWFNSLD-347382', 'LKJSC-W1', 'Mama Mojo!', '2012-12-24 20:00:00', 'GEDE', 'GEDE-23423364363452', 'Printed');

INSERT INTO ORDER_ITEM(ID, ORDER_ID, TARIF_NAME, TARIF_DESCRIPTION, TARIF_CONDITIONS, TARIF_QTY, TARIF_PRICE_CHF, BOOKING_REF, EVENT_UID, EVENT_NAME, EVENT_DATE, BOOKING_SYSTEM_CODE, PARTNER_REFERENCE, STATE)
VALUES (2, 1, 'Row 14-28', 'Exchangeable, any row', 'Some conditions...', 1, 43.50, 'F32-FWFNSLD-456243', 'LKJSC-W1', 'Mama Mojo!', '2012-12-24 20:00:00', 'GEDE', 'GEDE-23423364364562', 'Printed');

INSERT INTO ORDER_ITEM(ID, ORDER_ID, TARIF_NAME, TARIF_DESCRIPTION, TARIF_CONDITIONS, TARIF_QTY, TARIF_PRICE_CHF, BOOKING_REF, EVENT_UID, EVENT_NAME, EVENT_DATE, BOOKING_SYSTEM_CODE, PARTNER_REFERENCE, STATE)
VALUES (3, 1, 'R1-3', 'Exchangeable, any row', 'Some conditions...', 3, 68.70, 'F32-FWFNSLD-456687', 'LKJSC-W1', 'Mama Mojo!', '2012-12-24 20:00:00', 'GEDE', 'GEDE-23423364364312', 'Validated');

INSERT INTO ORDER_ITEM(ID, ORDER_ID, TARIF_NAME, TARIF_DESCRIPTION, TARIF_CONDITIONS, TARIF_QTY, TARIF_PRICE_CHF, BOOKING_REF, EVENT_UID, EVENT_NAME, EVENT_DATE, BOOKING_SYSTEM_CODE, PARTNER_REFERENCE, STATE)
VALUES (4, 1, 'Row 10-13', 'Exchangeable, any row', 'Some conditions...', 1, 52.75, 'F32-FWFNSLD-974258', 'LKJSC-W1', 'Mama Mojo!', '2012-12-24 20:00:00', 'GEDE', 'GEDE-234233643322114', 'Validated');

INSERT INTO ORDER_ITEM(ID, ORDER_ID, TARIF_NAME, TARIF_DESCRIPTION, TARIF_CONDITIONS, TARIF_QTY, TARIF_PRICE_CHF, BOOKING_REF, EVENT_UID, EVENT_NAME, EVENT_DATE, BOOKING_SYSTEM_CODE, PARTNER_REFERENCE, STATE)
VALUES (5, 1, 'Row 10-13', 'Exchangeable, any row', 'Some conditions...', 1, 52.75, 'F32-FWFNSLD-532758', 'LKJSC-W1', 'Mama Mojo!', '2012-12-24 20:00:00', 'GEDE', 'GEDE-234233643322114', 'Printed');

INSERT INTO EVENTS_SEQUENCES(SEQ_NAME, SEQ_COUNT) VALUES ('ORDERS', 10);
INSERT INTO EVENTS_SEQUENCES(SEQ_NAME, SEQ_COUNT) VALUES ('ORDER_ITEM', 10);
INSERT INTO EVENTS_SEQUENCES(SEQ_NAME, SEQ_COUNT) VALUES ('ACCOUNT_ENTRY', 10);
INSERT INTO EVENTS_SEQUENCES(SEQ_NAME, SEQ_COUNT) VALUES ('PAYMENT', 10);
