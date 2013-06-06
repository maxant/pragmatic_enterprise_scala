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
package ch.maxant.scalabook.shop.ui
import javax.inject.Named
import javax.annotation.PostConstruct
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import scala.collection.JavaConversions._
import ch.maxant.scalabook.shop.bom.Tarif
import ch.maxant.scalabook.shop.orders.services.OrderService
import javax.ejb.EJB
import javax.faces.context.FacesContext
import ch.maxant.scalabook.shop.orders.services.ReservationExpiredException
import javax.faces.application.FacesMessage
import ch.maxant.scalabook.shop.bom.Reservation
import javax.servlet.http.HttpServletResponse
import ch.maxant.scalabook.shop.util.Configuration
import javax.servlet.http.HttpServletRequest
import ch.maxant.scalabook.shop.bom.Order
import java.util.logging.Logger
import java.util.logging.Level
import ch.maxant.scalabook.shop.common.services.TechnicalException
import java.util.{List => JList}
import ch.maxant.scalabook.shop.bom.Booking
import java.security.Principal
import ch.maxant.scalabook.shop.orders.services.email.EmailService

@Named
@RequestScoped
class PaymentConfirmation extends WithFacesContext {

	@EJB var emailService: EmailService = null
    @EJB var orderService: OrderService = null
	@Inject var config: Configuration = null
    @Inject var model: Model = null
    @Inject var log: Logger = null
    @Inject var principal: Principal = null

    private var error: String = null
    private var title: String = "Order Failed"
    
    @PostConstruct
	def getOrderConfirmation() = {

        error = null

		val token = request.getParameter("TOKEN")

        try{
	    	
			if(model.order == null){
				//session has been lost somehow...
				throw new TechnicalException("ERR-046")
			}else{
		    	if(token == null){
		    		throw new TechnicalException("ERR-043")
		    	}else{
		    		//update model with the token, so that in the case of 
		    		//errors, everything can be analysed
	    			model.order = model.order.copy(paymentToken = token)

		    		//TODO the partner should return the price and ref number, so we can ensure the user (cracker) hasnt modified it!
		    		
		    		model.order = orderService.verifyPayment(model.order, token)
		    		title = "Order Complete"
		    	}
	    	}
	    	
	    }catch{
	        case e: TechnicalException => 
	        	log.log(Level.WARNING, "error during payment confirmation: " + e.code)
	            error = "Technical Error " + e.code
	            error = "Sorry, we encountered a technical error (" + e.code + ")"
	            if(model.order != null){
	            	error += " during the processing of order " + model.order.getUuid
	            }
	        	error += ".  Someone is looking into the problem."
	        	sendErrorMail(error, token)
	        case e: Exception => 
		        log.log(Level.SEVERE, "error during payment confirmation", e)
		        error = "Sorry, we encountered a technical error (ERR-044)"
		        error += ".  Someone is looking into the problem."
	            sendErrorMail(error, token)
	    }
	}
    
    private def sendErrorMail(msg: String, token: String) = {
        val customer = 
            if(principal == null){
                "not logged in"
            }else{
                principal.getName()
            }
        emailService.enqueueEmail(
                EmailService.ORDERS_DEPARTMENT,
                EmailService.ORDERS_DEPARTMENT,
                "Failed Payment",
                msg + "\r\nPayment Token: " + token + "\r\nCustomer: " + customer)
    }

	def getOrder() = model.order

	def getError() = error

	def getTitle() = title

}

