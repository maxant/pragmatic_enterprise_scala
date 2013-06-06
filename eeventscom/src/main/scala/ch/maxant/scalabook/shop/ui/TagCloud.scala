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
import javax.inject.Inject
import scala.collection.JavaConversions._
import javax.enterprise.context.ApplicationScoped
import javax.annotation.PostConstruct
import scala.beans.BeanProperty
import org.primefaces.model.tagcloud.TagCloudModel
import org.primefaces.model.tagcloud.DefaultTagCloudModel
import org.primefaces.model.tagcloud.DefaultTagCloudItem
import ch.maxant.scalabook.shop.events.services.EventService
import javax.ejb.EJB
import javax.enterprise.event.Observes
import ch.maxant.scalabook.shop.events.services.qualifiers.EventsUpdated
import java.net.URLEncoder
import scala.util.Random

@Named
@ApplicationScoped
class TagCloud {

    @EJB var eventService: EventService = null
    
    var model: TagCloudModel = null

    def getModel() = {
        model
    }
    
    @PostConstruct
    def init {

        //DefaultTagCloudItems need a strength from 1-5.  If we just give the
        //city count, we dont get the desired effect!
        //lets order the cities by count, and then group into 5 equally sized groups
        val cities = eventService.distinctCities
        
        val orderedCities = cities.sortWith( (a, b) => a._2 > b._2)

        var strength = 5
        var i = orderedCities.size
        val citiesToUse = orderedCities.map { c =>
            val c2 = (c._1, c._2, strength)
            i -= 1
            if(i < (strength * orderedCities.size / 5.0)){
                strength -= 1
            }
            c2
        }
        
        model = new DefaultTagCloudModel()
        
        //shuffle them!
        citiesToUse.sortWith((c1,c2)=>Random.nextBoolean).foreach{ city =>
        	model.addTag(new DefaultTagCloudItem(
        	        city._1 + " (" + city._2 + ")", 
        	        "javascript: showCity('" + encode(city._1) + "');", 
        	        city._3)
        	)
        }
    }

    def encode(s: String) = URLEncoder.encode(s, "UTF-8")
    
    /** when events change, we need to modify our tag clouds! */
    def eventsUpdated(@Observes @EventsUpdated u: Unit) = init
}

                    