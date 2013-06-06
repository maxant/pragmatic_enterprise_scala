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
package ch.maxant.scalabook.shop.orders.services.accounting
import javax.ejb.Stateless
import javax.ejb.LocalBean
import javax.inject.Inject
import java.util.logging.Logger
import javax.enterprise.event.Observes
import javax.enterprise.event.TransactionPhase
import ch.maxant.scalabook.shop.bom.Order
import java.text.SimpleDateFormat
import ch.maxant.scalabook.shop.util.Constants._
import java.util.Date
import javax.ejb.EJB
import ch.maxant.scalabook.shop.orders.services.email.EmailService

@Stateless
@LocalBean
private[accounting] class CompensationService {
    
    @Inject var log: Logger = null
	@EJB var emailService: EmailService = null

    def handleFailedOrderCompletion(@Observes(during = TransactionPhase.AFTER_FAILURE) failedOrder: Order) = {
        //the problem here, is that the user has just paid
        //for an order, but we could not commit the result
        //to the database and so they have nothing in 
        //their account, and they have no email.
        //we need to manually check that the payment
        //has completed, and then manually update
        //the status of their order items, so that
        //they can print them.  we then need to email
        //them the link, so that they can download the 
        //tickets.
        
        //TODO what transaction context is used here?
        //there may well be none, and anyway, don't 
        //rely on being able to save anything here - 
        //its quite possible that you wont be able to, 
        //because a transaction just failed!

        //TODO could we use an observes with a during = AFTER_SUCCESS to send the email? instead of pushing it into a JMS queue which gets committed with the DB? => no, because if we fail after TX commit, the email will never be sent
        
        //TODO
        val sdf = new SimpleDateFormat(DATE_TIME_FORMAT_INCL_MS)
        emailService.enqueueEmail(
                EmailService.ORDERS_DEPARTMENT,
                EmailService.ORDERS_DEPARTMENT,
                "Order Completion Failed",
                "Order " + failedOrder.uuid + " failed.  " +
                		"Please complete it manually after checking payment.  " +
                		"See log, around timestamp " + sdf.format(new Date()) + ".  " +
        				"Payment Reference was: " + failedOrder.paymentToken)
    }
   
}