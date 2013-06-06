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
package ch.maxant.scalabook.shop.orders.services.accounting
import javax.ejb.Stateless
import scala.collection.JavaConversions.asScalaIterator
import scala.collection.JavaConversions.asScalaBuffer
import java.util.{List => JList}
import javax.ejb.LocalBean
import javax.ejb.TransactionManagement
import javax.annotation.Resource
import javax.ejb.TransactionAttribute
import javax.ejb.TransactionAttributeType
import javax.ejb.TransactionManagementType
import javax.enterprise.inject.Instance
import javax.inject.Inject
import javax.annotation.PostConstruct
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Buffer
import scala.collection.mutable.HashMap
import java.util.logging.Logger
import ch.maxant.scalabook.shop.{bom => b}
import ch.maxant.scalabook.shop.bom.EventOffer
import ch.maxant.scalabook.shop.bom.EventTeaser
import ch.maxant.scalabook.shop.util.Constants.ONE_HOUR_IN_MS
import java.util.concurrent.CountDownLatch
import ch.maxant.scalabook.shop.bom.Tarif
import java.util.concurrent.Future
import java.util.Date
import javax.ejb.Schedule
import javax.persistence.PersistenceContext
import javax.persistence.EntityManager
import ch.maxant.scalabook.shop.util.Constants._
import org.junit.Test
import org.apache.commons.codec.binary.Base64
import System.{currentTimeMillis => now}
import javax.ejb.SessionContext
import java.math.BigDecimal
import ch.maxant.scalabook.shop.util.StateMachine

/**
 * This service is for creating and updating records for the back office, to tell them what to expect
 * when they receive a bill from partners at the end of the month.
 */
@Stateless
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
private[services] class AccountingService {
    
    @Inject var log: Logger = null
	@PersistenceContext(unitName="theDatabase") var em: EntityManager = null
    @Resource var ctx: SessionContext = null

    /**
     * Creates a booking, just before we call the partner to do the actual booking.
     * 
     * @return the ID of the account booking record
     */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) //save this, regardless of what happens!
	def createBooking(bookingSystem: String, partnerReference: String, eventId: Int, tarifName: String, totalPrice: BigDecimal) = {
        val b = new AccountEntry(bookingSystem, eventId, tarifName, totalPrice, partnerReference)
        em.persist(b)
        b.id
    }
	
    /**
     * sets our order reference number into the record with the given id and should
     * be called when the partner has confirmed the booking.
     */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) //save this, regardless of what happens!
	def updateBooking(id: Long, referenceNumber: String) = {
	    var b = em.find(classOf[AccountEntry], id)
	    b = b.copy(referenceNumber = referenceNumber, lastUpdated = new Date())
	    b = b.modifyState(AccountingStates.Booked)
		em.merge(b)
	}
    
	/**
	 * The booking is now in a state where the user may also print the tickets.
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) //save this, regardless of what happens!
	def completeBooking(referenceNumber: String) = {
		val query = em.createQuery("select ab from AccountEntry ab where ab.referenceNumber = :ref", classOf[AccountEntry])
		query.setParameter("ref", referenceNumber)
		var record = query.getSingleResult

		record = record.modifyState(AccountingStates.Completed)
		
		em.merge(record)
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	def getBookingsWhichNeedCancelling = {
		val query = em.createQuery("select ab from AccountEntry ab where (ab.state = 'Pending' or ab.state = 'Booked') and ab.lastUpdated < :ts", classOf[AccountEntry])
		query.setParameter("ts", new Date(now - ONE_HOUR_IN_MS))
		query.getResultList
    }

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	def cancelBooking(ref: String) = {
		val query = em.createQuery("select ab from AccountEntry ab where ab.partnerReference = :ref", classOf[AccountEntry])
		query.setParameter("ref", ref)
		var record = query.getSingleResult
		
		record = record.modifyState(AccountingStates.Cancelled)
		
		em.merge(record)
	}
}
