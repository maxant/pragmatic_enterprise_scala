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

import java.lang.System.{currentTimeMillis => now}
import java.sql.Timestamp
import scala.slick.driver.MySQLDriver.simple.Database
import scala.slick.driver.MySQLDriver.simple.Session
import scala.slick.driver.MySQLDriver.simple.columnBaseToInsertInvoker
import scala.slick.driver.MySQLDriver.simple.Table
import ch.maxant.scalabook.play20.plugins.xasupport.XAContext
import ch.maxant.scalabook.validation.integration.UserRepository.User
import play.api.libs.concurrent.Akka
import play.api.Play
import akka.actor.Actor
import akka.actor.Props
import java.util.concurrent.CountDownLatch

object ValidationRepository extends ValidationRepo {

	val Validations = new Table[Validation]("TICKET_VALIDATION"){
		def bookingRef = column[String]("BOOKING_REF")
		def userId = column[String]("USER_ID")
		def when = column[java.sql.Timestamp]("THE_TIME")
		def * = bookingRef ~ userId ~ when <> (Validation, Validation.unapply _)
	}
	
	/** 
	 * adds the given validation and marks the order item as having been validated.
	 * the db constraint ensures no duplicate validations occur!
	 */
	override def addValidation(bookingRef: String, validator: User)(implicit ctx: XAContext) {
		val ds = ctx.lookupDS("jdbc/maxant/scalabook_admin")
	    Database.forDataSource(ds) withSession { implicit sess: Session =>
//		AppDB.database withSession { implicit sess: Session => breaks 2PC!!
//	        sess.conn.setAutoCommit(false) - not needed. Bitronix takes care of it, so long as the DS comes from JNDI.  If oyu use AppDB.database withSession ... then you don't have 2PC!
	        println("connGetAutocommit=" + sess.conn.getAutoCommit())
	        
            Validations.insert(Validation(bookingRef, validator.email, new Timestamp(now)))
        }

        /*
	    import play.api.db.DB
	    import play.api.Play.current
	    Database.forDataSource(DB.getDataSource()) withSession { implicit db: Session =>
			Validations.insert(Validation(bookingRef, validator.email, new Timestamp(now)))
        }
        */
	}	
}

