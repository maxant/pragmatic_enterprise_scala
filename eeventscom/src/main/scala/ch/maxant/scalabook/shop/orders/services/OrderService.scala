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

import ch.maxant.scalabook.shop.common.persistence.{jpa=>jpa}
import javax.ejb.Stateless
import ch.maxant.scalabook.shop.orders.services.paymentpartner.PaymentPartnerService
import javax.ejb.LocalBean
import javax.inject.Inject
import java.util.logging.Logger
import scala.collection.mutable.Buffer
import java.util.UUID
import java.util.Date
import ch.maxant.scalabook.shop.bom.Reservation
import javax.enterprise.event.Event
import javax.ejb.EJB
import scala.collection.mutable.ListBuffer
import ch.maxant.scalabook.shop.util.Constants.TWENTY_SECONDS_IN_MS
import ch.maxant.scalabook.shop.util.Constants.CHF
import java.util.concurrent.TimeUnit
import scala.collection.JavaConversions._
import ch.maxant.scalabook.shop.orders.services.paymentpartner.PaymentPartnerService3
import ch.maxant.scalabook.shop.orders.services.paymentpartner.PaymentPartnerService2
import ch.maxant.scalabook.shop.orders.services.paymentpartner.PaymentPartnerService
import ch.maxant.scalabook.shop.orders.services.qualifiers.CompletedSale
import javax.annotation.security.RolesAllowed
import javax.ejb.SessionContext
import javax.annotation.Resource
import ch.maxant.scalabook.shop.util.Configuration
import ch.maxant.scalabook.shop.util.Roles
import ch.maxant.scalabook.shop.bom.Party
import javax.persistence.PersistenceContext
import javax.persistence.EntityManager
import javax.sql.DataSource
import scala.slick.driver.MySQLDriver.simple._
import javax.ejb.TransactionAttribute
import javax.ejb.TransactionAttributeType
import javax.ejb.TransactionManagement
import javax.ejb.TransactionManagementType
import ch.maxant.scalabook.shop.util.Constants._
import java.util.ArrayList
import ch.maxant.scalabook.shop.util.LogProducer_
import ch.maxant.scalabook.shop.util.StateMachine
import java.net.URL
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import ch.maxant.scalabook.shop.{bom=>b}
import ch.maxant.scalabook.shop.common.services.UserService
import ch.maxant.scalabook.shop.orders.services.accounting.AccountingService
import ch.maxant.scalabook.shop.orders.services.payment.IPaymentService
import ch.maxant.scalabook.shop.common.persistence.scalaquery.User
import ch.maxant.scalabook.shop.orders.services.email.EmailService
import javax.interceptor.Interceptors
import ch.maxant.scalabook.shop.common.services.ValidationInterceptor
import ch.maxant.scalabook.shop.bom.NoExpiredReservations

/**
 * handles order fulfilment.
 */
@Stateless
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
class OrderService extends OrdersAdapterClient {

	@Inject protected var config: Configuration = null
    @Inject protected var log: Logger = null
    @Inject @CompletedSale protected var orderCompletedEvent: Event[b.Order] = null
    @EJB protected var paymentService: IPaymentService = null
	@EJB protected var paymentPartnerService: PaymentPartnerService = null
	@EJB var paymentPartnerService2: PaymentPartnerService2 = null
	@Inject var paymentPartnerService3: PaymentPartnerService3 = null
	@EJB protected var emailService: EmailService = null
	@EJB protected var userService: UserService = null
	@EJB protected[services] var accountingService: AccountingService = null
	@Resource protected var ctx: SessionContext = null
	@PersistenceContext(unitName="theDatabase") protected var em: EntityManager = null
	@Resource(name="java:/jdbc/MyXaDS") protected var datasource: DataSource = null

	@RolesAllowed(Array("registered"))
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Interceptors(Array(classOf[ValidationInterceptor]))
    def checkout(@NoExpiredReservations reservations: List[Reservation]) = {
        
        //check none have expired
        //if any have, then the client
        //should now allow any of them
        //to be sold.
        val now = new Date()
        reservations.foreach{ r =>
            if(r.expiry.before(now)){
            	throw new ReservationExpiredException(r)
            }
        }

        //do the booking for each reservation with the partner
        val fbookings = reservations.map{ r =>
            getAdapter(r.bookingSystem).bookReservation(r)
        }
        
        val bookings = fbookings.map{ fb =>
            fb.get(OrderService.BOOKING_TIMEOUT, TimeUnit.MILLISECONDS)
        }
        
        var order = new b.Order(UUID.randomUUID(), null, bookings, null)
        
        val paymentRecordId = paymentService.createPayment(order)
        
        val token = paymentPartnerService.generatePaymentToken(order)

        paymentService.addToken(paymentRecordId, token)
        
        val user = userService.getLoggedInUser.get //RolesAllowed ensures user is logged in
        
        order = order.copy(partyLeader = createPartyLeaderFromUser(user), paymentToken = token)
        
        persistOrder(order)

        order
    }
    
