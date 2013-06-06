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
package ch.maxant.scalabook.shop.ui
import javax.inject.Named
import javax.annotation.PostConstruct
import javax.enterprise.context.RequestScoped
import java.util.Date
import scala.beans.BeanProperty
import java.util.{List => JList}
import ch.maxant.scalabook.shop.bom.EventTeaser
import javax.inject.Inject
import javax.ejb.EJB
import ch.maxant.scalabook.shop.events.services.EventService
import java.util.logging.Logger
import ch.maxant.scalabook.shop.util.Conversions.asDateFromDateOnly
import ch.maxant.scalabook.shop.util.Constants._
import java.text.ParseException
import javax.faces.context.FacesContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.Cookie
import ch.maxant.scalabook.shop.common.services.UserService
import java.security.Principal

/**
 * a controller, responsible for authentication.
 */
@Named
@RequestScoped
class Login extends WithFacesContext {

	//would like to make this a constant in a companion object, but cannot:
    //Caused by: org.jboss.weld.exceptions.UnproxyableResolutionException: WELD-001437 Normal scoped bean class ch.maxant.scalabook.ui.Login is not proxyable because the type is final or it contains a final method public static final java.lang.String ch.maxant.scalabook.ui.Login.EmailCookieName() - Managed Bean [class ch.maxant.scalabook.ui.Login] with qualifiers [@Any @Default @Named].
    //its a CDI problem, the EJB container doesn't seem to care.  See UserService for an example
    val EmailCookieName = "ch.maxant.scalabook.events.email"
    
    @Inject var log: Logger = null
    @Inject var model: Model = null
    @Inject var userService: UserService = null
    @Inject var principal: Principal = null
    
    private var email: String = null

	@BeanProperty
	var error: String = null

	@BeanProperty
	var password: String = null

	def login() = {
        try{
            error = null
            request.login(email, password)
        	log.info("logged in successfully as " + email + " with sessId " + request.getSession().getId())
        }catch{
            case e: Exception => 
                error = "could not log you in"
        }
        "index"
    }
    
    def logout() = {
        request.logout()
    	model.user = null
    	request.getSession.invalidate
    	"home" //this is defined in a navigation rule
    }
	
	def getEmail() = {
	    if(email == null){
	        //perhaps its already set in a cookie?
	        var cookies = request.getCookies
	        cookies = if(cookies != null){
	        	cookies.filter(c => EmailCookieName == c.getName())
	        }else{
	            Array[Cookie]()
	        }
	        if(cookies.size > 0){
	            val cookie = cookies(0)
	            email = cookie.getValue()
	        }
	    }
	    
	    email
    }
	
	def setEmail(email: String) = {
	    this.email = email
	}
	
    def isLoggedIn() = {
        userService.isLoggedIn
        //alternatively: request.getUserPrincipal() != null && request.getUserPrincipal().getName() != null && request.getUserPrincipal().getName() != "anonymous" //watch out - JBoss specific!
    }
    
    def isUserInRole(role: String) = {
        request.isUserInRole(role)
    }
    
    def getUser = {
        //lazy load
        if(model.user == null && isLoggedIn){
        	model.user = userService.getLoggedInUser match {
        	    case Some(u) => u
        	    case None => null
        	}

        	//do this here, rather than in the #login method, since we may have been logged in using the login.jsp page, if the user tried a secure url!
		    val cookie = new Cookie(EmailCookieName, email)
	        response.addCookie(cookie)
        }
        model.user
    }
}
