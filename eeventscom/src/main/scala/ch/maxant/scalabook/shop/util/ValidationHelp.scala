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
object ValidationHelp {
	type Size = javax.validation.constraints.Size @field
    type Min = javax.validation.constraints.Min @field
    type Max = javax.validation.constraints.Max @field
    type DecimalMin = javax.validation.constraints.DecimalMin @field
    type DecimalMax = javax.validation.constraints.DecimalMax @field
    type Digits = javax.validation.constraints.Digits @field
    type Null = javax.validation.constraints.Null @field
    type NotNull = javax.validation.constraints.NotNull @field
    type AssertFalse = javax.validation.constraints.AssertFalse @field
    type AssertTrue = javax.validation.constraints.AssertTrue @field
    type Future = javax.validation.constraints.Future @field
    type Past = javax.validation.constraints.Past @field
    type Pattern = javax.validation.constraints.Pattern @field
}


