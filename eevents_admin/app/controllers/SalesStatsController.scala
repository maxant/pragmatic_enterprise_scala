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

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import play.api.libs.concurrent.Promise
import scala.xml.Elem
import scala.xml.NodeSeq
import ch.maxant.scalabook.play20.plugins.neo4j.Neo4JSupport
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.RelationshipType
import System.{currentTimeMillis => now}
import org.neo4j.cypher.javacompat.ExecutionEngine
import ch.maxant.scalabook.play20.plugins.LoggingHelp
import java.util.HashMap
import scala.collection.mutable.ListBuffer

object SalesStatsController extends Controller with Neo4JSupport with LoggingHelp {

	val CustomersWhoBoughtXAlsoBoughtYQuery = 
//, currentUser=node:users(email={email})
//WHERE NOT(currentUser-[:BOUGHT]-otherStuff)
//, currentUser
"""
START event=node:events(uid={uid})
MATCH event<-[:BOUGHT]-person-[:BOUGHT]->otherStuff
RETURN otherStuff.uid, count(otherStuff)
ORDER BY count(otherStuff) DESC, otherStuff.uid ASC 
LIMIT 10
"""
    
    val EventUid = "eventUid"
	val UserEmail = "email"
    val EmailKey = "email"
    val EventKey = "uid"
    val UsersIndexName = "users"
    val EventsIndexName = "events"

    /** adds a sale of an event to a user to the sales statistics database */
    def addSale = Action { implicit request =>

        val eventUid = request.queryString(EventUid)(0)
        val emailAddress = request.queryString(UserEmail)(0)
        
        withNeo4J{ implicit db =>
		    val indexManager = db.index
		    val userIndex = indexManager.forNodes(UsersIndexName)
		    val eventIndex = indexManager.forNodes(EventsIndexName)

		    // **********
		    // get or create user
		    // **********
		    var userNode: Node = null
	        var indexHits = userIndex.get(EmailKey, emailAddress)
	        if(indexHits.size() < 1){
	        	userNode = db.createNode()
				userNode.setProperty(EmailKey, emailAddress)
				userIndex.add(userNode, EmailKey, emailAddress)
	        }else{
	            userNode = indexHits.next()
	        }

		    // **********
		    // get or create event
		    // **********
		    var eventNode: Node = null
	        indexHits = eventIndex.get(EventKey, eventUid)
	        if(indexHits.size() < 1){
	        	eventNode = db.createNode()
				eventNode.setProperty(EventKey, eventUid)
				eventIndex.add(eventNode, EventKey, eventUid)
	        }else{
	            eventNode = indexHits.next()
	        }

		    // **********
		    // create relationship
		    // **********
			userNode.createRelationshipTo(eventNode, RelTypes.BOUGHT)

		    // **********
		    // return result
		    // **********
			Ok(<result>OK</result>)
        }
    }

	/** returns XML containing events which may be interesting to the given user, as they just added eventUid to their cart */
	def customerWhoBought(eventUid: String, email: String) = Action { implicit request => 
	    
	    withNeo4J{ db =>
	        val engine = new ExecutionEngine(db)
	
	        val params = new HashMap[String, Object]()
	        params.put(EmailKey, email)
	        params.put(EventKey, eventUid)
	        
	        var start = now
			var r = engine.execute(CustomersWhoBoughtXAlsoBoughtYQuery, params)
			val time = now - start
			log.info("got results " + r + " in " + time + " ms"); start = System.currentTimeMillis
println("got results " + r + " in " + time + " ms"); start = System.currentTimeMillis
		    
	        val events = ListBuffer[(Any,Any)]()
	        val rs = r.iterator()
	        while(rs.hasNext()){
	            val row = rs.next()
	            events += row.get("otherStuff.uid") -> row.get("count(otherStuff)")
	        }
	        val xml = 
<result>
<timeMilliseconds>{(time)}</timeMilliseconds>
<events>{events.map{e =>
	<event><uid>{e._1}</uid><count>{e._2}</count></event>
}}

</events>
</result>
			Ok(xml)
	    }
    }

}

object RelTypes extends Enumeration {

	protected class MyVal() extends Val() with RelationshipType {
	    def name = this.toString
	}
        
	val BOUGHT = new MyVal() // indicates that a user has bought tickets to an event
}
