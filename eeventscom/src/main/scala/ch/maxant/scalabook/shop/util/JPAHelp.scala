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
package ch.maxant.scalabook.shop.util

import scala.annotation.meta.field

/**
 * the follow is used to force the annotations to be hung on the fields, rather than accessor methods.
 * see also http://www.scala-lang.org/api/current/scala/annotation/target/package.html
 */
object JPAHelp {
	type Id = javax.persistence.Id @field
	type TableGenerator = javax.persistence.TableGenerator @field
	type GeneratedValue = javax.persistence.GeneratedValue @field
	type Column = javax.persistence.Column @field
	type OneToOne = javax.persistence.OneToOne @field
	type ManyToOne = javax.persistence.ManyToOne @field
	type OneToMany = javax.persistence.OneToMany @field
	type ManyToMany = javax.persistence.ManyToMany @field
	type OrderBy = javax.persistence.OrderBy @field
	type MapKeyJoinColumn = javax.persistence.MapKeyJoinColumn @field
	type OrderColumn = javax.persistence.OrderColumn @field
	type Cacheable = javax.persistence.Cacheable @field
	type JoinColumn = javax.persistence.JoinColumn @field
	type Version = javax.persistence.Version @field
	type Temporal = javax.persistence.Temporal @field
}


