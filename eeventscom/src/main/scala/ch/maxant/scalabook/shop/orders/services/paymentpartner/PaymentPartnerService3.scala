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

import java.lang.System.{nanoTime => now}
import java.net.URL
import java.util.logging.Logger
import scala.collection.JavaConversions.asScalaBuffer
import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.GenericObjectPool
import com.lapyap.ws.Payment
import com.lapyap.ws.PaymentService
import ch.maxant.scalabook.shop.bom.Order
import ch.maxant.scalabook.shop.util.Configuration
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.xml.ws.BindingProvider
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import com.lapyap.ws.ValidateTokenResponse

@ApplicationScoped
private[services] class PaymentPartnerService3 {

    @Inject var config: Configuration = null
    @Inject var log: Logger = null

    /** a pool of clients, since they take a long time to create and are not thread safe. */
    private val pool = new GenericObjectPool[Payment](WSClientFactory)
    
    private lazy val service: Payment = {
        val cl = Thread.currentThread.getContextClassLoader
        val ih = new Wrapper(pool)
        val interfaces: Array[Class[_]] = Array(classOf[com.lapyap.ws.Payment])
        java.lang.reflect.Proxy.newProxyInstance(cl, interfaces, ih).asInstanceOf[com.lapyap.ws.Payment]
    }
    
    @PostConstruct
    def init() = {
        pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK)
        pool.setMaxActive(20) //you can use this to throttle the requests
    }
    
    /** Before you can validate a token with the partner,
     * the client needs to create a payment.  in order to
     * create a payment, they need a generated token
     * which contains encrypted information about 
     * the sale.  This ensures that the client cannot
     * try and fake the amount that they pay, and provides
     * safety for the merchant that the payment was for 
     * the correct amount. */
    def generatePaymentToken(order: Order):String = {
        val ourRef = order.uuid.toString
        val amount = String.valueOf(order.getTotalPrice.value)
        val currency = order.getTotalPrice.currency.getCurrencyCode
        val description = "Ticket for " + order.bookings.map(_.eventName).mkString(",")

        service.generateRequestToken(config.c.merchantId, ourRef, amount, currency, description)
    }

    /** checks the token with the parters web service to 
     *  determine if they sent the token */
    def isValidToken(order: Order, token: String): Boolean = {
        service.validateToken(config.c.merchantId, order.uuid.toString, token)
    }

    private object WSClientFactory extends PoolableObjectFactory[Payment] {

        override def activateObject(obj: Payment) {
            // nothing to do?
        }

        override def destroyObject(obj: Payment) {
            // nothing to do
        }

        override def makeObject = {

            val url = new URL(config.c.getPaymentPartnerUrl + "Payment?wsdl") //"http://localhost:8080/PaymentPartner/Payment?wsdl"
            val partner = new PaymentService(url).getPaymentPort()

            //set username and password.  and optionally set the URL.
            val ctx = partner.asInstanceOf[BindingProvider].getRequestContext()
            ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, config.c.getPaymentPartnerUrl + "Payment")
            ctx.put(BindingProvider.USERNAME_PROPERTY, "someUsername")
            ctx.put(BindingProvider.PASSWORD_PROPERTY, "somePassword")
            
            partner
        }

        override def passivateObject(obj: Payment) {
            // nothing to do
        }

        override def validateObject(obj: Payment) = true // clients are always valid
    }
}

class Wrapper(pool: GenericObjectPool[Payment]) extends InvocationHandler {
    def invoke(proxy: Object, method: Method, args: Array[Object]): Object = {
        var success = false
        val service = pool.borrowObject
        try{
            val ret = method.invoke(service, args:_*)
            success = true
            pool.returnObject(service)
            ret
        }finally{
            if(!success){
                pool.invalidateObject(service)
            }
        }
    }
}

object PaymentServiceTest extends App {
    
    case class Asdf(i: Int)
    
    val url = new URL("file:///c:/temp/Payment.wsdl.xml")
    import System.{nanoTime => now}
    val r = new java.util.Random()
    for(j <- 1 to 25){
        val max = 1000
        val start = now
        for(i <- 1 to max){
//            val ps = new PaymentService()
//            val s = r.nextInt(1000)
//            val s = new Asdf(r.nextInt())
//            val ps = new PaymentService(url)
            val p = createProxy
        }
        println("done, avg instantiation time is " + ((now-start)/max))
    }

    def createProxy = {
        
        val cl = Thread.currentThread.getContextClassLoader
        val ih = new Wrapper(null)
        val interfaces: Array[Class[_]] = Array(classOf[com.lapyap.ws.Payment])
        java.lang.reflect.Proxy.newProxyInstance(cl, interfaces, ih).asInstanceOf[com.lapyap.ws.Payment]
    }
}

object TestBrokenClient extends App {
    val service = new PaymentService().getPaymentPort()
    var b = service.validateToken("merchantId", "merchantRef", "token")

    println("turn off web service please")
    
    try{
        b = service.validateToken("merchantId", "merchantRef", "token")
    }catch{
        case e: Exception => println("caught exception: " + e)
    }
    
    println("now turn on web service please")

    try{
        b = service.validateToken("merchantId", "merchantRef", "token")
    }catch{
        case e: Exception => println("caught exception: " + e)
    }
    
    val merchantId = "merchantId"
    val merchantRef = "merchantRef"
    val token = "token"

    def doSomethingElse()={}

    test1()
    test2()
    test3()

    def test1()=
    {
        val asyncResult = service.validateTokenAsync(merchantId, merchantRef, token)
        doSomethingElse()
        val result = asyncResult.get(10L, java.util.concurrent.TimeUnit.SECONDS).isValid()
    }

    def test2()=
    {
        val handler = new javax.xml.ws.AsyncHandler[ValidateTokenResponse](){
            def handleResponse(res: javax.xml.ws.Response[ValidateTokenResponse]) {
                //do something with the result
            }
        }

        val asyncResult = service.validateTokenAsync(merchantId, merchantRef, token, handler)
        doSomethingElse()
    }

    val latch = new java.util.concurrent.CountDownLatch(1)
    def test3()=
    {
        implicit def functionToAsyncHandler(f: Boolean => Unit) = new javax.xml.ws.AsyncHandler[ValidateTokenResponse](){
            def handleResponse(res: javax.xml.ws.Response[ValidateTokenResponse]) {
                println("in callback " + System.currentTimeMillis)
                f(res.get.isValid)
            }
        }

        println("before async service call " + System.currentTimeMillis)
        service.validateTokenAsync(merchantId, merchantRef, token, {isValid: Boolean => 
            println("in function " + System.currentTimeMillis)
            println("it is valid: " + isValid)
            latch.countDown()
        })
        println("after async service call " + System.currentTimeMillis)
        doSomethingElse()
        println("after doing something else" + System.currentTimeMillis)
    }

    latch.await()
    println("done")
}