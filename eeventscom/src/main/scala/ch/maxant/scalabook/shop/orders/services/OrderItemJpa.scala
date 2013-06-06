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

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.TableGenerator
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Column
import javax.persistence.FetchType
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Table
import java.math.BigDecimal
import java.util.Date
import javax.persistence.TemporalType
import ch.maxant.scalabook.shop.util.JPAHelp

@Entity(name="OrderItem")
@Table(name="ORDER_ITEM")
@Access(AccessType.FIELD)
private[services] case class OrderItemJpa (
	
    @JPAHelp.Id
	@JPAHelp.TableGenerator(name="ORDER_ITEM_GENERATOR", table="EVENTS_SEQUENCES",  pkColumnValue="ORDER_ITEM", pkColumnName="SEQ_NAME", valueColumnName="SEQ_COUNT")
	@JPAHelp.GeneratedValue(strategy=GenerationType.TABLE, generator="ORDER_ITEM_GENERATOR")
	id: java.lang.Long,

	@JPAHelp.JoinColumn
	@JPAHelp.ManyToOne
	var order: OrderJpa,
	
	@JPAHelp.Column(name="TARIF_NAME")
	tarifName: String,
	
	@JPAHelp.Column(name="TARIF_DESCRIPTION")
	tarifDescription: String,
	
	@JPAHelp.Column(name="TARIF_CONDITIONS")
	tarifConditions: String,
	
	@JPAHelp.Column(name="TARIF_QTY")
	qty: Int,
	
	@JPAHelp.Column(name="TARIF_PRICE_CHF")
	price: BigDecimal,
	
	@JPAHelp.Column(name="BOOKING_REF")
	bookingReference: String,
	
	@JPAHelp.Column(name="EVENT_UID")
	eventUid: String,
	
	@JPAHelp.Column(name="EVENT_NAME")
	eventName: String,

	@JPAHelp.Column(name="EVENT_DATE")
	@JPAHelp.Temporal(TemporalType.TIMESTAMP) 	
	eventDate: Date,
	
	@JPAHelp.Column(name="BOOKING_SYSTEM_CODE")
	bsCode: String,
	
	@JPAHelp.Column(name="PARTNER_REFERENCE")
	partnerReference: String,

	state: String = OrderItemStates.Created.toString
) {
    
    /** default no arg constructors are required by jpa */
    def this() = {
        this(null, null, null, null, null, 0, null, null, null, null, null, null, null)
    }

    /** creates a copy of this immutable object, by modifying the state, if possible. */
    def modifyState(targetState: OrderItemStates.Value) = {
        if(this.state == targetState.toString && OrderItemStates.Printed.equals(targetState)){
            //printed -> printed is OK! we dont care how often its printed
        }else{
        	OrderItemStates.stateMachine.checkValidTransition(this.state, targetState.toString)
        }
		this.copy(state = targetState.toString)
    }

    def isValidated = OrderItemStates.Validated.toString == this.state
}
