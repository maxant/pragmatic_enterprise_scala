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
package ch.maxant.scalabook.shop.common.persistence.jpa

import java.util.ArrayList
import java.util.{List => JList}

import scala.beans.BeanProperty
import scala.collection.JavaConversions.asScalaBuffer

import ch.maxant.scalabook.shop.util.JPAHelp
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.GenerationType
import javax.persistence.CascadeType
import javax.persistence.FetchType
import javax.persistence.Entity

@Entity
@Access(AccessType.FIELD)
case class User(

    //dont forget the prefix!
    @JPAHelp.Id
	@JPAHelp.TableGenerator(name="USER_GENERATOR", table="USER_SEQUENCE",  pkColumnValue="USER", pkColumnName="SEQ_NAME", valueColumnName="SEQ_COUNT")
	@JPAHelp.GeneratedValue(strategy=GenerationType.TABLE, generator="USER_GENERATOR")
    @BeanProperty
	id: Long,

	@BeanProperty
	@JPAHelp.Column(unique=true)
	email: String,

	password: String,

	@BeanProperty
	name: String,
	
	//EAGER, since we will always want all roles which the user has.
	//cascade, so that we don't have to worry about children.
	//orphanRemoval, so that if we remove a user, the role record isnt left hanging in the DB with a null FK.
	//no back refs, because that causes chaos with construction, toString, ## and == (e.g. StackOverflowExceptions or the inability to construct objects graphs)
	@JPAHelp.OneToMany(cascade=Array(CascadeType.ALL), fetch=FetchType.EAGER, orphanRemoval=true)
	@JPAHelp.JoinColumn(name="user_id") //so that no back ref is needed, instead of mappedBy on the @OneToMany
	@JPAHelp.OrderBy("role ASC") //so that we are consistent
	private val jroles: JList[Role],
	
	@scala.transient
	someNonPersistentData: String

) {

    /** ctor for creating a user */
    def this(email: String, password: String, name: String, roles: JList[Role]) = {
    	this(0L, email, password, name, roles, "whoCares_itsNotPersistent")
    }

    /** default no arg constructors are required by jpa */
    def this() = {
        this(null, null, null, new ArrayList[Role])
    }
    
    def roles = {
        //prepend and reverse gives good performance, better than append
        var roles = List[Role]()
        for(r <- jroles){
            roles = r :: roles
        }
    	roles.reverse
    } 
    
}
