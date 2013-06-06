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
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.MultiFormatWriter
import com.google.zxing.BarcodeFormat

@WebServlet(Array("/barcode"))
class BarcodeServlet extends HttpServlet {
       
	@Inject var config: Configuration = null
	
	@RolesAllowed(Array("asdf")) //TODO does this cause clicking on the email to ask for login? if not, move into the secure folder
	override def doGet(request: HttpServletRequest, response: HttpServletResponse) = {
	    val ref = request.getParameter("id")
	    
	    if(ref == null) {
	    	response.getWriter.write("id parameter must not be ommitted from request URL!")
	    }else{

			response.setContentType(MimeConstants.MIME_PNG)
			//response.setContentLength(unknown)
			response.setHeader("Content-Disposition","attachment; filename=\"" + ref + ".png\"")
			response.setHeader("Pragma", "public")
			response.setHeader("Cache-Control","cache")
			response.setHeader("Cache-Control","must-revalidate")
	        
	        val barcodeWriter = new MultiFormatWriter()
	    	val matrix = barcodeWriter.encode(ref, BarcodeFormat.QR_CODE, 100, 100)
	    	MatrixToImageWriter.writeToStream(matrix, "PNG", response.getOutputStream)
	    }
    }
}
