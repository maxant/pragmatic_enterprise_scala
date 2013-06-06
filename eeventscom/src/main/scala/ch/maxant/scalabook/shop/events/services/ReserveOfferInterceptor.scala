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
import javax.interceptor.AroundInvoke
import javax.inject.Inject
import System.{currentTimeMillis => now}
import javax.interceptor.InvocationContext
import java.util.logging.Logger

import ch.maxant.scalabook.shop.util.Formats;

private[services] class ReserveOfferInterceptor() extends Formats {
    
    @Inject
    var log: Logger = null
    
    @AroundInvoke
    def intercept(ctx: InvocationContext): Object = {
        val start = now()
        val ret = ctx.proceed()
        var adapterName = ctx.getTarget().getClass().getName()
        adapterName = adapterName.drop(adapterName.lastIndexOf(".")+1)
        log.info("Reserved offer from " + adapterName + " with " +
                ctx.getParameters().mkString(",") + 
                " in " + (now()-start) + "ms, started at " +
                asTime(start))
        ret
    }
}

