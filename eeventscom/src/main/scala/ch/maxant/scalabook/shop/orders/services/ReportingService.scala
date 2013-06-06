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

import java.util.logging.Logger

import scala.collection.JavaConversions.asScalaBuffer

import ch.maxant.scalabook.shop.bom.Order
import ch.maxant.scalabook.shop.common.services.SessionService
import ch.maxant.scalabook.shop.common.services.qualifiers.ModelDestroyed
import ch.maxant.scalabook.shop.orders.services.qualifiers.CompletedSale
import javax.ejb.LocalBean
import javax.ejb.Stateless
import javax.enterprise.event.Observes
import javax.inject.Inject

@Stateless
@LocalBean
private[services] class ReportingService {

    @Inject var log: Logger = null

    def registerUnsoldReservations(@Observes @ModelDestroyed model: SessionService.Wrapper) = {
        //persist, so that we know what things people thought
        //about buying, but then didnt actually buy.  useful
        //for reporting.
        //TODO - write these into a reporting table
        if(model.reservations != null){
            model.reservations.foreach{ r =>
                log.info("reporting missed potential order: " + r + " for user " + model.user)
            }
        }
    }
    
    def registerSaleCompleted(@Observes @CompletedSale order: Order){
        //TODO create a reporting table and add this to it
        println("reporting writing order completed: " + order)
    }
}

