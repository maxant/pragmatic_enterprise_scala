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
package ch.maxant.scalabook.play20.plugins.neo4j

import play.api.mvc.RequestHeader
import play.api.mvc.Results
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.mvc.Result
import play.api.mvc.Action
import play.api.mvc.Security
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import bitronix.tm.TransactionManagerServices
import java.util.Hashtable
import javax.naming.Context._
import javax.naming.InitialContext
import javax.sql.DataSource
import bitronix.tm.BitronixTransaction
import java.io.File
import scala.slick.driver.MySQLDriver.simple._
import scala.collection.mutable.ListBuffer
import java.sql.Connection
import java.sql.SQLException
import bitronix.tm.BitronixTransactionManager
import javax.jms.ConnectionFactory
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.GraphDatabaseService
import ch.maxant.scalabook.play20.plugins.LoggingHelp

class Neo4JRollbackException(e: Exception) extends Exception(e)

trait Neo4JSupport extends LoggingHelp {

    private implicit lazy val db = play.api.Play.current.plugin[Neo4JSupportPlugin] match {
      case Some(plugin) => plugin.db
      case None => throw new Exception("There is no Neo4JSupport plugin registered. Make sure it is enabled. See play documentation. (Hint: add it to play.plugins)")
    }

    /**
     * Use this flow control to do something with neo4j inside an 
     */
    def withNeo4J[T](f: GraphDatabaseService => T): T = {
    	val txn = db.beginTx()
    	try{
    	    val ret = f(db)
    	    txn.success()
    	    ret
    	}catch{
    	    case e: Exception => {
    	    	log.warn("neo4j transaction failure: " + e.getMessage())
    	    	txn.failure()
    	    	throw new Neo4JRollbackException(e)
    	    }
    	}finally{
    		txn.finish()
    	}
    }
}

class Neo4JSupportPlugin(app: play.Application) extends Plugin with LoggingHelp {

	protected[neo4j] implicit var db: GraphDatabaseService = null

	private lazy val databaseStore = Play.current.configuration.getString("neo4j.store").get
	
    override def onStart {
	    if(db == null){
	    	db = new GraphDatabaseFactory().newEmbeddedDatabase(databaseStore)
	    }
		log.info("Started neo4j with database " + databaseStore)
    }

    override def onStop {
		//on graceful shutdown, we want to shutdown the TM too
        if(db != null){
		    log.info("Shutting down neo4j")
	        db.shutdown
			log.info("neo4j shut down")
        }
	}

}
