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
package ch.maxant.scalabook.shop.util
import java.util.Currency
import javax.inject.Named
import javax.enterprise.context.ApplicationScoped
import java.util.Date
import java.util.Locale

object Constants {

    //TODO turn these into proper scala constants - camel case with capital first letter!  see SO.
    
    val CHF = Currency.getInstance("CHF")
	val EUR = Currency.getInstance("EUR")

    val DATE_FORMAT_DATE_AND_TIME = "dd. MMM yyyy, HH:mm"
    val TIME_FORMAT_INCL_MS = "HH:mm:ss.SSS"
    val DATE_TIME_FORMAT_INCL_MS = "dd/MM/yyyy HH:mm:ss.SSS"
    val DATE_FORMAT_DATE_ONLY = "dd/MMM/yyyy"
    val DATE_FORMAT_TECH_DATE_TIME = "yyyyMMdd_HHmm"
        
    val TWENTY_SECONDS_IN_MS = 20 * 1000
    val ONE_MINUTE_IN_MS = 60 * 1000L
    val TEN_MINUTES_IN_MS = 10 * ONE_MINUTE_IN_MS
    val ONE_HOUR_IN_MS = 60 * ONE_MINUTE_IN_MS
    val ONE_DAY_IN_MS = 24 * ONE_HOUR_IN_MS
    val ONE_YEAR_IN_MS = 365 * ONE_DAY_IN_MS

    def inOneYear = new Date(System.currentTimeMillis() + ONE_YEAR_IN_MS)

    val SUISSE = new Locale("fr", "CH")
    val SCHWEIZ = new Locale("de", "CH")

    val UTF8 = "UTF-8"
}