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

import ch.maxant.scalabook.validation.integration.UserRepository.User
import ch.maxant.scalabook.play20.plugins.xasupport.XAContext
import java.sql.Timestamp

abstract trait ValidationRepo {

    def addValidation(bookingRef: String, validator: User)(implicit ctx: XAContext)
}

case class Validation(
    bookingRef: String, userId: String, when: Timestamp){
}
