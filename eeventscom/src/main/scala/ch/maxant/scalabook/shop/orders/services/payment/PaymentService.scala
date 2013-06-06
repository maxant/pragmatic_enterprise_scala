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

import java.util.logging.Logger

import ch.maxant.scalabook.shop.bom.Order
import javax.ejb.EJB
import javax.ejb.Local
import javax.ejb.Stateless
import javax.ejb.TransactionAttribute
import javax.ejb.TransactionAttributeType
import javax.ejb.TransactionManagement
import javax.ejb.TransactionManagementType
import javax.inject.Inject
import javax.persistence.Access
import javax.persistence.Entity
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Stateless
@EJB(beanInterface = classOf[IPaymentService], name="PaymentService")
@TransactionManagement(TransactionManagementType.CONTAINER)
private[services] class PaymentService extends IPaymentService {
    
	@PersistenceContext(unitName="theDatabase") var em: EntityManager = null
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) //create immediately, regardless, since this is a financially relevant web service call.eg what if the user never returns to our site?
	def createPayment(order: Order) = {
        val payment = new Payment(order.uuid.toString, order.getTotalPrice.value)
        em.persist(payment)

        payment.id
    }
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) //create immediately, regardless, since this is a financially relevant web service call.eg what if the user never returns to our site?
	def addToken(id: Long, token: String) = {
	    var p = em.find(classOf[Payment], id)
        p = p.copy(token1 = token)
	    p = p.modifyState(PaymentStates.AddedToken)
	    em.merge(p)
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) 
	def userLandedBackAtOurSite(uuid: String, token2: String){
	    var p = findRecord(uuid)
        p = p.copy(token2 = token2)
	    p = p.modifyState(PaymentStates.UserLandedBackAtOurSite)
	    em.merge(p)
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) 
	def paymentValid(uuid: String){
	    var p = findRecord(uuid)
	    p = p.modifyState(PaymentStates.Validated)
	    em.merge(p)
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) 
	def paymentInvalid(uuid: String){
	    //TODO based on this, workflow could inform an agent to contact the user
	    var p = findRecord(uuid)
	    p = p.modifyState(PaymentStates.FailedToValidate)
	    em.merge(p)
	}
	
	private def findRecord(uuid: String) = {
		val query = em.createQuery("select p from Payment p where p.orderUuid= :uuid", classOf[Payment])
		query.setParameter("uuid", uuid)
		query.getSingleResult
	}
}

