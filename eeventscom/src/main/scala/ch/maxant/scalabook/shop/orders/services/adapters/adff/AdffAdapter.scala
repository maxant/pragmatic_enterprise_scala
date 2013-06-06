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
package ch.maxant.scalabook.shop.orders.services.adapters.adff
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
import System.{currentTimeMillis => now}
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import java.util.Locale
import ch.maxant.scalabook.shop.util.Constants.{SUISSE, EUR, TEN_MINUTES_IN_MS}
import scala.util.Random
import ch.maxant.scalabook.shop.events.services.EventsPartnerAdapter
import ch.maxant.scalabook.shop.orders.services.OrdersPartnerAdapter

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
@Stateless(name="AdffAdapterForOrders")
@LocalBean
private[services] class AdffAdapter extends OrdersPartnerAdapter {

    override def bookingSystemCode = "ADFF"

    protected override def bookReservationWithPartner(reservation: Reservation): Booking = {
        //TODO call partner web service
        simulateRemoteWebServiceCall(50L + Random.nextInt(50))
       
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