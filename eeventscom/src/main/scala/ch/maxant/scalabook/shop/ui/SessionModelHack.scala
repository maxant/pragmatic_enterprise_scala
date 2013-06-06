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

import scala.beans.BeanProperty

import javax.enterprise.context.SessionScoped
import javax.inject.Named

/**
 * without this bean, there are no session scoped beans
 * in the entire application and since JSF is configured
 * to keep state on the client, we run into problems.
 * 
 * a) programmatic login doesn't seem to survive more than a page.
 * b) when creating an order, the order ID is not shown on the screen until the second order.
 */
@Named
@SessionScoped
@serializable
class SessionModelHack {

	@BeanProperty val dummyString = ""
}

