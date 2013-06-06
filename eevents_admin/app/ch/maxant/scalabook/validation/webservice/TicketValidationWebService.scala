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
package ch.maxant.scalabook.validation.webservice
import ch.maxant.scalabook.validation.integration.UserRepository
import scala.xml.Elem
import play.api.mvc.RequestHeader
import ch.maxant.scalabook.play20.plugins.soap.SoapBodyParser
import play.api.mvc.BodyParser
import play.api.mvc.Results._
import play.api.mvc.BodyParsers.parse
import ch.maxant.scalabook.play20.plugins.soap.SoapMessage
import ch.maxant.scalabook.validation.integration.UserRepo
import ch.maxant.scalabook.play20.plugins.soap.Authenticator

case class TicketValidation(
        referenceNumber: String,
        eventUid: String,
		user: UserRepository.User
)

class TicketValidationBodyParser(requestHeader: RequestHeader, user: UserRepository.User) extends SoapBodyParser[TicketValidation](requestHeader) {
	    
    override def mapXmlToModel(xml: Elem)= {
	    //eg.
        //<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
	    //  <soap:Body>
		//    <ns2:validateTicket xmlns:ns2="http://validation.scalabook.maxant.ch/">
		//      <arg0>
		//        <bookingRefNumber>asdf</bookingRefNumber>
		//      </arg0>
		//    </ns2:validateTicket>
		//  </soap:Body>
		//</soap:Envelope>
        val a = xml.child
        val b = a.filterNot(_.toString.trim == "").head
        val c = b.child
        val d = c.filterNot(_.toString.trim == "").head
        val method = d.label
		val bookingRef = (xml \\ "bookingRefNumber").text
		val eventUid = (xml \\ "eventUid").text

		val ticket = TicketValidation(bookingRef, eventUid, user)
	    SoapMessage(method, ticket)
    }    
}

object TicketValidationWebService {

    var userRepository: UserRepo = UserRepository
    
	def validationParser(role: String) = {

	    var user: Option[UserRepository.User] = null
        
        val authenticator = new Authenticator {
            def isAuthentic(username: String, password: String): Boolean = {
                userRepository.authenticate(username, password).isDefined
            }
            def isAuthorized(username: String, role: String): Boolean = {
                user = userRepository.findByEmail(Some(username))
                user.exists(_.isInRole(role))
            }
        }
	    
        SoapBodyParser.getSecureParser(authenticator, role){ requestHeader =>
            new TicketValidationBodyParser(requestHeader, user.get)
        }
	}
    
    def mapResultOld(result: Boolean, message: String) = {
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <ns2:validateTicketResponse xmlns:ns2="http://validation.scalabook.maxant.ch/">
		    <return>
                <result>{result}</result>
		        <message>{message}</message>
		    </return>
        </ns2:validateTicketResponse>
    </soap:Body>
</soap:Envelope>
    }
    
    def mapResult(result: Boolean, message: scala.xml.Elem) = {
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <ns2:validateTicketResponse xmlns:ns2="http://validation.scalabook.maxant.ch/">
            <return>
                <result>{result}</result>
                <message>{message}</message>
            </return>
        </ns2:validateTicketResponse>
    </soap:Body>
</soap:Envelope>
    }

    def mapError(t: Throwable) = {
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
	<soap:Body>
        <soap:Fault>
            <faultcode>soap:Server</faultcode>
			<faultstring>{t.getMessage}</faultstring>
		</soap:Fault>
	</soap:Body>
</soap:Envelope>
    }
    
    val wsdl =
<wsdl:definitions name="ValidationServiceService" targetNamespace="http://validation.scalabook.maxant.ch/" xmlns:ns1="http://schemas.xmlsoap.org/soap/http" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:tns="http://validation.scalabook.maxant.ch/" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
  <wsdl:types>
<xs:schema elementFormDefault="unqualified" targetNamespace="http://validation.scalabook.maxant.ch/" version="1.0" xmlns:tns="http://validation.scalabook.maxant.ch/" xmlns:xs="http://www.w3.org/2001/XMLSchema">
<xs:element name="validateTicket" type="tns:validateTicket"/>
<xs:element name="validateTicketResponse" type="tns:validateTicketResponse"/>
<xs:complexType name="validateTicket">
    <xs:sequence>
      <xs:element minOccurs="0" name="validationRequest" type="tns:ValidationRequest"/>
    </xs:sequence>
  </xs:complexType>
<xs:complexType name="ValidationRequest">
    <xs:sequence>
      <xs:element name="bookingRefNumber" type="xs:string"/>
      <xs:element minOccurs="0" name="eventUid" type="xs:string"/>
      <xs:element minOccurs="0" name="eventDate" type="xs:dateTime"/>
    </xs:sequence>
  </xs:complexType>
<xs:complexType name="validateTicketResponse">
    <xs:sequence>
      <xs:element minOccurs="0" name="return" type="tns:ValidationResult"/>
    </xs:sequence>
  </xs:complexType>
<xs:complexType name="ValidationResult">
    <xs:sequence>
      <xs:element name="result" type="xs:boolean"/>
      <xs:element minOccurs="0" name="message" nillable="true" type="xs:string"/>
    </xs:sequence>
  </xs:complexType>
</xs:schema>
  </wsdl:types>
  <wsdl:message name="validateTicketResponse">
    <wsdl:part element="tns:validateTicketResponse" name="parameters">
    </wsdl:part>
  </wsdl:message>
  <wsdl:message name="validateTicket">
    <wsdl:part element="tns:validateTicket" name="parameters">
    </wsdl:part>
  </wsdl:message>
  <wsdl:portType name="ValidationService">
    <wsdl:operation name="validateTicket">
      <wsdl:input message="tns:validateTicket" name="validateTicket">
    </wsdl:input>
      <wsdl:output message="tns:validateTicketResponse" name="validateTicketResponse">
    </wsdl:output>
    </wsdl:operation>
  </wsdl:portType>
  <wsdl:binding name="ValidationServiceServiceSoapBinding" type="tns:ValidationService">
    <soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>
    <wsdl:operation name="validateTicket">
      <soap:operation soapAction="" style="document"/>
      <wsdl:input name="validateTicket">
        <soap:body use="literal"/>
      </wsdl:input>
      <wsdl:output name="validateTicketResponse">
        <soap:body use="literal"/>
      </wsdl:output>
    </wsdl:operation>
  </wsdl:binding>
  <wsdl:service name="ValidationServiceService">
    <wsdl:port binding="tns:ValidationServiceServiceSoapBinding" name="ValidationServicePort">
      <soap:address location="http://localhost:9000/ValidationService"/>
    </wsdl:port>
  </wsdl:service>
</wsdl:definitions>
}

