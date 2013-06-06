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
import java.util.Date
import Constants._
import java.math.BigDecimal
import java.text.SimpleDateFormat
import ch.maxant.scalabook.shop.bom.Price

/**
 * standard implicit conversions, required throughout
 * the system.
 */
object Conversions {

    def JList[T](ts: T*)={
        val list = new java.util.ArrayList[T]()
        for(t <- ts){
            list.add(t)
        }
        list
    }
    
    implicit def asDateFromTechDateTime(s: String): Date = {
    	if(s == null || s == ""){
            null
        }else{
        	new SimpleDateFormat(DATE_FORMAT_TECH_DATE_TIME).parse(s)
        }
    }
    
    implicit def asDateFromDateOnly(s: String): Date = {
        if(s == null || s == ""){
            null
        }else{
        	new SimpleDateFormat(DATE_FORMAT_DATE_ONLY).parse(s)
        }
    }
    
    implicit def asPrice(s: String): Price = {
        new Price(
                new BigDecimal(s), 
                Constants.CHF)
    }

    implicit def asJavaBigDecimal(bd: scala.math.BigDecimal) = bd.bigDecimal
}