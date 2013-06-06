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
import ch.maxant.scalabook.shop.orders.services.OrderItemJpa
import org.apache.xmlgraphics.util.MimeConstants
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.text.DecimalFormat
import javax.annotation.security.DeclareRoles
import ch.maxant.scalabook.shop.bom.Booking

@WebServlet(Array("/secure/Pdf"))
@DeclareRoles(Array("registered"))
class PdfServlet extends HttpServlet {
       
    @EJB var userService: UserService = null
	@EJB var orderService: OrderService = null
	@Inject var config: Configuration = null
	private val fopFactory = FopFactory.newInstance
	private val transformerFactory = TransformerFactory.newInstance
	
	@RolesAllowed(Array("registered"))
	override def doGet(request: HttpServletRequest, response: HttpServletResponse) = {
	    val ref = request.getParameter("PDFReference")
	    
	    if(ref == null) {
	    	response.getWriter.write("PDFReference parameter must not be ommitted from request URL!")
	    }else{
		    //user is logged in, due to "secure" in the url, so lets check this reference belongs to them
		    val user = userService.getLoggedInUser.get
		    
		    val opt = orderService.getBooking(user.id, ref) 
		    opt match {
		    	case Some(b) => {

		    	    if(b.isValidated) {
		    	    	response.getWriter.write("Booking has already been used at the event and cannot be printed again!")
		    	    }else{
						// //////////////////////////
						// xsl-fo -> pdf
						// //////////////////////////
						val xfo = getXfo(b)
						val data = ("<?xml version='1.0' encoding='ISO-8859-1'?>" + xfo.toString).getBytes("ISO-8859-1")
	
						response.setContentType(MimeConstants.MIME_PDF)
						//response.setContentLength(data.length) -> dont do this, its plain wrong and corrupts the PDF!
						response.setHeader("Content-Disposition","attachment; filename=\"" + b.referenceNumber + ".pdf\"")
						response.setHeader("Pragma", "public")
						response.setHeader("Cache-Control","cache")
						response.setHeader("Cache-Control","must-revalidate")
			    	    
						val is = new ByteArrayInputStream(data)
						val src = new StreamSource(is)
						val fop = fopFactory.newFop(MimeConstants.MIME_PDF, response.getOutputStream)
						val transformer = transformerFactory.newTransformer
						val res = new SAXResult(fop.getDefaultHandler)
	
						// Start XSLT transformation and FOP processing, writing to the output stream
						transformer.transform(src, res)
	
						orderService.markItemAsPrinted(ref)
		    	    }
		    	}
	
		    	case None => response.getWriter.write("Booking " + ref + " not found or does not appear to be yours. Please contact support for help: " + config.c.siteUrl)
		    }
	    }
	}
    
    private def getXfo(b: Booking) = {
        
        val ddMMMyyyy = new SimpleDateFormat("dd. MMM yyyy")
        val hhmm = new SimpleDateFormat("HH:mm")
        val x_00 = new DecimalFormat("0.00")
        val logoUrl = "url('" + config.c.siteUrl + "javax.faces.resource/logo.png.jsf?ln=images')"
        val barcodeUrl = "url('" + config.c.siteUrl + "barcode?id=" + b.referenceNumber + "')"

        <fo:root xmlns:fo='http://www.w3.org/1999/XSL/Format'>
    		<fo:layout-master-set>
    			<fo:simple-page-master master-name='A4' page-height='29.7cm' page-width='21cm' margin-top='2cm' margin-bottom='2cm' margin-left='2cm' margin-right='2cm'>
    				<fo:region-body/>
    			</fo:simple-page-master>
    		</fo:layout-master-set>
    		<fo:page-sequence master-reference='A4'>
    			<fo:flow flow-name='xsl-region-body'>
        			<fo:block>
        				<fo:table table-layout='fixed' width='100%' >
        					<fo:table-column column-width="35mm" />
        					<fo:table-column />
       						<fo:table-body>
       							<fo:table-row>
       								<fo:table-cell>
                                        <fo:block>
                                            <fo:external-graphic src={logoUrl} />
                                        </fo:block>
       								</fo:table-cell>
       								<fo:table-cell>
                                        <fo:block>    </fo:block>
                                        <fo:block font-size='18pt' space-before='20mm'>{b.eventName}</fo:block>
                                        <fo:block><fo:leader leader-length='99%' leader-pattern='rule' alignment-baseline='middle' rule-thickness='0.5pt' color='grey'/></fo:block>
                                        <fo:block font-size='12pt' space-after='5mm'>On: {ddMMMyyyy.format(b.eventDate)}</fo:block>
                                        <fo:block font-size='12pt' space-after='5mm'>At: {hhmm.format(b.eventDate)}</fo:block>
                                        <fo:block font-size='12pt' space-after='5mm'>{b.tarif.quantity} persons, total price: {x_00.format(b.tarif.getTotalPrice.value)} CHF</fo:block>
                                        <fo:block font-size='10pt' space-after='5mm'>Tarif: {b.tarif.name}</fo:block>
                                        <fo:block font-size='10pt' space-after='5mm'>Description: {b.tarif.description}</fo:block>
                                        <fo:block font-size='8pt' space-after='5mm'>Conditions: {b.tarif.conditions}</fo:block>
                                        <fo:block font-size='10pt' font-style='italic' space-after='5mm'>Booking reference {b.referenceNumber}</fo:block>
                                        <fo:block><fo:leader leader-length='99%' leader-pattern='rule' alignment-baseline='middle' rule-thickness='0.5pt' color='grey'/></fo:block>
                                        <fo:block text-align='right'>
                                            <fo:external-graphic src={barcodeUrl}  content-height='100px' content-width='100px'/>
                                        </fo:block>
                                        <fo:block><fo:leader leader-length='99%' leader-pattern='rule' alignment-baseline='middle' rule-thickness='0.5pt' color='grey'/></fo:block>
                                        <fo:block text-align='right' font-size='8pt' color='grey' space-after='25mm'>Powered by http://www.maxant.ch</fo:block>
                                        <fo:block text-align='center' font-weight='bold' font-size='12pt' color='red' space-before='30mm' space-after='5mm'>
                                        This ticket is a sample only, and not valid for anything.
                                        <fo:block/>
                                        You have not been charged for this ticket!
                                        </fo:block>
       								</fo:table-cell>
       							</fo:table-row>
        					</fo:table-body>
        				</fo:table>
        			</fo:block>
    			</fo:flow>
    		</fo:page-sequence>
    	</fo:root>
    }
}
