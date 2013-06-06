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
package ch.maxant.scalabook.shop.orders.services.payment
import ch.maxant.scalabook.shop.util.StateMachine

object PaymentStates extends Enumeration {
    val Created = Value //we are about to request a payment token
	val AddedToken = Value //we added the token, and the user can now go do the payment
    val UserLandedBackAtOurSite = Value //the user has done the payment, but we have not yet validated it
    val Validated = Value //the users payment was also successfully validated
    val FailedToValidate = Value //the users payment was not validated

	val stateMachine = new StateMachine("ch/maxant/scalabook/shop/orders/services/payment/PaymentStates.scxml")

}