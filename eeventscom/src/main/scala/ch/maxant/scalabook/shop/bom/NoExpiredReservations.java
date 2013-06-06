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
package ch.maxant.scalabook.shop.bom;

import static java.lang.annotation.ElementType.TYPE;
 
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 
import javax.validation.Constraint;
import javax.validation.Payload;
	 
@Documented
@Constraint(validatedBy = NoExpiredReservationsValidator.class)
@Target( { TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoExpiredReservations {

	 String message() default "Some or all elements have expired.";
	 
	 Class<?>[] groups() default {};
	 
	 Class<? extends Payload>[] payload() default {};
	}