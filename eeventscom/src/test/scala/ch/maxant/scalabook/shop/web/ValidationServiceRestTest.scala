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
package ch.maxant.scalabook.shop.web

import java.net.URL
import scala.xml.XML
import org.junit.Assert._
import org.junit.Test
import org.apache.commons.httpclient.methods.DeleteMethod
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpStatus
import scala.util.parsing.json.JSON

class ValidationServiceRestTest {

    val server = "localhost"
    val port = 8080
    val urlString = s"http://$server:$port/eeventscom/r/validation/bookings/LKJSC-W1"

    @Test
    def testRemoteRetrieval{

        val url = new URL(urlString)
        val xml = XML.load(url)

        val tickets = for(t <- xml.child filterNot (_.toString.trim == "")) yield {
            new Ticket(
                    (t \ "@bookingRef").text, 
                    (t \ "tarifName").text, 
                    (t \ "qty").text.toInt, 
                    BigDecimal((t \ "tarifPriceCHF").text), 
                    (t \ "state").text,
                    (t \ "customerName").text
                )
        }
        
        assertEquals(1, tickets.size)
    }

    case class Ticket(
        bookingReference: String, 
        tarifName: String, 
        qty: Int, 
        tarifPrice: BigDecimal, 
        state: String, 
        customerName: String)
        
    @Test
    def testDelete {
        val delete = new DeleteMethod(urlString)
        val conn = new HttpClient()
        try{
            val statusCode = conn.executeMethod(delete)
            if(statusCode == HttpStatus.SC_OK) {
                val body = delete.getResponseBody
                val json = new String(body)
                val response = JSON.parseFull(json)
                println(json + "=>" + response)
                val obj = response.get.asInstanceOf[Map[String, Any]]
                assertTrue(obj.contains("error"))
                assertEquals(112.0, obj("error").asInstanceOf[Map[String, Any]]("code"))
            }else fail("Bad result: " + statusCode)
        }finally{
            delete.releaseConnection()
        }
    }
    
    @Test
    def testDeletePlayAPI {
//TODO investigate lists        import play.api.libs.json._
//        import play.api.libs.functional.syntax._
//
//        implicit val errorWrites = Json.writes[TestError]        
//        implicit val listWrites = Json.writes[List[TestError]]        
//        implicit val answerWrites = Json.writes[TestAnswer]        
//  
//        val err = new TestAnswer(error = List(TestError(112, "No, not going to delete a customer order!")))
//        val json = Json.toJson(err)
    }
    
}

case class TestAnswer(value: String = null, errors: List[TestError])
case class TestError(code: Int, message: String)
