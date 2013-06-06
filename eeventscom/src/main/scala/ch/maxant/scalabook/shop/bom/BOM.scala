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
package ch.maxant.scalabook.shop.bom

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.{List => JList}
import java.util.Locale
import java.util.UUID
import scala.collection.JavaConversions.asScalaBuffer
import scala.beans.BeanProperty
import org.apache.commons.lang3.StringUtils.abbreviate
import ch.maxant.scalabook.shop.orders.services.OrderItemStates
import ch.maxant.scalabook.shop.util.Constants.TEN_MINUTES_IN_MS
import ch.maxant.scalabook.shop.util.Conversions.asDateFromTechDateTime
import ch.maxant.scalabook.shop.util.Conversions.asPrice
import ch.maxant.scalabook.shop.util.Constants
import ch.maxant.scalabook.shop.util.ValidationHelp

/**
 * Partners sell tickets to events.  An event
 * is a particular showing at a particular time
 * at a particular venue.  
 * 
 * A teaser relates to such an event.
 * 
 * A teaser is not an offer. 
 * Such teasers are used as examples
 * on say the home page.  It's quite
 * possible the given price is no longer
 * available, but it was, in the last
 * five minutes - the TeaserUpdaterService
 * updates Teasers in the background and the
 * last update may have been five minutes ago
 * when a certain price was still available.
 * 
 * @param uid our own unique ID for this event.
 * Tickets to events are sold by one or more 
 * partners and its quite normal that several 
 * partners sell tickets to the same event.
 * But partners only ever know their own ID
 * for an event, and it is up to us
 * to map that to our own code.  For example,
 * two partners sell tickets to 
 * "International Comedy" for
 * the 2nd of February at the 
 * "Lido Club, Lausanne".  One partner, ADFF 
 * may refer to it as event "4" and another 
 * partner, GEDE may refers
 * to it as event "5".  But to us, it is
 * simply event "C".
 * 
 * @param bsIds tuples of booking system
 * code and event ID, which can sell
 * tickets to this event.  As an example
 * partner ADFF may refer to the 
 * event as "4" - so the tuple element 
 * for them will be (4, ADFF).  Another 
 * partner, GEDE may refer to the event as
 * "5", so their tuple will be (5, GEDE).
 */
case class EventTeaser(
        @BeanProperty uid: String, 
        @BeanProperty name: String,
        @BeanProperty description: String,
        @BeanProperty date: Date,
        @BeanProperty examplePrice: Price,
        @BeanProperty imgPath: String,
        @BeanProperty bsIds: List[(Int, String)],
        @BeanProperty address: Address
    ){
    
    //helper constructor for constructing with strings
    def this(uid: String, name: String, description: String,
            date: String, examplePrice: String,
            imgPath: String, bsIds: List[(Int, String)],
            address: Address)={
        //explicitly use asPrice otherwise compiler gets confused
        this(uid, name, 
            abbreviate(description, 100), 
            date, asPrice(examplePrice), 
            imgPath, bsIds, address)
    }
}

/**
 * An offer for a ticket to an event, say 
 * "International Comedy" at 
 * "Lido Club, Lausanne" on the 2nd of
 * February.  All the tarifs attached
 * to this offer were available at the 
 * time the offer was retrieved, but 
 * offers are not guaranteed and may
 * become unavailable between the time
 * of the offer and the time of a 
 * reservation.  An offer comes from 
 * a certain booking system.  But 
 * the same event can be offered 
 * by several booking systems.
 * in such a case, several offer objects
 * would be returned to the client.
 * 
 * @See EventTeaser - notice no inheritance
 * is used here.  There is a note about 
 * that in the book
 * in the section on case classes!  It is
 * easier to duplicate fields here and 
 * keep classes separated, than to muck 
 * around with hashCode and equals.
 * 
 * @param id The ID known to the booking system 
 * which provided this offer.  For example,
 * if the booking system was GEDE, the ID 
 * might be 4.
 * 
 * @param uid Our code which identifies this event
 * regardless of booking system.  Booking system
 * ADFF might refer to this event as having ID
 * 4 and booking system (partner) GEDE might
 * refer to this event as having ID
 * 5.  We refer to it as event "C".  See EventTeaser
 * and its definition of the UID parameter.
 * 
 * @param bookingSystem The booking system code, 
 * eg. ADFF or GEDE, which identifies which 
 * booking system this offer has come from.
 * Used to dispatch the reservation of a tarif
 * in this offer to the relevant booking system.
 * 
 * @param tarifs the list of tarifs available in the offer,
 * each with their own price and conditions.
 * 
 * @param likes the number of people who have "liked" this event.
 * 
 * @param address the location of this event.
 * 
 * @param when The date and time of this event.
 */
