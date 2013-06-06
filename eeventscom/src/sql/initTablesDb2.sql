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
USE SCALABOOK_ADMIN;

DELETE FROM CONFIGURATION;
DELETE FROM TICKET_VALIDATION;
DELETE FROM ROLE;
DELETE FROM USER;


INSERT INTO USER(EMAIL, PASSWORD, NAME)
VALUES ('lana@hippodrome-london.com', 'kS7IA7LOSeSlQQaNSVq1cA==', 'Lana Mills'); -- password is asdf

INSERT INTO ROLE(EMAIL, ROLE)
VALUES ('lana@hippodrome-london.com', 'validator'); -- this is a person who can validate tickets


INSERT INTO TICKET_VALIDATION(BOOKING_REF, USER_ID, THE_TIME)
VALUES ('F32-FWFNSLD-974258', 'lana@hippodrome-london.com', now());
INSERT INTO TICKET_VALIDATION(BOOKING_REF, USER_ID, THE_TIME)
VALUES ('F32-FWFNSLD-456687', 'lana@hippodrome-london.com', now());

INSERT INTO CONFIGURATION(KEY_, VALUE_)
VALUES ('validationUrl', 'http://localhost:8083/eeventscom/r/validation/');

