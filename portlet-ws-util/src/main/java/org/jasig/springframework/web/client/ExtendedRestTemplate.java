/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.springframework.web.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class ExtendedRestTemplate extends RestTemplate implements ExtendedRestOperations {
    public ExtendedRestTemplate() {
        super();
        
        final List<ClientHttpRequestInterceptor> interceptors = Collections.<ClientHttpRequestInterceptor>singletonList(HeaderSettingClientHttpRequestInterceptor.INSTANCE);
        super.setInterceptors(interceptors);
    }

    public ExtendedRestTemplate(ClientHttpRequestFactory requestFactory) {
        super(requestFactory);
        
        final List<ClientHttpRequestInterceptor> interceptors = Collections.<ClientHttpRequestInterceptor>singletonList(HeaderSettingClientHttpRequestInterceptor.INSTANCE);
        super.setInterceptors(interceptors);
    }
    
    
    @Override
    public final void setInterceptors(List<ClientHttpRequestInterceptor> interceptors) {
        //Make sure that the interceptors list always includes HeaderSettingClientHttpRequestInterceptor
        if (!interceptors.contains(HeaderSettingClientHttpRequestInterceptor.INSTANCE)) {
            final ArrayList<ClientHttpRequestInterceptor> newInterceptors = new ArrayList<ClientHttpRequestInterceptor>(interceptors.size() + 1);
            newInterceptors.addAll(newInterceptors);
            newInterceptors.add(HeaderSettingClientHttpRequestInterceptor.INSTANCE);
            interceptors = newInterceptors;
        }
        
        super.setInterceptors(interceptors);
    }

    @Override
    public <T> T getForObject(String url, Class<T> responseType, HttpHeaders headers, Object... urlVariables) throws RestClientException {
        try {
            HeaderSettingClientHttpRequestInterceptor.HEADERS_LOCAL.set(headers);
            return super.getForObject(url, responseType, urlVariables);
        }
        finally {
            HeaderSettingClientHttpRequestInterceptor.HEADERS_LOCAL.remove();
        }
        
    }
    
    @Override
    public int proxyRequest(ProxyResponse proxyResponse, String url, HttpMethod method, HttpHeaders headers, Object... urlVariables)
            throws RestClientException {
        try {
            HeaderSettingClientHttpRequestInterceptor.HEADERS_LOCAL.set(headers);
            
            final ProxyResponseExtractor responseExtractor = new ProxyResponseExtractor(proxyResponse);
            return super.execute(url, method, null, responseExtractor, urlVariables);
        }
        finally {
            HeaderSettingClientHttpRequestInterceptor.HEADERS_LOCAL.remove();
        }
    }

    private static class HeaderSettingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {
        public static final HeaderSettingClientHttpRequestInterceptor INSTANCE = new HeaderSettingClientHttpRequestInterceptor();
        private static final ThreadLocal<HttpHeaders> HEADERS_LOCAL = new ThreadLocal<HttpHeaders>();
        
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                throws IOException {
            
            final HttpHeaders additionalHeaders = HEADERS_LOCAL.get();
            if (additionalHeaders != null) {
                request.getHeaders().putAll(additionalHeaders);
            }
            
            return execution.execute(request, body);
        }
    }

    private static class ProxyResponseExtractor implements ResponseExtractor<Integer> {
        private final ProxyResponse proxyResponse;
        
        public ProxyResponseExtractor(ProxyResponse proxyResponse) {
            this.proxyResponse = proxyResponse;
        }

        @Override
        public Integer extractData(ClientHttpResponse response) throws IOException {
            final HttpStatus statusCode = response.getStatusCode();
            this.proxyResponse.setHttpStatus(statusCode);
            
            final HttpHeaders headers = response.getHeaders();
            this.proxyResponse.setHttpHeaders(headers);
            
            final InputStream body = response.getBody();
            final OutputStream outputStream = proxyResponse.getOutputStream();
            return IOUtils.copy(body, outputStream);
        }
    }
}
