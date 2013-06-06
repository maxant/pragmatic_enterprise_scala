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
package ch.maxant.scalabook.validation

import play.api.db._
import play.api.Play.current
import scala.collection.immutable.Map
import ch.maxant.scalabook.validation.integration.AppDB
import scala.slick.driver.MySQLDriver.simple._

object Config {

    private case class Config(
		key: String, value: String){
	}
	
	private val Configs = new Table[Config]("CONFIGURATION"){
		def key = column[String]("KEY_")
		def value = column[String]("VALUE_")
		def * = key ~ value <> (Config, Config.unapply _)
	}

	private lazy val map: Map[String, String] = AppDB.database withSession { implicit db: Session =>
    	Map(
	        Query(Configs).list.map {c => c.key -> c.value }: _*
	      )
    }
    
    lazy val ValidationUrl = map("validationUrl")
    
}