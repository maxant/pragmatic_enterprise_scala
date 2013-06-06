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
package ch.maxant.scalabook.shop.ui
import javax.inject.Named
import javax.enterprise.context.RequestScoped
import java.util.Date
import scala.beans.BeanProperty
import java.util.{List => JList}
import ch.maxant.scalabook.shop.bom.EventTeaser
import javax.inject.Inject
import javax.ejb.EJB
import ch.maxant.scalabook.shop.events.services.EventService
import java.util.logging.Logger
import scala.collection.JavaConversions._
import ch.maxant.scalabook.shop.util.Conversions.asDateFromDateOnly
import ch.maxant.scalabook.shop.util.Constants._
import java.text.ParseException
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Named
@RequestScoped
class Register {

	@BeanProperty
	@NotNull @Size(min=3)
	var name: String = null
	
	def register() = {
	  "registered"
	}
	
}

