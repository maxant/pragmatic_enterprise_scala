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
package ch.maxant.scalabook.shop.util;

import java.util.logging.Logger
import javax.enterprise.context.Dependent
import javax.enterprise.inject.Produces
import javax.enterprise.inject.spi.InjectionPoint;
import java.util.logging.Level

/**
 * This class is a log producer.  It's nice, because it lets us tell the injection
 * framework where to get instances of the logger in order to inject them.
 * 
 * Note: I know `java.util.logging` perhaps isnt your favourite logger, but 
 * I didn't want to build a dependency on JBoss logging, in case you want to 
 * deploy this app someplace else.
 */
class LogProducer {

	@Produces
	def getLogger(ip: InjectionPoint) = {
	    Logger.getLogger(ip.getMember().getDeclaringClass().getName())
	}
}

/** useful for non CDI classes */
object LogProducer_ {
    def getLogger = {
		val cn = Thread.currentThread().getStackTrace()(2).getClassName
		Logger.getLogger(cn)
	}    
}