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
package ch.maxant.scalabook.shop.orders.services
import ch.maxant.scalabook.shop.util.StateMachine

object OrderItemStates extends Enumeration {
	val Created = Value //before going to the payment partner, the order and its items are created
	val Printed = Value //its been printed at least one time.
	val Validated = Value //the ticket has been scanned at an event

	val stateMachine = new StateMachine("ch/maxant/scalabook/shop/orders/services/OrderItemStates.scxml")

	/** the enum for the given string, or None */
	def valueOf(s: String) = {
	    values.find(_.toString == s)
	}
}
