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
import javax.inject.Inject
import javax.enterprise.inject.Instance
import javax.enterprise.inject.Any
import scala.collection.JavaConversions.asScalaIterator

/**
 * Adapter clients have access to the adapters
 * deployed in the container, so that they
 * can call partner systems.
 * 
 * Technically speaking, the adapters are
 * unknown and simply deployed.  During 
 * creation of this trait, all are injected
 * (hence the @Any annotation on the adapters
 * field).  The trait lets the client choose 
 * specifically which adapter to use by passing
 * its code.
 */
private[services] trait EventsAdapterClient {

    @Inject @Any protected var adapters : Instance[EventsPartnerAdapter] = null

    /**
     * get the adapter for the given code.  
     */
    def getAdapter(code: String): EventsPartnerAdapter = {
    	for(a <- adapters.iterator){ //implicit conversion
    	    if(a.bookingSystemCode == code)
    	        return a
    	}
    	throw new IllegalArgumentException("unknown partner " + code)
    }
    
}