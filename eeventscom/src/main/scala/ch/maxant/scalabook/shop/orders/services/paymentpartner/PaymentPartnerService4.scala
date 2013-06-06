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
package ch.maxant.scalabook.shop.orders.services.paymentpartner

import java.lang.System.{currentTimeMillis => now}
import java.math.BigDecimal
import java.net.URL
import java.util.Date
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.seqAsJavaList
import scala.concurrent.Await
import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.math.BigDecimal.javaBigDecimal2bigDecimal

import com.lapyap.ws.Payment
import com.lapyap.ws.PaymentService

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.TypedActor
import akka.actor.TypedProps
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.routing.SmallestMailboxRouter
import akka.util.Timeout
import ch.maxant.scalabook.shop.bom.Booking
import ch.maxant.scalabook.shop.bom.Order
import ch.maxant.scalabook.shop.bom.Price
import ch.maxant.scalabook.shop.bom.Tarif
import ch.maxant.scalabook.shop.orders.services.OrderItemStates
import ch.maxant.scalabook.shop.util.Configuration
import ch.maxant.scalabook.shop.util.Constants
import ch.maxant.scalabook.shop.util.NonCDIConfig
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.inject.Named
import javax.xml.ws.BindingProvider

/**
 * extremely important: we want the akka system to exist
 * just one time in the entire JVM, since it contains
 * a thread execution pool!  imagine if this service
 * was pooled and each instance had its own
 * akka system, how many threads might be 
 * floating around!
 */
@Named
@ApplicationScoped
private[services] class PaymentPartnerService4 {

    @Inject var log: Logger = null
    @Inject var config: Configuration = null

    private implicit val timeout = Timeout(20 seconds)

    private var system: ActorSystem = null
    private var _master: Master = null

    @PostConstruct
    def init() = {
        system = ActorSystem("PaymentServiceClient")
        _master = TypedActor(system).typedActorOf(TypedProps(classOf[Master], new MasterImpl()), "master")
        
        log.info("finished creating payment partner service.")
    }
    
    @PreDestroy
    def shutdown() = {
        log.info("shutting down payment partner service...")
        TypedActor(system).stop(_master)
        system.shutdown()
        log.info("payment partner service shut down successfully.")
    }
    
    def master = _master
}

private[paymentpartner] abstract trait Master {
    def generatePaymentToken(order: Order, merchantId: String): Try[String]    
}

private[paymentpartner] class MasterImpl extends Master {

    private implicit val timeout = Timeout(20 seconds)

    val strat = akka.actor.OneForOneStrategy() {
        case e: Exception => {
            akka.actor.SupervisorStrategy.Restart
        }
    }

    private val router = TypedActor.context.actorOf(Props[Worker].
            withRouter(SmallestMailboxRouter(5, supervisorStrategy = strat)), "worker")

    override def generatePaymentToken(order: Order, merchantId: String): Try[String] = {
        
        val description = "Ticket for " + order.bookings.map(_.eventName).mkString(",")

        implicit val context = TypedActor.context
        
        val er = EncryptionRequest(merchantId,
                order.uuid.toString, 
                order.getTotalPrice.value,
                order.getTotalPrice.currency,
                description)

        val response = router ? er
        
        Await.result(response, timeout.duration)

        response.mapTo[String].value.get 
    }
}

private[paymentpartner] class Worker extends Actor {
    
    var partner: Payment = null
    val config = new NonCDIConfig
    config.load()

    val log = Logger.getLogger("worker" + self.path.name)

    override def postRestart(reason: Throwable) { log.info(" *** postRestart");preStart() }
    
    def receive = {
        case er: EncryptionRequest => 
            try{
                val token = getPartner().generateRequestToken(er.merchantId, er.merchantRef, er.amount.toString, er.currency.getCurrencyCode(), er.description)
                sender ! token
            }catch{
                case e: Exception => {
                    sender ! akka.actor.Status.Failure(e) //sends reply to caller
                    throw e //causes restart
                }
            }
    }

    def getPartner() = {
        if(partner == null){
            val url = new URL(config.getPaymentPartnerUrl() + "Payment?wsdl") //"http://localhost:8080/PaymentPartner/Payment?wsdl"
            partner = new PaymentService(url).getPaymentPort()

            //set username and password.  and optionally set the URL.  normally, we just set the WSDL location, which contains the URL. see above.
            val ctx = partner.asInstanceOf[BindingProvider].getRequestContext()
            ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, config.getPaymentPartnerUrl() + "Payment")
            ctx.put(BindingProvider.USERNAME_PROPERTY, "someUsername")
            ctx.put(BindingProvider.PASSWORD_PROPERTY, "somePassword")
        }
        partner
    }
}

object PaymentPartnerService4Test extends App {
    val svc = new PaymentPartnerService4
    svc.log = Logger.getLogger("test")
    val config = new Configuration
    svc.config = config
    try{
        svc.init
        
        import scala.collection.JavaConversions._
        import java.math.BigDecimal
        import java.util.Date
        import java.util.UUID
        import ch.maxant.scalabook.shop.util.Constants
        import ch.maxant.scalabook.shop.orders.services.OrderItemStates
        import scala.concurrent.ExecutionContext.Implicits.global
        
        val bookings = List(Booking(Tarif("tarifName", "tarifConds", "tarifDescr", 1, "ADFF", Price(new BigDecimal("12.95"), Constants.CHF), 1, 1),
                "refNum",
                "eventUid",
                "eventName",
                new Date(),
                "ADFF",
                "partRef",
                OrderItemStates.Created
        ))
        
        try{
            val token1 = svc.master.generatePaymentToken(Order(UUID.randomUUID(), null, bookings, "paymentToken1", new Date(), "State"), config.c.merchantId)
            svc.log.info(" *** token1=" + token1.get)
        }catch{
            case e: Exception => svc.log.log(Level.SEVERE, " *** ERROR calling PaymentPartnerService4 " + e.getMessage)
        }

        //did error handling work?

        try{
            val token2 = svc.master.generatePaymentToken(Order(UUID.randomUUID(), null, bookings, "paymentToken1", new Date(), "State"), config.c.merchantId)
            svc.log.info(" *** token2=" + token2.get)
        }catch{
            case e: Exception => svc.log.log(Level.SEVERE, " *** ERROR calling PaymentPartnerService4 " + e.getMessage)
        }

    }finally{
        svc.shutdown
    }
    svc.log.info(" *** done")

}