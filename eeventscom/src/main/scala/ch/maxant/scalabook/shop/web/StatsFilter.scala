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
package ch.maxant.scalabook.shop.web

import java.util.concurrent.atomic.AtomicInteger

import com.jamonapi.MonitorFactory

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest

@WebFilter(urlPatterns = Array("/*"), asyncSupported = true) //IMPORTANT!! any filter which stands along side our Push Servlet needs to also support async!
class StatsFilter extends Filter {

	override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) = {
	    
	    StatsFilter.incrementPageRequests

	    //add user to jamon monitoring to identify power users, for example
	    val principal = request.asInstanceOf[HttpServletRequest].getUserPrincipal
	    val user = if(principal == null) "Anonymous" else principal.getName
	    val mon = MonitorFactory.start("pageHits." + user)
	    
		// pass the request along the filter chain
		chain.doFilter(request, response);

	    mon.stop
	}
	
	override def init(c: FilterConfig): Unit = {}
	override def destroy: Unit = {}
}

object StatsFilter {
    private val pageRequestCount = new AtomicInteger
	private val liveSessionCount = new AtomicInteger
	private val totalSessionCount = new AtomicInteger
	val blockedPagesCount = new AtomicInteger
    
    def incrementPageRequests = pageRequestCount.incrementAndGet
    def incrementLiveSessions = {
        totalSessionCount.incrementAndGet
		liveSessionCount.incrementAndGet
    }
    def decrementLiveSessions = liveSessionCount.decrementAndGet

    def numPageRequests = pageRequestCount.get
    def numSessionsLive = liveSessionCount.get
    def numSessionsTotal = totalSessionCount.get
}