    private def persistOrder(order: b.Order) {
        val user = userService.getLoggedInUser.get
        val items = order.bookings.map(b => new OrderItemJpa(
                null, //set by JPA
                null, //back reference cant be set until parent is instantiated
                b.tarif.name,
                b.tarif.description,
                b.tarif.conditions,
                b.tarif.quantity,
                b.tarif.price.value,
                b.referenceNumber,
                b.eventUid,
                b.eventName,
                new java.sql.Timestamp(b.eventDate.getTime),
                b.bookingSystem,
                b.partnerReference
        ))

        val po = OrderJpa(null/*set by JPA*/, order.uuid.toString, user.id, new Date(), items)
        
        log.info("persisting order " + po)

        items.foreach(_.order = po) //otherwise we get "Column 'ORDER_ID' cannot be null"
        
        em.persist(po) //saves children too, due to cascade
        
        //tell our stats graph db
        updateStats(items, user.email)
    }

    private def updateStats(items: Seq[OrderItemJpa], email: String) {
        if(true) return; //dont use at the mo, it doesnt seem reliable.. TODO work more on this bit
    	items.par.foreach{ oi =>
    	    try{
		        val url = new URL(config.c.addSaleUrl + "?eventUid=" + URLEncoder.encode(oi.eventUid) + "&email=" + URLEncoder.encode(email))
		        val is = url.openStream()
				try {
				    var curr = is.read()
				    while(curr >= 0){
				        curr = is.read()
				    }
				} finally {
					is.close()
				}
				
				//dont actually care what the result is - so long as the call was made!
				
    	    }catch{
    	        case e: Exception => log.warning("failed to update sales stats for sale of " + oi.eventUid + " to " + email)
    	    }
    	}
    }
    
    
    /**
     * token is the token provided by the payment partner,
     * which we send back to them to verify its correct
     * and they called our page to say the payment was 
     * complete.
     * 
     * @return the order with the given payment token set in it.
     */
    @RolesAllowed(Array("registered"))
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    def verifyPayment(order: b.Order, token: String) = {

		paymentService.userLandedBackAtOurSite(order.uuid.toString, token)
        
	    if(paymentPartnerService.isValidToken(order, token)){

	    	paymentService.paymentValid(order.uuid.toString)

	    	paymentValid(order.uuid)
	        
	        sendConfirmationEmail(order)

	        //we really are now ready for the user to print
	        order.bookings.foreach{b => 
	            accountingService.completeBooking(b.referenceNumber)
	        }
	    	
	    	//TODO load/save session with a filter
	    	//TODO test session management
	    	//TODO add session removal with a session filter! can that filter manage new sessions? eg we can see where a cookie is different fromthe session ID after start up
	    	//TODO http://java.dzone.com/articles/primefaces-push-atmosphere

	    	//TODO make partner service have an scala aspect trait
	    	//TODO if not, talk about wanting to do that, and not being able to use scala native constructs
	    }else{
	    	paymentService.paymentInvalid(order.uuid.toString)

	    	//TODO the choice as to whether to cancel should be based on order state, not accounting state!
	    	//bookings will be cancelled because the accounting records are only pending, not booked
	    	
	    	throw new Exception("Invalid token - see logs.")
	    }
        
	    //inform any decoupled listeners
	    //(also ensures that if the transaction cannot
	    //be committed, that listeners like the 
	    //CompensationService get an event!
	    orderCompletedEvent.fire(order)

        order.copy(paymentToken = token)
    }
    
