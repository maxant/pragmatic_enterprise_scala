@echo off

rem <!--
rem  *   Copyright 2013 Ant Kutschera
rem  *   
rem  *   Licensed under the Apache License, Version 2.0 (the "License");
rem  *   you may not use this file except in compliance with the License.
rem  *   You may obtain a copy of the License at
rem  *
rem  *       http://www.apache.org/licenses/LICENSE-2.0
rem  *
rem  *   Unless required by applicable law or agreed to in writing, software
rem  *   distributed under the License is distributed on an "AS IS" BASIS,
rem  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem  *   See the License for the specific language governing permissions and
rem  *   limitations under the License.
rem -->

set PROJECT_DIR=W:\training6_scalabook\workspace\eeventscom\
set JAVA_HOME=t:\jdk1.7.0_b147_32bit
set WSDL_URL=http://localhost:8080/PaymentPartner/Payment?wsdl

echo Generating WS client for WSDL at %WSDL_URL%

cd /D %PROJECT_DIR%
rmdir /S /Q src\gen\java
mkdir src\gen\java

rem WSDL is in bindings.xml too!!

%JAVA_HOME%\bin\wsimport.exe -verbose -s src/gen/java -b bindings.xml -d WebContent\WEB-INF\classes %WSDL_URL%
rem t:\apache-cxf-2.6.2\bin\wsdl2java.bat -client -b bindings.xml -verbose -d src/gen/java %WSDL_URL%

echo Please refresh your Eclipse project now.
pause