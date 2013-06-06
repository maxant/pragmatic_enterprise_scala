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
package ch.maxant.scalabook.shop.orders.services.accounting
import ch.maxant.scalabook.shop.util.StateMachine

object AccountingStates extends Enumeration {
	val Pending = Value //we are about to send, or have sent a request to our partner. the result is currently not known
	val Booked = Value //the result was successfully received from our partner - they will charge us for the purchase
	val Cancelled = Value //the result was in the pending/booked state for too long, and so the booking was cancelled with the partner
	val Completed = Value // the customer has the ability to print their tickets and we have the payment from them
	
	val stateMachine = new StateMachine("ch/maxant/scalabook/shop/orders/services/accounting/AccountingStates.scxml")
}