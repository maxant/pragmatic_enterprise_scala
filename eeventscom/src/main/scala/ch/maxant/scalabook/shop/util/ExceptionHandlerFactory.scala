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
package ch.maxant.scalabook.shop.util
import javax.faces.context.ExceptionHandlerWrapper
import javax.faces.FacesException
import javax.ejb.EJBException
import javax.el.ELException
import java.io.FileNotFoundException
import javax.faces.context.FacesContext
import java.io.IOException
import javax.servlet.http.HttpServletResponse
import java.lang.reflect.InvocationTargetException
import java.util.logging.Logger
import java.util.logging.Level

/**
 * we want to handle errors ourselves, so we add our own global 
 * exception handler.
 * 
 * http://javalabor.blogspot.com/2011/09/jsf-2-global-exception-handling.html
 */
class ExceptionHandlerFactory(parent: javax.faces.context.ExceptionHandlerFactory) extends javax.faces.context.ExceptionHandlerFactory {

    @Override
    def getExceptionHandler(): ExceptionHandler = {
  	    new ExceptionHandler(parent.getExceptionHandler())
    }
}

class ExceptionHandler(wrapped: javax.faces.context.ExceptionHandler) extends ExceptionHandlerWrapper {

    override def getWrapped(): javax.faces.context.ExceptionHandler = {
        wrapped
    }
 
    override def handle() = {

        val unhandled = getUnhandledExceptionQueuedEvents().iterator()
        while(unhandled.hasNext){
        	var t = unhandled.next.getContext.getException
        	while (t.getCause != null) {
        	    t = if(t.isInstanceOf[InvocationTargetException]) {
        	    	t.asInstanceOf[InvocationTargetException].getTargetException()
        	    }else{
        	    	t.getCause
        	    }
        	}
        	
        	Logger.getLogger("exception-handler").log(Level.WARNING, "caught unhandled exception: " + t.getMessage + ", redirecting to error page", t)
        	
        	val facesContext = FacesContext.getCurrentInstance
			val externalContext = facesContext.getExternalContext
			val requestMap = externalContext.getRequestMap
			requestMap.put("errorMsg", t.getMessage)
        	if(!t.isInstanceOf[javax.faces.application.ViewExpiredException]){ //seems to cause a StackOverflow
	    		try {
	    			externalContext.dispatch("/error.jsf")
	    			facesContext.responseComplete
		        } finally {
		        	unhandled.remove
		        }
        	}else{
//        		externalContext.getResponse().asInstanceOf[HttpServletResponse].sendRedirect("/index.jsf")
        	}
        }
        wrapped.handle
    }
}