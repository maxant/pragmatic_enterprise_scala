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
package ch.maxant.scalabook.shop.util;

import org.junit.Assert._
import org.junit.Test

class StateMachineTest {

	@Test
	def testOrderStates = {
	    
	    val machine = new StateMachine("ch/maxant/scalabook/shop/orders/services/OrderStates.scxml")
	    
	    assertEquals(2, machine.states.size)
	    
	    assertEquals("Pending", machine.initialState.id)
	    assertEquals(machine.initialState, machine.states(0))
	    assertFalse(machine.initialState.isFinal)
	    
	    assertEquals("Paid", machine.states(1).id)
	    assertTrue(machine.states(1).isFinal)
	    
	    assertTrue(machine.isValidTransition("Pending", "Paid"))
	    assertFalse(machine.isValidTransition("Pending", "asdf"))
	    
	    assertFalse(machine.isValidTransition("Paid", "Pending"))
	    assertFalse(machine.isValidTransition("Paid", "asdf"))
	    
	    assertFalse(machine.isValidTransition("asdf", "Pending"))
	    assertFalse(machine.isValidTransition("asdf", "Paid"))
	}

	@Test
	def testOrderItemStates = {
	    
	    val machine = new StateMachine("ch/maxant/scalabook/services/OrderItemStates.scxml")
	    
	    assertEquals(3, machine.states.size)

	    assertEquals("Created", machine.initialState.id)
	    assertEquals(machine.initialState, machine.states(0))
	    assertFalse(machine.initialState.isFinal)
	    
	    assertEquals("Validated", machine.states(1).id)
	    assertFalse(machine.states(1).isFinal)
	    
	    assertEquals("Printed", machine.states(2).id)
	    assertTrue(machine.states(2).isFinal)
	    
	    assertTrue(machine.isValidTransition("Created", "Validated"))
	    assertFalse(machine.isValidTransition("Created", "Printed"))
	    assertFalse(machine.isValidTransition("Created", "asdf"))

	    assertTrue(machine.isValidTransition("Validated", "Printed"))
	    assertFalse(machine.isValidTransition("Validated", "Created"))
	    assertFalse(machine.isValidTransition("Validated", "asdf"))

	    assertFalse(machine.isValidTransition("Printed", "Validated"))
	    assertFalse(machine.isValidTransition("Printed", "Created"))
	    assertFalse(machine.isValidTransition("Printed", "asdf"))

	    assertFalse(machine.isValidTransition("asdf", "Created"))
	    assertFalse(machine.isValidTransition("asdf", "Validated"))
	    assertFalse(machine.isValidTransition("asdf", "Printed"))
	}
	
}
