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
package ch.maxant.scalabook.shop.orders.services.accounting

import java.util.logging.Logger

import scala.collection.JavaConversions.asScalaBuffer

import ch.maxant.scalabook.shop.orders.services.OrdersAdapterClient
import javax.ejb.Asynchronous
import javax.ejb.EJB
import javax.ejb.LocalBean
import javax.ejb.Schedule
import javax.ejb.Stateless
import javax.ejb.TransactionAttribute
import javax.ejb.TransactionManagement
import javax.inject.Inject
import javax.persistence.Access
import javax.persistence.Entity
import javax.persistence.Table

@Stateless
@LocalBean
private[accounting] class CleanupService extends OrdersAdapterClient {
    
    @Inject var log: Logger = null
    @EJB var accountingService: AccountingService = null

    /** 
     * runs every three minutes, and 
     * cancels tickets which were not
     * completely booked. not persistent
     * because we don't need to 
     * run all the backlogged instances
     * when the server starts.  running 
     * every three minutes is sufficient
     * since the task handles cancellation
     * of all outstanding tickets anyway.
     */
    @Schedule(minute="*/3", hour="*", persistent=false)
    def cleanup() = {
        log.info("Cleaning up...")

        val bookingsToDelete = accountingService.getBookingsWhichNeedCancelling
        val toCancel = bookingsToDelete.map(b => (b.bookingSystemCode, b.partnerReference))

        toCancel.foreach{ case (code, ref) => 
            log.info("cancelling " + code + "/" + ref)
            getAdapter(code).cancelBooking(ref) 
        }
        
        log.info("Finished clean up.")
    }
   
}