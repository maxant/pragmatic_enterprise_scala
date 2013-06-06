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
package controllers

import scala.concurrent._
import scala.util.Success
import org.junit.Assert._
import org.junit.Test
import ch.maxant.scalabook.play20.plugins.rolebasedauth.Secured
import ch.maxant.scalabook.play20.plugins.xasupport._
import ch.maxant.scalabook.validation.integration.UserRepository._
import ch.maxant.scalabook.validation.integration._
import play.api.templates.Html
import play.api.test._
import play.api.test.Helpers._
import org.junit.Before
import play.GlobalSettings

class ValidationControllerTest extends play.test.WithApplication {

    @Test
    def testNotLoggedIn {
        start
        
        //setup inputs
        val request = FakeRequest(POST, "/validate")

        //call to method under test
        //prefereably don't do direct calls to the controller method!  use route instead
        val result = route(request).get
        
        //assertions
        val s = status(result)
        assertEquals(SEE_OTHER, s) //redirect to login page
        assertEquals("/login", redirectLocation(result).get) //redirect to login page
    }

    @Test
    def testValidate {

        import scala.collection.JavaConversions._

        val fakeApp = new play.test.FakeApplication(
                new java.io.File("."),
                classOf[FakeApplication].getClassLoader,
                new java.util.HashMap,
                List("ch.maxant.scalabook.play20.plugins.xasupport.XASupportPlugin"),
                null)

        start(fakeApp)

        //setup inputs
        val request = FakeRequest(POST, "/validate").
            withSession(ValidationController.EventUid -> "LKJSC-W1").
            withSession(Secured.Username -> "lana@hippodrome-london.com").
            withHeaders("content-type" -> "application/x-www-form-urlencoded").
            withFormUrlEncodedBody(ValidationController.BookingRef -> "bookingRefAsdf")

        //setup dependencies
        ValidationController.ticketGateway = TicketGatewayStub
        ValidationController.validationRepository = ValidationRepoStub
        ValidationController.userRepository = UserRepoStub

        //call to method under test
        val result = route(request).get
        
        //assertions
        val c = contentAsString(result) //waits for async result
        val s = status(result)
        assertEquals(OK, s)
        assertTrue(c.contains("bookingRefStillOpen"))
        assertFalse(c.contains("bookingRefAsdf"))
    }
    
}

object ValidationRepoStub extends ValidationRepo {
    override def addValidation(bookingRef: String, validator: User)(implicit ctx: XAContext){
        assert(bookingRef == "bookingRefAsdf")
        assert(validator.email == "lana@hippodrome-london.com")
    }
}

object TicketGatewayStub extends TicketAdapter {

    override def getOpenBookings2(eventUid: String): Future[scala.util.Try[Seq[Ticket]]] = {
        assert(eventUid == "LKJSC-W1")
        
        val t1 = Ticket("bookingRefAsdf", "tarifName", 1, new BigDecimal(new java.math.BigDecimal("12.99")), "Printed", "customerName")
        val t2 = Ticket("bookingRefStillOpen", "tarifName", 1, new BigDecimal(new java.math.BigDecimal("12.99")), "Printed", "customerName")
        val l = List(t1, t2)
        val s = Success(l)
        Promise.successful(s).future
    }
    
    override def addValidation(user: User, bookingRef: String)(implicit ctx: XAContext) {
        assert(user.email == "lana@hippodrome-london.com")
        assert(bookingRef == "bookingRefAsdf")
    }
}

object UserRepoStub extends UserRepo {
    def findByEmail(email: Option[String]): Option[UserRepository.User] = {
        val email = "lana@hippodrome-london.com"
        val user = UserRepository.User(email, "Lana Mills", null)
        user._roles = List(Role(email, "validator"))
        Some(user)
    }
    def authenticate(email: String, password: String) = findByEmail(Some(email))
}