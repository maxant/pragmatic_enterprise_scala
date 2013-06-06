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

import org.junit.Assert._
import org.junit.Test
import play.api.test._
import play.api.test.Helpers._
import org.junit.Before

class TicketControllerTest extends play.test.WithApplication {

    @Before
    def init {
        start
    }
    
    @Test
    def testHealth {
        
        implicit val request = FakeRequest()
         
        val result = TicketController.health()(request)
        
        assertEquals(OK, status(result))
        assertEquals("text/xml", contentType(result).get)
    }
}