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
package ch.maxant.scalabook.play20.plugins.rolebasedauth

import play.api.mvc.RequestHeader
import play.api.mvc.Results
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.mvc.Result
import play.api.mvc.Action
import play.api.mvc.Security
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

/** See Secured trait */
object Secured {
    /** the name of the request parameter which contains 
     * the username */
    val Username = "username"

    /** this is the model with which Secured works */
	case class User(username: String, roles: List[String]) {
        def isInRole(role: String) = roles.contains(role)
    }
}

/**
 * Based on Zentasks sample of Play 2.0.3, but improved to allow role based authorization.
 * 
 * This is a library trait.  Any application requiring role based security needs to subclass this trait and 
 * provide two things: 1) a way to get the user from their username, and 2) a function providing implementation
 * to use in the case of a failed login.
 * 
 * see IsAuthorized(String)(success)(failure) for details.
 */
abstract trait Secured { self: Controller =>
  
    private def getUsername(request: RequestHeader) = request.session.get(Secured.Username)

    /** Redirect to login if the user in not authorized. */
    private def onUnauthorized(request: RequestHeader) = Results.Redirect(controllers.routes.LoginController.login)
  
    /**
     * This method checks whether the calling user has the authorisation to request the url.
     */
    def IsAuthorized(role: String)(success: => Option[Secured.User] => Request[AnyContent] => Result)(failure: (String, String) => play.api.templates.Html) = {
	    val ret = Security.Authenticated(getUsername, onUnauthorized) { username =>
  			val user = findUser(Some(username))
  			//pass null as the Database here, because we expect this "detached" object to already have the roles loaded!
  			//it is the responsibility of the loader (UserRepo) to have loaded the roles too, since the TX is finished.
  			user.filter(_.roles.contains(role)).map { u =>
                Action{ request => 
                    success(Some(u))(request)
                }
  			}.getOrElse{
  	        	Action{
  	                 self.Ok(failure(username, role)).withNewSession
                     //new session,so that the username isnt in there anymore
  	           	}
  			}
	    }
	    ret
    }

    /** sub traits must provide a way to get the user */
    def findUser(username: Option[String]): Option[Secured.User]
}