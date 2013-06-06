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
import org.junit.Assert._
import org.junit.Test
import ch.maxant.scalabook.play20.plugins.rolebasedauth.Secured
import controllers.ValidationController
import play.api.test.Helpers._
import play.api.test._
import org.junit.Before

class RoutesTest extends play.test.WithApplication {
    
    @Test
    def testNonExistentPath {
        
        start
        
        val request = FakeRequest(GET, "/asdf")
        
        val result = route(request)
        
        assertEquals(None, result)
    }

    @Test
    def testIndexNotLoggedIn {
        
        start
        
        val request = FakeRequest(GET, "/")
        
        val Some(result) = route(request)
                
        assertEquals(SEE_OTHER, status(result))
        assertEquals("/login", redirectLocation(result).get)
    }
    
    @Test
    def testIndexLoggedIn {
        
        start
        
        val request = FakeRequest(GET, "/").withSession((Secured.Username -> "lana@hippodrome-london.com"))

        val Some(result) = route(request)
        
        assertEquals(OK, status(result))
        assertTrue(contentAsString(result).contains("Lana Mills"))
        assertTrue(contentAsString(result).contains("Please enter the event ID for which you want to validate tickets"))
    }
    
}