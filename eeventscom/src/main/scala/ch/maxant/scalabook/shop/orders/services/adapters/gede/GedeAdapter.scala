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
package ch.maxant.scalabook.shop.orders.services.adapters.gede

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
@Stateless(name="GedeAdapterForOrders")
@LocalBean
private[services] class GedeAdapter extends OrdersPartnerAdapter {

    override def bookingSystemCode = "GEDE"

    protected override def bookReservationWithPartner(reservation: Reservation): Booking = {
        
        simulateRemoteWebServiceCall(100L + Random.nextInt(50))
        
        //TODO call partners web service

        new Booking(reservation.tarif, 
	            UUID.randomUUID().toString(), 
	    		reservation.eventUid, 
	    		reservation.eventName,
	    		reservation.eventWhen,
	    		reservation.bookingSystem,
	    		reservation.partnerReference)
    }

    protected override def cancelBookingWithPartner(parnterReference: String): Future[Unit] = {
        //TODO call partners web service
        new AsyncResult()
    }
    
}