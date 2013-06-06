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
package ch.maxant.scalabook.shop.common.services.rules

import scala.collection.JavaConversions._
import java.util.{List => JList}
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.TableGenerator
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Column
import javax.persistence.AccessType
import javax.persistence.Access
import scala.annotation.meta.field
import java.util.ArrayList
import javax.persistence.FetchType
import scala.beans.BeanProperty
import javax.persistence.Table
import javax.persistence.Cacheable
import javax.persistence.CascadeType
import ch.maxant.scalabook.shop.util.JPAHelp

@Entity
@Access(AccessType.FIELD)
case class Rule(

    //dont forget the prefix!
    @JPAHelp.Id
	@JPAHelp.TableGenerator(name="RULE_GENERATOR", table="RULE_SEQUENCE",  pkColumnValue="RULE", pkColumnName="SEQ_NAME", valueColumnName="SEQ_COUNT")
	@JPAHelp.GeneratedValue(strategy=GenerationType.TABLE, generator="RULE_GENERATOR")
	id: Long,

	name: String,
	expression: String,
	outcome: String,
	priority: Int,
	namespace: String,
	description: String
) {

    /** default no arg constructors are required by jpa */
    def this() = {
        this(0L, null, null, null, 0, null, null)
    }
    
    /** converts this rule into a rule engine rule */
    def convert() = new ch.maxant.rules.Rule(name, expression, outcome, priority, namespace, description)
    
}
