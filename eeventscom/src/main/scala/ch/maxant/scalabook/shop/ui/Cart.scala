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
import java.util.ArrayList
import java.util.{List=>JList}
import scala.collection.JavaConversions._
import ch.maxant.scalabook.shop.orders.services.OrderService
import javax.ejb.EJB
import javax.faces.context.FacesContext
import ch.maxant.scalabook.shop.orders.services.ReservationExpiredException
import javax.faces.application.FacesMessage
import ch.maxant.scalabook.shop.bom.Reservation
import javax.servlet.http.HttpServletResponse
import ch.maxant.scalabook.shop.util.Configuration
import ch.maxant.scalabook.shop.events.services.EventService
import ch.maxant.scalabook.shop.common.services.UserService
import scala.collection.mutable.ListBuffer
import java.net.URLEncoder

@Named
@RequestScoped
class Cart extends WithFacesContext {

    @EJB var orderService: OrderService = null
	@EJB var eventService: EventService = null
	@EJB var userService: UserService = null
	@Inject var config: Configuration = null
    @Inject var model: Model = null
    var theTarifThatWasClicked: Tarif = null
    
	@PostConstruct
	def init(): Unit = {
    }
	
	def getReservations()={
	    model.reservations
	}
	
	def getIsEmpty = {
	    model.reservations == null || model.reservations.size() == 0
	}
	
	def getNumItems = {
	    if(model.reservations != null){
	        model.reservations.map(_.tarif.quantity).sum
	    }else{
	        0
	    }
	}
	
	def remove() = {
	    //keep everything, except the clicked tarif
	    model.reservations = model.reservations.filter(_.tarif != theTarifThatWasClicked)

        //(re)calculate the insurance
        model.insurance = eventService.calculateInsurance(model.reservations)
	}
	
	def setTarifThatWasClicked(t: Tarif) = {
	    this.theTarifThatWasClicked = t
	}
	
	def checkout() = {

	    try{
	        if(model.reservations.isEmpty()){
	            
	            ctx.addMessage(null, 
	                    new FacesMessage("Please put something into your cart."))
                null
	        }else if(ctx.getExternalContext().getUserPrincipal() == null){
	            "loginForPayment" //user MUST be logged in in order to checkout
	        }else{
	            
	            //book reservations, and insurance, if relevant
	            if(model.insuranceSelected && model.insurance != null){
	            	model.order = orderService.checkout(model.insurance :: model.reservations.toList)
	            }else{
	            	model.order = orderService.checkout(model.reservations.toList)
	            }
	            
    			model.reservations = new ArrayList[Reservation]

    			//send user to payment partner to make the payment 
    			
    			val url = config.c.paymentPartnerUrl + 
    					"?token=" + URLEncoder.encode(model.order.paymentToken, "UTF-8"); //payment token is in this instance what we should give to the payment partner, ie. that, containing encrypted info about the sale which they base their screens on.

				response.sendRedirect(url)
				
				"checkout"
	        }
	    }catch{
	        case e: ReservationExpiredException => 
	            ctx.addMessage(null, 
	                    new FacesMessage("Reservation for tarif " + 
	                            e.reservation.tarif.name + 
	                            " has expired.  Please remove it from the cart."))
	        null
	    }
	}
	
	def style(res: Reservation) = {
	    if(res != null && res.isExpired){
	        "expired"
	    }else{
	        ""
	    }
	}
	
	def getTotalPrice = {
	    val tot = model.reservations.map(_.tarif.getTotalPrice).reduce(_+_)
	    if(model.insuranceSelected && model.insurance != null) {
	        tot + model.insurance.tarif.getTotalPrice
	    }else{
	        tot
	    }
	}
	
	def getEventsOthersBought = {
	    val email = if(model.user == null) "anon" else model.user.email
	    eventService.getEventsOthersBought(model.reservations, email)
	}	    

}

