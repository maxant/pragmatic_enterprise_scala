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
import scala.collection.immutable.{List => SList}
import java.util.{List => JList}
import scala.collection.JavaConversions._
import scala.beans.BeanProperty
import java.text.SimpleDateFormat
import java.util.Date
import javax.annotation.PostConstruct
import javax.ejb.EJB
import ch.maxant.scalabook.shop.util.Constants
import javax.inject.Inject
import ch.maxant.scalabook.shop.bom.Price
import ch.maxant.scalabook.shop.bom.Reservation
import java.math.BigDecimal
import org.apache.commons.lang3.StringUtils.abbreviate
import javax.faces.context.FacesContext
import javax.servlet.http.HttpServletRequest
import java.util.logging.Logger
import ch.maxant.scalabook.shop.events.services.EventService
import ch.maxant.scalabook.shop.bom.EventOffer
import ch.maxant.scalabook.shop.bom.Tarif
import ch.maxant.scalabook.shop.bom.Comment
import javax.enterprise.context.RequestScoped
import java.util.logging.Level
import javax.faces.application.FacesMessage
import javax.faces.bean.ViewScoped
import javax.faces.view.ViewMetadata
import javax.faces.component.UIComponent
import ch.maxant.scalabook.shop.bom.Rating
import java.util.HashMap
import java.util.ArrayList
import scala.collection.mutable.ListBuffer

@Named
@RequestScoped
class Details extends WithFacesContext {

    @Inject var log: Logger = null
	@Inject var model: Model = null
	@EJB var eventService: EventService = null
	@Inject var login: Login = null

	@BeanProperty var uid: String = null
	@BeanProperty var rating = -1
	@BeanProperty var ratingReadonly = false
	@BeanProperty var selectedTarifs = new HashMap[String, BigDecimal]()
	@BeanProperty var commentToAdd: String = null
	
    private var generalError: String = null
    
    @PostConstruct
    def init(): Unit = {
	    /** reads parameter 'uid' from the 
	     * request and merges all tarifs
	     * from the relevant booking systems 
	     * in order to show relevant tarifs.
	     * stores the result in the model, 
	     * as an (unguaranteed) offer.
	     */
log.info("\r\n\r\n" + ctx.isPostback() + "/" + ctx.getCurrentPhaseId() + "\r\n\r\n")
		if(!ctx.isPostback()){
		    
		    
		    generalError = null

			if(uid == null || uid == ""){
				uid = request.getParameter("uid")
				println("why here - view params should be working!")
//<f:metadata>
//	<f:viewParam id="uid" name="uid" value="#{details.uid}"
//	    required="true"
//	    requiredMessage="You did not specify a uid. (The uid parameter is missing in the URL)"
//	    >
//    </f:viewParam>        
//    <f:event listener="#{details.uid}" type="preRenderView"></f:event>
//</f:metadata>
			
			}
	    	var getOffer = true
	    	if(uid == null){
	    	    println("No UID specified in details page")
	    	    //throw new IllegalArgumentException("No UID specified")
	    	}else{
	    	    if(model.selectedEvent != null){
	    	        if(model.selectedEvent.uid == uid){
	    	            //no need to get the offer, we have it already
//to create realistic load, lets not optimise this, since the load tester currently always requests the same event.
//TODO make load tester request different events and uncomment this!
//	    	            getOffer = false
//	            		log.info("using existing offer")
	    	        }else{
	    	            //its a different uid, so we 
	    	            //shall get a new offer
	    	        	log.info("getting new offer")
	    	        }
	    	    }else{
	    	    	log.info("getting initial offer")
	    	        //no selected event, but uid 
	    	        //is in request, so we shall
	    	        //get a new offer for it
	    	    }
	    	}

	    	if(getOffer){
	    		model.selectedEvent = eventService.getOffer(uid)
	    	}

	    	this.rating = model.selectedEvent.rating.rating
			this.ratingReadonly = model.selectedEvent.rating.readOnly
	    	
	    	//init the number of tarifs selected
			selectedTarifs.clear
	    	model.selectedEvent.tarifs.foreach{ t =>
	    		selectedTarifs(t.name) = new BigDecimal(0)
	    	}
    	}
    }
    
    //called by jsf when addToCart button is clicked
    def addToCart(): String = {

        generalError = null
    	
    	//determine how many tickets the user wants.
    	//map the values to Ints and sum them
        val total = selectedTarifs.values.map(_.intValue).sum
        if(total < 1){
            generalError = "Please select at least one tarif."
            return null
        }
        
        //no need to check availability - 
        //the partner will do this for us!

        log.info("reserving tarifs " + selectedTarifs)
        val allTarifs = model.selectedEvent.tarifs
        try{

            //get the actual tarif objects, based on any 
            //tarif codes that were supplied with 
            //quantities > 0
            val tarifsToReserve = allTarifs.filter{ t =>
            	selectedTarifs.get(t.name).intValue > 0
            }

            //turn the tarif objects into copies, containing
            //the quantity that the user wants.
            val tarifs = tarifsToReserve.map{ t => 
                t.copy(quantity = selectedTarifs(t.name).intValue)
            } 

            //do the reservation!
            val allReservations = new ListBuffer[Reservation]()
    		allReservations.addAll(model.reservations)
            allReservations.addAll(eventService.reserveOffer(model.selectedEvent, tarifs))
            model.reservations = allReservations

            //(re)calculate the insurance
            model.insurance = eventService.calculateInsurance(model.reservations)
            
        	"cart"
        }catch{
            case e: Exception => 
                log.log(Level.SEVERE, "could not reserve tarif", e)
	            val msg = "Could not reserve tarif " + 
//	            		model.selectedTarif.name + 
	            		", please select another one."
        		generalError = msg
	            		
	            //reload the available tarifs, and forward to this page,
	            //it will then show the new offer
	            model.selectedEvent = eventService.getOffer(model.selectedEvent.uid)
	            null //redisplay form
        }
    }
    
    // called by ajax when the like button is clicked
    def doRating() = {
        val r = eventService.updateRating(model.selectedEvent.uid, rating, login.getUser)
        this.rating = r.rating
        this.ratingReadonly = r.readOnly
        log.info("new rating: " + r)
    }

    //used by the view to set the quantities entered by the user
    def getChosenTarifs()={
        val ts = selectedTarifs
        ts
    }
    
    def getGeneralError = generalError
    
    def addComment = {
        eventService.addComment(commentToAdd, model.selectedEvent.uid)
        
        //need to ensure this comment is added to the model, so the page can be refreshed.
        val newComment = Comment(model.user.name, commentToAdd, new Date())
        model.selectedEvent.comments.prepend(newComment)
    }
    
}

