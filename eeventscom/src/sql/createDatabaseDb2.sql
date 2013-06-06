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
-- from a fresh install, mysql -u root -p mysql  //give password as '', ie no password

DROP DATABASE IF EXISTS SCALABOOK_ADMIN;
CREATE DATABASE SCALABOOK_ADMIN DEFAULT CHARSET UTF8;
USE MYSQL;
DELETE FROM USER WHERE USER = '';

UPDATE mysql.user SET Password=PASSWORD('password') WHERE User='root';
FLUSH PRIVILEGES;

USE SCALABOOK_ADMIN;