    protected def sendConfirmationEmail(order: b.Order){
        val accountUrl = config.c.siteUrl + "secure/account.jsf"
		val pdfUrl = config.c.siteUrl + "secure/Pdf?PDFReference="

		val firstName = order.partyLeader.firstName
		val orderUuid = order.uuid
        
		var body = 
s"""
Thanks $firstName, for placing your order with eEvents.com for the following events.<br/>
Tickets can be printed by downloading the PDFs by clicking the following links.<br/>
The order number is $orderUuid and can be viewed online at: <a href='$accountUrl'>$accountUrl<br/>
<br/>
"""
		for(b <- order.bookings){
			body += " - " + b.getDescription() + " <a href='" + pdfUrl + b.referenceNumber + "'>" + b.referenceNumber + "</a><br/>"
		}
body += """
<br/>
Thanks from your eEvents.com team!
"""
        
        emailService.enqueueEmail( order.partyLeader.email,
                				EmailService.ORDERS_DEPARTMENT,
                				"eEvents.com Order #" + order.uuid,
                				body)
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    def getBooking(userId: Long, bookingReference: String): Option[b.Booking] = {

        //the following code might look strange, but I wanted to use a join in scalaquery
        //together with a simple JPA query :-/  just to see if it works!?
        //it might have been more efficient to load the OrderItem and then load the Order that goes with it,
        //thus avoiding a join and select in favour of two selects
        
        import OrderSQ._
        import OrderItemSQ._
        import User._

        Database.forDataSource(datasource) withSession { implicit db: Session =>
	        //select o.user_id from orders o, order_item oi where oi.booking_ref = :bookingRef and oi.order_id = o.id

            val userIds = for{
                oi <- OrderItems if oi.bookingRef === bookingReference
                o <- Orders if o.id === oi.orderId
            } yield o.userId
            
            log.info("checking if bookingRef " + bookingReference + " belongs to user " + userId + ": " + userIds.selectStatement)
            
            if(userIds.list.contains(userId)){
		        val q = em.createQuery("select oi from OrderItem oi where oi.bookingReference = :bookingRef", classOf[OrderItemJpa])
		        q.setParameter("bookingRef", bookingReference)
		        val oi = q.getSingleResult
		        val t = b.Tarif(oi.eventName, oi.tarifConditions, oi.tarifDescription, -1/*id*/, oi.bsCode, b.Price(oi.price, CHF), -1, oi.qty)
		        val booking = b.Booking(t, oi.bookingReference, oi.eventUid, oi.eventName, oi.eventDate, oi.bsCode, oi.partnerReference, OrderItemStates.valueOf(oi.state).getOrElse(null))
		        return Some(booking)
            }
        }
        
        None
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    def getOrderItems(eventUid: String) = {
    	import Booking.Bookings
    	Database.forDataSource(datasource) withSession { implicit db: Session =>

    	  //TODO https://groups.google.com/forum/?fromgroups=#!topic/scalaquery/oJKcNVlsCvI
    	  //old: Query(Bookings).where(_.eventUid === eventUid)./*sortBy(Bookings.state.asc, Bookings.bookingRef.asc).*/list
    	  Query(Bookings).where(_.eventUid === eventUid).sortBy(b => (b.state, b.bookingRef)).list
    	}
    }
    
    /**
     * get a list of orders for the given user, ordered by date desc. 
     * order items are filled, including tarifs, as much as we can, 
     * considering much of the info is no longer available!
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    def getOrders(user: jpa.User) = {
        val orders = new ArrayList[b.Order]()
        val pl = createPartyLeaderFromUser(user)
        val q = em.createQuery("select o from Order o where o.userId = :userId order by o.when desc", classOf[OrderJpa])
        q.setMaxResults(100) //limit coz this is a test system where just one  is used in load tests, ie they have millions of orders!
        q.setParameter("userId", user.id)
        val os = q.getResultList()
        os.foreach { o => 
            val bookings = new ArrayList[b.Booking]()
            o.items.foreach { oi =>
                val tarif = new b.Tarif(oi.tarifName, oi.tarifConditions, oi.tarifDescription, -1, null, new b.Price(oi.price, CHF), 0, oi.qty)
                bookings.add(new b.Booking(tarif, oi.bookingReference, oi.eventUid, oi.eventName, oi.eventDate, oi.bsCode, oi.partnerReference, OrderItemStates.valueOf(oi.state).getOrElse(null)))
            }
            orders.add(new b.Order(UUID.fromString(o.uuid), pl, bookings, null/*no need to hand this out!*/, o.when, o.state))
        }

        orders
    }
    
    private def createPartyLeaderFromUser(user: jpa.User) = {
        val names = user.name.split(" ")
        val firstname = if(names.length > 0) names(0) else ""
        val lastname = if(names.length > 1) user.name.substring(user.name.indexOf(" ")) else ""
        new b.Party(firstname, lastname, null, user.email, null)
    }

    private def paymentValid(orderUuid: UUID) {
        val q = em.createQuery("select o from Order o where o.uuid = :uuid", classOf[OrderJpa])
        q.setParameter("uuid", orderUuid.toString)
        var o = q.getSingleResult
        
        //check the state transition is valid
        o = o.modifyState(OrderStates.Paid)
        em.merge(o)
    }
    
    /** updates the order item with the given bookingReference to have the state Printed. */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    def markItemAsPrinted(bookingRef: String) {
        val q = em.createQuery("select oi from OrderItem oi where oi.bookingReference = :bookingReference", classOf[OrderItemJpa])
        q.setParameter("bookingReference", bookingRef)
        var oi = q.getSingleResult()
        oi = oi.modifyState(OrderItemStates.Printed)
        em.merge(oi)
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    def validateOrderItem(bookingRef: String) = {
    	val q = em.createQuery("select oi from OrderItem oi where oi.bookingReference = :bookingRef", classOf[OrderItemJpa]);
    	q.setParameter("bookingRef", bookingRef)
    	var oi = q.getSingleResult
		log.info("Validating booking reference " + bookingRef)
		oi = oi.modifyState(OrderItemStates.Validated)
		em.merge(oi)
	}
    
}

object OrderService {
    val BOOKING_TIMEOUT = TWENTY_SECONDS_IN_MS
}

class ReservationExpiredException(val reservation: Reservation) extends Exception

