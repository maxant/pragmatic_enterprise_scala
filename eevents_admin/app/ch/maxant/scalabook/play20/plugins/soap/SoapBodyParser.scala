/*
 *   Copyright 2013 Play Framework & Ant Kutschera
 *   Based on JSON Body Parser from Play Framework!
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
package ch.maxant.scalabook.play20.plugins.soap

import scala.Array.canBuildFrom
import scala.util.control.Exception
import scala.xml.Elem
import scala.xml.XML

import org.apache.commons.codec.binary.Base64

import play.api.Logger
import play.api.Play
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Input.El
import play.api.libs.iteratee.Input.Empty
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Traversable
import play.api.mvc.BodyParser
import play.api.mvc.BodyParsers.parse
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results
import play.api.mvc.Results.BadRequest
import play.api.mvc.Results.Unauthorized

/** a container for the method and message */
case class SoapMessage[T](method: String, message: T)

/**
 * An abstract `BodyParser` for SOAP messages.  The concrete 
 * implementation needs to specify mappings from XML to 
 * model classes.
 * 
 * You need one concrete implementation per possible request.
 */
abstract class SoapBodyParser[T](rh: RequestHeader, maxSize: Int = 1024*100) extends BodyParser[SoapMessage[T]] {
    
    def mapXmlToModel(xml: Elem): SoapMessage[T]
    
    override def apply(rh: RequestHeader) : Iteratee[Array[Byte], Either[Result,SoapMessage[T]]] = {
        Traversable.takeUpTo[Array[Byte]](maxSize).apply(Iteratee.consume[Array[Byte]]().mapDone { bytes =>
            Exception.allCatch[SoapMessage[T]].either {
                val charset = rh.charset.getOrElse("utf-8")
                val body = new String(bytes, charset)
                val xml = XML.loadString(body)
        	    mapXmlToModel(xml)
            }.left.map { e =>
                val result = Play.maybeApplication.map{
                    _.global.onBadRequest(rh, "Invalid XML")
                }.getOrElse(Results.BadRequest)
                
                (result, bytes)
            }
        }).flatMap(Iteratee.eofOrElse(Results.EntityTooLarge)).flatMap {
            case Left(b) => Done(Left(b), Empty)
            case Right(it) => it.flatMap {
                case Left((r, in)) => Done(Left(r), El(in))
                case Right(tv) => Done(Right(tv), Empty)
            }
        }
    }
}

/** callback used by SoapBodyParser */
trait Authenticator {
    def isAuthentic(username: String, password: String): Boolean
    def isAuthorized(username: String, role: String): Boolean
}

object SoapBodyParser {

    /**
     * Gets a parser, using the given function, but first 
     * checking security.  This requires that the request 
     * contains the `Authorization' content header with a 
     * basic token, such as is sent when a JAX-WS client 
     * sends security credentials using the following Java 
     * code:
     * 
	 * Map<String, Object> ctx = ((BindingProvider)port).getRequestContext();
     * ctx.put(BindingProvider.USERNAME_PROPERTY, username);
     * ctx.put(BindingProvider.PASSWORD_PROPERTY, password);
     * 
     * If you don't want security to be checked, then simply 
     * use your function to instantiate the `BodyParser`, 
     * and pass that to the `Action`.
     */
	def getSecureParser[T](authenticator: Authenticator, role: String)(applicationSpecificParserFactory: (RequestHeader => BodyParser[SoapMessage[T]]) ) = parse.using { rh =>
    	if(rh.contentType.exists(_.startsWith("text/xml"))){
    	    //e.g.: Authorization: Basic c29tZVVzZXJuYW1lOnNvbWVQYXNzd29yZA==
    		val authHeader = rh.headers.get("Authorization")
    		authHeader.map{ auth => 

    		    Logger.debug("authorising user...")
    		    
    		    //the following is not very functional.  
    		    //partly because I want to really home in 
    		    //on the problem, and deliver a nice 
    		    //specific message and functional 
    		    //programming with Options is like the 
    		    //elvis operator whereby it simply does 
    		    //nothing, if None is present.  Not 
    		    //conducive to returning a specific 
    		    //error to help the caller resolve the issue!
    		    
    			if(auth.startsWith("Basic")){
    			    val data = auth.split(" ")(1).getBytes()
    			    val value = Base64.decodeBase64(data)
    			    val userPswdPair = new String(value).split(":")

    			    if(authenticator.isAuthentic(userPswdPair(0), userPswdPair(1))){
    			        if(authenticator.isAuthorized(userPswdPair(0), role)){
    			            applicationSpecificParserFactory(rh)
    			        }else{
    			            parse.error(Unauthorized("user not in role " + role))
    			        }
    			    }else{
    			        parse.error(Unauthorized("username / password unmatched"))
    			    }
    			}else{
    				parse.error(Unauthorized("authorization header doesnt contain Basic"))
    			}
		    }.getOrElse{
        		parse.error(Unauthorized("no authorization header found"))
    		}
    	} else {
    		parse.error(BadRequest("contentType not text/xml"))
    	}
	}    
}

