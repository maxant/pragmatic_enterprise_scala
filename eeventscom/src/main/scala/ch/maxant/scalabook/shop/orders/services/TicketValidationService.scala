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
package ch.maxant.scalabook.shop.orders.services

import javax.mail.{Session => MailSession}
import java.util.logging.Logger
import javax.jms.Message
import java.util.logging.Level
import javax.jms.TextMessage
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.Transport
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart
import javax.mail.Message.RecipientType
import javax.ejb.MessageDrivenContext
import javax.jms.ConnectionFactory
import javax.jms.Queue
import javax.jms.{Session => JMSSession}
import javax.ejb.TransactionManagement
import javax.ejb.TransactionManagementType
import javax.ejb.MessageDriven
import javax.ejb.ActivationConfigProperty
import javax.jms.MessageListener
import javax.inject.Inject
import javax.annotation.Resource
import javax.ejb.EJB
import scala.xml.XML
import java.io.ByteArrayInputStream

/**
 * this is the message driven bean which responds to 
 * messages send into the ticket validation q when transactions 
 * are committed.
 * 
 * note that the exact annotation configuration is container specific; 
 * below is what jboss requires.
 */
@MessageDriven(
        activationConfig=Array(new ActivationConfigProperty(propertyName="destination", propertyValue="java:/queue/ticketValidation") ),
        messageListenerInterface=classOf[MessageListener])
@TransactionManagement(TransactionManagementType.CONTAINER)
private[services] class TicketValidationService {

    @Inject var log: Logger = null
    @EJB var orderService: OrderService = null
    
    def onMessage(m: Message) {
        
    	log.log(Level.INFO, "Validating ticket...")
        try{
            val s = m.asInstanceOf[TextMessage].getText
            val xml = XML.load(new ByteArrayInputStream(s.getBytes("UTF-8")))
            val bookingRef = (xml \ "bookingReference").text
            log.log(Level.INFO, "Validating ticket " + bookingRef)
            orderService.validateOrderItem(bookingRef)
        }catch{
            case e: Exception => {
                log.log(Level.SEVERE, "Failed to validate ticket", e)
                //TODO add to workflow, for an admin to investigate...
            }
        }
    }
    
}

