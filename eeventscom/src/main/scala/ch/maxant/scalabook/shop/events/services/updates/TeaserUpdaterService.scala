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
package ch.maxant.scalabook.shop.events.services
import javax.ejb.Stateless
import scala.collection.JavaConversions.asScalaIterator
import scala.collection.JavaConversions.asScalaBuffer
import java.util.{List => JList}
import javax.ejb.LocalBean
import javax.ejb.TransactionManagement
import javax.annotation.Resource
import javax.ejb.TransactionAttribute
import javax.ejb.TransactionAttributeType
import javax.ejb.TransactionManagementType
import javax.enterprise.inject.Instance
import javax.inject.Inject
import javax.annotation.PostConstruct
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Buffer
import scala.collection.mutable.HashMap
import java.util.logging.Logger
import ch.maxant.scalabook.shop.bom.EventOffer
import ch.maxant.scalabook.shop.bom.EventTeaser
import java.util.concurrent.CountDownLatch
import ch.maxant.scalabook.shop.bom.Tarif
import java.util.concurrent.Future
import java.util.Date
import javax.ejb.Schedule
import javax.ejb.EJB
import ch.maxant.scalabook.shop.bom.EventOffer
import ch.maxant.scalabook.shop.util.Constants.CHF
import scala.collection.mutable.MultiMap
import ch.maxant.scalabook.shop.bom.Price
import ch.maxant.scalabook.shop.events.services.currency.CurrencyService

@Stateless
@LocalBean
private[services] class TeaserUpdaterService extends EventsAdapterClient {
    
    type FLEO = Future[List[EventOffer]]
    
    @Inject var log: Logger = null
    @EJB var eventService: EventService = null
    @EJB var currencyService: CurrencyService = null

    /** 
     * runs every so often, and 
     * gets the list of events
     * from partners and sticks them 
     * in our list of teasers, so that
     * we can service our users 
     * needs quicker.  Avoids us having 
     * to ask each adapter everytime 
     * the home page is clicked.
     */
    @Schedule(minute="*/5", hour="*", persistent=false)
    def update() = {

//TODO events that have run their course are then removed from teasers, by this service which has a different schedule to do that.

//TODO event teasers are only shown when explicitly set to.  imagine a play which runs for several months - there will be 60 events for the same gig!  how can we display this info to the user?  they have to enter a city / date.  at least a date!

//TODO add city to event!  then we can search on city and date, and freetext for name/description.

        //collect all event offers from our partners,
        //asynchronously
    	val results = new ListBuffer[FLEO]()
    	for(a <- adapters.iterator){ 
    	    results += a.getEvents()
    	}

    	//group by event UID
    	val groupedEventsFromPartners = results.map(_.get()).flatten.groupBy(_.uid)

	    //match the events to those we have in the DB
	    //update our DB with new ones, and ones which 
	    //arent found can be removed from the DB
    	val teasers = eventService.getTeasers()

		val uidsToRemove = new ListBuffer[String]
		val uidsToUpdate = new ListBuffer[String]
    	
    	teasers.foreach{ teaser =>
    	    if(groupedEventsFromPartners.contains(teaser.uid)){
    	        uidsToUpdate += teaser.uid
    	    }else{
    	        uidsToRemove += teaser.uid
    	    }
    	}

    	val groupedTeasers = teasers.groupBy(_.uid)
    	
    	val uidsToAdd = groupedEventsFromPartners.keySet.diff(groupedTeasers.keySet)

    	//remove
    	eventService.removeEvent(uidsToRemove.toList) //toList so that its immutable!
    	
    	//add
    	val toAdd = uidsToAdd.map{ uid => (uid, groupedEventsFromPartners(uid).toList) } //toList so that its immutable!
        eventService.addEvent(toAdd)

    	//udpate
    	uidsToUpdate.foreach{ uid => 
    		handleUpdate(uid, groupedEventsFromPartners(uid).toList)
    	}
    }
    
    private def handleUpdate(uid: String, toUpdate: List[EventOffer]) = {

        val teaserToUpdate = eventService.getTeaser(uid)
        
        val tarifsWithPricesInCHF = toUpdate.map(_.tarifs).flatten.map{ t =>
		    if(t.price.currency == CHF){
		        t
		    }else{
		        t.copy(price = currencyService.convertPrice(t.price, CHF))
		    }
        }
        
        val minPriceValue = tarifsWithPricesInCHF.map(_.price.value).min
        val minPrice = new Price(minPriceValue, CHF)
        
        val bookingSystemIDs = toUpdate.map{ eo =>
            (eo.id, eo.bookingSystem)
    	}
        
        val newTeaser = teaserToUpdate.copy(examplePrice = minPrice, bsIds = bookingSystemIDs)

	    eventService.updateEvent(newTeaser)
    }


//    def aMethod(d: Double, i: Int)
//
//    val result = aMethod(9.0, 3)
    
    
    
    
    
    
    
    
    
    
    
}
