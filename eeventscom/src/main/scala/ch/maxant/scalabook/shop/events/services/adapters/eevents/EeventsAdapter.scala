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
package ch.maxant.scalabook.shop.events.services.adapters.eevents

import javax.ejb.Stateless
import javax.ejb.LocalBean
import java.util.Date
import System.{currentTimeMillis => now}
import ch.maxant.scalabook.shop.bom.Tarif
import ch.maxant.scalabook.shop.bom.Reservation
import ch.maxant.scalabook.shop.bom.Booking
import javax.ejb.Asynchronous
import java.util.concurrent.Future
import ch.maxant.scalabook.shop.bom.EventOffer
import ch.maxant.scalabook.shop.bom.Tarif
import ch.maxant.scalabook.shop.util.Conversions.{asPrice, JList}
import scala.collection.immutable.TreeSet
import javax.ejb.AsyncResult
import javax.interceptor.Interceptors
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import java.util.Locale
import ch.maxant.scalabook.shop.util.Constants.{SUISSE, CHF, TEN_MINUTES_IN_MS}
import ch.maxant.scalabook.shop.bom.Address
import ch.maxant.scalabook.shop.bom.Price
import ch.maxant.scalabook.shop.events.services.EventsPartnerAdapter
import ch.maxant.scalabook.shop.orders.services.OrdersPartnerAdapter
import ch.maxant.scalabook.shop.events.services.GetEventsInterceptor
import ch.maxant.scalabook.shop.events.services.ReserveOfferInterceptor

/**
 * Sometimes eevents has its own offers. Especially insurance!
 */
@Stateless(name="EeventsAdapterForEvents")
@LocalBean
private[services] class EeventsAdapter extends EventsPartnerAdapter {

    override def bookingSystemCode = "Eevents"

    @Asynchronous
    @Interceptors(Array(classOf[GetEventsInterceptor]))
    override def getEvents(from: Date = null, to: Date = null): FLEO = {
        new AsyncResult(List()) //empty list, since we dont have are own events, we just sell insurance!
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
        throw new UnsupportedOperationException
    }
    
    def getInsuranceReservation(priceCHF: java.math.BigDecimal) = {
        //TODO should come out of the database really!
        val t = new Tarif("Insurance", "Obtain a 100% refund, if you cancel.",
        		"Cancellation Insurance",
        		1, bookingSystemCode,
        		new Price(priceCHF, CHF), 
        		99, 1)
        
        new Reservation(t, 1, "INS-087", "Insurance", new Date(), bookingSystemCode, "eEvents-" + now + ":" + counter.incrementAndGet, null, new Date(Long.MaxValue)) //these dont expire!
    }
}