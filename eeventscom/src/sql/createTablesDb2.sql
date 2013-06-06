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

DROP TABLE IF EXISTS TICKET_VALIDATION;
DROP TABLE IF EXISTS ROLE;
DROP TABLE IF EXISTS USER;
DROP TABLE IF EXISTS CONFIGURATION;
DROP TABLE IF EXISTS SILLY_STATS;

-- ---------------------------------------------------------------
-- USER MANAGEMENT
-- ---------------------------------------------------------------

CREATE TABLE USER (
	EMAIL VARCHAR(255) NOT NULL, 
	PASSWORD VARCHAR(255) NOT NULL, 
	NAME VARCHAR(255) NOT NULL, 
	PRIMARY KEY (EMAIL)
) ENGINE = INNODB;

-- role uses a sequence in table user_sequence
CREATE TABLE ROLE (
	ROLE VARCHAR(255) NOT NULL, 
	EMAIL VARCHAR(255) NOT NULL, 
	FOREIGN KEY (EMAIL) REFERENCES USER(EMAIL)
) ENGINE = INNODB;

CREATE INDEX IDX_ROLE_EMAIL ON ROLE(EMAIL);

-- table of ticket validations.  interesting, no pk!  :-)
CREATE TABLE TICKET_VALIDATION (
	BOOKING_REF VARCHAR(255) NOT NULL, 
	USER_ID VARCHAR(255) NOT NULL,
	THE_TIME TIMESTAMP NOT NULL,
	PRIMARY KEY (BOOKING_REF), -- cant allow a ticket number to be validated twice!
	FOREIGN KEY (USER_ID) REFERENCES USER(EMAIL)
) ENGINE = INNODB;

-- table of config
CREATE TABLE CONFIGURATION (
	KEY_ VARCHAR(255)  NOT NULL, 
	VALUE_ VARCHAR(255) NOT NULL,
	PRIMARY KEY(KEY_)
) ENGINE = INNODB;

-- a dumb table for demonstrating when TLS doesn't work
CREATE TABLE SILLY_STATS (
    NAME VARCHAR(255)  NOT NULL, 
    VAL VARCHAR(255) NOT NULL
) ENGINE = INNODB;

