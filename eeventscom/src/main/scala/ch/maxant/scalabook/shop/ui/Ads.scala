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
package ch.maxant.scalabook.shop.ui

@javax.enterprise.inject.Model // == @Named + @RequestScoped
class Ads {

	def getAds() = {
	    println("calculating ads...")
        Thread.sleep(3000)
    	"some Ads which are time consuming to create... Blah blah blah... does anyone actually ever click on these things?!"
	}
	
}

