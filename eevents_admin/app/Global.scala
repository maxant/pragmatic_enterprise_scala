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
import play.api.GlobalSettings
import play.api.Application
import play.api.mvc.RequestHeader
import play.api.mvc.EssentialAction
import play.api.mvc.Results._
import controllers.TicketController

object Global extends GlobalSettings {

    private var _app: Application = null
    
    override def onStart(app: Application) {
        _app = app
        println("Application is started in folder " + app.path.getAbsolutePath)
    }
    
//    override def onError(request: RequestHeader, ex: Throwable) = {
//        InternalServerError("oh boy: " + ex.getMessage)
//    }
    
//    override def onHandlerNotFound(request: RequestHeader) = {
//        BadRequest("unknown")
//    }
    
    override def doFilter(a: EssentialAction) = {
        //here, or in onRouteRequest
        //TicketController.resourceRequestCount.incrementAndGet
        a
    }
    
    override def onRouteRequest(request: RequestHeader) = {
        TicketController.resourceRequestCount.incrementAndGet
        super.onRouteRequest(request)
    }

}