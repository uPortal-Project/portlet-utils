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
package org.jasig.portlet.spring;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.portlet.PortletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;


/**
 * Processor that uses spring EL for the implementation.
 *
 * @author Josh Helmer, jhelmer@unicon.net
 */
@Service
public class SpringELProcessor implements IExpressionProcessor, BeanFactoryAware {
    private static final ParserContext PARSER_CONTEXT = new TemplateParserContext("${", "}");

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private BeanResolver beanResolver;
    private Properties properties = new Properties();


    /**
     * @param beanFactory
     * @{inheritDoc}
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanResolver = new BeanFactoryResolver(beanFactory);
    }


    /**
     * Set the properties loader to user.
     *
     * @param properties the properties loader
     */
    public void setProperties(final Properties properties) {
        this.properties = properties;
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public String process(String value, PortletRequest request) {
        Map<String, Object> context = getContext(request);

        StandardEvaluationContext sec = new StandardEvaluationContext(context);
        sec.addPropertyAccessor(new MapAccessor());
        sec.addPropertyAccessor(new ReflectivePropertyAccessor());
        sec.addPropertyAccessor(new DefaultPropertyAccessor(
                PARSER_CONTEXT.getExpressionPrefix(),
                PARSER_CONTEXT.getExpressionSuffix()));
        if (beanResolver != null) {
            sec.setBeanResolver(beanResolver);
        }
        SpelExpressionParser parser = new SpelExpressionParser();

        try {
            String processed = parser
                    .parseExpression(value, PARSER_CONTEXT)
                    .getValue(sec, String.class);
            return processed;
        } catch (SpelEvaluationException e) {
            throw new EvaluationException("Failed to process string '" + value
                    + "'. See nested error message and check your SpEL tokens in your string", e);
        }
    }


    /**
     * Setup the context for spring EL.   Will add all properties from an optional properties file as
     * ${property['propKey']}, the request parameters as ${requestParam.xxx}, the
     * PortletRequest as ${request.xxx} and user info as ${user}.  Examples
     *
     * ${protocol}://${server}:${port}/${contextPath}/view?param=${requestParam.param}
     * ${protocol}://${server}:${port}/${contextPath}/view/${requestParam['form.itemId']}
     * ${protocol}://${server}:${port}/${contextPath}/view?userId=${user['user.login.id']}
     * ${protocol}://${server}:${port}/${contextPath}/view?action=${property['proxy.action.key']}
     *
     * ${server}, ${port}, ${protocol}, and ${contextPath} are also available which are the
     * values for accessing the particular portlet (contextPath in particular is the portlet webapp's
     * context path name).
     *
     * The Spring EL context will include all properties from app-launcher.properties, all request
     * parameters namespaced as "request" and all properties from the user-info map namespaced as "user". Examples:
     *
     * @param request the portlet request to read params from
     * @return a map of properties
     */
    private Map<String, Object> getContext(PortletRequest request) {
        Map<String, Object> context = new HashMap<String, Object>();

        context.put("property", properties);

        Map<String, String> requestMap = new HashMap<String, String>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            requestMap.put(name, request.getParameter(name));
        }
        
        context.put( "server", request.getServerName());
        context.put( "port", request.getServerPort());
        context.put( "protocol", request.getScheme());
        context.put( "contextPath", request.getContextPath());
        
        context.put("request", request);
        context.put("requestParam", requestMap);

        Map<String, String> userInfo = (Map<String, String>)request.getAttribute(PortletRequest.USER_INFO);
        context.put("user", userInfo);

        
        return context;
    }
}
