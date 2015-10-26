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
package org.jasig.portlet.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;


/**
 * @author Josh Helmer, jhelmer@unicon.net
 *
 * Property accessor that tries to log an error and provide a default value
 * if a property can not be found.
 *
 * Limitation:  the accessor only gets access to the leaf property name.  If
 * you have a property like:  user.login.id, this will only be able to print
 * "${id}"  In the future need to revisit to try and find a more flexible
 * mechanism for handling without just throwing an exception.
 */
public class DefaultPropertyAccessor implements PropertyAccessor {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private String leading;
    private String trailing;

    public DefaultPropertyAccessor(final String leading, final String trailing) {
        this.leading = leading;
        this.trailing = trailing;
    }


    @Override
    public Class[] getSpecificTargetClasses() {
        return new Class[] { Object.class };
    }


    @Override
    public boolean canRead(EvaluationContext evaluationContext, Object o, String s) throws AccessException {
        return true;
    }


    @Override
    public TypedValue read(EvaluationContext evaluationContext, Object o, String s) throws AccessException {
        logger.error("Property '" + s + "' not found!");
        return new TypedValue(leading + s + trailing);
    }


    @Override
    public boolean canWrite(EvaluationContext evaluationContext, Object o, String s) throws AccessException {
        return false;
    }


    @Override
    public void write(EvaluationContext evaluationContext, Object o, String s, Object o2) throws AccessException {
    }
}
