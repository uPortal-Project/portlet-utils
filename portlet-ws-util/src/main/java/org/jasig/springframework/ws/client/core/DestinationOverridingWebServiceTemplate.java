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
package org.jasig.springframework.ws.client.core;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.destination.DestinationProvider;

/**
 * Can override parts of the destination URI
 * 
 * @author Eric Dalquist
 * @version $Revision: 1.1 $
 */
public class DestinationOverridingWebServiceTemplate extends WebServiceTemplate {
    private Integer portOverride = null;
    
    /**
     * @param portOverride Port to use in the destination URI, overrides the port from the {@link DestinationProvider}
     */
    public void setPortOverride(String portOverride) {
        try {
            this.portOverride = Integer.parseInt(portOverride);
        }
        catch (NumberFormatException nfe) {
            //ignore
        }
    }

    @Override
    public String getDefaultUri() {
        final DestinationProvider destinationProvider = this.getDestinationProvider();
        if (destinationProvider != null) {
            final URI uri = destinationProvider.getDestination();
            if (uri == null) {
                return null;
            }
            
            if (portOverride == null) {
                return uri.toString();
            }
            
            final URI overridenUri;
            try {
                overridenUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), portOverride, uri.getPath(), uri.getQuery(), uri.getFragment());
            }
            catch (URISyntaxException e) {
                this.logger.error("Could not override port on URI " + uri + " to " + portOverride, e);
                return uri.toString();
            }
            
            return overridenUri.toString();
        }

        return null;
    }

}
