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
package ch.maxant.scalabook.shop.events.services
import javax.ejb.Stateless
import scala.collection.JavaConversions.asScalaIterator
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import java.util.{List => JList}
import java.math.BigDecimal
import javax.ejb.LocalBean
import javax.ejb.TransactionManagement
import javax.annotation.Resource
import javax.ejb.TransactionAttribute
import javax.ejb.TransactionAttributeType
import javax.ejb.TransactionManagementType
import javax.enterprise.inject.Instance
import javax.inject.Inject
import javax.annotation.PostConstruct
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Buffer
import scala.collection.mutable.HashMap
import java.util.logging.Logger
import ch.maxant.scalabook.shop.bom.EventOffer
import ch.maxant.scalabook.shop.bom.EventTeaser
import java.util.concurrent.CountDownLatch
import ch.maxant.scalabook.shop.bom.Tarif
import ch.maxant.scalabook.shop.util.Constants.{inOneYear, SUISSE}
import java.util.concurrent.Future
import java.util.Date
import ch.maxant.scalabook.shop.events.services.qualifiers.EventsUpdated
import ch.maxant.scalabook.shop.util.Constants.CHF
import org.apache.commons.lang3.StringUtils._
import javax.ejb.EJB
import ch.maxant.scalabook.shop.bom.Address
import ch.maxant.rules.Engine
import ch.maxant.rules.SubRule
import javax.persistence.PersistenceContext
import javax.persistence.EntityManager
import ch.maxant.scalabook.shop.events.services.adapters.eevents.EeventsAdapter
import scala.collection.parallel.ParSeq
import ch.maxant.scalabook.shop.bom.Reservation
import ch.maxant.scalabook.shop.bom.Rating
import ch.maxant.rules.NoMatchingRuleFoundException
import javax.ejb.SessionContext
import scala.util.Random
import java.text.SimpleDateFormat
import javax.ejb.Schedule
import java.util.Calendar
import javax.enterprise.event.{Event=>JavaEvent}
import javax.enterprise.event.Observes
import javax.sql.DataSource
import scala.collection.mutable.ArrayBuffer
import org.apache.commons.lang3.StringUtils
import javax.annotation.security.RolesAllowed
import org.primefaces.push.PushContext
import org.primefaces.push.PushContextFactory
import javax.faces.application.FacesMessage
import ch.maxant.scalabook.shop.util.Configuration
import java.net.URL
import java.io.ByteArrayOutputStream
import scala.xml.XML
import java.net.URLEncoder
import ch.maxant.scalabook.shop.events.services.currency.CurrencyService
import ch.maxant.scalabook.shop.common.services.rules.RuleEngine
import ch.maxant.scalabook.shop.events.services.ratings.RatingService
import ch.maxant.scalabook.shop.common.services.UserService
import ch.maxant.scalabook.shop.common.services.UnknownEventException
import ch.maxant.scalabook.shop.common.persistence.jpa.User
import javax.ejb.TimerService
import javax.ejb.ApplicationException

@Stateless
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
class EventService extends EventsAdapterClient {
    
    type FLEO = Future[List[EventOffer]]
    type FOEO = Future[Option[EventOffer]]

	@Inject var config: Configuration = null
    @Inject var log: Logger = null
    @EJB var currencyService: CurrencyService = null
    @EJB var eeventsAdapter: EeventsAdapter = null
    @EJB var ruleEngine: RuleEngine = null
    @EJB var ratingService: RatingService = null
    @EJB var userService: UserService = null
	@PersistenceContext(unitName="theDatabase") var em: EntityManager = null
	@Resource(name="java:/jdbc/MyXaDS") var datasource: DataSource = null
	@Resource var ctx: SessionContext = null
	@Inject @EventsUpdated var eventsUpdatedEvent: JavaEvent[Unit] = null
	
	private var cachedCities = List[(String, Int)]()

