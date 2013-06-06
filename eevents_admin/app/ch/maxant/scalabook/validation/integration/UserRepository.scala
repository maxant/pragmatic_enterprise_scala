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
package ch.maxant.scalabook.validation.integration

import play.api.db._
import play.api.Play.current
import scala.slick.driver.MySQLDriver.simple._
import org.apache.commons.codec.binary.Base64
import play.api.Logger
import java.security.MessageDigest

object UserRepository extends UserRepo {

    val HashAlgo = "MD5"

    /* *** USERS *** */
        
    case class User(email: String, name: String, password: String){
    	var _roles: List[Role] = null
		
    	//the implicit is needed by the "list" method.
    	//if the object is "detached", then there is no hope 
    	//anyway, so the caller better hope the roles were 
    	//already lazily loaded. just like hibernate, it 
    	//would fail hard, if there is no session, 
    	//coz the default one is null.
    	def roles(implicit session: Session = null) = {
    		if(_roles == null){
    			_roles = Query(Roles) where(_.email === email) list
    		}
    		_roles
    	}

        def isInRole(role: String)(implicit session: Session = null) = {
            roles.map(_.role).contains(role)
        }
    }

    val Users = new Table[User]("USER"){
		def email = column[String]("EMAIL", O.PrimaryKey)
		def name = column[String]("NAME")
		def password = column[String]("PASSWORD")
		def * = email ~ name ~ password <> (User, User.unapply _)
    }

    /* *** ROLES *** */
    
    case class Role(email: String, role: String)
    
    val Roles = new Table[Role]("ROLE"){
		def email = column[String]("EMAIL")
		def role = column[String]("ROLE")
		def * = email ~ role <> (Role, Role.unapply _)
		def user = Users.where(_.email === email) 
    }

    /* *** BUSINESS METHODS *** */
    
    def findByEmail(email: Option[String]): Option[User] = {
        email match {
            case Some(s) => {
			    AppDB.database.withSession { implicit db:Session =>
		
			        val q = Query(Users) where(_.email === email)
			        
			        //Logger.debug(q.selectStatement)
			        
			        val users = q.list
			        if(users.size == 0){
			            None
			        }else{
			            val user = users(0) //email is unique in the database, so this list will only ever have one entry
		                user.roles //eagerly load them ready for when we are no longer in a session
			            Some(user)
			        }
			    }
            }
            case None    => None
        }
    }
	
    /** @return None if the combination of email and password 
     * are invalid, otherwise Some(user) containing a user 
     * object for the given email address. */
    def authenticate(email: String, password: String): Option[User] = {
        val user = findByEmail(Some(email))
        if(user.isDefined){
            
	        val algorithm = MessageDigest.getInstance(HashAlgo)
		    val passBytes = password.getBytes
	        val hashValue = algorithm.digest(passBytes)
	        val data = Base64.encodeBase64(hashValue)
	        val hashedPassword = new String(data)

            if(hashedPassword == user.get.password){
                user
            }else{
                None
            }
            
        }else{
            None
        }
	}
}
    
abstract trait UserRepo {
    def findByEmail(email: Option[String]): Option[UserRepository.User]
    def authenticate(email: String, password: String): Option[UserRepository.User]
}    
