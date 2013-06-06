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
package ch.maxant.scalabook.btm

import java.util.Hashtable
import javax.naming.InitialContext
import javax.naming.Context
import scala.beans.BeanProperty
import javax.jms.XAConnectionFactory

class HornetQXAConnectionFactory extends XAConnectionFactory {

    lazy val qcf = {
		val JNDI_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory"
		val env = new Hashtable[String, String]()
		env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
		env.put(Context.PROVIDER_URL, providerUrl)
		env.put(Context.SECURITY_PRINCIPAL, securityPrincipal)
		env.put(Context.SECURITY_CREDENTIALS, securityCredentials)
		val ctx = new InitialContext(env)
		val qcf = ctx.lookup(name).asInstanceOf[org.hornetq.jms.client.HornetQJMSConnectionFactory]
		qcf
    }

    @BeanProperty
    var name = ""

    @BeanProperty
	var providerUrl = ""

    @BeanProperty
	var securityPrincipal = ""

    @BeanProperty
	var securityCredentials = ""
	
	@BeanProperty
	var forceSecurity = true
        
    def createXAQueueConnection = {
        if(forceSecurity){
            qcf.createXAQueueConnection(securityPrincipal, securityCredentials)
        }else{
        	qcf.createXAQueueConnection
        }
    }

    def createXAQueueConnection(username:String, password:String) = {
    	qcf.createXAQueueConnection(securityPrincipal, securityCredentials)
    }
    
	def createXAConnection = {
        if(forceSecurity){
        	qcf.createXAConnection(securityPrincipal, securityCredentials)
        }else{
        	qcf.createXAConnection
        }
	}
	
	def createXAConnection(username:String, password:String) = {
    	qcf.createXAConnection(securityPrincipal, securityCredentials)
	}
	
    def createQueueConnection = {
        if(forceSecurity){
        	qcf.createQueueConnection(securityPrincipal, securityCredentials)
        }else{
        	qcf.createQueueConnection
        }
    }

    def createQueueConnection(username:String, password:String) = {
		qcf.createQueueConnection(securityPrincipal, securityCredentials)
	}
	
	def createConnection = {
	    if(forceSecurity){
	    	qcf.createConnection(securityPrincipal, securityCredentials)
	    }else{
	    	qcf.createConnection
	    }
	}
	
	def createConnection(username:String, password:String) = {
		qcf.createConnection(securityPrincipal, securityCredentials)
	}
}