	@PostConstruct
	def init = updateCachedCities()
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
    def getTeaser(uid: String) = {
		val teasers = _getTeasers(null, null, null, null, uid)

		if(teasers == null || teasers.isEmpty){
		    null
		}else{
			teasers(0)
		}
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
	def getTeasers(from: Date, to: Date, searchText: String, city: String) = _getTeasers(from, to, searchText, city, null)
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	def getTeasers() = _getTeasers(null, null, null, null, null)
	
    /**
     * searches for teasers.  by default, 
     * from is today and to is in a years time.
     * from may not be more than to.
     */
    private def _getTeasers(from: Date, to: Date, searchText: String, city: String, uid: String) = {

	    //even if no criteria are given, select only from now until a year in the future!
        val f = if(from == null) new Date() else from
        val t = if(to == null) inOneYear else to

        if(t.before(f)) throw new IllegalArgumentException("'from' must be before 'to': " + f + "/" + t)

        var jql = "select e from Event e"
        val params = new HashMap[String, Any]()

        jql += " where e.when > :from"
        params("from") = f
        
        jql += " and e.when < :to"
        params("to") = t

        if(searchText != null){
	        jql += " and (lower(e.description) like :searchText or lower(e.name) like :searchText)"
	        params("searchText") =  "%" + searchText.toLowerCase + "%"
        }
        
        if(city != null){
        	jql += " and lower(e.city) like :city"
			params("city") = "%" + city.toLowerCase + "%"
        }
        
        if(uid != null){
        	jql += " and uid = :uid"
			params("uid") = uid
        }

        if(from == null && to == null && searchText == null && city == null && uid == null) {
            //no criteria were given, so lets reduce the results to just teasers
        	jql += " and e.isTeaser = true"
        }

        jql += " order by e.when asc"
        
        val query = em.createQuery(jql, classOf[Event])
        query.setMaxResults(50)
        params.foreach( e => query.setParameter(e _1, e _2))
		val events = query.getResultList
        
		val es = new ListBuffer[EventTeaser]()
		
		for(e <- events){
		    //fill in what we can!
			val a = new Address("-", "-", e.city, "-", "-", null)
			val name = StringUtils.abbreviate(e.name, 19)
			val desc = StringUtils.abbreviate(e.description, 200)
			val et = new EventTeaser(e.uid, name, desc, e.when, e.teaserPrice, e.imgName + ".png", e.bookingSystems.toList, a)
		    es += et
		}
        
        es.toList
    }
    
    /**
     * returns an EventOffer containing all tarifs found
     * in any booking system which can sell the given event.
     */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
    @throws(classOf[UnknownEventException])
    def getOffer(uid: String): EventOffer = {

	    //TODO comment this code out, its not necessary!
	    //exampleOfHowToLookUpObjectsInJNDI

	    val teaser = getTeaser(uid)
		getOffer(teaser.bsIds)
	}
	
    private def getOffer(idBookingSystemTuples: List[(Int, String)] ): EventOffer = {

	    class RequestResponse(val id: Int, val bsCode: String){
	        var result: FOEO = null
	    }
 
	    def callAdapter(rr: RequestResponse)={
	    	log.info("getting event " + 
	    			rr.id + " from " + 
	    			rr.bsCode)
	    	val a = getAdapter(rr.bsCode)
			rr.result = a.getEvent(rr.id)
	    }
        
        //get the relevant event from the given partners

		val rrs = new ListBuffer[RequestResponse]()
        idBookingSystemTuples.foreach{ e =>
            val rr = new RequestResponse(e _1, e _2)
            rrs += rr
            callAdapter(rr)
        }
        	
        //we have submitted all our requests and they are running 
    	//async.  now lets wait for the results...  instead of 
    	//explcitly waiting, we can just start processing, 
    	//since "get" will wait if the result has not arrived yet.
		val tarifs = new ListBuffer[Tarif]()
        var templateEvent: EventOffer = null
        rrs.foreach{ request =>
                        //waits here ------\/
	        val oeo = request.result.get()
	        oeo match {
			  	case Some(event) => 
			  	    tarifs ++= event.tarifs
			  	    templateEvent = event
			  	case None =>
			  	    //nothing to do since we cannot get an offer
			}
		}
		
		//ensure all prices are in CHF
		val tarifsWithCHFPrices = tarifs.map{ t =>
		    if(t.price.currency == CHF){
		        t
		    }else{
		        t.copy(price = currencyService.convertPrice(t.price, CHF))
		    }
		}
		
		if(templateEvent == null) 
		    throw new UnknownEventException(
		            "Unable to find event " + 
		            idBookingSystemTuples.mkString(","))

		val rating = ratingService.getRating(templateEvent.uid)

		//finally create an event with all the information from
		//some booing system - doesnt matter which, they should
		//all be the same.  then stick the union of all tarifs
		//in that event and return it for the customer to 
		//peruse the tarifs / prices
		templateEvent.copy(id = 0, 
		        tarifs = bufferAsJavaList(tarifsWithCHFPrices),
		        rating = rating)
    }
    
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
    def updateRating(uid: String, rating: Int, user: User)={
        ratingService.updateRating(uid, rating, user.id)
    }
    
    /** takes a sequence of tarifs for an event, and reserves them thru the partners booking system. */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
    def reserveOffer(event: EventOffer, tarifs: Seq[Tarif]) = {

        var reservations = tarifs.par.map{ t =>
        	getAdapter(t.bookingSystem).reserveOffer(event, t)
        }

        //so that the caller isnt surprised, we 
        //should convert this back to a non-parallel sequence
        reservations.seq
    }

    /** @return a "reservation", representing the insurance if the relevant business rule matches */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
    def calculateInsurance(reservations: Seq[Reservation]) = {

        //the rule engine expects an object which has the 
        //method getTotalPrice, so lets create an adapter 
        //so that the input to the rule engine has such a method
        class Input(self: Seq[Reservation]){
            def getTotalPrice = self.foldLeft(BigDecimal.ZERO){ (acc, r) => 
                acc.add(r.tarif.getTotalPrice.value) 
            }.doubleValue
        }
        
		try{
			val priceOfInsurance = ruleEngine.insurance.getBestOutcome(new Input(reservations))
			val insurance = eeventsAdapter.getInsuranceReservation(new BigDecimal(priceOfInsurance))
			insurance
		}catch{
		    case e: NoMatchingRuleFoundException => null
		}
    }
	
	@Resource var timerService: TimerService = null
	
	private def exampleOfHowToLookUpObjectsInJNDI {
	    val context = new javax.naming.InitialContext()
	    val datasource = context.lookup("java:/jdbc/MyXaDS").asInstanceOf[DataSource]
	    log.info("just for info: got a datasource: " + datasource + " and timerservice " + timerService)
	    ctx.getTimerService()
	    
	    {
	        /*
	        ctx.setRollbackOnly()
	        //test that the following is NOT commit to the rules table, because the exception which is thrown is not declared, so it cannot be an applicaiton excpetion, and that means a rollback is done automatically by the container.
            val conn = datasource.getConnection()
            val stmt = conn.prepareStatement("insert into rule values (3, 'name', '', '', 999, 'test', 'deleteMe')")
            stmt.executeUpdate()
            stmt.close
            conn.close
            throw new AException()//"testing that the rules table was not commit to!")
            */
	    }
	}
    
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
    def removeEvent(uids: Seq[String]){
        //TODO in the real world, we would remove these.
	    //in this demo, we generated random data, and the UIDs are not 
	    //all known by our partners - since our partners are simple stub implementations.
	    //so dont do ANYTHING here!
	    uids.foreach{ uid => log.info("removing event from catalogue: " + uid) }

	    eventsUpdatedEvent.fire()
    }

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
    def addEvent(events: Set[(String, List[EventOffer])]){
        
    	//TODO send email, this is a manual process, because we need to get a picture!
	    //also need to compare the contents.  they might have the same UID, but do the partners
	    //describe them in the same way?  how do we want to describe them?
    	
	    events.foreach{ case(uid: String, partnersDescription: List[EventOffer]) =>
		    log.info("we need to add an event to our catalogue for the following event found in our partner systems: " + uid)
	    }
	    
    	//TODO so that we dont send the same email over and over and over, lets
    	//track what we have sent mails for, and reduce it to one per day.
    }
    
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
    def updateEvent(e: EventTeaser){

	    //TODO the partners are just stubs in this demo system
	    //as such, there is no need to really update the teasers.
	    //TODO set the teaser price based on the given teaser!
	    
	    log.info("updating teaser " + e.uid)

	    //TODO firing this for every updated event is a bad idea because we 
	    //dont know what the observers do.  we need to fire this event at a 
	    //coarser level!  this is why I hate event based programming models...
	    //in actual fact, we regenerate the tag clouds and reload distinct cities
	    //every time we enter this method.  if the teaser price has changed for 
	    //loads of events as they sell out, this will kill our performance right here! Booooo!!
    	eventsUpdatedEvent.fire()
    }
    
    def distinctCities = {
        //hmmm thread safety is a word that comes to mind here - a synchronized might improve performance? at least the cachedCities are immutable! ah, I love scala
        if(cachedCities.isEmpty){
            updateCachedCities()
        }
        cachedCities
    }

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
    def updateCachedCities(@Observes @EventsUpdated u: Unit) {

        val conn = datasource.getConnection
        try{
            val stmt = conn.prepareStatement("SELECT COUNT(*), CITY FROM EVENT GROUP BY CITY ORDER BY CITY")
            val rs = stmt.executeQuery
            val list = new ArrayBuffer[(String, Int)]()
            while(rs.next){
        		val cnt = rs.getInt(1)
				val city = rs.getString(2)
        		list += ((city, cnt))
            }
            rs.close
            stmt.close
        	cachedCities = list.filterNot(_._1 == "-").toList
        }finally{
        	conn.close
        }
    }
    
	/** deletes all events, except the teasers.  updates the teasers dates tho! */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Schedule(hour="12", persistent=false)
    def genRandomEvents = {
	    //delete all ratings that are not for teasers
        val deleteRatings = em.createQuery("delete from Rating r where r.eventUid in (select e.uid from Event e where e.isTeaser = false)")
        deleteRatings.executeUpdate()

        val deleteEvents = em.createQuery("delete from Event e where e.isTeaser = false")
        deleteEvents.executeUpdate()
        
        //update the dates on the remaining events (4 teasers from the initial data load)
        val selectEvents = em.createQuery("select e from Event e", classOf[Event])
        val teasers = selectEvents.getResultList()
        for(t <- teasers){
            em.merge(t.copy(when = randomEventDate))
        }
        
        //create new random events (non-teasers)
        val image= Array("BWEIY-A4", "LKJSC-W1", "HWJK-7K", "RJGK-3M")
        val cities = Array("Lausanne", "Geneva", "Zurich", "Basel", "Bern", "London", "Hamburg", "Berlin", "Paris", "Budapest",
                "Milan", "Bonn", "Palezieux", "New York", "Munich", "Vienna", "Bath", "Bristol", "Loughborough", "Ostermundigen", "Worblaufen", "Frankfurt", "Amsterdam")
        val availableBookingSystems = Array("GEDE-1", "ADFF-1", "ADFF-2")
        
        for(i <- 1 to 1001){
            import Random.{nextPrintableChar => rc, nextInt => ri} 
            val uid = rc + rc + ri(10) + rc + "-" + rc + rc + ri(10)
            val e = new Event(uid, randomWords(1, 3), cities(Random.nextInt(cities.length)), randomEventDate, randomPrice, randomWords(40, 60),
                    			availableBookingSystems(Random.nextInt(availableBookingSystems.length)),
                    			false, image(Random.nextInt(image.length)))
            em.persist(e)
        }

        //finally regenerate the insurance!
        val insurance = new Event("INS-087", "Insurance","-", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2099-12-31 23:59:59"),
                new java.math.BigDecimal("5.00"), "Obtain a 100% refund, if you cancel.",
                			"EEVT-1", false, "-")
        em.persist(insurance)
        
        em.flush() //otherwise loading any event listeners using say JDBC wont get the new events!
        cachedCities = Nil //we cannot guarantee the order of event firing (another reason events suck), so lets make this null, in case the TagCloud gets the event first, and so it can reload cities
        
        //TODO now generate some ratings... currently we only have ratings for the teasers!

        eventsUpdatedEvent.fire()
	}
	
	/** generates a sentence of words, with anywhere from n to m words in it */
	private def randomWords(n: Int, m: Int) = {
    	assert(n >= 1, "n must be at least 1!")
	    assert(n < m, "n must be less than m!")
	    var s = "";
	    for(i <- 1 to n + Random.nextInt(m-n)){
	        s += " " + words(Random.nextInt(words.length))
	    }
	    s
	}
	
	private def randomPrice = {
	    new BigDecimal(100 + Random.nextInt(500)).divide(new BigDecimal(10))
	}
	
	private def randomEventDate = {
        val when = Calendar.getInstance()
        when.set(Calendar.MILLISECOND, 0)
        when.set(Calendar.MINUTE, 0)
        when.set(Calendar.HOUR_OF_DAY, 20 + Random.nextInt(3))
        when.add(Calendar.DAY_OF_YEAR, 10 + Random.nextInt(20))
        when.getTime
	}
	
	private val words = 
"""
Phasellus a viverra dui. Cras lacus dui, cursus a malesuada ac, tempor vel libero. Morbi sed est massa, non vestibulum ligula. Maecenas vel lectus ac mauris elementum eleifend. Curabitur et eros lectus, vitae gravida quam. Cras cursus neque vel nisl sagittis volutpat. Mauris suscipit vehicula enim. Duis venenatis ultrices ligula, eu cursus dui blandit vel. Aliquam eleifend mi a mi congue egestas egestas ipsum rutrum. Quisque sed ipsum eget leo imperdiet posuere.
Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Nullam quis felis posuere tellus consequat auctor. Donec ut consectetur massa. Proin ullamcorper risus eget neque condimentum quis elementum risus volutpat. Vivamus a neque augue. Vivamus metus nulla, aliquam ut feugiat eu, posuere sit amet sem. Aliquam diam sem, euismod non auctor sed, tempus non augue. Nulla at magna eu nibh ultricies luctus. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Nullam congue cursus eros in eleifend.
Vestibulum at odio orci, non ullamcorper nibh. Phasellus scelerisque dictum blandit. Aliquam erat volutpat. Nam dui dolor, eleifend porta bibendum vitae, interdum non augue. Aliquam aliquam pellentesque porttitor. Nam sed dolor arcu, et rhoncus quam. Suspendisse at tortor odio, quis blandit sapien. Curabitur mattis tincidunt metus sed mattis. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Etiam tincidunt egestas nisl, eget condimentum nibh malesuada nec. Donec et facilisis diam. Nunc vel felis at erat laoreet ullamcorper. Fusce semper ornare lacus vitae lacinia. Vestibulum et justo leo. Donec non eros neque.
Vivamus sed sollicitudin lorem. Fusce molestie lectus ut ante dapibus non porta nunc bibendum. Nunc ac nisl lacus, non placerat risus. Nullam euismod consectetur eleifend. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. In fringilla, magna vitae laoreet fermentum, justo massa adipiscing magna, nec porta felis metus vel nisl. Donec fringilla cursus vestibulum.
Praesent condimentum massa quis magna fringilla at bibendum purus fringilla. Etiam id malesuada nunc. Duis nec mi velit. Fusce lacinia ligula eget sapien dignissim convallis. Mauris libero ante, vulputate a rutrum in, pellentesque quis eros. Maecenas porttitor diam pellentesque quam pellentesque tempor. In non velit quis dolor hendrerit sagittis. Morbi adipiscing tempus elit aliquet pulvinar. Morbi interdum, est eu porttitor laoreet, risus magna mollis turpis, nec ullamcorper enim augue nec mauris. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Fusce vitae lorem diam. Nunc dignissim tempor vehicula. Suspendisse potenti. Suspendisse lobortis metus vitae risus porttitor eu tristique lectus ornare.
""".split(" ")


	@RolesAllowed(Array("registered"))
    def addComment(comment: String, eventUid: String) = {

	    val t = getTeaser(eventUid)
	    val user = userService.getLoggedInUser.get
	    val pushContext = PushContextFactory.getDefault().getPushContext()

	    em.persist(new Comment(eventUid, comment, user, new Date()))
	    
	    val q = em.createQuery("select distinct c.user from Comment c where c.eventUid = :eventUid and c.user.id <> :userId", classOf[User])
	    q.setParameter("eventUid", eventUid)
	    q.setParameter("userId", user.id)
	    val users = q.getResultList()
	    users.foreach{ u =>
	        
	        //TODO only send to users which have an active session, because otherwise we are wasting resources!
	        //TODO would be better to use a secret code which only belongs to this user, otherwise a hacker could intercept our messages because the client subscribes to this channel
	        
	    	pushContext.push("/user_" + u.id, 
	    	        new FacesMessage(FacesMessage.SEVERITY_INFO, 
	    	                user.name + " posted on '" + t.name + "'", 
	    	                comment + "<br/><a href='" + config.c.siteUrl + "eventDetails.jsf?uid=" + eventUid + "'>goto event</a>"
	                )
	    		)
	    }
	}

	/**
	 * @return a list of teasers which the current user may find interesting, based on what is in their cart, and
	 * what they previously bought, if known (depends on them being logged in)
	 */
	def getEventsOthersBought(reservations: JList[ch.maxant.scalabook.shop.bom.Reservation], email: String) = {
	
	    val events = reservations.par.map{r =>
	        getSuggestions(r.eventUid, email)
		}
	    
	    val v = events.seq.flatten
	    val w = v.groupBy(_._1).map{case (uid, counts) =>
	        (uid, counts.map(_._2).sum)
	    }
	    
	    //we just searched for any event which could be relevant, but we might have loads now. so lets take the top 10
	    
	    val orderedEvents = new ListBuffer[(String,Int)]
	    w.foreach{orderedEvents += _}

	    val eventUidsToReturn = orderedEvents.sortBy(_._2).map(_._1).take(10)
	    
	    val teasers = eventUidsToReturn.map(getTeaser(_))
	    val ret = teasers.filter(_ != null)
	    ret
	}

    private def getSuggestions(uid: String, email: String) = {
        
        val url = new URL(config.c.suggestionsUrl + URLEncoder.encode(uid) + "/" + URLEncoder.encode(email))
        val is = url.openStream()
		val baos = new ByteArrayOutputStream
		try {
		    var curr = is.read()
		    while(curr >= 0){
		        baos.write(curr)
		        curr = is.read()
		    }
		} finally {
			is.close()
		}
		
        val xml = XML.loadString(baos.toString())

        val events = xml \\ "event"
        
        events.map{event =>
            val uid = (event \ "uid").text
    		val cnt = (event \ "count").text.toInt
    		(uid, cnt)
        }
        
//		for(e <- events.child filterNot (_.toString.trim == "")) yield {
    }
	
}

//for test only
@ApplicationException(rollback=false)
class AException extends RuntimeException
