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
package ch.maxant.scalabook.shop.orders.services.paymentpartner

import java.lang.System.{currentTimeMillis => now}
import java.util.logging.Logger

import scala.collection.JavaConversions.asScalaBuffer

import com.lapyap.ws.PaymentService

import ch.maxant.scalabook.shop.bom.Order
import ch.maxant.scalabook.shop.common.services.TechnicalException
import ch.maxant.scalabook.shop.util.Configuration
import javax.ejb.LocalBean
import javax.ejb.Startup
import javax.ejb.Stateless
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.inject.Named
import javax.xml.ws.BindingProvider
import javax.xml.ws.WebServiceRef

/**
 * this is based on standard java ee WS client injection
 */
@Stateless
@LocalBean
private[services] class PaymentPartnerService2 {

    @Inject var log: Logger = null
    @Inject var config: Configuration = null
    
    @WebServiceRef var service: PaymentService = null

    /** 
     * Before you can validate a token with the partner,
     * the client needs to create a payment.  in order to
     * create a payment, they need a generated token
     * which contains encrypted information about 
     * the sale.  This ensures that the client cannot
     * try and fake the amount that they pay, and provides
     * safety for the merchant that the payment was for 
     * the correct amount.
     */
    def generatePaymentToken(order: Order):String = {
        
        val start = now

        val description = "Ticket for " + order.bookings.map(_.eventName).mkString(",")

        try{
            val token = getPort().generateRequestToken(
                    config.c.merchantId,
                    order.uuid.toString /*our ref*/, 
                    order.getTotalPrice.value.toString,
                    order.getTotalPrice.currency.getCurrencyCode,
                    description)
            
            log.info("generated payment token in " + (now - start) + "ms")
            
            return token
        }catch{
            case e: Exception => 
            throw new TechnicalException("ERR-047",
                    "Payment Request for order " + order.uuid + 
                    " could not be created: " + e, e)
        }
    }

    /** checks the token with the parters web service to 
     *  determine if they sent the token */
    def isValidToken(order: Order, token: String): Boolean = {
        
        val start = now

        val description = "Ticket for " + order.bookings.map(_.eventName).mkString(",")

        try{

            val valid = getPort().validateToken(config.c.merchantId, order.uuid.toString, token)
            
            
            log.info("validated token in " + (now - start) + "ms")
            
            return valid
        }catch{
            case e: Exception => 
                throw new TechnicalException("ERR-056",
                    "Token for order " + order.uuid + 
                    " could not be validated: " + e, e)
        }
    }

    /** sets up the port right */
    private def getPort() = {
        val start = now
        val partner = service.getPaymentPort()
        log.info("Took " + (now - start) + " ms to get port")
        
        //set username and password.  and optionally set the URL.  normally, we just set the WSDL location, which contains the URL. see above.
        val ctx = partner.asInstanceOf[BindingProvider].getRequestContext();
        ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, config.c.paymentPartnerUrl + "Payment")
        ctx.put(BindingProvider.USERNAME_PROPERTY, "someUsername")
        ctx.put(BindingProvider.PASSWORD_PROPERTY, "somePassword")

        partner
    }
}

