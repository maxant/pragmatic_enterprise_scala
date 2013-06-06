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
package views

import org.junit.Assert._
import org.junit.Test
import ch.maxant.scalabook.validation.integration.UserRepository.User
import controllers.TicketController
import play.api.test.Helpers._
import org.jsoup.Jsoup
import scala.collection.JavaConversions._

class IndexTest {

    @Test
    def testIndexPositive {
        val form = TicketController.eventSelectionForm
        val user = Some(User("john@maxant.ch", "John", null))
        val index = views.html.index(form, user)
        val content = contentAsString(index)
        assertTrue(content.contains("John"))
        assertFalse(content.contains("john@maxant.ch"))
        assertEquals("text/html", contentType(index))
    }

    @Test
    def testIndexPositive2 {

        //http://jsoup.org/cookbook/input/parse-document-from-string
        
        val form = TicketController.eventSelectionForm
        val user = Some(User("john@maxant.ch", "John", null))
        val index = views.html.index(form, user)
        val content = contentAsString(index)
        
        val doc = Jsoup.parse(content)

        assertEquals("eEvents.com", doc.title())
        val smalls = doc.select(".small")
        assertEquals("John | home | logout", smalls.get(0).text)
        assertEquals("home", smalls.get(0).children().get(0).text)
        assertEquals("logout", smalls.get(0).children().get(1).text)
        
        assertEquals("text/html", contentType(index))
    }
}