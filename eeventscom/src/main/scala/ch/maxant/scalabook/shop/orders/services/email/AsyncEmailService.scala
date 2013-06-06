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
package ch.maxant.scalabook.shop.orders.services.email

import java.lang.System.{currentTimeMillis => now}
import java.util.logging.Level
import java.util.logging.Logger
import javax.annotation.Resource
import javax.ejb.MessageDriven
import javax.ejb.MessageDrivenContext
import javax.ejb.TransactionManagement
import javax.ejb.TransactionManagementType
import javax.ejb.ActivationConfigProperty
import javax.inject.Inject
import javax.jms.ConnectionFactory
import javax.jms.Message
import javax.jms.MessageListener
import javax.jms.Queue
import javax.jms.TextMessage
import javax.mail.Message.RecipientType
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import ch.maxant.scalabook.shop.util.Constants._

/**
 * this is the message driven bean which responds to 
 * messages send into the mail q when transactions 
 * are committed.  this bean processes those
 * messages and sends emails for them.
 * it is transactional, so that if an exception occurs, the
 * message is not lost.
 * 
 * note that the exact annotation configuration is container specific; 
 * below is what jboss requires.
 */
@MessageDriven(
        activationConfig=Array(new ActivationConfigProperty(propertyName="destination", propertyValue="java:/queue/mail") ),
        messageListenerInterface=classOf[MessageListener])
@TransactionManagement(TransactionManagementType.CONTAINER)
private[email] class AsyncEmailService {

    @Inject var log: Logger = null
    @Resource(name="java:jboss/mail/Default") var session: javax.mail.Session = null
    @Resource var ctx: MessageDrivenContext = null
    @Resource(mappedName="java:/JmsXA") var cf: ConnectionFactory = null
    @Resource(mappedName="java:/queue/mail") var mailq: Queue = null

    def onMessage(m: Message) {
        
        try{
        	log.log(Level.INFO, "sending email: " + m);
        	sendMail(m)
	        log.log(Level.INFO, "sent email: " + m);
        }catch{
            case e: Exception => {
                if(m.getIntProperty(AsyncEmailService.Retries) < 10){
                	log.log(Level.WARNING, "failed but retrying to send email: " + e.getMessage, e);
                	try{

                	    //schedule for some time in the future
                	    m.setLongProperty("JMS_JBOSS_SCHEDULED_DELIVERY", now + ONE_HOUR_IN_MS)
                	    
                	    resend(m) //send to back 
                	}catch{
                	    case e: Exception => ctx.setRollbackOnly //everything failed, lets try again
                	}
                }else{
                    //time to give up
                	log.log(Level.SEVERE, "failed and given up sending email: " + e.getMessage, e);
                }
            }
        }
    }
    
    private def sendMail(m: Message) {
        val tm = m.asInstanceOf[TextMessage]
        val body = tm.getText
        val to = new InternetAddress(tm.getStringProperty(AsyncEmailService.To))
        val from = new InternetAddress(tm.getStringProperty(AsyncEmailService.From))
        val subject = tm.getStringProperty(AsyncEmailService.Subject)

        val messageBodyPart = new MimeBodyPart()
		messageBodyPart.setContent(body, "text/html");
		val multipart = new MimeMultipart("related");
		multipart.addBodyPart(messageBodyPart);

        val msg = new MimeMessage(session)
        msg.setText(body)
        msg.setFrom(from)
        msg.setSender(from)
        msg.setSubject(subject)
        msg.setContent(multipart)
		msg.addRecipient(RecipientType.TO, to);
        msg.addRecipient(RecipientType.BCC, new InternetAddress("scalabook@maxant.co.uk"));
        
        Transport.send(msg, Array(to))
    }
    
    private def resend(m: Message) {
        m.setIntProperty(AsyncEmailService.Retries, m.getIntProperty(AsyncEmailService.Retries)+1)
        send(m, mailq)
    }

    private def send(m: Message, q: Queue) {
        val c = cf.createConnection
        val s = c.createSession(true, javax.jms.Session.AUTO_ACKNOWLEDGE)
        val p = s.createProducer(q)
        
        p.send(m)
    }
   
}

object AsyncEmailService {
    val To = "maxant_to"
	val From = "maxant_from"
	val Subject = "maxant_subject"
	val Retries = "maxant_retries"
}