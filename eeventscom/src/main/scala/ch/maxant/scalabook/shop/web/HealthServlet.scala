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

import java.util.Date

import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(Array("/health"))
class HealthServlet extends HttpServlet {
       
	override def doGet(request: HttpServletRequest, response: HttpServletResponse) = {
	    val xml = 
<health>
  <service>
    <name>{classOf[HealthServlet].getName}</name>
    <status>OK</status>
    <freeMem>{Runtime.getRuntime.freeMemory}</freeMem>
    <totalMem>{Runtime.getRuntime.totalMemory}</totalMem>
    <maxMem>{Runtime.getRuntime.maxMemory}</maxMem>
    <availableProcessors>{Runtime.getRuntime.availableProcessors}</availableProcessors>
    <pageRequests>{StatsFilter.numPageRequests}</pageRequests>
    <liveSessions>{StatsFilter.numSessionsLive}</liveSessions>
    <totalSessions>{StatsFilter.numSessionsTotal}</totalSessions>
    <runningSince>{HealthServlet.runningSince}</runningSince>
    <blockedPagesTest>{StatsFilter.blockedPagesCount.get}</blockedPagesTest>
    <threadCount>{Thread.activeCount}</threadCount>
    <jvm-version>{System.getProperty("java.runtime.version")}</jvm-version>
    <scala-version>{scala.util.Properties.scalaPropOrElse("version.number", "unknown")}</scala-version>
    <scalaCompilerVersion>{scala.util.Properties.ScalaCompilerVersion}</scalaCompilerVersion>
    <scalaReleaseVersion>{scala.util.Properties.releaseVersion}</scalaReleaseVersion>
  </service>
</health>

	    response.getWriter().write(xml.toString)
	}

}

object HealthServlet {
    val runningSince = new Date
}