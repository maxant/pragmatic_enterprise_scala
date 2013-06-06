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
package ch.maxant.scalabook.shop.bom
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import java.util.Date

class NoExpiredReservationsValidator extends ConstraintValidator[NoExpiredReservations, List[Reservation]] {

	override def initialize(x: NoExpiredReservations) {
	  //use the annotation in some way, if you need to
	}

	override def isValid(reservations: List[Reservation], context: ConstraintValidatorContext) = {
        //check none have expired
        //if any have, then the client
        //should now allow any of them
        //to be sold.
        val now = new Date()
        for(r <- reservations){
        	if(r.expiry.before(now)){
        		false
        	}
        }
        true
	}
}