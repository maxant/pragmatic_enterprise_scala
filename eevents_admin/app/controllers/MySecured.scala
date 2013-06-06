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
package controllers
import ch.maxant.scalabook.play20.plugins.rolebasedauth.Secured
import ch.maxant.scalabook.validation.integration.UserRepository
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.mvc.Result
import ch.maxant.scalabook.validation.integration.UserRepo

/** application specific implementation of Secured, so that we can tell Secured how to get the user and what to do in case of a failure */
trait MySecured extends Secured { self: Controller =>
    
    var userRepository: UserRepo = UserRepository
    
    /** use this method rather than the super method with the same name, since we shall always deal with failure in the same way. */
    def IsAuthorized(role: String)(success: => Option[Secured.User] => Request[Any] => Result) = {
        super.IsAuthorized(role)(success)(failure)
    }

    /** tell Secured how to get the user from their username */
    override def findUser(email: Option[String]): Option[Secured.User] = {
        userRepository.findByEmail(email).map{u => 
            new Secured.User(u.email, u.roles.map(_.role))
    	}
    }

    /** application specific failure where the user is not logged in, or is not in the required role */
    private def failure(email: String, role: String): play.api.templates.Html = {
		views.html.error(
			"Unknown email or not in role '" + role + "'.", 
			userRepository.findByEmail(Some(email))
		)
    }
    
    implicit def securedUser2ApplicationUser(su: Option[Secured.User]): Option[UserRepository.User] = {
        su match {
            case Some(u) => userRepository.findByEmail(Some(u.username))
            case None => None
        }
    }
}