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

import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.annotation.security.RolesAllowed
import ch.maxant.scalabook.shop.util.Roles
import ch.maxant.scalabook.shop.util.Configuration
import javax.annotation.security.DeclareRoles
import ch.maxant.scalabook.shop.events.services.EventService
import java.lang.System.{currentTimeMillis => now}
import javax.ejb.EJB

@WebServlet(Array("/test"))
class TestServlet extends HttpServlet {
       
	override def doGet(request: HttpServletRequest, response: HttpServletResponse) = {
		request.setAttribute("someVariableName", "Hello World!")
		request.getRequestDispatcher("test.jsp").forward(request, response)
	}
}
