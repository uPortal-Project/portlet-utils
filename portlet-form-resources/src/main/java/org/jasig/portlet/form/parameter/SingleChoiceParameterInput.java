/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.form.parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * SingleChoiceParameterInput represents a single-valued form parameter chosen
 * from a constrained list of options.  Examples of this type of input include
 * HTML single-select menus and radio buttons.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class SingleChoiceParameterInput implements
        SingleValuedParameterInput {

    private List<Option> options = new ArrayList<Option>();
    private SingleChoiceDisplay display;
    private String defaultValue;

    /**
     * Get a list of valid options for this parameter.
     * 
     * @return
     */
    public List<Option> getOptions() {
        return this.options;
    }
    
    /**
     * Set a list of valid options for this parameter.
     * 
     * @param options
     */
    public void setOptions(List<Option> options) {
        this.options = options;
    }

    /**
     * Get the display configuration for this parameter.
     * 
     * @return
     */
    public SingleChoiceDisplay getDisplay() {
        return display;
    }

    /**
     * Set the display configuration for this parameter.
     * 
     * @param value
     */
    public void setDisplay(SingleChoiceDisplay value) {
        this.display = value;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}
