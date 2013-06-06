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
package ch.maxant.scalabook.shop.orders.services
import org.junit.Test
import java.util.logging.Logger
import ch.maxant.scalabook.shop.bom.Order
import java.util.UUID
import ch.maxant.scalabook.shop.orders.services.paymentpartner.PaymentPartnerService
import ch.maxant.scalabook.shop.orders.services.paymentpartner.PaymentPartnerService2

class PaymentPartnerServiceTest {

	@Test
	def testX() = {

	    val order = new Order(UUID.randomUUID(), null, null, null)
	    
		//for(j <- 1 to 1/*000*/){
		    println("x")
	    //for(i <- 1 to 10){
	    //    val t = new Thread(new Runnable(){
	    //        def run() = {
				    val ps = new PaymentPartnerService2()
				    ps.log = Logger.getLogger(this.getClass().getName());
			
				    for(i <- 1 to 10){
				    	assert(ps.isValidToken(order, "ASDF"))
				    }
				    
		//		    Thread.sleep(10000000)
	    //        }
	    //    })
	    //    t.setName("test_" + i)
	    //    t.setDaemon(false)
	    //    t.start()
	    //}
		//}	    
	    //
	    //Thread.sleep(10000000)
	}
    
}