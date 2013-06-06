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
package ch.maxant.scalabook.shop.orders.services

import scala.slick.driver.MySQLDriver.simple._

/**
 * here we arent going to model every column!
 * we are just using this class for read access to the DB when doing a join
 */
private[services] object OrderItemSQ {

    case class OrderItem(id: Long, orderId: Long, bookingRef: String)
    
    val OrderItems = new Table[OrderItem]("ORDER_ITEM") {
		def id = column[Long]("ID", O.PrimaryKey)
		def orderId = column[Long]("ORDER_ID")
//		def order = foreignKey("ORDER_ID", orderId, Order)(_.id)
        def bookingRef = column[String]("BOOKING_REF")
        def * = id ~ orderId ~ bookingRef <> (OrderItem, OrderItem.unapply _)
    }
    
}