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
package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import ch.maxant.scalabook.validation.integration.UserRepository
import ch.maxant.scalabook.validation.integration.TicketGateway
import ch.maxant.scalabook.validation.integration.Ticket
import ch.maxant.scalabook.validation.integration.ValidationRepository
import java.util.UUID
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.libs.concurrent.Promise
import scala.xml.Elem
import play.api.libs.concurrent.Redeemable
import ch.maxant.scalabook.play20.plugins.xasupport.XASupport.withXaTransaction
import ch.maxant.scalabook.validation.Config
import play.api.libs.ws.WS
import scala.xml.NodeSeq
import java.util.Date
import org.apache.commons.codec.binary.Base64
import ch.maxant.scalabook.validation.webservice.TicketValidation
import ch.maxant.scalabook.validation.webservice.TicketValidationBodyParser
import ch.maxant.scalabook.validation.webservice.TicketValidationWebService
import ch.maxant.scalabook.play20.plugins.rolebasedauth.Secured
import scala.util._
import ch.maxant.scalabook.validation.integration.ValidationRepo
import ch.maxant.scalabook.validation.integration.TicketAdapter
import scala.concurrent.ExecutionContext.Implicits.global
import ch.maxant.scalabook.play20.plugins.soap.SoapMessage
import scala.concurrent.Future

object ValidationController extends Controller with MySecured {

    var validationRepository: ValidationRepo = ValidationRepository
    var ticketGateway: TicketAdapter = TicketGateway

    val EventUid = "eventUid"
   	val BookingRef = "bookingRef"

    /* ********************************************** *
     *    EVENT VALIDATION WEB SITE
     * ********************************************** */

    val eventValidationForm = Form(
        BookingRef -> nonEmptyText
    )

    /** for validation via website */
    def validateTicket = IsAuthorized(Roles.validator) { user => implicit request =>
println("request " + System.identityHashCode(request))
println("session " + request.session)
println("headers " + request.headers)
println("queryString " + request.queryString)
println(request.getQueryString(BookingRef))
        val eventUid = request.session.get(EventUid)
println("eventUid " + eventUid)
        eventValidationForm.bindFromRequest.fold(
			errors => {
println("errors ")
				BadRequest(views.html.ticketsInEvent(eventUid.getOrElse("asdf"), null, user, errors))
			},
			bookingRef => {
println("got booking ref " + bookingRef)
				Async {	
                    val appUser = securedUser2ApplicationUser(user)
                    val validation = Akka.future {
                        doTicketValidation(appUser.get, bookingRef)
                    } // end future
                    
                    val tickets = ticketGateway.getOpenBookings2(eventUid.getOrElse("asdf"))
                    
                    validation.zip(tickets).map{ pair =>
                        
                        pair._1 match {
                            case Success(s) => {
                                pair._2 map { allTickets =>
                                    prepareSuccessfulHtmlResponse(allTickets, bookingRef, eventUid.getOrElse("asfd"), appUser)
                                } recover {
                                    case ex: Exception => 
                                        BadRequest(
                                            views.html.error(ex.getMessage, user)
                                        )
                                } get
                            }
                            case Failure(ex) => {
                                BadRequest(
                                    views.html.error(ex.getMessage, user)
                                )
                            }
                        } //end match
                    } // end map all tickets

				}
			}
		)
    }

    /* ********************************************** *
     *    EVENT VALIDATION SOAP MESSAGE
     * ********************************************** */

	/** gets the wsdl for the service */
	def validateTicketSoapWsdl = Action { request => 
        Ok( TicketValidationWebService.wsdl )
    }
    
