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
import java.net.URL
import java.util.Currency
import java.util.logging.Logger

import scala.collection.JavaConversions.asScalaBuffer
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import com.lapyap.ws.Payment
import com.lapyap.ws.PaymentService

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.routing.SmallestMailboxRouter
import akka.util.Timeout
import ch.maxant.scalabook.shop.bom.Order
import ch.maxant.scalabook.shop.common.services.TechnicalException
import ch.maxant.scalabook.shop.util.Configuration
import ch.maxant.scalabook.shop.util.NonCDIConfig
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.ejb.ConcurrencyManagement
import javax.ejb.ConcurrencyManagementType.BEAN
import javax.ejb.LocalBean
import javax.ejb.Singleton
import javax.ejb.Startup
import javax.inject.Inject
import javax.xml.ws.BindingProvider

/**
 * extremely important: we want the akka system to exist
 * just one time in the entire JVM, since it contains
 * a thread execution pool!  imagine if this service
 * was pooled and each instance had its own
 * akka system, how many threads might be 
 * floating around!
 */
@Singleton
@Startup
@ConcurrencyManagement(BEAN) // => with CMConcurrency, default lock type is WRITE which woudln't work here, where we block on the return from the partner.  with bmc it can use the synchronized keyword as well as all the classes in java.util.concurrent
//alternatively turn this guy into an app scoped CDI bean, since its non transactional anyway!
private[services] class PaymentPartnerService {

    @Inject var log: Logger = null
    @Inject var config: Configuration = null

    private implicit val timeout = Timeout(20 seconds)
//    private implicit val timeout = Timeout.never //TODO rather use 20 seconds, but until we are on a stable akka, it doesnt seem to work...

    private var system: ActorSystem = null
    private var master: ActorRef = null

    @PostConstruct
    def init() = {
        system = ActorSystem("PaymentServiceClient")
        master = system.actorOf(Props[PaymentPartnerMaster],  "PaymentPartnerMaster")
        log.info("finished creating payment partner service.")
    }
    
    @PreDestroy
    def shutdown() = {
        log.info("shutting down payment partner service...")
        system.stop(master)
        system.shutdown()
        log.info("payment partner service shut down successfully.")
    }
    
    /** 
     * Before you can validate a token with the partner,
     * the client needs to create a payment.  in order to
     * create a payment, they need a generated token
     * which contains encrypted information about 
     * the sale.  This ensures that the client cannot
     * try and fake the amount that they pay, and provides
     * safety for the merchant that the payment was for 
     * the correct amount.
     */
    def generatePaymentToken(order: Order) = {
        
        val start = now

        val description = "Ticket for " + order.bookings.map(_.eventName).mkString(",")
        
        //http://doc.akka.io/docs/akka/2.0/scala/futures.html
        //alternative: master.ask(...)
        val token = master ? new EncryptionRequest(config.c.merchantId,
                order.uuid.toString, 
                order.getTotalPrice.value,
                order.getTotalPrice.currency,
                description)

        val response = Await.result(token, timeout.duration).asInstanceOf[Response]

        log.info("got generated payment request in " + (now - start) + "ms")
        
        if(response.success){
            response.result
        }else{
            throw new TechnicalException("ERR-047",
                    "Payment Request for order " + order.uuid + 
                    " could not be created: " + 
                    response.ex, response.ex)
        }
    }

    /** checks the token with the parters web service to 
     *  determine if they sent the token */
    def isValidToken(order: Order, token: String) = {
        
        val start = now
                
        //http://doc.akka.io/docs/akka/2.0/scala/futures.html
        //alternative: master.ask(...)
        val valid = master ? new ValidationRequest(order.uuid.toString(), token)
        
        val response = Await.result(valid, timeout.duration).asInstanceOf[Response]
                
        log.info("validated token in " + (now - start) + "ms")
        
        if(response.success){
            true
        }else{
            throw new TechnicalException("ERR-056",
                    "Token for order " + order.uuid + 
                    " could not be validated: " + 
                    response.ex, response.ex)
        }
    }
}

/**
 * this actor routes requests to workers, each
 * containing a web service client, which gets 
 * reused, so that a) a fixed number of clients
 * exist in the system regardless of how many
 * EJB threads are running (which might
 * be interesting in terms of resources) and 
 * b) so that the non-thread-safe clients can
 * be used over and over again.
 * 
 * This actor exsits just once in the entire
 * system, because it is contained in the
 * singleton TokenValidator which belongs
 * to the payment service.
 */
private[paymentpartner] class PaymentPartnerMaster() extends Actor {

    private val router = context.actorOf(Props[PaymentPartnerWorker].
            withRouter(SmallestMailboxRouter(5)), "PaymentPartnerDelegateRouter")

    def receive = {
        case er: EncryptionRequest => 
            router.forward(er) //forward causes the sender ref in the worker to be the sender of token, rather than the master
        case vr: ValidationRequest => 
            router.forward(vr) //forward causes the sender ref in the worker to be the sender of token, rather than the master
    }
}

/**
 * This actor is instantiated multiple times
 * and messages are routed to this actor
 * by the router inside the 
 * PaymentPartnerDelegateMaster.
 */
private[paymentpartner] class PaymentPartnerWorker extends Actor {
    
    var partner: Payment = null
    val config = new NonCDIConfig
    config.load()

    def receive = {
        case EncryptionRequest(merchantId, ourRef, amount, currency, description) => 
            try{
                val token = getPartner().generateRequestToken(merchantId, ourRef, amount.toString, currency.getCurrencyCode(), description)
                sender ! new Response(token)
            }catch{
                case e: Exception => 
                    partner = null //reconnect to be on the safe side
                    sender ! new Response(e)
            }
        case ValidationRequest(ourRef, token) => 
            try{
                val valid = getPartner().validateToken(config.merchantId, ourRef, token)
                sender ! new Response()
            }catch{
                case e: Exception => 
                    partner = null //reconnect to be on the safe side
                    sender ! new Response(e)
            }
    }
    
    def getPartner() = {
        //lazy load - because if we dont, we have no place to 
        //catch the problem that say the webservice is down;
        //creating the port fails if the web service is
        //down, and if we did this somewhere else, the 
        //actor would just lie there useless.  alternatively
        //we could implement a supervisor strategy to 
        //restart failed children.
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

abstract sealed trait PaymentMessage{}

private case class ValidationRequest(orderId: String, token: String) extends PaymentMessage{}

private case class Response(success: Boolean, ex: Exception, result: String) extends PaymentMessage{

    def this(ex: Exception)={
        this(false, ex, null)
    }
    
    def this(result: String)={
        this(true, null, result)
    }
    
    def this()={
        this(true, null, null)
    }
}

private case class EncryptionRequest(
                    merchantId: String, 
                    merchantRef: String,
                    amount: BigDecimal,
                    currency: Currency,
                    description: String
                    ) extends PaymentMessage{
}