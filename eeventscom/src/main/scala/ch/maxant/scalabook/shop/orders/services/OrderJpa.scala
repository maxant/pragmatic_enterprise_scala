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
import java.util.{List => JList}
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.TableGenerator
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.TemporalType
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

@Entity(name="Order")
@Table(name="ORDERS")
@Access(AccessType.FIELD)
private[services] case class OrderJpa(

    //dont forget the prefix!
    @JPAHelp.Id
	@JPAHelp.TableGenerator(name="ORDER_GENERATOR", table="EVENTS_SEQUENCES",  pkColumnValue="ORDERS", pkColumnName="SEQ_NAME", valueColumnName="SEQ_COUNT")
	@JPAHelp.GeneratedValue(strategy=GenerationType.TABLE, generator="ORDER_GENERATOR")
	id: java.lang.Long,
	
	uuid: String,
	
	@JPAHelp.Column(name="USER_ID")
	userId: Long,
	
	@JPAHelp.Column(name="THE_TIME")
    @JPAHelp.Temporal(TemporalType.TIMESTAMP)
	when: Date,
	
	@JPAHelp.OneToMany(cascade=Array(CascadeType.ALL), fetch=FetchType.LAZY, orphanRemoval=true, mappedBy="order")
	private val jitems: JList[OrderItemJpa],
	
	state: String = OrderStates.Pending.toString
) {

    /** default no arg constructors are required by jpa */
    def this() = {
        this(null, null, -1L, null, new ArrayList[OrderItemJpa])
    }
    
    def items = {
        //prepend and reverse gives good performance, better than append
        var items = List[OrderItemJpa]()
        for(i <- jitems){
            items = i :: items
        }
    	items.reverse
    } 

    /** creates a copy of this immutable object, by modifying the state, if possible. */
    def modifyState(targetState: OrderStates.Value) = {
	    OrderStates.stateMachine.checkValidTransition(this.state, targetState.toString)
		this.copy(state = targetState.toString)
    }
    
}

