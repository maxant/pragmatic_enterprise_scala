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
package ch.maxant.scalabook.shop.ui
import java.util.logging.Logger
import java.util.{List => JList}

import scala.beans.BeanProperty

import ch.maxant.scalabook.shop.bom.Order
import ch.maxant.scalabook.shop.orders.services.OrderService
import javax.annotation.PostConstruct
import javax.ejb.EJB
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.inject.Named

@Named
@RequestScoped
class Account extends WithFacesContext {

    @Inject var log: Logger = null
	@Inject var login: Login = null
	@EJB var orderService: OrderService = null

	@BeanProperty var orders: JList[Order] = null
	
	@PostConstruct def init = {
		if(!ctx.isPostback()){ //only read when creating the view, not if per chance someone posts back to it.  currently no postback is done - this view is read only!
			orders = orderService.getOrders(login.getUser)
			
			log.info("loaded the following orders for " + login.getUser.getEmail() + ": " + orders)
		}
    }
    
    def getNumOrders = orders.size
}

