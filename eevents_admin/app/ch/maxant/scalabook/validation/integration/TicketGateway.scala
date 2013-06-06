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
package ch.maxant.scalabook.validation.integration

import play.api.Play.current
import java.util.Date
import System.{currentTimeMillis => now}
import play.api.libs.ws.WS
import ch.maxant.scalabook.validation.Config
import play.api.mvc.Results._
import javax.sql.DataSource
import ch.maxant.scalabook.play20.plugins.xasupport._
import play.api.Logger
import javax.jms.Session
import UserRepository.User
import javax.naming.InitialContext
import javax.jms.QueueConnectionFactory
import javax.jms.Queue
import javax.jms.TextMessage
import java.util.Hashtable
import javax.naming.Context
import org.hornetq.api.core.TransportConfiguration
import org.hornetq.api.core.DiscoveryGroupConfiguration
import java.util.HashMap
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory
import javax.jms.XAQueueConnectionFactory
import javax.jms.XAQueueConnection
import javax.jms.QueueConnection
import bitronix.tm.resource.jms.JndiXAConnectionFactory
import play.api.libs.ws.Response
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import controllers.EventResponder
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise
import scala.concurrent.Future
import play.api.mvc.Result
import play.api.mvc.AsyncResult

object TicketGateway extends TicketAdapter {

	/** Used to get information about tickets, from the main system using a REST call, async.  As such, it needs callbacks. */
	def getOpenBookings(eventUid: String, user: Option[UserRepository.User], request: Request[AnyContent], callback: EventResponder, bookingRefToIgnore: String): Future[Result] = {
	    getOpenBookings(eventUid, user, request, callback.showSuccess _, callback.showFailure _, bookingRefToIgnore)
	}

	/** a successful attempt at doing async programming with futures rather than callbacks */
    override def getOpenBookings2(eventUid: String): Future[scala.util.Try[Seq[Ticket]]] = {
println("Thread in gateway=" + Thread.currentThread.getId+"/"+Thread.currentThread.getName)
        
        import scala.util.Failure
        import scala.util.Success
        import scala.util.Try
        
        val url = Config.ValidationUrl + "bookings/" + eventUid
        Logger.debug("getting bookings from " + url)
        
        //the following call is asynchronous.  We are providing a callback which 
        //gets called when a response arrives.
        WS.url(url).get().map{ wsResponse =>
            try{
println("Thread in wsResponse =" + Thread.currentThread.getId+"/"+Thread.currentThread.getName)

                //at some time in the future, a response has arrived! parse it, and prepare the 
                //response to the browser.
                if(200 == wsResponse.status){
                    val tickets = convertXmlToTickets(wsResponse.xml)
                    Success(tickets)
                }else{
                    Failure(new Exception("Invalid response: " + wsResponse.status + "/" + wsResponse.body))
                }        
            }catch{
                case e: Exception => Failure(e)
            }
        }
   }

	/** 
	 * Potentially a more functional way to get the open bookings, 
	 * since this takes functions as parameters for the callback
	 * methods, rather than a callback object. 
	 */
	def getOpenBookings(
	        eventUid: String, 
	        user: Option[UserRepository.User], 
	        request: Request[AnyContent], 
	        success: (String, Seq[Ticket], Int, Option[UserRepository.User], Request[AnyContent]) => play.api.mvc.Result, 
	        failure: (String, Option[UserRepository.User]) => play.api.mvc.Result,
	        bookingRefToIgnore: String):Future[Result] = {
	    val url = Config.ValidationUrl + "bookings/" + eventUid
	    Logger.debug("getting bookings from " + url)
	    
	    //the following call is asynchronous.  We are providing a callback which 
	    //gets called when a response arrives.
		WS.url(url).get().map{ wsResponse =>
	        getBookings(eventUid, user, request, bookingRefToIgnore, success, failure)(wsResponse)
	    }
	}

    /** a callback for getting bookings shown in the ticketsInEvent page. uses currying! */
    private def getBookings(eventUid: String, 
            				user: Option[UserRepository.User], 
            				request: Request[AnyContent], 
            				bookingRefToIgnore: String, 
					        success: (String, Seq[Ticket], Int, Option[UserRepository.User], Request[AnyContent]) => play.api.mvc.Result, 
					        failure: (String, Option[UserRepository.User]) => play.api.mvc.Result
        				)
						(response: play.api.libs.ws.Response)
						:play.api.mvc.Result = {

        try{
		    //at some time in the future, a response has arrived! parse it, and prepare the 
		    //response to the browser.
	        if(200 == response.status){
				var tickets = convertXmlToTickets(response.xml)
				val totalNumberOfTickets = tickets.size
	        	//remove the one just validated, if present, because even though we validated it before calling the REST service,
	        	//JBoss may not have had a chance to process the JMS message.  It will, when it can, but that may be after the 
	        	//REST call.
	        	tickets = tickets.filter(t=> t.state.equals("Printed") && t.bookingReference != bookingRefToIgnore)
	        	success(eventUid, tickets, totalNumberOfTickets, user, request)
	        }else{
	        	failure("Invalid response: " + response.status + "/" + response.body, user)
	        }        
        }catch{
            case e: Exception => failure(e.getMessage, user)
        }
    }
    
    private def convertXmlToTickets(xml: scala.xml.Elem) = {
		for(t <- xml.child filterNot (_.toString.trim == "")) yield {
	    	new Ticket(
	    	        (t \ "@bookingRef").text, 
	    	        (t \ "tarifName").text, 
	    	        (t \ "qty").text.toInt, 
	    	        BigDecimal((t \ "tarifPriceCHF").text), 
	    	        (t \ "state").text,
	    	        (t \ "customerName").text
    	        )
		}
    }
    
	/** adds the given validation and marks the order item as having been validated */
	override def addValidation(user: User, bookingRef: String)(implicit ctx: XAContext) = {

        val xml = 
            <ticketValidation>
        		<bookingReference>{bookingRef}</bookingReference>
	    		<validatorId>{user.email}</validatorId>
        	</ticketValidation>

		val qcf = ctx.lookupCF("jms/maxant/scalabook/ticketvalidations")
		val qc = qcf.createConnection("ticketValidation","password")
		val qs = qc.createSession(false, Session.AUTO_ACKNOWLEDGE)
		val q = qs.createQueue("ticketValidationQueue") //val q = ctx.lookup(QUEUE).asInstanceOf[Queue]
		val sender = qs.createProducer(q)
		val m = qs.createTextMessage(xml.toString)
		sender.send(m)
		sender.close
		qs.close
		qc.close
	}	

	/** here we wont use callbacks, because they can be painful.  lets just wait for the result.  this method isnt called often, so it shouldnt hurt us! */
	def getEventStats(eventUid: String)(f: (Int, Int) => Result): AsyncResult = {
	    val url = Config.ValidationUrl + "bookings/" + eventUid
	    Async {
            WS.url(url).get().map{ wsResponse =>
                if(200 == wsResponse.status){
                    var tickets = convertXmlToTickets(wsResponse.xml)
                    val totalNumberOfTickets = tickets.size
                    val numberOfOpenTickets = tickets.filter(t=> t.state.equals("Printed")).size
                    f(numberOfOpenTickets, (totalNumberOfTickets - numberOfOpenTickets))
                }else{
                    f(1,1)
                }
            }       
	    }
	}
}

