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

import ch.maxant.scalabook.shop.orders.services.OrderService
import javax.ejb._
import javax.ws.rs.ApplicationPath
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Application
import javax.ws.rs.core.MediaType._
import javax.ws.rs.core.Response

/**
 * Provides access to sales data for requesters in the 
 * role "validator", ie. people who scan tickets 
 * and validate them.
 * 
 * call me at: http://localhost:8080/eeventscom/r/validation/bookings/LKJSC-W1
 */
@ApplicationPath("r")
@Path("validation")
class ValidationServiceRest extends Application {

    @EJB val orderService: OrderService = null
    
    @GET
    @Path("bookings/{eventUid}")
    @Produces(Array(APPLICATION_XML))
    def getOrderItems(@PathParam("eventUid") eventUid: String) = {
        
        val xml =
            <orderItems>
                {orderService.getOrderItems(eventUid).filterNot(_.tarifName == "Insurance").map { b =>
                    <orderItem bookingRef={b.bookingRef}>
                        <tarifName>{b.tarifName}</tarifName>
                        <tarifPriceCHF>{b.tarifPriceCHF.toString}</tarifPriceCHF>
                        <qty>{b.qty}</qty>
                        <state>{b.state}</state>
                        <customerName>{b.partyName}</customerName>
                    </orderItem>
                }}
            </orderItems>
        

        //1) use a builder
        //2) "type" is a scala keyword - to avoid the compiler error, simply use back-quotes to escape it
        Response.ok(xml.toString).`type`(APPLICATION_XML_TYPE).build
    }

    /**
     * just for testing purposes!
     */
    @DELETE
    @Path("bookings/{eventUid}")
    @Produces(Array(APPLICATION_JSON))
    def deleteOrderItems(@PathParam("eventUid") eventUid: String) = {
//        val json = """{
//"error" :{
//    "code" : 112,
//    "msg" : "No, not going to delete a customers order!"
//}
//}"""
        import play.api.libs.json._
        import play.api.libs.functional.syntax._

        implicit val errorWrites = Json.writes[Error]        
        implicit val answerWrites = Json.writes[Answer]        
  
        val err = new Answer(error = Error(112, "No, not going to delete a customer order!"))
        val json = Json.toJson(err)
        Response.ok(json.toString).`type`(APPLICATION_JSON_TYPE).build
    }

//alternatively: {for (oi <- ...) yield <oi>{oi}</oi>}
}

case class Answer(value: String = null, error: Error)
case class Error(code: Int, message: String)

