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
package org.jasig.apache.http.conn.scheme;

import java.util.Collections;
import java.util.Set;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * Utility for creating a {@link SchemeRegistry} in spring
 * 
 * @author Eric Dalquist
 */
public class SchemeRegistryFactoryBean extends AbstractFactoryBean<SchemeRegistry> {
    private boolean extendDefault = true;
    private Set<Scheme> schemes = Collections.emptySet();
    
    /**
     * If true {@link SchemeRegistryFactory#createDefault()} is used to create the {@link SchemeRegistry}
     * before the additional schemes are registered via {@link SchemeRegistry#register(Scheme)}, defaults
     * to true; 
     */
    public void setExtendDefault(boolean extendDefault) {
        this.extendDefault = extendDefault;
    }

    /**
     * {@link Scheme}s to register with the {@link SchemeRegistry}
     */
    public void setSchemes(Set<Scheme> schemes) {
        this.schemes = schemes;
    }

    @Override
    public Class<?> getObjectType() {
        return SchemeRegistry.class;
    }

    @Override
    protected SchemeRegistry createInstance() throws Exception {
        //Create the registry
        final SchemeRegistry registry;
        if (extendDefault) {
            registry = SchemeRegistryFactory.createDefault();
        }
        else {
            registry = new SchemeRegistry();
        }
        
        //Register additional schemes
        for (final Scheme scheme : schemes) {
            registry.register(scheme);
        }
        
        return registry;
    }

}
