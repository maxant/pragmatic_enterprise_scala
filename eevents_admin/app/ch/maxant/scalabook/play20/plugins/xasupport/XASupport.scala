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
package ch.maxant.scalabook.play20.plugins.xasupport

import java.io.File
import java.util.Hashtable

import bitronix.tm.BitronixTransactionManager
import bitronix.tm.TransactionManagerServices
import javax.jms.ConnectionFactory
import javax.naming.Context.INITIAL_CONTEXT_FACTORY
import javax.naming.InitialContext
import javax.sql.DataSource
import play.api.Logger
import play.api.Play
import play.api.Plugin
import play.api.mvc.Controller

class XAContext {

	private val env = new Hashtable[String, String]()
	env.put(INITIAL_CONTEXT_FACTORY, "bitronix.tm.jndi.BitronixInitialContextFactory")
	private var _namingCtx: InitialContext = null
	private def getInitialisedNamingCtx = {
        if(_namingCtx == null) {
            println("-------------------")
            println("-------------------")
            println("Creating new IC")
            println("-------------------")
            println("-------------------")
            _namingCtx = new InitialContext(env)
        }
        _namingCtx
	}
	
	var rollbackOnly = false
	
	private def lookup(name: String, secondTry: Boolean = false): Any = {
	    try{
	        getInitialisedNamingCtx.lookup(name)
	    }catch{
	        case e: Exception => {
	            //try restarting it, in case it was not available at startup
	            if(!secondTry){
	                _namingCtx = null
	                lookup(name, true)
	            }else{
	                //give up
	                throw e
	            }
	        }
	    }
	}
	def lookupDS(name: String) = {
		lookup(name).asInstanceOf[DataSource]
	}
	def lookupCF(name: String) = {
		lookup(name).asInstanceOf[ConnectionFactory]
	}
}

object XASupport { 

    private lazy val tm: bitronix.tm.BitronixTransactionManager = 
        play.api.Play.current.plugin[XASupportPlugin] match {
          case Some(plugin) => plugin.tm
          case None => throw new Exception("There is no XASupport plugin registered. Make sure it is enabled. See play documentation. (Hint: add it to play.plugins)")
        }

    /**
     * Use this flow control to make resources used inside `f` commit with the XA protocol.
     * Conditions: get resources like drivers or connection factories out of the context passed to f.
     * Connections are opened and closed as normal, for example by the withSession flow control offered 
     * by ScalaQuery / Slick.
     */
    def withXaTransaction[T](f: XAContext => T): T = {
		tm.begin

		//get a ref to the transaction, in case when we want to commit we are no longer on the same thread and TLS has lost the TX.
		//we have no idea what happens inside f!  they might spawn new threads or send work to akka asyncly
		val t = tm.getCurrentTransaction
		Logger("XASupport").info("Started XA transaction " + t.getGtrid())
		val ctx = new XAContext()
		var completed = false
		try{
		    val result = f(ctx)
    		completed = true
		    if(!ctx.rollbackOnly){
                Logger("XASupport").info("committing " + t.getGtrid() + "...")
                t.commit
                Logger("XASupport").info("committed " + t.getGtrid())
		    }
		    result
		}finally{
		    if(!completed || ctx.rollbackOnly){
		        //in case of exception, or in case of set rollbackOnly = true
	            Logger("XASupport").warn("rolling back (completed=" + completed + "/ctx.rollbackOnly=" + ctx.rollbackOnly)
	            t.rollback
		    }
		}
    }
}

class XASupportPlugin(app: play.Application) extends Plugin {

    protected[xasupport] var tm: BitronixTransactionManager = null
    
    override def onStart {
        val defaultPath = Play.current.configuration.getString("xasupport.defaultConfig").get
            println("-------------------")
            println("-------------------")
            println("OnStart")
            println("-------------------")
            println("-------------------")
        
        val file = new File(defaultPath)
        if(file.exists()){
            println("-------------------")
            println("-------------------")
            println("Exists, at " + file.getAbsolutePath)
            println("-------------------")
            println("-------------------")
        }else{
            println("-------------------")
            println("-------------------")
            println("NOT EXISTS, at " + file.getAbsolutePath)
            println("-------------------")
            println("-------------------")
        }
        Logger("XASupport").info("Using Bitronix config at " + file.getAbsolutePath)
        val prop = System.getProperty("bitronix.tm.configuration", file.getAbsolutePath) //default
        System.setProperty("bitronix.tm.configuration", prop) //override with default, if not set

        //start the TM
        tm = TransactionManagerServices.getTransactionManager
        
        Logger("XASupport").info("Started TM with resource config " + TransactionManagerServices.getConfiguration.getResourceConfigurationFilename)
    }

    override def onStop {
		//on graceful shutdown, we want to shutdown the TM too
	    Logger("XASupport").info("Shutting down TM")
		tm.shutdown
		Logger("XASupport").info("TM shut down")
	}

}
