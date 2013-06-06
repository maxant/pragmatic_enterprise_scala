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
package ch.maxant.scalabook.shop.common.persistence.scalaquery

/*
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import org.scalaquery.session._
*/

import scala.slick.driver.MySQLDriver.simple._

object User {

    case class User(id: Long, email: String, password: String, name: String)
    
    val Users = new Table[User]("USER") {
		def id = column[Long]("ID", O.PrimaryKey)
        def email = column[String]("EMAIL")
        def password = column[String]("PASSWORD")
        def name = column[String]("NAME")
        def * = id ~ email ~ password ~ name <> (User, User.unapply _)
    }
    
    
    
}