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
package org.jasig.portlet.spring

import org.springframework.beans.factory.BeanFactory

import javax.portlet.PortletRequest

/**
 * Tests SpringELProcessor
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

class SpringELProcessorTest extends spock.lang.Specification {
    def processor = new SpringELProcessor()
    def portletRequest = Mock(PortletRequest)
    def beanFactory = Mock(BeanFactory)

    def setup() {
    }

    def "Properties extraction test"() {
        setup:
        portletRequest.getParameterNames() >> new StringTokenizer("");
        def props = new Properties(['key': 'value', 'dotted.key': 'the value'])
        processor.setProperties(props)
        expect:
        processor.process("got property['key']=\${property['key']}", portletRequest) == "got property['key']=value"
        processor.process("got property.key=\${property.key}", portletRequest) == "got property.key=value"
        processor.process("got property['dotted.key']=\${property['dotted.key']}", portletRequest) == "got property['dotted.key']=the value"
        // Not desirable, but map access to non-existent key simply drops out the term
        processor.process("bad property['doesNotExist']=\${property['doesNotExist']}", portletRequest) == "bad property['doesNotExist']="
    }

    def paramSetup() {
        portletRequest.getParameterNames() >> new StringTokenizer("bob sam sally");
        portletRequest.getParameter("bob") >> "theBob"
        portletRequest.getParameter("sam") >> "mySam"
        portletRequest.getParameter("sally") >> "isSally"
    }

    def "Parameter names extraction test map param"() {
        setup:
        paramSetup()
        expect:
        processor.process("I see \${requestParam['bob']}", portletRequest) == "I see theBob"

    }

    def "Parameter names extraction test dotted param"() {
        setup:
        paramSetup()
        expect:
        processor.process("I see \${requestParam.sam}", portletRequest) == "I see mySam"

    }
}
