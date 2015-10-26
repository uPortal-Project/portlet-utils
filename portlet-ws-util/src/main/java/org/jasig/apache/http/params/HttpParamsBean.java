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
package org.jasig.apache.http.params;

import java.util.Map;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import com.google.common.base.Function;

/**
 * HttpClient 4 params object that can be configured from a Map
 * 
 * @author Eric Dalquist
 */
public class HttpParamsBean implements HttpParams {
    private final HttpParams delegate = new BasicHttpParams();
    
    public HttpParamsBean(Map<String, Object> parameters) {
        for (final Map.Entry<String, Object> parameter : parameters.entrySet()) {
            final String name = parameter.getKey();
            final Object value = parameter.getValue();
            this.delegate.setParameter(name, value);
        }
    }

    public HttpParams setParameter(String name, Object value) {
        return delegate.setParameter(name, value);
    }

    /**
     * @deprecated (4.1)
     */
    @Deprecated
    public HttpParams copy() {
        return delegate.copy();
    }

    public boolean removeParameter(String name) {
        return delegate.removeParameter(name);
    }

    public HttpParams setLongParameter(String name, long value) {
        return delegate.setLongParameter(name, value);
    }

    public HttpParams setIntParameter(String name, int value) {
        return delegate.setIntParameter(name, value);
    }

    public HttpParams setDoubleParameter(String name, double value) {
        return delegate.setDoubleParameter(name, value);
    }

    public HttpParams setBooleanParameter(String name, boolean value) {
        return delegate.setBooleanParameter(name, value);
    }
    
    public Object getParameter(String name) {
        return delegate.getParameter(name);
    }
    
    private <T> T getTypedParameter(String name, T defaultValue, Class<T> type, Function<Object, T> parser) {
        final Object value = delegate.getParameter(name);
        
        //If null return default value
        if (value == null) {
            return defaultValue;
        }
        
        //If already the right type just cast and return
        final Class<? extends Object> valueType = value.getClass();
        if (type.isAssignableFrom(valueType)) {
            return type.cast(value);
        }
        
        //Try parsing the value to the desired type
        try {
            return parser.apply(value);
        }
        catch (Exception e) {
            final ClassCastException cce = new ClassCastException("Cannot convert '" + value + "' of type " + valueType.getName() + " to " + type.getName());
            cce.initCause(e);
            throw cce;
        }
    }

    public long getLongParameter(final String name, long defaultValue) {
        return getTypedParameter(name, defaultValue, Long.TYPE, new Function<Object, Long>() {
            public Long apply(Object value) {
                final Long longValue = Long.valueOf(value.toString());
                
                //If this parameter is supposed to be a long store it as such to save future parsing work
                delegate.setLongParameter(name, longValue);
                
                return longValue;
            }
        });
    }

    public int getIntParameter(final String name, int defaultValue) {
        return getTypedParameter(name, defaultValue, Integer.TYPE, new Function<Object, Integer>() {
            public Integer apply(Object value) {
                final Integer intValue = Integer.valueOf(value.toString());
                
                //If this parameter is supposed to be an int store it as such to save future parsing work
                delegate.setIntParameter(name, intValue);
                
                return intValue;
            }
        });
    }

    public double getDoubleParameter(final String name, double defaultValue) {
        return getTypedParameter(name, defaultValue, Double.TYPE, new Function<Object, Double>() {
            public Double apply(Object value) {
                final Double doubleValue = Double.valueOf(value.toString());
                
                //If this parameter is supposed to be a double store it as such to save future parsing work
                delegate.setDoubleParameter(name, doubleValue);
                
                return doubleValue;
            }
        });
    }

    public boolean getBooleanParameter(final String name, boolean defaultValue) {
        return getTypedParameter(name, defaultValue, Boolean.TYPE, new Function<Object, Boolean>() {
            public Boolean apply(Object value) {
                final Boolean booleanValue = Boolean.valueOf(value.toString());
                
                //If this parameter is supposed to be a boolean store it as such to save future parsing work
                delegate.setBooleanParameter(name, booleanValue);
                
                return booleanValue;
            }
        });
    }

    public boolean isParameterTrue(final String name) {
        return getBooleanParameter(name, false);
    }

    public boolean isParameterFalse(final String name) {
        return !getBooleanParameter(name, false);
    }
}