case class EventOffer(
        @BeanProperty id: Int,
        @BeanProperty uid: String,
        @BeanProperty name: String,
        @BeanProperty description: String,
        @BeanProperty when: Date,
        @BeanProperty imgPath: String,
        @BeanProperty bookingSystem: String,
        @BeanProperty tarifs: JList[Tarif],
        @BeanProperty rating: Rating,
        @BeanProperty address: Address,
        @BeanProperty comments: JList[Comment]
        ){

    //helper constructor, allowing construction
    //based on strings
    def this(id: Int, uid: String, name: String, 
            description: String, when: String, 
            imgPath: String, bs: String, 
            tarifs: JList[Tarif],
            rating: Rating, address: Address,
            comments: JList[Comment])={
        //explicitly use asDate otherwise compiler gets confused
        this(id, uid, name, description, 
                asDateFromTechDateTime(when), 
                imgPath, bs, tarifs, rating,
                address, comments)
    }
}

/**
 * the rating for an event. Only settable if the user is logged in,
 * and has not already set it.
 */
case class Rating(
        @BeanProperty rating: Int,
        @BeanProperty readOnly: Boolean,
        @BeanProperty numRates: Int
        ){
}

/**
 * a comment, left by a user
 */
case class Comment(
        @BeanProperty username: String,
        @BeanProperty comment: String,
        @BeanProperty when: Date
        ){
}

/**
 * a price, together with a currency.
 */
case class Price(
        @BeanProperty value: BigDecimal,
        @BeanProperty currency: Currency
    ){
    
    def this(value: String, currency: Currency) = {
        this(new BigDecimal(value), currency)
    }
    
    override def toString()={
        new DecimalFormat("#.00").format(value) + " " + currency
    }
    
    def +(that: Price) = {
        if(this.currency != that.currency){
            throw new IllegalArgumentException(
                    "Currencies must be the same (" + 
                    this.currency + "/" + 
                    that.currency + ")")
        }
        this.copy(value = this.value.add(that.value))
    }
    def *(multiplicand: Int) = {
        if(multiplicand < 0) {
            throw new IllegalArgumentException("Multiplicand must be >= 0")
        }else if(multiplicand == 0){
            this.copy(value = BigDecimal.ZERO)
        }else{
            this.copy(value = this.value.multiply(new BigDecimal(multiplicand)))
        }
    }
}

/**
 * A tarif represents a price for a ticket to an event, 
 * together with the conditions which the ticket is based
 * upon.  For example, a partner called GEDE might
 * offer two tarifs for tickets to event "C": a flexible 
 * but therefore more expensive price, for which the ticket
 * may be exchanged; an inflexible 
 * but therefore cheaper price, for which the ticket
 * may NOT be exchanged.
 * 
 * @param name the name of this tarif
 * 
 * @param conditions the conditions attached to this tarif,
 * ie. the flexibility of the ticket.
 * 
 * @param eventID the ID known to the partner, 
 * for example 5 known to partner GEDE.
 * 
 * @param bookingSystem The booking system code, 
 * eg. ADFF or GEDE, which identifies which 
 * booking system this offer has come from.
 * Used to dispatch the reservation of a tarif
 * in this offer to the relevant booking system.
 * 
 * @param availability how many tickets at this 
 * tarif are available for sale from the partner
 * who offered this tarif?
 * 
 * @param quantity the number of this tarif 
 * that is requested by the user, or which 
 * has been reserved / booked.  The contents
 * of this attribute depends on the use case 
 * in which this object is being used.
 * 
 * @param state Taken from OrderItemStates
 */
