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
package org.jasig.springframework.ws.client.support.destination;

import org.jasig.springframework.core.io.DelegatingResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.client.support.destination.Wsdl11DestinationProvider;

/**
 * Wraps the WSDL {@link Resource} so that {@link Resource#exists()} always returns true.
 * 
 * @author Eric Dalquist
 * @version $Revision: 1.1 $
 */
public class FailSafeWsdl11DestinationProvider extends Wsdl11DestinationProvider {

    @Override
    public void setWsdl(Resource wsdlResource) {
        super.setWsdl(new DelegatingResource(wsdlResource) {
            @Override
            public boolean exists() {
                return true;
            }
        });
    }
    
}
