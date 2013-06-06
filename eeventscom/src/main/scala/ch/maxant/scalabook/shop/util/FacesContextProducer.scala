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
import javax.enterprise.inject.Produces
import javax.enterprise.context.RequestScoped
import javax.faces.context.FacesContext

/**
 * This class produces the faces context, so we can inject it elsewhere.  Injection 
 * is nicer than a call to a static method, coz its easier to swap it out for testing.
 * 
 * @see http://web.archiveorange.com/archive/v/fhwU2FmwjbzqE6H9m0jT
 */
class FacesContextProducer {

    @Produces
	@RequestScoped
	def getFacesContext() = {
		FacesContext.getCurrentInstance();
	}

}