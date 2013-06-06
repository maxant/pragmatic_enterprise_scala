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
package ch.maxant.scalabook.shop.util
import scala.xml.XML
import scala.collection.mutable.ListBuffer

class StateMachine(fileName: String) {

    /** all states in the finite state machine */
    val states= init

    private def init ={
    	val is = this.getClass().getClassLoader().getResourceAsStream(fileName)
		val scxml = XML.load(is)
		is.close
		val initStateName = (scxml \ "@initialstate").text
		val states = new ListBuffer[State]
		scxml match {
			case <scxml>{ss @ _*}</scxml> => {
				for (s @ <state>{_*}</state> <- ss) {
					val transitions = new ListBuffer[Transition]
					for (t @ <transition>{_*}</transition> <- s.child) {
						val event = (t \ "@event").text
						val target = (t \ "@target").text
						transitions += Transition(event, target)
					}
					val id = (s \ "@id").text
					val isFinal = "true" == (s \ "@final").text
					states += State(id, transitions.toSet, isFinal)
				}
			}
		}		

		//sort, so that initialState is at the start
		
		states.sortBy(_.id != initStateName).toList
    }

    /** the state which is marked as the initial state */
    lazy val initialState = states(0)

    /** 
     * checks if the transition from the oldState to the newState is valid.  
     * if either oldState or newState is unknown, then false is returned.
     * @return true if there is a direct transition available from oldState to newState.
     * 
     */
    def isValidTransition(oldState: String, newState: String) = {
        val transitionsOfRelevantState = states.find(_.id == oldState).map(_.validTransistions)
        if(transitionsOfRelevantState.isDefined){
            transitionsOfRelevantState.get.map(_.target).contains(newState)
        }else{
        	false
        }
    }

    /** similar to #isValidTransition(String, String) except it throws an IllegalStateException, if the transition is not valid */
    def checkValidTransition(oldState: String, newState: String) = {
	    if(!isValidTransition(oldState, newState)) {
	    	throw new IllegalStateException("Not allowed to transition between " + oldState + " and " + newState)
	    }
    }
}

case class State(id: String, validTransistions: Set[Transition], isFinal: Boolean)
case class Transition(event: String, target: String)