case class Tarif(
        @BeanProperty name: String,
        @BeanProperty conditions: String,
        @BeanProperty description: String,
        @BeanProperty eventId: Int,
        @BeanProperty bookingSystem: String,
        @BeanProperty price: Price,
        @BeanProperty availability: Int, //how many are still available?
        @BeanProperty quantity: Int //how many do we want/have we got?
    ){

    /** rather than a scala style "totalPrice" we need a bean conform getter, so that this class can be used in JSF too. */
    def getTotalPrice() = {
        price * quantity
    }
}

/**
 * When a user wants to purchase an EventOffer
 * the "reserve" it.  A reservation means that 
 * it is guaranteed to be available for the next
 * 10 minutes.  The reservation is confirmed
 * with the partner, and they agree NOT to sell
 * the given tarif to a different customer which 
 * would cause the availability to drop and may 
 * mean the tarif is no longer available.
 * 
 * @param expiry the time after which this 
 * reservation is no longer bookable and the partner 
 * may offer the tarif to other customers.
 * 
 * @param eventId The booking systems ID for 
 * the Event for which this tarif is valid
 * 
 * @param eventUid Our UID for 
 * the Event for which this tarif is valid
 * 
 * @param tarif The tarif which has been reserved.
 * It's quantity field is set to the number of
 * tickets which have been reserved.
 * 
 * @param partnerReference The partners reference number
 */
case class Reservation(
        @BeanProperty tarif: Tarif,
        @BeanProperty eventId: Int,
        @BeanProperty eventUid: String,
        @BeanProperty eventName: String,
        @BeanProperty eventWhen: Date,
        @BeanProperty bookingSystem: String,
        @BeanProperty partnerReference: String,
        @BeanProperty eventAddress: Address,
        @BeanProperty expiry: Date = new Date(System.currentTimeMillis() + TEN_MINUTES_IN_MS)
    ){

    /** creates a reservation which will expire in 10 minutes */
    def this(
        tarif: Tarif,
        eventId: Int,
        eventUid: String,
        eventName: String,
        eventWhen: Date,
        bookingSystem: String,
        partnerReference: String,
        eventAddress: Address ) = {
        this(tarif, eventId, eventUid, eventName, eventWhen, bookingSystem, partnerReference, eventAddress, new Date(System.currentTimeMillis() + TEN_MINUTES_IN_MS))
    }
    
    def isExpired = {
        new Date().after(expiry)
    }
}

/**
 * A user purchases a Reservation and at that
 * point becomes financially responsible to 
 * pay for the ticket.  Equally, the partner will
 * no longer make the ticket available to other 
 * customers.  A fully paid booking gives the 
 * user the right to print a ticket to the event.
 * 
 * @param tarif the tarif which was booked.  Its
 * quantity attribute shows how many of the tarif
 * were booked.
 * 
 * @param referenceNumber the reference number in our
 * system which refers to the booking.
 * 
 * @param eventUid Our code which uniquely identifies
 * the event, for example "C".
 * 
 * @param eventName The name of the event.
 * 
 * @param eventDate the date and time of the event.
 * 
 * @param bookingSystem The booking system from which
 * this booking was made.
 * 
 * @param partnerReference The partners unique 
 * reference to this booking.
 */
