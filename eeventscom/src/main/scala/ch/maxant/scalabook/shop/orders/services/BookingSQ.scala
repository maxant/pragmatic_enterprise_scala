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

import scala.slick.driver.MySQLDriver.simple._

private[services] object Booking {

    case class Booking(partyName: String, bookingRef: String, tarifName: String, tarifPriceCHF: BigDecimal, qty: Int, state: String, eventUid: String)
    
    val Bookings = new Table[Booking]("BOOKINGS") {
        def partyName = column[String]("NAME")
        def bookingRef = column[String]("BOOKING_REF")
        def tarifName = column[String]("TARIF_NAME")
        def tarifPriceCHF = column[BigDecimal]("TARIF_PRICE_CHF")
        def qty = column[Int]("TARIF_QTY")
        def state = column[String]("STATE")
        def eventUid = column[String]("EVENT_UID")
        def * = partyName ~ bookingRef ~ tarifName ~ tarifPriceCHF ~ qty ~ state ~ eventUid <> (Booking, Booking.unapply _)
    }
    
    
    
}