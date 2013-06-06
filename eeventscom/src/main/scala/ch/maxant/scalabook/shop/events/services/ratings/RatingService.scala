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
package ch.maxant.scalabook.shop.events.services.ratings
import javax.ejb.Stateless
import javax.persistence.PersistenceContext
import javax.persistence.EntityManager
import ch.maxant.scalabook.shop.util.Constants._
import org.junit.Test
import javax.ejb.SessionContext
import javax.ejb.LocalBean
import javax.ejb.TransactionManagement
import javax.inject.Inject
import java.util.logging.Logger
import javax.ejb.TransactionManagementType
import javax.annotation.Resource
import javax.ejb.TransactionAttribute
import javax.ejb.TransactionAttributeType
import javax.ejb.EJB
import javax.persistence.Persistence
import java.util.Properties
import javax.persistence.LockModeType
import java.math.BigInteger
import ch.maxant.scalabook.shop.common.services.UserService
import ch.maxant.scalabook.shop.{bom => b}
import javax.sql.DataSource
import java.sql.PreparedStatement
import java.sql.ResultSet

@Stateless
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
private[services] class RatingService {
    
    @Inject var log: Logger = null
	@PersistenceContext(unitName="theDatabase") var em: EntityManager = null
    @Resource var ctx: SessionContext = null
    @EJB var userService: UserService = null
    @Resource(name="java:/jdbc/MyXaDS") protected var datasource: DataSource = null

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	def getRating(eventUid: String) = {
        
        //read the rating, if it exists, then read whether the user can modify the rating
        
		val q1 = em.createQuery("select r from Rating r where r.eventUid = :eventUid", classOf[Rating])
		q1.setParameter("eventUid", eventUid)
		val rs = q1.getResultList //rather than singleResult, coz we dont want an exception
		val r = if(rs.isEmpty) 0 else rs.get(0).avgRating

		val email = ctx.getCallerPrincipal.getName
		var alreadyRated = false
		if(email != null){
			val q2 = em.createQuery("""
				SELECT count(*)
				FROM RatingUserMapping rum, User u 
				where 
					rum.userId = u.id 
					and u.email = :email
					and rum.eventUid = :eventUid
				""")
			q2.setParameter("email", email)
			q2.setParameter("eventUid", eventUid)
			val num = q2.getSingleResult.asInstanceOf[Number]
			alreadyRated = num.intValue > 0
		}

		//if user is logged in, and has not already rated the event, then it is ratable
		val rateable = userService.isLoggedIn && !alreadyRated
		
		//test code! TODO delete me
		//getAvgRatingJDBC(eventUid)
		//val r2 = getRatingById(eventUid)
		
		new b.Rating(r.toInt, !rateable, 0/*TODO*/)
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    def getAvgRatingJDBC(eventUid: String) = {

        val conn = datasource.getConnection
        var stmt: Option[PreparedStatement] = null
        var rs: ResultSet = null
        try{
            stmt = Some(conn.prepareStatement("select * from rating r where r.event_uid = ?"))
            stmt.get.setString(1, eventUid) //not zero based!
            rs = stmt.get.executeQuery
            val r = if(!rs.next()) {
                0
            }else{
                val sumRatings = rs.getLong("sum_ratings")
                val numRatings = rs.getInt("num_ratings")
                (BigDecimal(sumRatings) / BigDecimal(numRatings)).setScale(0, BigDecimal.RoundingMode.UP).toInt
            }
            r
        }finally{
            if(rs != null) rs.close
            stmt.map(_.close)
            conn.close
        }

    }

    def getRatingById(eventUid: String) = {
        em.find(classOf[Rating], eventUid)
    }    
    
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	def updateRating(eventUid: String, rating: Int, userId: Long)= {
	    //i want to insert if the row doesnt exist, otherwise i want to update.
	    //but what happens if two users click on the rating widget at the same time?
	    //i have several options:
		//1) select for update + update -> the update can be "escalated" to lock a table or page, depending on many factors which can lead to dead locks
	    //2) select and then update or insert and monitor the update count. add the original value to the update statement and if update count is 0, try again
	    //3) write a stored procedure - doesnt help - at first glance it appears as though i am making the operation atomic, but the DB is multithreaded too
	    //   and could suspend a thread after a select and before the update.
	    //4) go native! h2 supports "merge", others have their own thing, mysql used here has "insert into T (x,y...) values (...) on duplicate key update...
	    val q = em.createNativeQuery(
	            """ INSERT INTO RATING (EVENT_UID, SUM_RATINGS, NUM_RATINGS)
	            	VALUES (:eventUid, :newRating, 1) 
	            	ON DUPLICATE KEY UPDATE
	            SUM_RATINGS = SUM_RATINGS + :newRating, 
	            NUM_RATINGS = NUM_RATINGS + 1
	            """) 
	    q.setParameter("newRating", rating)
	    q.setParameter("eventUid", eventUid)
	    q.executeUpdate

		//now block the user from doing another rating
		em.persist(RatingUserMapping(-1, eventUid, userId, rating))
		
		em.flush() //without this, the RUM isnt visible to a select which is done in JPQL inside #getRating(eventUid) - because we used a native query above, rather than JPQL and Mapped Entities!
		
		getRating(eventUid)
	}

	@Test
	def testCreateUser = {

	    import org.easymock.EasyMock._;
	    
	    ctx = createMock(classOf[SessionContext])
	    //("jane@maxant.ch")

	    val properties = new Properties
        properties.put("javax.persistence.jdbc.driver", "org.h2.Driver")
	    properties.put("javax.persistence.jdbc.url", "jdbc:h2:tcp://localhost/data/scalabook;AUTO_RECONNECT=TRUE;DB_CLOSE_ON_EXIT=FALSE")
	    properties.put("javax.persistence.jdbc.user", "sas")
	    properties.put("javax.persistence.jdbc.password", "password")
	    properties.put("eclipselink.logging.level", "FINE")

	    em = Persistence.createEntityManagerFactory("theDatabase").createEntityManager
//	    em = Persistence.createEntityManagerFactory("theDatabase", properties).createEntityManager(properties)
	    //createEntityManagerFactory("myPu").createEntityManager

	    assert(getRating("asdf").getRating == 3)
	    assert(getRating("LKJSC-W1").getRating == 4)
//	    
//    val emf = new LocalContainerEntityManagerFactoryBean();
//    emf.setPersistenceProviderClass(org.eclipse.persistence.jpa.PersistenceProvider.class); //If your using eclipse or change it to whatever you're using
//    emf.setPackagesToScan("com.yourpkg"); //The packages to search for Entities, line required to avoid looking into the persistence.xml
//    emf.setPersistenceUnitName(SysConstants.SysConfigPU);
//    emf.setJpaPropertyMap(properties);
//    emf.setLoadTimeWeaver(new ReflectiveLoadTimeWeaver()); //required unless you know what your doing
//    return emfConfigBean;
    
	}
}

