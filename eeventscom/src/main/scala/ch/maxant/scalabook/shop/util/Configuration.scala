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
import java.util.Currency
import javax.inject.Named
import javax.enterprise.context.ApplicationScoped
import java.util.Date
import scala.beans.BeanProperty
import javax.enterprise.context.ApplicationScoped
import javax.ejb.Startup
import javax.annotation.PostConstruct

@Named(value="config")
@ApplicationScoped
@Startup
class Configuration {

    @BeanProperty
    val c = new NonCDIConfig()
    
    @PostConstruct
    def init()={
        c.load()
    }
}

//done this way, so that it can be used outside of CDI too
class NonCDIConfig {

    @BeanProperty
    val paymentPartnerUrl = "http://paymentpartnerch:8080/PaymentPartner/"

    @BeanProperty
	val merchantId = "4OIN0VLO2"

	@BeanProperty
	val siteUrl = "http://localhost:8080/eeventscom/"
    
	@BeanProperty
	val remoteSessionCacheUrl = "http://localhost:8099/ehcache/rest/events/"
	
	@BeanProperty
	val suggestionsUrl = "http://localhost:9000/customersWhoBought/"
	
	@BeanProperty
	val addSaleUrl = "http://localhost:9000/addSale"
	
    def load() = {
        //TODO read config out of database
    }
}