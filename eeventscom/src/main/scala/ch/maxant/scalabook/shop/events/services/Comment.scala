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
package ch.maxant.scalabook.shop.events.services

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
import java.util.Date
import ch.maxant.scalabook.shop.util.JPAHelp
import ch.maxant.scalabook.shop.common.persistence.jpa.User

@Entity
@Access(AccessType.FIELD)
private[services] case class Comment(

    //dont forget the prefix!
    @JPAHelp.Id
	@JPAHelp.TableGenerator(name="COMMENT_GENERATOR", table="EVENTS_SEQUENCES",  pkColumnValue="COMMENT", pkColumnName="SEQ_NAME", valueColumnName="SEQ_COUNT")
	@JPAHelp.GeneratedValue(strategy=GenerationType.TABLE, generator="COMMENT_GENERATOR")
	id: Long,

	@JPAHelp.Column(name="EVENT_UID")
	eventUid: String,

	comment: String,

	@JPAHelp.OneToOne(fetch=FetchType.EAGER)
	@JPAHelp.JoinColumn(name="USER_ID") 
	user: User,
	
	@JPAHelp.Column(name="THE_TIME")
	when: Date
) {

    /** ctor for creating a comment */
    def this(eventUid: String, comment: String, user: User, when: Date) = {
    	this(0L, eventUid, comment, user, when)
    }

    /** default no arg constructors are required by jpa */
    def this() = {
        this(null, null, null, null)
    }
    
}
