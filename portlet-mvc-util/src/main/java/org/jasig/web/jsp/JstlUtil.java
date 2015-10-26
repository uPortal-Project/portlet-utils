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
package org.jasig.web.jsp;

import java.util.Collection;

import org.springframework.beans.factory.config.FieldRetrievingFactoryBean;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Utilities for use in JSPs
 * 
 * @author Eric Dalquist
 */
public class JstlUtil {
    private static final LoadingCache<String, Object> STATIC_FIELD_CACHE = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, Object>() {
        @Override
        public Object load(String key) throws Exception {
            final FieldRetrievingFactoryBean fieldRetrievingFactoryBean = new FieldRetrievingFactoryBean();
            fieldRetrievingFactoryBean.setStaticField(key);
            fieldRetrievingFactoryBean.afterPropertiesSet();
            final Object value = fieldRetrievingFactoryBean.getObject();
            
            if (value == null) {
                return NULL_PLACEHOLDER;
            }
            
            return value;
        }
    });
    
    
    private static final Object NULL_PLACEHOLDER = new Object();
    
    public static Object constant(String staticField) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        final Object value = STATIC_FIELD_CACHE.getUnchecked(staticField);
        if (value == NULL_PLACEHOLDER) {
            return null;
        }
        return value;
    }
    
    public static boolean contains(Collection<?> coll, Object o) {
        return coll.contains(o);
    }
    
    public static boolean equalsIgnoreCase(String s1, String s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 == null) {
            return false;
        }
        return s1.equalsIgnoreCase(s2);
    }
}