case class Booking(
        @BeanProperty tarif: Tarif,
        @BeanProperty referenceNumber: String,
        @BeanProperty eventUid: String,
        @BeanProperty eventName: String,
        @BeanProperty eventDate: Date,
        @BeanProperty bookingSystem: String,
        @BeanProperty partnerReference: String,
        @BeanProperty state: OrderItemStates.Value
    ){

    def this(
            tarif: Tarif,
            referenceNumber: String,
            eventUid: String,
            eventName: String,
            eventDate: Date,
            bookingSystem: String,
            partnerReference: String
    ) = this(tarif, referenceNumber, eventUid, eventName, eventDate, bookingSystem, partnerReference, null)
    
    /** rather than a scala style "description" we need a bean conform getter, so that this class can be used in JSF too. */
    def getDescription()={
        eventName + " (" + new SimpleDateFormat(Constants.DATE_FORMAT_DATE_AND_TIME).format(eventDate) + ")"
    }

    /** has the ticket already been used? */
    def isValidated = OrderItemStates.Validated == state
}

/**
 * Bookings are grouped together into an order.
 * 
 * @param uuid The unique reference number for all bookings
 * paid for in a single sale.
 * 
 * @param partyLeader the person who made the booking(s).
 * 
 * @param bookings The list of 1..n bookings that were made.
 *
 * @param paymentToken The token which our payment partner 
 * gave us to confirm the validity of the payment.  Kept
 * as a reference to the payment, in case of any disputes.
 * 
 * @param state Optional, value of current OrderStates (Pending or Paid)
 */
case class Order(
        @BeanProperty uuid: UUID,
        @BeanProperty partyLeader: Party,
        @BeanProperty bookings: JList[Booking],
        @BeanProperty paymentToken: String,
        @BeanProperty created: Date,
        @BeanProperty state: String
    ){
    
    def this(uuid: UUID,
        partyLeader: Party,
        bookings: JList[Booking],
        paymentToken: String
    ) = this(uuid, partyLeader, bookings, paymentToken, null, null)

    /** get the total price of all bookings in this order.
     *  rather than a scala style "totalPrice" we need a bean
     *  conform getter, so that this class can be used in JSF too.
     */
    def getTotalPrice() = {
        bookings.map(_.tarif.getTotalPrice()).reduce(_+_)
    }

    /** 
     * This method explicitly returns a Scala collection rather 
     * than a Java one, to show what happens when JSF XHTML 
     * uses this property rather than a Java Collection.  
     * The exception in the UI is:
     * 
     * javax.el.ELException: /paymentConfirmation.xhtml: The class 'scala.collection.immutable.$colon$colon' does not have the property 'referenceNumber'.
     * 
     * The solution is to have bean methods which return Java collections, 
     * so that JSF can iterate them.  Using the @BeanProperty annotation
     * this code does that implicitly.  This method is just a demonstration
     * of how things can go wrong.  If the model were based on Scala collections,
     * then manual bean methods would need to be provided.  It boils down to a question
     * of what the default type of attributes should be, and where conversions are needed
     * and which design is more efficient in terms of CPU.  In terms of programming,
     * the import scala.collection.JavaConversions._ reduces the problem to a runtime one.
     */
    def getBookingsAsScalaList()={
        bookings.toList
    }
    
    /** rather than a scala style "createdFormatted" we need a bean conform getter, so that this class can be used in JSF too. */
    def getCreatedFormatted = new SimpleDateFormat(Constants.DATE_FORMAT_DATE_AND_TIME).format(created)
}

/** an address */
case class Address(
    @BeanProperty line1: String,
    @BeanProperty line2: String,
    @BeanProperty city: String,
    @BeanProperty state: String,
    @BeanProperty zip: String,
    @BeanProperty country: Locale
    ){
}

/** a person or other such entity */
case class Party(
    @BeanProperty firstName: String,
    @BeanProperty lastName: String,
    @BeanProperty dateOfBirth: Date,
    @BeanProperty email: String,
    @BeanProperty password: String
    ){
}

    




