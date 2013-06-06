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
package ch.maxant.scalabook.shop.orders.services.payment

import java.math.BigDecimal

import ch.maxant.scalabook.shop.util.JPAHelp
import ch.maxant.scalabook.shop.util.ValidationHelp
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.GenerationType
import javax.persistence.Entity

@Entity
@Access(AccessType.FIELD)
private[payment] case class Payment(

    @JPAHelp.Id
	@JPAHelp.TableGenerator(name="PAYMENT_GENERATOR", table="EVENTS_SEQUENCES",  pkColumnValue="PAYMENT", pkColumnName="SEQ_NAME", valueColumnName="SEQ_COUNT")
	@JPAHelp.GeneratedValue(strategy=GenerationType.TABLE, generator="PAYMENT_GENERATOR")
	id: Long,

	@JPAHelp.Column(name="ORDER_UUID")
	@ValidationHelp.Size(min=36, max=36) //checked when committing data
	orderUuid: String,
	
	@JPAHelp.Column(name="PRICE_CHF")
	priceChf: BigDecimal,
	
	@JPAHelp.Column(name="TOKEN_1")
	token1: String,
	
	@JPAHelp.Column(name="TOKEN_2")
	token2: String,
	
	state: String = PaymentStates.Created.toString
) {


    /** default no arg constructors are required by jpa */
    def this() = this(0L, null, null, null, null)
    
    /** constructor for creating a Pending record */
    def this(referenceNumber: String, priceChf: BigDecimal) = {
    	this(0L, referenceNumber, priceChf, null, null)
    }
    
    /** creates a copy of this immutable object, by modifying the state, if possible. */
    def modifyState(targetState: PaymentStates.Value) = {
	    PaymentStates.stateMachine.checkValidTransition(this.state, targetState.toString)
		this.copy(state = targetState.toString)
    }

}

