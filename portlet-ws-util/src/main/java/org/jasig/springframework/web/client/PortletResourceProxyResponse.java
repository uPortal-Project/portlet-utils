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
package org.jasig.springframework.web.client;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.ResourceResponse;

import org.jasig.springframework.web.client.ExtendedRestOperations.ProxyResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import com.google.common.collect.ImmutableSet;

/**
 * Implementation of {@link ProxyResponse} that wraps a portlet {@link ResourceResponse}
 * 
 * @author Eric Dalquist
 */
public class PortletResourceProxyResponse implements ProxyResponse {
    private final ResourceResponse resourceResponse;
    private final Set<String> excludedHeaders;

    public PortletResourceProxyResponse(ResourceResponse resourceResponse) {
        this.resourceResponse = resourceResponse;
        this.excludedHeaders = Collections.emptySet();
    }
    
    public PortletResourceProxyResponse(ResourceResponse resourceResponse, Set<String> excludedHeaders) {
        this.resourceResponse = resourceResponse;
        this.excludedHeaders = ImmutableSet.copyOf(excludedHeaders);
    }

    @Override
    public void setHttpStatus(HttpStatus status) {
        this.resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(status.value()));
    }

    @Override
    public void setHttpHeaders(HttpHeaders headers) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            if (!this.excludedHeaders.contains(headerName)) {
                for (String headerValue : entry.getValue()) {
                    this.resourceResponse.addProperty(headerName, headerValue);
                }
            }
        }        
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return resourceResponse.getPortletOutputStream();
    }
}
