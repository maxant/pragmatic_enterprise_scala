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

import scala.collection.JavaConversions._
import java.math.BigDecimal
import java.util.{List => JList}
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.TableGenerator
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Column
import javax.persistence.AccessType
import javax.persistence.Access
import java.util.ArrayList
import javax.persistence.FetchType
import javax.persistence.Table
import javax.persistence.Cacheable
import javax.persistence.CascadeType
import java.util.Date
import ch.maxant.scalabook.shop.util.JPAHelp

@Entity
@Table(name="ACCOUNT_ENTRY")
@Access(AccessType.FIELD)
private[accounting] case class AccountEntry (

    @JPAHelp.Id
	@JPAHelp.TableGenerator(name="ACCOUNT_ENTRY_GENERATOR", table="EVENTS_SEQUENCES",  pkColumnValue="ACCOUNT_ENTRY", pkColumnName="SEQ_NAME", valueColumnName="SEQ_COUNT")
	@JPAHelp.GeneratedValue(strategy=GenerationType.TABLE, generator="ACCOUNT_ENTRY_GENERATOR")
	id: Long,

	@JPAHelp.Column(name="BOOKING_SYSTEM_CODE")
	bookingSystemCode: String,
	
	@JPAHelp.Column(name="EVENT_ID")
	eventId: Int,
	
	@JPAHelp.Column(name="TARIF_NAME")
	tarifName: String,
	
	@JPAHelp.Column(name="TARIF_PRICE_CHF")
	tarifPriceChf: BigDecimal, //MUST be a java.math.BigDecimal, since JPA doesn't know Scala types!
	
	@JPAHelp.Column(name="REFERENCE_NUMBER")
	referenceNumber: String,
	
	@JPAHelp.Column(name="PARTNER_REFERENCE")
	partnerReference: String,
	
	created: Date,
	
	@JPAHelp.Column(name="LAST_UPDATED")
	lastUpdated: Date,
	
	state: String = AccountingStates.Pending.toString
) {

    /** default no arg constructors are required by jpa */
    def this() = {
        this(0L, null, 0, null, null, null, null, null, null)
    }
    
    /** constructor for creating a Pending record */
    def this(bookingSystemCode: String, eventId: Int, tarifName: String, tarifPriceChf: BigDecimal, partnerReference: String) = {
    	this(0L, bookingSystemCode, eventId, tarifName, tarifPriceChf, null, partnerReference, new Date(), new Date())
    }

    /** creates a copy of this immutable object, by modifying the state, if possible. */
    def modifyState(targetState: AccountingStates.Value) = {
	    AccountingStates.stateMachine.checkValidTransition(this.state, targetState.toString)
		this.copy(lastUpdated = new Date(), state = targetState.toString)
    }
}

