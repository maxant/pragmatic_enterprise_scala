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
package ch.maxant.scalabook.shop.events.services
import scala.collection.JavaConversions._
import java.util.Date
import ch.maxant.scalabook.shop.bom.Tarif
import ch.maxant.scalabook.shop.bom.Reservation
import ch.maxant.scalabook.shop.bom.Booking
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
import ch.maxant.scalabook.shop.common.services.PartnerAdapter

/**
 * common parts for the events component
 */
private[services] trait EventsPartnerAdapter extends PartnerAdapter {

    type FLEO = Future[List[EventOffer]]
    type FOEO = Future[Option[EventOffer]]

    /** returns a list of events, between the 
     *  given dates, which are currently still
     *  on sale with the partner.
     *  dates are optional.
     */
    def getEvents(from: Date = null, to: Date = null): FLEO

    /** returns a specific event currently
     *  on sale with the partner.
     */
    def getEvent(id: Int): FOEO
    
    /** 
     * reserves places for the given tarif at the given event.
     * the quantity must be set in the tarif, and the tarif name
     * must exist in the event offer, otherwise the partner 
     * may not be able to make the reservation or booking!
     */
    def reserveOffer(event: EventOffer, tarif: Tarif): Reservation
    
    /** fetches comments from the database, used for creating EventOffers */
    def getComments(eventUid: String) = {
		val query = em.createQuery("select c from Comment c where c.eventUid = :uid order by c.when desc", classOf[Comment])
		query.setParameter("uid", eventUid)
		val results = query.getResultList

		val comments = new ArrayList[b.Comment]()
		for(r <- results){
		    comments.add(b.Comment(r.user.name, r.comment, r.when))
		}
		
		comments
    }
    
}
