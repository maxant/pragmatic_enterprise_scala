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
package ch.maxant.scalabook.shop.events.services.adapters.gede

import javax.ejb.Stateless
import javax.ejb.LocalBean
import ch.maxant.scalabook.shop.util.Conversions.{asPrice, JList}
import java.util.Date
import System.{currentTimeMillis => now}
import ch.maxant.scalabook.shop.bom.Price
import ch.maxant.scalabook.shop.bom.EventOffer
import ch.maxant.scalabook.shop.bom.EventTeaser
import ch.maxant.scalabook.shop.bom.Tarif
import ch.maxant.scalabook.shop.bom.Reservation
import ch.maxant.scalabook.shop.bom.Booking
import ch.maxant.scalabook.shop.bom.Address
import javax.ejb.Asynchronous
import javax.ejb.AsyncResult
import java.util.concurrent.Future
import javax.interceptor.Interceptors
import ch.maxant.scalabook.shop.util.Constants.{SUISSE, EUR, TEN_MINUTES_IN_MS}
import java.util.concurrent.atomic.AtomicInteger
import java.util.UUID
import scala.util.Random
import ch.maxant.scalabook.shop.events.services.EventsPartnerAdapter
import ch.maxant.scalabook.shop.events.services.GetEventsInterceptor
import ch.maxant.scalabook.shop.events.services.ReserveOfferInterceptor
import ch.maxant.scalabook.shop.orders.services.OrdersPartnerAdapter

/**
 * GEDE (Goldsmith, Ede & Damp Events)
 * is a partner who has a webservice
 * for selling tickets to events.
 * 
 * They model events somewhat differently
 * than we do in that they have an "event"
 * which is the same object over many dates
 * and when you get offers, you tell it which
 * date you want an offer for.
 * 
 * That makes our lives somewhat harder, because
 * their model doesn't directly map to ours.  
 * But the architecture can handle it, due
 * to the adapters it contains.
 * 
 * Additionally, GEDE has no concept
 * of "reservations".  You load a 
 * catalogue of available events,
 * choose a date and a tarif
 * and book it immediately.  If it 
 * fails (say a tarif has been sold out) 
 * then you simply try again, using a different
 * tarif.
 * 
 * In order to map their model to ours, we 
 * create a booking immediately when we reserve,
 * and have the responsibility to 
 * cancel it with GEDE, should the user 
 * not complete a booking and payment.
 * 
 * TODO call actual partner, rather than using a stub impl!
 */
@Stateless(name="GedeAdapterForEvents")
@LocalBean
private[services] class GedeAdapter extends EventsPartnerAdapter {

    override def bookingSystemCode = "GEDE"

    @Asynchronous
    @Interceptors(Array(classOf[GetEventsInterceptor]))
    override def getEvents(from: Date = null, to: Date = null): FLEO = {
        simulateRemoteWebServiceCall(150 + Random.nextInt(50))
        
        val t1 = new Tarif("TAS", "Some conditions...",
                "Exchangeable, any row",
                1, bookingSystemCode,
                new Price("19.20", EUR), 
                653, 0)
        
        val t2 = new Tarif("ERS", "Some conditions...",
        		"Non-Exchangeable, any row",
        		1, bookingSystemCode,
        		new Price("14.95", EUR), 
        		75, 0)
        
        val a = new Address("Le Lido", "Rue de Bourg 17", "Lausanne", "VD", "1003", SUISSE)
        
        val uid = "LKJSC-W1"

            val e1 = new EventOffer(1, uid, "Mama Mojo!", 
"""Tu quod Google nunc vertit ad et a Latine? Ago 
quod paucis tantum inveni, sed tamen puto vere frigus! 
Nunc si operatus ex hoc puzzle, committitur admirans 
quod alia has hoc libro continet. Esset sortem.
""",
                	"20120301_2000",
                	"LKJSC-W1.png", 
                	bookingSystemCode,
                	JList(t1, t2),
                	null,
                	a, getComments(uid))

        new AsyncResult(List(e1))
    }

    @Asynchronous
    @Interceptors(Array(classOf[GetEventsInterceptor]))
    override def getEvent(id: Int): FOEO = {
        
        //since this is a stub, we can cheat by simply calling the 
        //method to get all offers, and search for the relevant event.
        //altho the interface has futures, this local call will not 
        //be intercepted by the container to run in parallel.
        val matchingEvent = getEvents().get().find{ offer => 
            offer.id == id
        }
        new AsyncResult(matchingEvent)
    }

    @Interceptors(Array(classOf[ReserveOfferInterceptor]))
    override def reserveOffer(event: EventOffer, tarif: Tarif): Reservation = {
        simulateRemoteWebServiceCall(350 + Random.nextInt(50))
        
    	new Reservation(tarif, event.id, event.uid, 
    	        event.name, event.when,
    	        tarif.bookingSystem, "GEDE-" + now + ":" + counter.incrementAndGet, event.address)
    }
    
}