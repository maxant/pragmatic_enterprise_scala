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

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import ch.maxant.scalabook.validation.integration.TicketGateway
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Iteratee
import play.api.mvc.Controller
import play.api.mvc.WebSocket

object LiveEventController extends Controller with MySecured {

    val publisher = Akka.system.actorOf(Props[Publisher])
    
    def registerWebsocket(eventId: String) = WebSocket.using[String] { request =>
        println("adding websocket client for " + eventId)

        val (out, channel) = Concurrent.broadcast[String] // this can be shared

        publisher ! Add(eventId, channel)

        val in = Iteratee.foreach[String](e => Unit /*we dont expect any messages to be sent from the client*/).mapDone { _ =>
            println("removing websocket client")
            publisher ! Remove(eventId, channel)
        }
        
        (in, out)
    }

    def showPage(eventId: String) = IsAuthorized(Roles.validator) { user => implicit request =>
        
        import scala.concurrent.ExecutionContext.global
/*        
        Async {
            val tickets = TicketGateway.getOpenBookings2(eventId)
            tickets.map { t => 
                t map { allTickets =>
                    val totalNumTickets = allTickets.size
                    val printedTickets = allTickets.filter(t=> t.state.equals("Printed"))
                    Ok(
                        views.html.monitorEvent(
                            eventId, 
                            user, 
                            (printedTickets.size, totalNumTickets - printedTickets.size)
                        )
                    )
    
                } recover {
                    case ex: Exception => 
                        BadRequest(
                            views.html.error(ex.getMessage, user)
                        )
                } get
            }
        }
*/        
        TicketGateway.getEventStats(eventId){ (a, b) =>
            Ok(views.html.monitorEvent(eventId, user, (a, b)))
        }
        
    }
    
}

class Publisher extends Actor {
    
    private val clients = new HashMap[String, ListBuffer[Concurrent.Channel[String]]]()

    def receive = {

        case e: ValidationEvent => {
        	Logger.info("ticket validated: " + e.eventId + ":" + e.validated + "/" + e.total)
			clients.getOrElse(e.eventId, Nil).foreach { channel =>
        		Logger.info("pushing event to browser")
        		val msg = (e.total-e.validated) + "/" + e.validated
        		
        		channel.push(msg) 

	            Logger.info("pushed '" + msg + "'")
	        }
        }

        case e: Add => {
	        val clientsForEvent = clients.getOrElseUpdate(e.eventId, new ListBuffer[Concurrent.Channel[String]]())
	        clientsForEvent += e.channel
        	Logger.info("added ws client")
        }

        case e: Remove => {
            clients(e.eventId) -= e.channel
    		Logger.info("removed ws client")
        }
    }
    
}

sealed abstract class Event
private case class Add(eventId: String, channel: Concurrent.Channel[String]) extends Event
private case class Remove(eventId: String, channel: Concurrent.Channel[String]) extends Event
case class ValidationEvent(eventId: String, validated: Int, total: Int) extends Event
