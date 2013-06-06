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

import ch.maxant.scalabook.play20.plugins.rolebasedauth.Secured
import ch.maxant.scalabook.validation.integration.TicketGateway
import ch.maxant.scalabook.validation.integration.Ticket
import ch.maxant.scalabook.validation.integration.UserRepository
import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.Mode
import java.util.concurrent.atomic.AtomicLong

object TicketController extends Controller with MySecured with EventResponder {

    val EventUid = "eventUid"
    val BookingRef = "bookingRef"

    val runningSince = new java.util.Date()
    val resourceRequestCount = new AtomicLong
        
    /* ********************************************** *
     *    MISC
     * ********************************************** */
    def health = Action { 
        val xml = <health>
                <service>
                <name>{this.getClass.getName}</name>
                <status>OK</status>
                <freeMem>{Runtime.getRuntime.freeMemory}</freeMem>
                <totalMem>{Runtime.getRuntime.totalMemory}</totalMem>
                <maxMem>{Runtime.getRuntime.maxMemory}</maxMem>
                <availableProcessors>{Runtime.getRuntime.availableProcessors}</availableProcessors>
                <resourceRequests>{resourceRequestCount.get}</resourceRequests>
                <runningSince>{runningSince}</runningSince>
                <threadCount>{Thread.activeCount}</threadCount>
                <jvm-version>{System.getProperty("java.runtime.version")}</jvm-version>
                <scala-version>{scala.util.Properties.scalaPropOrElse("version.number", "unknown")}</scala-version>
                <scalaCompilerVersion>{scala.util.Properties.ScalaCompilerVersion}</scalaCompilerVersion>
                <scalaReleaseVersion>{scala.util.Properties.releaseVersion}</scalaReleaseVersion>
                <play.home>{System.getProperty("play.home")}</play.home>
                <play.version>{System.getProperty("play.version")}</play.version>
                </service>
                </health>
        Ok(xml)
    }
    
    def wait_ = Action {
        import play.api.Play.current
        if(play.api.Play.isDev){
            //for testing how play handles threads
            Thread.sleep(4000000)
        }
        Ok("Waited")
    }

    /* ********************************************** *
     *    EVENT RESPSONDER METHODS (Async Callbacks used to render the open bookings for an event)
     * ********************************************** */

    /** callback method for what to do when we successfully get tickets from the rest service */
    override def showSuccess(eventUid: String, tickets: Seq[Ticket], totalNumberOfTickets: Int, user: Option[UserRepository.User], request: Request[AnyContent]) = {

        //push this event to any listening browsers
        LiveEventController.publisher ! ValidationEvent(eventUid, tickets.size, totalNumberOfTickets)
                        
        Ok(views.html.ticketsInEvent(eventUid, tickets, user, eventSelectionForm)).withSession(request.session + (EventUid -> eventUid) )
    }
    
    /** callback method for what to do when we fail to get tickets from the rest service */
    override def showFailure(msg: String, user: Option[UserRepository.User]) = {
        BadRequest(views.html.error(msg, user))
    }

    /* ********************************************** *
     *    EVENT SELECTION
     * ********************************************** */
    
    def index = IsAuthorized(Roles.validator) { user => _ =>
        Ok(
            views.html.index(eventSelectionForm, user)
        )
    }

    def error = Action { implicit request => 
        val user = UserRepository.findByEmail(request.session.get(Secured.Username))
        Ok(views.html.error("some error", user))
    }

    val eventSelectionForm = Form(
        EventUid -> nonEmptyText
    )
    
    def selectEvent = IsAuthorized(Roles.validator) { user => implicit request =>
        val res2 = eventSelectionForm.bindFromRequest.fold(
            errors => {
                BadRequest(views.html.index(errors, user))
            },
            eventUid => {
                //old
                //Async {
                    //TicketGateway.getOpenBookings(eventUid, user, request, this, null)
                //}
                
                /*
                 * Thread before Async=   10527/play-akka.actor.default-dispatcher-13
                 * Thread in Async=       10527/play-akka.actor.default-dispatcher-13
                 * Thread in gateway=     10527/play-akka.actor.default-dispatcher-13
                 * Thread in wsResponse = 10526/ForkJoinPool-2-worker-5
                 * Thread in success=     10526/ForkJoinPool-2-worker-5
                 * 
                 * 
                 * Thread before Async=   11959/play-akka.actor.default-dispatcher-18
                 * Thread in Async=       11959/play-akka.actor.default-dispatcher-18
                 * Thread in gateway=     11959/play-akka.actor.default-dispatcher-18
                 * called gateway
                 * async done
                 * bind done
                 * Thread in wsResponse = 13336/ForkJoinPool-2-worker-9
                 * Thread in success=     13336/ForkJoinPool-2-worker-9
                 */
                
println("Thread before Async=" + Thread.currentThread.getId+"/"+Thread.currentThread.getName)
                val res = Async{
println("Thread in Async=" + Thread.currentThread.getId+"/"+Thread.currentThread.getName)
                    //new
                    import scala.util._
                    import scala.concurrent.ExecutionContext.Implicits.global
                    
                    //Future[scala.util.Try[Seq[Ticket]]] 
                    val tickets = TicketGateway.getOpenBookings2(eventUid)
println("called gateway")
    
                    tickets.map { t => 
                        t map { allTickets =>
println("Thread in success=" + Thread.currentThread.getId+"/"+Thread.currentThread.getName)
    
                            val totalNumTickets = allTickets.size
                            val printedTickets = allTickets.filter(t=> t.state.equals("Printed"))
    
                            Ok(
                                views.html.ticketsInEvent(eventUid, printedTickets, user, eventSelectionForm)
                            ).withSession(request.session + (EventUid -> eventUid) )
                            
                        } recover {
                            case ex: Exception => 
                                BadRequest(
                                    views.html.error(ex.getMessage, user)
                                )
                        } get
                    }
                }                
println("async done")

                res

            }
        )
println("bind done")
        
        res2
    }
}

