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
import javax.ejb.EJB
import ch.maxant.scalabook.shop.common.services.UserService
import ch.maxant.scalabook.shop.orders.services.OrderService
import javax.inject.Inject
import java.io.OutputStream
import java.io.ByteArrayOutputStream
import java.io.BufferedOutputStream
import org.apache.fop.apps.FopFactory
import javax.xml.transform.TransformerFactory
import java.io.ByteArrayInputStream
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.sax.SAXResult
import org.apache.xmlgraphics.util.MimeConstants
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.text.DecimalFormat
import javax.annotation.security.DeclareRoles
import java.net.URL
import java.net.HttpURLConnection

@WebServlet(urlPatterns = Array("/CallWsSyncTest"), asyncSupported = false)
class CallWsSyncTest extends HttpServlet {
       
	override def doGet(request: HttpServletRequest, response: HttpServletResponse) = {
	    response.getOutputStream().print("<html><body>starting request...")
		response.flushBuffer()
	    println("starting sync ...")
	    val url = new URL("http://localhost:8086/TestWS/index.jsp")
		val is = url.openStream()
		val baos = new ByteArrayOutputStream()
		try {
		    var curr = is.read()
		    while(curr != -1){
		        baos.write(curr)
		        curr = is.read()
		    }
		} finally {
			is.close()
		}
	    val result = new String(baos.toByteArray())
	    println("got sync " + result)
	    response.getOutputStream().print("got result: " + result)
		response.flushBuffer()
	    println("finished sync")
	}
    
}
