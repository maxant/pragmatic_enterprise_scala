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
package ch.maxant.scalabook.shop.common.services

import java.util.logging.Logger
import org.apache.commons.codec.binary.Base64
import org.junit.Test
import java.util.{ArrayList => JList}
import scala.collection.JavaConversions._
import ch.maxant.scalabook.shop.common.persistence.jpa.User
import javax.annotation.Resource
import javax.ejb.LocalBean
import javax.ejb.SessionContext
import javax.ejb.Stateless
import javax.ejb.TransactionAttribute
import javax.ejb.TransactionAttributeType
import javax.ejb.TransactionManagement
import javax.ejb.TransactionManagementType
import javax.inject.Inject
import javax.persistence.Access
import javax.persistence.Entity
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import ch.maxant.scalabook.shop.common.persistence.jpa.Role
import scala.util.Random

@Stateless
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
class UserService {
    
    @Inject var log: Logger = null
	@PersistenceContext(unitName="theDatabase") var em: EntityManager = null
    @Resource var ctx: SessionContext = null

    def isLoggedIn = {
		val cp = ctx.getCallerPrincipal() 
		var name: String = null
		if(cp != null){
		    name = ctx.getCallerPrincipal().getName()
		}
		
		name != null && name != "anonymous" //watch out - JBoss specific!
    }

    def someTestMethod {
        if(ctx.isCallerInRole("admin")){
           //...
        }
    }
    
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
    def getLoggedInUser = if(isLoggedIn) Some(findUser(ctx.getCallerPrincipal().getName())) else None
	
	/** gets the user from the database */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	def findUser(email: String) = {
		val query = em.createQuery("select u from User u where u.email = :email", classOf[User])
		query.setParameter("email", email)
		val u = query.getSingleResult

		//TODO delete me- test code!
		//createUser(Random.nextString(10), "Test", "asdf")
   
		u
    }
    
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
    def createUser(email: String, name: String, password: String) = {

	    val algorithm = java.security.MessageDigest.getInstance(UserService.HashAlgorithm)
	    val passBytes = password.getBytes// [97, 115, 100, 102, -4] -> DO NOT use UTF8 encoding, because it doesn't work.  JBoss doesn't do it either.
        val hashValue = algorithm.digest(passBytes) //[18, -28, 14, 43, 15, 110, -92, 42, -91, 41, 47, -103, -102, -65, 72, 89] sun MD5
        val hashedPassword = new String(Base64.encodeBase64(hashValue))
        println("hashed password is: " + hashedPassword)
        //asdfü => EuQOKw9upCqlKS+Zmr9IWQ== for MD5
        
        //TODO finish this method - so far its only useful for generating hash encoded passwords
        val roles = List(new Role(email, "registered"))
        val user = new User(email, hashedPassword, name, roles)
	    
	    em.persist(user)
    }
	
	@Test
	def testCreateUser = {
	    //createUser("john@maxant.co.uk", "John", "asdf")
	    createUser("jake@maxant.co.uk", "John", "asdf")
	}
}

object UserService {
    val HashAlgorithm = "MD5"
}