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

import javax.servlet.http.HttpServletResponse;

import org.jasig.springframework.web.client.ExtendedRestOperations.ProxyResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import com.google.common.collect.ImmutableSet;

/**
 * Implementation of {@link ProxyResponse} that wraps a {@link HttpServletResponse}
 * 
 * @author Eric Dalquist
 */
public class HttpServletProxyResponse implements ProxyResponse {
    private final HttpServletResponse servletResponse;
    private final Set<String> excludedHeaders;

    public HttpServletProxyResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
        this.excludedHeaders = Collections.emptySet();
    }
    
    public HttpServletProxyResponse(HttpServletResponse servletResponse, Set<String> excludedHeaders) {
        this.servletResponse = servletResponse;
        this.excludedHeaders = ImmutableSet.copyOf(excludedHeaders);
    }

    @Override
    public void setHttpStatus(HttpStatus status) {
        this.servletResponse.setStatus(status.value());
    }

    @Override
    public void setHttpHeaders(HttpHeaders headers) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            if (!this.excludedHeaders.contains(headerName)) {
                for (String headerValue : entry.getValue()) {
                    this.servletResponse.addHeader(headerName, headerValue);
                }
            }
        }        
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return servletResponse.getOutputStream();
    }
}
