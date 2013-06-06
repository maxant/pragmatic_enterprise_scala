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
package ch.maxant.scalabook.shop.common.services

import java.util.Set
import javax.interceptor.AroundInvoke
import javax.interceptor.InvocationContext
import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory
import java.util.logging.Logger
import ch.maxant.scalabook.shop.util.LogProducer_
import org.hibernate.validator.method.MethodValidator
import org.hibernate.validator.HibernateValidator
import org.hibernate.validator.HibernateValidatorConfiguration


/**
 * an aspect (AOP) for validating method inputs.
 */
class ValidationInterceptor {

    private var log: Logger = LogProducer_.getLogger
    
    private val factory = Validation.buildDefaultValidatorFactory
    private val validator: Validator = factory.getValidator
    //private val methodValidator = MethodValidatorFactory.createValidator
    private val methodValidator = Validation.byProvider[HibernateValidatorConfiguration, HibernateValidator](classOf[HibernateValidator]).configure.buildValidatorFactory.getValidator.unwrap(classOf[MethodValidator])    
    
	/**
	 * this method is an aspect which has been configured to be called around the execute method,
	 * using the annotation @Inteceptors.
	 */
	@AroundInvoke
	def intercept(ctx: InvocationContext) = {
		val start = System.nanoTime

		var result: Option[String] = None
		val vs = methodValidator.validateAllParameters(ctx.getTarget, ctx.getMethod, ctx.getParameters)
		if(!vs.isEmpty()){
			//invalid!
			result = Some(vs.iterator.next.getMessage)
		}

		for(p <- ctx.getParameters()){
			val vs2 = validator.validate(p)
			if(!vs2.isEmpty()){
			  if(result == None){
			    result = Some(vs2.iterator().next().getMessage())
			  }
			}
		}

		log.info("validation completed on " + ctx.getMethod.getName + " in " + ((System.nanoTime-start)/1000000.0) + "ms. error=" + result.getOrElse("none"))
		
		result match {
		  case None => ctx.proceed()
		  case Some(s) => throw new IllegalArgumentException(s)
		}
	}
}
