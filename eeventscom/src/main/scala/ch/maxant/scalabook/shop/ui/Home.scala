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
import javax.enterprise.context.RequestScoped
import java.util.Date
import scala.beans.BeanProperty
import java.util.{List => JList}
import ch.maxant.scalabook.shop.bom.EventTeaser
import javax.inject.Inject
import javax.ejb.EJB
import ch.maxant.scalabook.shop.events.services.EventService
import java.util.logging.Logger
import scala.collection.JavaConversions._
import ch.maxant.scalabook.shop.util.Conversions.asDateFromDateOnly
import ch.maxant.scalabook.shop.util.Constants._
import java.text.ParseException

@Named
@RequestScoped
//@javax.enterprise.inject.Model // == @Named + @RequestScoped
class Home {

    val DATE_FORMAT = "dd/MMM/yyyy"
    
	@EJB var eventService: EventService = null
    @Inject var log: Logger = null
    
    private var generalError: String = null

	@BeanProperty
	var from: Date = null

	@BeanProperty
	var to: Date = null
	
	@BeanProperty
	var city: String = null
	
	@BeanProperty
	var searchText: String = null
	
	def getTeasers(): JList[EventTeaser] = {
	    
        generalError = null

		log.info("getting teasers...")

		//use implicit conversions on next line!
		var teasers: List[EventTeaser] = null
		try{
			teasers = eventService.getTeasers(from, to, searchText, city)
		}catch{
		    case pe: ParseException => 
		        generalError = "Date format must be " + DATE_FORMAT_DATE_ONLY
		        //dont use criteria!
			    teasers = eventService.getTeasers()
		    case iae: IllegalArgumentException => 
		        generalError = "Error: " + iae.getMessage()
		        //dont use criteria!
			    teasers = eventService.getTeasers()
		}

		teasers
	}
	
	def update() = {
	    //stay on page, updating using 
	    //the newly set from, to  and searchText
	    null
	}

    def getGeneralError() = {
        generalError
    }
    
    /** called simply to show how exceptions are handled */
    def exception() = {
        throw new Exception("this is a test")
    }

    /** gives a list of filtered cities back, used by the autoComplete component for searching for events */
    def getCities(filter: String): JList[String] = {
        val f = filter.toLowerCase
        val l = eventService.distinctCities.map(_._1).filter(_.toLowerCase.contains(f))
        l
    }
}

