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
import ch.maxant.scalabook.validation.integration.Ticket
import ch.maxant.scalabook.validation.integration.UserRepository
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.mvc.Controller

/**
 * the interface of a callback used to render the response for the validated tickets at an event.
 */
abstract trait EventResponder { self: Controller =>

    def showSuccess(eventUid: String, tickets: Seq[Ticket], totalNumberOfTickets: Int, user: Option[UserRepository.User], request: Request[AnyContent]): play.api.mvc.Result
    
    def showFailure(msg: String, user: Option[UserRepository.User]): play.api.mvc.Result
}