    def validateTicketSoap = Action(TicketValidationWebService.validationParser(Roles.validator)) { request => 
println("a " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
        val ret2 = Async {
println("b " + Thread.currentThread().getId() + " " + System.currentTimeMillis)

            request.body match {
                case SoapMessage("validateTicket", message) => {
                    handleValidateTicketSoap(message)
                }
                case SoapMessage(method, message) => {
                    val ex = new Exception("Unexpected operation " + method + " with message " + message)
                    val xml = TicketValidationWebService.mapError(ex)
                    Future(InternalServerError(xml))
                }
            }
        } // end async

println("t " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
    
        ret2
    } // end method

    private def handleValidateTicketSoap(message: TicketValidation) = {
        val eventUid = message.eventUid
        val ref = message.referenceNumber
        val user = message.user

        val validation = Akka.future {
            doTicketValidation(user, ref)
        } // end future
println("i " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
            
        val tickets = ticketGateway.getOpenBookings2(eventUid)
println("j " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
            
        val ret = validation.zip(tickets).map{ pair =>
println("k " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
                
            pair._1 match {
                case Success(s) => {
println("l " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
                    pair._2 map { allTickets =>
println("m " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
                        prepareSuccessfulXmlResponse(allTickets, ref, eventUid)
                    } recover {
                        case ex: Exception => 
println("q " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
                            val xml = TicketValidationWebService.mapError(ex)
                            InternalServerError(xml)
                    } get
                }
                case Failure(ex) => {
println("r " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
                    val xml = TicketValidationWebService.mapError(ex)
                    InternalServerError(xml)
                }
            } //end match
        } // end map all tickets
println("s " + Thread.currentThread().getId() + " " + System.currentTimeMillis)

        ret
    } // end method

    private def doTicketValidation(user: UserRepository.User, ref: String) = {
println("c " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
                try{
                    withXaTransaction { implicit ctx =>
println("d " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
                        validationRepository.addValidation(ref, user)
println("e " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
                        ticketGateway.addValidation(user, ref)
println("f " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
                        Success(null)
                    }
                }catch{
                    //the following exceptions are thrown in the withXaTransaction block, so can be handled here. 
                    case e: java.sql.SQLIntegrityConstraintViolationException => {
println("g " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
                        val message = "This ticket has already been validated!! Stop the customer and refer them to the desk."
                        Logger.warn(message + "/" + e.getMessage)
                        Failure(new Exception(message))
                    }
                    case t: Throwable => {
println("h " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
                        Logger.error("Failed to validate ticket " + ref, t)
                        Failure(t)
                    }
                } // end catch
    }

    private def prepareSuccessfulXmlResponse(allTickets: Seq[Ticket], ref: String, eventUid: String) = {
        val stats = prepareSuccessfulResponse(allTickets, ref, eventUid)

        val ticketsXml = <tickets><unscanned>{stats._2.size}</unscanned><total>{stats._1}</total></tickets>

        val xml = TicketValidationWebService.mapResult(true, ticketsXml)
println("p " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
        Ok(xml)
    }    

    private def prepareSuccessfulHtmlResponse(allTickets: Seq[Ticket], ref: String, eventUid: String, user: Option[UserRepository.User]) = {
        val stats = prepareSuccessfulResponse(allTickets, ref, eventUid)

        Ok(
            views.html.ticketsInEvent(eventUid, stats _2, user, TicketController.eventSelectionForm)
        )
    }    

    private def prepareSuccessfulResponse(allTickets: Seq[Ticket], ref: String, eventUid: String) = {
        val totalNumTickets = allTickets.size
                            
        //exclude the one that was just validated
        val printedTickets = allTickets.filter{t=> 
            t.state.equals("Printed") && 
            t.bookingReference != ref
        }

println("n " + Thread.currentThread().getId() + " " + System.currentTimeMillis)
        
        //push this event to any listening browsers
        LiveEventController.publisher ! ValidationEvent(eventUid, printedTickets.size, totalNumTickets)
        
println("o " + Thread.currentThread().getId() + " " + System.currentTimeMillis)

        (totalNumTickets, printedTickets)
    }    
    
    
    
    
    
    
//ignore the following, it was used in developing the withXATransaction plugin
//    def do2pc = {Action { 
//		XASupport.tm.begin
//		val t = XASupport.tm.getCurrentTransaction
//		
//		try{
//			val ctx = new XAContext()
//	        val ticketDS = ctx.lookupDS("jdbc/maxant/scalabook/ab")
//	        val validationDS = ctx.lookupDS("jdbc/maxant/scalabook/ticketvalidationsMysql")
//
//		    val c1 = ticketDS.getConnection()
//		    c1.setAutoCommit(false)
//		    ctx.connsToClose += c1
//		    val c2 = validationDS.getConnection()
//		    c2.setAutoCommit(false)
//		    ctx.connsToClose += c2
//
//		    //val s1 = c1.prepareStatement("update order_item set state = 'Validated' where booking_ref = 'F32-FWFNSLD-532758'")
//		    val s1 = c1.prepareStatement("insert into temp values ('a', 'b')")
//		    s1.executeUpdate()
//		    s1.close()
//println("a")	        
//		    val s2 = c2.prepareStatement("insert into ticket_validation (booking_ref, the_time, user_id) values('F32-FWFNSLD-532758', current_timestamp(), '5')")
//println("b")
//		    s2.executeUpdate()
//println("c")
//		    s2.close()
//println("d")
//
//		    ctx.connsToClose.foreach(c => c.close)
//println("e")
//		    
//		    t.commit
//println("f")
//		}catch{
//		    case e: Exception => {
//println("99")
//		    	t.rollback
//		    	println("\r\n\r\n" + e.getMessage)
//		    }
//		}
//println("200")		
//		Ok(views.html.index(eventSelectionForm, None))
//    }}
    
}

