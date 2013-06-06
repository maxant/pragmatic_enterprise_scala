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
package ch.maxant.scalabook.shop.web;

import java.io.IOException;

import javax.faces.application.StateManager;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

public class MyStateManager extends StateManager {

    @Override
    public UIViewRoot restoreView(FacesContext arg0, String arg1, String arg2) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String getViewState(FacesContext context) {
        // TODO Auto-generated method stub
        return super.getViewState(context);
    }
    
    @Override
    public boolean isSavingStateInClient(FacesContext context) {
        // TODO Auto-generated method stub
        return super.isSavingStateInClient(context);
    }
    
    @Override
    public Object saveView(FacesContext context) {
        // TODO Auto-generated method stub
        return super.saveView(context);
    }
    
    @Override
    public void writeState(FacesContext arg0, Object arg1) throws IOException {
        // TODO Auto-generated method stub
        super.writeState(arg0, arg1);
    }
    
}
