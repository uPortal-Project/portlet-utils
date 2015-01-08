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
package org.jasig.apache.http.impl.client;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.RequestContent;

/**
 * Allows for the <code>Content-Length</code> and <code>Transfer-Encoding</code> headers
 * to be present on the response.
 * 
 * @see RequestContent#RequestContent(boolean)
 */
 public class ResponseHeaderHandlingHttpClient extends DefaultHttpClient {
    
    public ResponseHeaderHandlingHttpClient() {
        super();
    }

    public ResponseHeaderHandlingHttpClient(ClientConnectionManager conman, HttpParams params) {
        super(conman, params);
    }

    public ResponseHeaderHandlingHttpClient(ClientConnectionManager conman) {
        super(conman);
    }

    public ResponseHeaderHandlingHttpClient(HttpParams params) {
        super(params);
    }

    /**
     * Override just to set RequestContent(true)
     */
    @Override
    protected BasicHttpProcessor createHttpProcessor() {
        final BasicHttpProcessor parentHttpProcessor = super.createHttpProcessor();
        
        for (int i = 0; i < parentHttpProcessor.getRequestInterceptorCount(); i++) {
            final HttpRequestInterceptor requestInterceptor = parentHttpProcessor.getRequestInterceptor(i);
            
            //Replace the existing RequestContent interceptor with a version that sets overwrite=true
            if (requestInterceptor instanceof RequestContent) {
                parentHttpProcessor.removeRequestInterceptorByClass(RequestContent.class);
                parentHttpProcessor.addInterceptor(new RequestContent(true), i);
                break;
            }
        }
        
        return parentHttpProcessor;
    }
}
