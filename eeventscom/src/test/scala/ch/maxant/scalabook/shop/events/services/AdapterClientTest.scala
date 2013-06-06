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

import org.junit.Assert.{assertEquals, assertTrue, assertNull}
import org.junit.Test
import java.util.Date
import ch.maxant.scalabook.shop.bom.Tarif
import ch.maxant.scalabook.shop.bom.Reservation
import ch.maxant.scalabook.shop.bom.EventOffer
import javax.enterprise.inject.Instance
import ch.maxant.scalabook.tooling.InstanceImpl
import java.util.concurrent.Future
import ch.maxant.scalabook.shop.bom.Booking

class AdapterClientTest {

	@Test
	def testGetAdapter() {
	    
	    //set up
	    val p1 = new MyPartnerAdapter("p1")
	    val p2 = new MyPartnerAdapter("p2")
	    val instances = new InstanceImpl(p1, p2)
		val ac = new Object with EventsAdapterClient

		//use reflection to set the field, since its protected in the trait
		val f = ac.getClass().getDeclaredFields()
		assertEquals(f(0).getName(), "adapters")
		f(0).setAccessible(true)
		f(0).set(ac, instances)
	    
	    //positive tests
	    assertEquals(p1.bookingSystemCode, ac.getAdapter("p1").bookingSystemCode)
	    assertEquals(p2.bookingSystemCode, ac.getAdapter("p2").bookingSystemCode)

	    //negative tests
	    assertNull(ac.getAdapter("p3")) //p3 doesnt exist in the list!
	}

	/** 
	 * a pretty dumb impl of the partner adapter interface.
	 * it is only used to test the AdapterClient and so only
	 * needs to return the code provided in the constructor.
	 */
	class MyPartnerAdapter(code: String) extends EventsPartnerAdapter {
	    override def bookingSystemCode = code
	    override def getEvents(from: Date = null, to: Date = null): FLEO = null
	    override def getEvent(id: Int): FOEO = null
	    override def reserveOffer(event: EventOffer, tarif: Tarif): Reservation = null
	}
 	
}

