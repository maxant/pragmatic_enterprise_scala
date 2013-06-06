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
package ch.maxant.scalabook.shop.orders.services.email

import java.util.logging.Logger

import javax.annotation.Resource
import javax.ejb.LocalBean
import javax.ejb.Stateless
import javax.inject.Inject
import javax.jms.ConnectionFactory
import javax.jms.Queue
import javax.jms.Session

@Stateless
@LocalBean
class EmailService {
    
    @Inject var log: Logger = null
    @Resource(mappedName="java:/JmsXA") var cf: ConnectionFactory = null
    @Resource(mappedName="java:/queue/mail") var q: Queue = null
    
    def enqueueEmail(to: String, from: String, subject: String, body: String) = {
        val c = cf.createConnection
        val s = c.createSession(true, Session.AUTO_ACKNOWLEDGE)
        val p = s.createProducer(q)
        
        val m = s.createTextMessage(body)
        m.setStringProperty(AsyncEmailService.To, to)
        m.setStringProperty(AsyncEmailService.From, from)
        m.setStringProperty(AsyncEmailService.Subject, subject)
        m.setIntProperty(AsyncEmailService.Retries, 0)
        
        p.send(m)
        s.close()
        c.close()
    }
}

object EmailService {
    
    val ORDERS_DEPARTMENT = "scalabook_orders@maxant.co.uk"

}