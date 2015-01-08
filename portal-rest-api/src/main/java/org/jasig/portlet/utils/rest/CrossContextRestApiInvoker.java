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
package org.jasig.portlet.utils.rest;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides an interface for invoking REST APIs that reside in the same web 
 * container but a different context.
 * 
 * @author awills
 */
public interface CrossContextRestApiInvoker {
	
	/**
	 * Invokes the specified REST API using a cross-context {@link RequestDispatcher}.  
	 * The <code>uri</code> parameter should be of the following format:
	 * 
	 * <blockquote>/contextName/resource</blockquote>
	 * 
	 * Don't forget the leading slash!
	 * 
	 * @param req
	 * @param res
	 * @param uri
	 * @return
	 */
	RestResponse invoke(HttpServletRequest req, HttpServletResponse res, String uri);

	/**
	 * Invokes the specified REST API using a cross-context {@link RequestDispatcher}.  
	 * The <code>uri</code> parameter should be of the following format:
	 * 
	 * <blockquote>/contextName/resource/{param1}?{param2}</blockquote>
	 * 
	 * Don't forget the leading slash!  URI parameters will be replaced with the 
	 * (first) value in the Map, whereas querystring parameters will also use 
	 * the Map key as a parameter name.  For example, given param1=foo and 
	 * param2=[foo,bar], the URI about would be converted to the following:
	 * 
	 * <blockquote>/contextName/resource/foo?param2=foo&param2=bar</blockquote>
	 * 
	 * @param req
	 * @param res
	 * @param uri
	 * @param params
	 * @return
	 */
	RestResponse invoke(HttpServletRequest req, HttpServletResponse res, 
						String uri, Map<String, String[]> params);

}
