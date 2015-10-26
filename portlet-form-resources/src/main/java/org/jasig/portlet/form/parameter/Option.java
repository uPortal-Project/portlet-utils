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

/**
 * Option represents a pre-defined form field option, such as an entry in a 
 * select, radiobutton, or checkbox menu.
 * 
 * @author Jen Bourey, jennifer.bourey
 * @version $Revision$
 */
public class Option {

    private String value;
    private String labelKey;

    /**
     * Get the value for this option.
     * 
     * @return value for this option
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the value for this option.
     * 
     * @param value value for this option
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get the key of the message to be used for this option's label.
     * 
     * @return key of the message to be used for this option's label
     */
    public String getLabelKey() {
        return labelKey;
    }

    /**
     * Set the key of the message to be used for this option's label.
     * 
     * @param labelKey key of the message to be used for this option's label
     */
    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

}
