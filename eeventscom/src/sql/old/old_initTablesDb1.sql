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
DELETE FROM PAYMENT;
DELETE FROM ACCOUNT_BOOKING;
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

INSERT INTO EVENT(UID)
VALUES ('LKJSC-W1');

INSERT INTO EVENT(UID)
VALUES ('BWEIY-A4');

INSERT INTO RATING_USER_MAPPING (ID, EVENT_UID, USER_ID, RATING)
VALUES (1, 'LKJSC-W1', 2, 4);

INSERT INTO RATING (EVENT_UID, SUM_RATINGS, NUM_RATINGS)
VALUES ('LKJSC-W1', 44, 11);

INSERT INTO RATING (EVENT_UID, SUM_RATINGS, NUM_RATINGS)
VALUES ('BWEIY-A4', 3, 1);

INSERT INTO EVENTS_SEQUENCES(SEQ_NAME, SEQ_COUNT) VALUES ('RATING', 10);
INSERT INTO EVENTS_SEQUENCES(SEQ_NAME, SEQ_COUNT) VALUES ('RATING_USER_MAPPING', 10);

INSERT INTO COMMENT(ID, EVENT_UID, COMMENT, USER_ID, WHEN)
VALUES (1, 'BWEIY-A4', 'I cant wait to go to this concert, Ive been waiting years for them to come back to town!', 1, '2012-09-16 10:50:01');

INSERT INTO COMMENT(ID, EVENT_UID, COMMENT, USER_ID, WHEN)
VALUES (2, 'BWEIY-A4', 'Yeah, me neither.', 2, '2012-09-16 10:50:31');

INSERT INTO EVENTS_SEQUENCES(SEQ_NAME, SEQ_COUNT) VALUES ('COMMENT', 10);

INSERT INTO ORDERS(ID, UUID, USER_ID, WHEN, STATE)
VALUES (1, 'ASDF-FDAS-ASDF', 1, '2012-09-16 10:50:31', 'Paid');

INSERT INTO ORDER_ITEM(ID, ORDER_ID, TARIF_NAME, TARIF_QTY, TARIF_PRICE_CHF, BOOKING_REF, EVENT_UID, EVENT_NAME, EVENT_DATE, BOOKING_SYSTEM_CODE, PARTNER_REFERENCE, STATE)
VALUES (1, 1, 'Last Minute Saver R14-20', 3, 12.99, 'F32-FWFNSLD-347382', 'LKJSC-W1', 'Mama Mojo!', '2012-12-24 20:00:00', 'GEDE', 'GEDE-23423364363452', 'Printed');

INSERT INTO ORDER_ITEM(ID, ORDER_ID, TARIF_NAME, TARIF_QTY, TARIF_PRICE_CHF, BOOKING_REF, EVENT_UID, EVENT_NAME, EVENT_DATE, BOOKING_SYSTEM_CODE, PARTNER_REFERENCE, STATE)
VALUES (2, 1, 'Row 14-28', 1, 43.50, 'F32-FWFNSLD-456243', 'LKJSC-W1', 'Mama Mojo!', '2012-12-24 20:00:00', 'GEDE', 'GEDE-23423364364562', 'Printed');

INSERT INTO ORDER_ITEM(ID, ORDER_ID, TARIF_NAME, TARIF_QTY, TARIF_PRICE_CHF, BOOKING_REF, EVENT_UID, EVENT_NAME, EVENT_DATE, BOOKING_SYSTEM_CODE, PARTNER_REFERENCE, STATE)
VALUES (3, 1, 'R1-3', 3, 68.70, 'F32-FWFNSLD-456687', 'LKJSC-W1', 'Mama Mojo!', '2012-12-24 20:00:00', 'GEDE', 'GEDE-23423364364312', 'Validated');

INSERT INTO ORDER_ITEM(ID, ORDER_ID, TARIF_NAME, TARIF_QTY, TARIF_PRICE_CHF, BOOKING_REF, EVENT_UID, EVENT_NAME, EVENT_DATE, BOOKING_SYSTEM_CODE, PARTNER_REFERENCE, STATE)
VALUES (4, 1, 'Row 10-13', 1, 52.75, 'F32-FWFNSLD-974258', 'LKJSC-W1', 'Mama Mojo!', '2012-12-24 20:00:00', 'GEDE', 'GEDE-234233643322114', 'Validated');

INSERT INTO ORDER_ITEM(ID, ORDER_ID, TARIF_NAME, TARIF_QTY, TARIF_PRICE_CHF, BOOKING_REF, EVENT_UID, EVENT_NAME, EVENT_DATE, BOOKING_SYSTEM_CODE, PARTNER_REFERENCE, STATE)
VALUES (5, 1, 'Row 10-13', 1, 52.75, 'F32-FWFNSLD-532758', 'LKJSC-W1', 'Mama Mojo!', '2012-12-24 20:00:00', 'GEDE', 'GEDE-234233643322114', 'Printed');

INSERT INTO EVENTS_SEQUENCES(SEQ_NAME, SEQ_COUNT) VALUES ('ORDERS', 10);
INSERT INTO EVENTS_SEQUENCES(SEQ_NAME, SEQ_COUNT) VALUES ('ORDER_ITEM', 10);
INSERT INTO EVENTS_SEQUENCES(SEQ_NAME, SEQ_COUNT) VALUES ('ACCOUNT_BOOKING', 10);
INSERT INTO EVENTS_SEQUENCES(SEQ_NAME, SEQ_COUNT) VALUES ('PAYMENT', 10);
