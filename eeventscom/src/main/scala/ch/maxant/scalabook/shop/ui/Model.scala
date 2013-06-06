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
import System.{currentTimeMillis => now}
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.faces.context.FacesContext
import scala.beans.BeanProperty
import javax.annotation.PostConstruct
import java.util.UUID
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.Cookie
import javax.annotation.PreDestroy
import javax.ejb.EJB
import java.util.List
import ch.maxant.scalabook.shop.common.services.SessionService
import ch.maxant.scalabook.shop.bom.EventTeaser
import ch.maxant.scalabook.shop.bom.EventOffer
import ch.maxant.scalabook.shop.bom.Reservation
import ch.maxant.scalabook.shop.bom.Order
import com.thoughtworks.xstream.XStream
import java.util.ArrayList
import java.util.logging.Logger
import ch.maxant.scalabook.shop.common.persistence.jpa.User

/** represents the UI model */
@Named
@RequestScoped
@serializable
class Model extends WithFacesContext {

    val SessionCookieName = "ch.maxant.scalabook.events.session"
    
    var maxantSessId: String = null
    
    @Inject var log: Logger = null
    @EJB var sessionService: SessionService = null

	@BeanProperty var user: User = null
	@BeanProperty var selectedEvent: EventOffer = null
	@BeanProperty var reservations: List[Reservation] = new ArrayList[Reservation] //explicitly give the type, so that scala.collection.JavaConversions works!
	@BeanProperty var order: Order = null
	@BeanProperty var insurance: Reservation = null
	@BeanProperty var insuranceSelected: Boolean = true
    
	def emptyCart()={
	    setReservations(null)
	    setInsurance(null)
	    setInsuranceSelected(true) //by default, its always selected
	}
    
    @PostConstruct
    def init {
//println("************************** web container session id: " + request.getSession().getId() + "******************************")

        val start = now
        
        //is the user calling the server after a restart, or calling a different node in the cluster?
        //then they know their session ID, so we can use it to load the session from the cache server.
        //otherwise, this is a brand new session.
		var cs = request.getCookies()
		if(cs == null) cs = new Array[Cookie](0)
        val c = cs.filter(_.getName == SessionCookieName)
        if(!c.isEmpty){
            maxantSessId = c(0).getValue
println("##### reusing session id " + maxantSessId)
        }else{
            //its a new session
        	val newId = UUID.randomUUID.toString
println("##### using NEW session id " + newId)
        	response.addCookie(new Cookie(SessionCookieName, newId))
            maxantSessId = newId
        }
        
        val storedModel = sessionService.get(maxantSessId)
        if(storedModel != null){
            this.user = storedModel.user
            this.selectedEvent = storedModel.selectedEvent
            this.reservations = storedModel.reservations
            this.order = storedModel.order
            this.insurance = storedModel.insurance
            this.insuranceSelected = storedModel.insuranceSelected
        }else{
			user = null
			selectedEvent = null
			reservations = new ArrayList[Reservation]
			order = null
			insurance = null
			insuranceSelected = true
        }
        
        log.info("loaded session in " + (now-start) + " ms")
    }

    @PreDestroy
    def destroy {
        //store the session at the end of the request
        sessionService.put(maxantSessId, this)
    }

}

