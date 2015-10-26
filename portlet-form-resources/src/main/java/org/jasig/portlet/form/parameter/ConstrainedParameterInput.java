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

import java.util.List;

/**
 * ConstrainedParameterInput represents an input type which is constrained
 * to a predefined list of values.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public interface ConstrainedParameterInput extends ParameterInput {

    /**
     * Get the list of valid options for this parameter.
     * 
     * @return list of valid options for this parameter
     */
    public List<Option> getOptions();
    
    /**
     * Set the list of valid options for this parameter.
     * 
     * @param options list of valid options for this parameter
     */
    public void setOptions(List<Option> options);
    
}
