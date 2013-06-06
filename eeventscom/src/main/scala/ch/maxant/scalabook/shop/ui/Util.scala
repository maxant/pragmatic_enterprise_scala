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
import javax.inject.Named
import javax.annotation.PostConstruct
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import scala.collection.JavaConversions._
import ch.maxant.scalabook.shop.bom.Tarif
import ch.maxant.scalabook.shop.orders.services.OrderService
import javax.ejb.EJB
import javax.faces.context.FacesContext
import ch.maxant.scalabook.shop.orders.services.ReservationExpiredException
import javax.faces.application.FacesMessage
import ch.maxant.scalabook.shop.bom.Reservation
import javax.servlet.http.HttpServletResponse
import ch.maxant.scalabook.shop.util.Configuration
import java.net.URLEncoder

@Named
@RequestScoped
class Util {
    
    @Inject var ctx: FacesContext = null
    
	def getError()={

		val externalContext = ctx.getExternalContext()
		val requestMap = externalContext.getRequestMap()
		requestMap.get("errorMsg")
    }
}
