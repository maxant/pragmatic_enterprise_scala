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
package ch.maxant.scalabook.shop.orders.services
import scala.collection.JavaConversions._
import java.util.Date
import ch.maxant.scalabook.shop.bom.Tarif
import ch.maxant.scalabook.shop.bom.Reservation
import javax.ejb.LocalBean
import javax.ejb.Stateless
import ch.maxant.scalabook.shop.bom.EventOffer
import ch.maxant.scalabook.shop.{bom => b}
import java.util.concurrent.Future
import ch.maxant.scalabook.shop.bom.Reservation
import javax.persistence.PersistenceContext
import javax.persistence.EntityManager
import java.util.ArrayList
import javax.ejb.EJB
import java.util.concurrent.TimeUnit
import javax.ejb.Asynchronous
import javax.ejb.AsyncResult
import scala.util.Random
import System.{currentTimeMillis => now}
import java.util.concurrent.atomic.AtomicLong
import ch.maxant.scalabook.shop.orders.services.accounting.AccountingService
import ch.maxant.scalabook.shop.common.services.PartnerAdapter

/**
 * common parts for the ordering component
 */
private[services] trait OrdersPartnerAdapter extends PartnerAdapter {

	type FB = Future[b.Booking]

    @EJB var accountingService: AccountingService = null
    
    /** book the given reservation, becoming 
     * financially responsible for it.
     */
    @Asynchronous
    def bookReservation(reservation: Reservation): FB = {

		//it is about to be financially relevant, so we better create a record
        val id = accountingService.createBooking(reservation.bookingSystem, reservation.partnerReference, reservation.eventId, reservation.tarif.name, reservation.tarif.getTotalPrice.value)

        //call through to specific implementation
        val booking = bookReservationWithPartner(reservation)
        
        //it is now financially relevant, so we better update the record
        accountingService.updateBooking(id, booking.referenceNumber)
        
        new AsyncResult(booking)
    }

    /** each adapter must provide the details on how to do the booking with the partner */
    protected def bookReservationWithPartner(reservation: Reservation): b.Booking
    
    /** does a cancellation for cases where we were unable to complete */
    @Asynchronous
    def cancelBooking(partnerReference: String): Future[Unit] = {
        //the cancellation worked, so lets update the accounting record
        accountingService.cancelBooking(partnerReference)
        cancelBookingWithPartner(partnerReference)
    }

    /** each adapter must provide the details on how to do the cancellation with the partner */
    protected def cancelBookingWithPartner(partnerReference: String): Future[Unit]

}
