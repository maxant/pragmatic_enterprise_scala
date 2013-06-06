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
package ch.maxant.scalabook.shop.web

import java.util.{List => JList}

import scala.collection.JavaConversions.seqAsJavaList

import ch.maxant.scalabook.shop.orders.services.OrderService
import ch.maxant.scalabook.shop.util.Conversions.asJavaBigDecimal
import javax.ejb.EJB
import javax.jws.WebMethod
import javax.jws.WebParam
import javax.jws.WebService
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType.FIELD
import javax.xml.bind.annotation.XmlType

/**
 * Provides access to sales data for requesters in the 
 * role "validator", ie. people who scan tickets 
 * and validate them.
 * 
 * call me at: http://localhost:8080/eeventscom/ValidationServiceSoap
 */
@WebService
class ValidationServiceSoap {

    @EJB private val orderService: OrderService = null

    /** use a JList here, since JAX-B 
     * doesnt work with Scala collections! */
	@WebMethod
    def getOrderItems(@WebParam(name="eventUid") eventUid: String): JList[SoapOrderItem] = {
	    
    	orderService.getOrderItems(eventUid).filterNot(_.tarifName == "Insurance").map { b => 
    	    val price = new SoapPrice(b.tarifPriceCHF, "CHF")
    	    new SoapOrderItem(b.bookingRef, b.tarifName, price, b.qty, b.state, b.partyName)
    	}
	}
}

@XmlAccessorType(FIELD)
@XmlType(name="OrderItem")
case class SoapOrderItem(

        /** an example where I want to specify something more complex... */
		@XMLHelp.XmlElement(name="bookingRefNumber", required=true, nillable=false)
        bookingReferenceNumber: String,
        
        tarifName: String,

        tarifPrice: SoapPrice,
        
        qty: Int,
        
        state: String,
        
		customerName: String
){
    def this() = this(null, null, null, 0 , null, null)
}

@XmlAccessorType(FIELD)
@XmlType(name="Price")
case class SoapPrice(

        value: java.math.BigDecimal,
        
        currencyCode: String
){
    def this() = this(null, null)
}
