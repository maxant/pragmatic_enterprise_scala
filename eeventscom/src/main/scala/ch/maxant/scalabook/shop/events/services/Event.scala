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
import java.math.BigDecimal
import ch.maxant.scalabook.shop.bom.Price
import ch.maxant.scalabook.shop.util.Constants
import java.util.StringTokenizer
import scala.collection.mutable.ListBuffer
import ch.maxant.scalabook.shop.util.JPAHelp
import javax.validation.constraints.Min
import ch.maxant.scalabook.shop.util.ValidationHelp

@Entity
@Access(AccessType.FIELD)
private[services] case class Event(

    //dont forget the prefix!
    @JPAHelp.Id
	uid: String,
	
//	@ValidationHelp.Max(400) //ignored except for writing data!
	name: String,
	
	city: String,
	
	@JPAHelp.Column(name="THE_TIME")
	when: Date,
	
	@JPAHelp.Column(name="TEASER_PRICE_CHF")
	private val _teaserPrice: BigDecimal,
	
	description: String,
	
	@JPAHelp.Column(name="BOOKING_SYSTEMS")
	private val _bookingSystems: String,
	
	@JPAHelp.Column(name="IS_TEASER")
	isTeaser: Boolean,
	
	@JPAHelp.Column(name="IMG_NAME")
	imgName: String	
) {

    /** default no arg constructors are required by jpa */
    def this() = {
        this(null, null, null, null, null, null, null, false, null)
    }
    
    def teaserPrice = new Price(_teaserPrice, Constants.CHF)
    
    /** returns a sequence of tuples.  the first element is the booking system's own code for this event, the second element is the booking system code, e.g. GEDE. */
    def bookingSystems = {
        for(p <- _bookingSystems.split(",")) yield {
            val e = p.split("-")
            (Integer.parseInt(e(1)), e(0))
        } 
    }
}
