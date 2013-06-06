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
import org.junit.Assert._
import scala.collection.JavaConversions._
import ch.maxant.scalabook.shop.bom._
import java.util.UUID
import ch.maxant.scalabook.shop.util.Constants
import java.math.BigDecimal
import java.util.Date
import ch.maxant.scalabook.shop.orders.services.email.EmailService
import ch.maxant.scalabook.shop.util.Configuration

class OrderServiceTest {

    @Test
    def testCheckout {
        val svc = new OrderService
        svc.accountingService = null
    }
    
    @Test
    def testEmail {
        val orderUuid = UUID.randomUUID
        val t = new Tarif("name", "conditions", "description", 1, "adff", new Price(new BigDecimal("29.90"), Constants.CHF), 100, 0)
        val bs = List(new Booking(t, "refNum", "eventUid", "eventName", new Date(0L), "bs", "partnerRef"))
        val order = new Order(orderUuid, new Party("John", "Smith", new Date(0L), "john@maxant.ch", null), bs, null)

        class MockEmailService extends EmailService {
            var to: String = null
            var from: String = null
            var subject: String = null
            var body: String = null
            var called = false
            override def enqueueEmail(to: String, from: String, subject: String, body: String) = {
                if(called) throw new IllegalStateException("already called")
                this.to = to
                this.from = from
                this.subject = subject
                this.body = body
                called = true
            }
        }

        val emailSvc = new MockEmailService
        
        class CUT extends OrderService {
            
            //set injected fields
            this.emailService = emailSvc
            this.config = new Configuration
            
            //widen access modifier to make it testable
            override def sendConfirmationEmail(order: Order){
                super.sendConfirmationEmail(order)
            }
        }
        val svc = new CUT
        
        //run test
        svc.sendConfirmationEmail(order)
        
        assertEquals("john@maxant.ch", emailSvc.to)
        assertEquals("scalabook_orders@maxant.co.uk", emailSvc.from)
        assertEquals("eEvents.com Order #" + orderUuid, emailSvc.subject)
        
        val body = s"""
Thanks John, for placing your order with eEvents.com for the following events.<br/>
Tickets can be printed by downloading the PDFs by clicking the following links.<br/>
The order number is $orderUuid and can be viewed online at: <a href='http://localhost:8080/eeventscom/secure/account.jsf'>http://localhost:8080/eeventscom/secure/account.jsf<br/>
<br/>
 - eventName (01. Jan 1970, 01:00) <a href='http://localhost:8080/eeventscom/secure/Pdf?PDFReference=refNum'>refNum</a><br/>
<br/>
Thanks from your eEvents.com team!
"""            
        assertEquals(body, emailSvc.body)
   }

    @Test
    def testStringContext {
        var colour = "red"
        var width = 2.3d
        
        var output = s"The car is $colour"
        assert("The car is red" == output)
        
        output = f"The van is $width%2.2fm wide"
        assert("The van is 2.30m wide" == output)
    }

}