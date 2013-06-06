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
package ch.maxant.scalabook.shop.events.services.ratings

import javax.persistence.Access
import javax.persistence.Entity
import javax.persistence.AccessType
import javax.persistence.GenerationType
import javax.persistence.Table
import ch.maxant.scalabook.shop.util.JPAHelp

/**
 * maps users to their ratings, so that we can ensure
 * each user only rates an event one time
 */
@Entity
@Access(AccessType.FIELD)
@Table(name="RATING_USER_MAPPING")
private[ratings] case class RatingUserMapping(

    @JPAHelp.Id
	@JPAHelp.TableGenerator(name="RUM_GENERATOR", table="EVENTS_SEQUENCES",  pkColumnValue="RATING_USER_MAPPING", pkColumnName="SEQ_NAME", valueColumnName="SEQ_COUNT")
	@JPAHelp.GeneratedValue(strategy=GenerationType.TABLE, generator="RUM_GENERATOR")
	id: Long,

	@JPAHelp.Column(name="EVENT_UID")
	eventUid: String,

	//we dont need the actual user here, so no point creating an artificial join which will eat CPU
	@JPAHelp.Column(name="USER_ID")
	userId: Long,
	
	rating: Int
) {

    /** default no arg constructors are required by jpa */
    def this() = {
        this(-1, null, -1, -1)
    }
    
}
