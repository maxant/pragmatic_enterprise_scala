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
package ch.maxant.scalabook.shop.events.services.adapters.adff
import javax.ejb.Stateless
import javax.ejb.LocalBean
import java.util.Date
import ch.maxant.scalabook.shop.bom.Tarif
import ch.maxant.scalabook.shop.bom.Reservation
import ch.maxant.scalabook.shop.bom.Booking
import ch.maxant.scalabook.shop.bom.Address
import ch.maxant.scalabook.shop.bom.EventOffer
import ch.maxant.scalabook.shop.bom.Tarif
import javax.ejb.Asynchronous
import java.util.concurrent.Future
import ch.maxant.scalabook.shop.util.Conversions.{asPrice, JList}
import scala.collection.immutable.TreeSet
import javax.ejb.AsyncResult
import javax.interceptor.Interceptors
import System.{currentTimeMillis => now}
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import java.util.Locale
import ch.maxant.scalabook.shop.util.Constants.{SUISSE, EUR, TEN_MINUTES_IN_MS}
import scala.util.Random
import ch.maxant.scalabook.shop.events.services.EventsPartnerAdapter
import ch.maxant.scalabook.shop.orders.services.OrdersPartnerAdapter
import ch.maxant.scalabook.shop.events.services.GetEventsInterceptor
import ch.maxant.scalabook.shop.events.services.ReserveOfferInterceptor

/**
 * ADFF (A.D. Faigel Finance)
 * is a company which provides finance
 * to companies which put on events.
 * 
 * As a value-added service to their 
 * customers, they sell tickets to the 
 * events over their webservice.
 * 
 * Their model is very similar to our own
 * so the mapping is quite simple.
 */
@Stateless(name="AdffAdapterForEvents")
@LocalBean
private[services] class AdffAdapter extends EventsPartnerAdapter {

    override def bookingSystemCode = "ADFF"

    @Asynchronous
    @Interceptors(Array(classOf[GetEventsInterceptor]))
    override def getEvents(from: Date = null, to: Date = null): FLEO = {
        simulateRemoteWebServiceCall(150 + Random.nextInt(50))
        
        val t1 = Tarif("T1", "Some conditions...", 
                "Front three rows, non-refundable", 
                1, bookingSystemCode,
                "20.35", 45, 0)
        
        val t2 = Tarif("T2a", "Some conditions...", 
        		"Rows 4-10, non-refundable", 
        		1, bookingSystemCode,
        		"15.95", 3, 0)
        
        val t3 = Tarif("T2b", "Some conditions...", 
        		"Rows 4-10, refundable", 
        		1, bookingSystemCode,
        		"17.35", 400, 0)
        
        val a1 = new Address("Le Lido", "Rue de Bourg 17", "Lausanne", "VD", "1003", SUISSE)
        
        val uid1 = "LKJSC-W1"
        val e1 = new EventOffer(1, uid1, "Mama Mojo!", 
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
                	a1, getComments(uid1))

        val t4 = Tarif("FSR", "Some conditions...", 
                "Free seating, refundable", 
                2, bookingSystemCode,
                "22.99", 63, 0)
        
        val t5 = Tarif("SRO", "Some conditions...", 
        		"Balcony, standing room only, non-refundable", 
        		2, bookingSystemCode,
        		"12.40", 74, 0)
        
        val a2 = new Address("Loft Club", "Place Bel-Air 1", "Lausanne", "VD", "1003", SUISSE)
        		
        val uid2 = "BWEIY-A4"
        val e2 = new EventOffer(2, uid2, "The Lights, live",
"""Sensus autem est bonum quod libet proximo sit maior 
subtilitate clamat. Cum vigilantes in penis ostende 
nocte, sunt ostendit a dildo excogitatoris. Posuit 
quam ampla probatio processus est ut iustus ius figura 
et magnitudine enim vultu et sentire omnes variis 
preferences habent.
""", 
                	"20120301_2100", 
                	"BWEIY-A4.png", 
                	bookingSystemCode,
                	JList(t1, t2),
                	null,
                	a2, getComments(uid2))
                	
                	
        new AsyncResult(List(e1, e2))
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
    override def reserveOffer(event: EventOffer, tarif: Tarif) = {
        simulateRemoteWebServiceCall(250 + Random.nextInt(50))
        
    	new Reservation(tarif, event.id, event.uid, 
    	        event.name, event.when,
    	        tarif.bookingSystem, "ADFF-" + now + ":" + counter.incrementAndGet, event.address)
    }
}