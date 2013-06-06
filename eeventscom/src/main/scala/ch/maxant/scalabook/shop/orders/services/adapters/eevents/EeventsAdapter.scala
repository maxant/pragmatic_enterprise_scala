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
package ch.maxant.scalabook.shop.orders.services.adapters.eevents

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
@Stateless(name="EeventsAdapterForOrders")
@LocalBean
private[services] class EeventsAdapter extends OrdersPartnerAdapter {

    override def bookingSystemCode = "Eevents"

    protected override def bookReservationWithPartner(reservation: Reservation): Booking = {
        
        //this is instantaneous since we dont track the states of such objects in our system.
        //the accounting is enough to know we sold something
        
        new Booking(reservation.tarif, 
            UUID.randomUUID.toString, 
    		reservation.eventUid, 
    		reservation.eventName, 
    		reservation.eventWhen, 
    		reservation.bookingSystem,
    		reservation.partnerReference)
    }

	protected override def cancelBookingWithPartner(parnterReference: String): Future[Unit] = {
        //noop since we don't need to cancel these in our system. as long as accounting reflects the cancellation, thats all that matters!
        new AsyncResult()
    }
    
}