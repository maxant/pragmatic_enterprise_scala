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
package ch.maxant.scalabook.shop.ui.validators

import javax.faces.validator.Validator
import javax.faces.context.FacesContext
import javax.faces.component.UIComponent
import javax.faces.validator.ValidatorException
import javax.faces.application.FacesMessage
import java.util.ResourceBundle
import javax.servlet.http.HttpServletRequest

/**
 * A JSF validator.
 */
class EmailValidator extends Validator {

	private val NULL = "ch.maxant.scalabook.shop.ui.validators.EmailValidator.NULL"
	private val NO_AT = "ch.maxant.scalabook.shop.ui.validators.EmailValidator.NO_AT"
	private val NO_DOT = "ch.maxant.scalabook.shop.ui.validators.EmailValidator.NO_DOT"

	def validate(ctx: FacesContext, component: UIComponent, value: Object): Unit = {

		val request = FacesContext.getCurrentInstance().getExternalContext().getRequest().asInstanceOf[HttpServletRequest];
	    val bundle = ResourceBundle.getBundle("messages", request.getLocale());
	    
	    if(value == null) throw new ValidatorException(new FacesMessage(bundle.getString(NULL)))
		val svalue = value.toString();
		var index = svalue.indexOf("@");
		if(index == -1) throw new ValidatorException(new FacesMessage(bundle.getString(NO_AT)));
		index = svalue.indexOf(".", index);
		if(index == -1) throw new ValidatorException(new FacesMessage(bundle.getString(NO_DOT)));
    }

}