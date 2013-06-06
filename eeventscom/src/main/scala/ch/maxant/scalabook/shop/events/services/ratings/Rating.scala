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

import ch.maxant.scalabook.shop.util.JPAHelp
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Entity

/**
 * have not used a technical primary key in this case, because 
 * it was a headache to get the concurrency right.
 * when a user rates, we need to do an update
 * (the rating always exists, its created with the event)
 * we do the update with pesimistic locking.
 * 
 * hmm, so we could have a tech key!
 */
@Entity
@Access(AccessType.FIELD)
private[ratings] case class Rating(

    @JPAHelp.Id
    @JPAHelp.Column(name="EVENT_UID")
	eventUid: String,

	@JPAHelp.Column(name="SUM_RATINGS")
	sumRatings: Long,
	
	@JPAHelp.Column(name="NUM_RATINGS")
	numRatings: Int
) {

    /** default no arg constructors are required by jpa */
    def this() = {
        this(null, -1, -1)
    }

    /** the average rating, rounded */
    def avgRating = {
        //round up, we want ratings between 1 and 5.  simple division rounds down with "toInt", causing 0-4
        (BigDecimal(sumRatings) / BigDecimal(numRatings)).setScale(0, BigDecimal.RoundingMode.UP).toInt
    }
}
