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
package ch.maxant.scalabook.shop.common.services
import scala.collection.JavaConversions._
import java.util.Date
import ch.maxant.scalabook.shop.bom.Tarif
import ch.maxant.scalabook.shop.bom.Reservation
import ch.maxant.scalabook.shop.bom.Booking
import javax.ejb.LocalBean
import javax.ejb.Stateless
import ch.maxant.scalabook.shop.bom.EventOffer
import ch.maxant.scalabook.shop.{bom => b}
import java.util.concurrent.Future
import ch.maxant.scalabook.shop.bom.Reservation
import javax.persistence.PersistenceContext
import javax.persistence.EntityManager
import java.util.ArrayList
import javax.ejb.EJB
import java.util.concurrent.TimeUnit
import javax.ejb.Asynchronous
import javax.ejb.AsyncResult
import scala.util.Random
import System.{currentTimeMillis => now}
import java.util.concurrent.atomic.AtomicLong

/**
 * Common parts of all Adapters to our business partners.
 */
trait PartnerAdapter {

	@PersistenceContext(unitName="theDatabase") var em: EntityManager = null
    
    /** the booking code used to identify this adapter */
    def bookingSystemCode: String
    
    /** @param t the time to sleep, simulating wait times for the remote web service call response */
    protected def simulateRemoteWebServiceCall(t: Long) {
        Thread.sleep(t)

        if(true) return;
        
        //heat the processor for a little bit and produce 
        //plenty of short term garbage, just like when 
        //calling a web service
        val start = now
        while(now < start + 1L){ //up to a ms of work.
            val s1 = new String("asdf" + Random.nextPrintableChar())
            val s2 = new String("fdsa" + Random.nextPrintableChar())
            val s3 = s1 + s2
            if(s3.length() > 500) throw new RuntimeException("this will never happen, but guarantees no compiler will optimise our useless code away")
        }
    }
 
    protected def counter = PartnerAdapterHelp.counter
}

object PartnerAdapterHelp {
    val counter = new AtomicLong
}