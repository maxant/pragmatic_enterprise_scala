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
package ch.maxant.scalabook.shop.events.services.currency

import java.math.BigDecimal
import java.util.Currency
import java.util.logging.Logger

import ch.maxant.scalabook.shop.bom.Price
import ch.maxant.scalabook.shop.events.services.EventsAdapterClient
import ch.maxant.scalabook.shop.util.Constants.CHF
import ch.maxant.scalabook.shop.util.Constants.EUR
import javax.ejb.LocalBean
import javax.ejb.Stateless
import javax.inject.Inject

@Stateless
@LocalBean
private[services] class CurrencyService extends EventsAdapterClient {
    
    @Inject var log: Logger = null

    def convertPrice(p: Price, c: Currency) = {
        if(p.currency == EUR && c == CHF){
            new Price(p.value.multiply(new BigDecimal("1.2")), CHF)
        }else{
        	throw new IllegalArgumentException("Cannot convert from " + p.currency + " to " + c)
        }
    }
   
}