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
package org.jasig.xml.bind;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.bind.DatatypeConverter;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

/**
 * Utility for use with JAXB to use Joda Time for dates/times/dateTimes.<br/>
 * Example Usage:
 * <pre>
&lt;jaxb:bindings&gt;
    &lt;jaxb:globalBindings fixedAttributeAsConstantProperty=&quot;true&quot; enableFailFastCheck=&quot;true&quot;&gt;
        &lt;jaxb:javaType name=&quot;org.joda.time.DateTime&quot; xmlType=&quot;xs:dateTime&quot; parseMethod=&quot;org.jasig.portlet.jaxb.util.JodaTypeConverter.parseDateTime&quot; printMethod=&quot;org.jasig.portlet.jaxb.util.JodaTypeConverter.printDateTime&quot; /&gt;
        &lt;jaxb:javaType name=&quot;org.joda.time.DateMidnight&quot; xmlType=&quot;xs:date&quot; parseMethod=&quot;org.jasig.portlet.jaxb.util.JodaTypeConverter.parseDate&quot; printMethod=&quot;org.jasig.portlet.jaxb.util.JodaTypeConverter.printDate&quot; /&gt;
        &lt;jaxb:javaType name=&quot;org.joda.time.LocalTime&quot; xmlType=&quot;xs:time&quot; parseMethod=&quot;org.jasig.portlet.jaxb.util.JodaTypeConverter.parseTime&quot; printMethod=&quot;org.jasig.portlet.jaxb.util.JodaTypeConverter.printTime&quot; /&gt;
    &lt;/jaxb:globalBindings&gt;
&lt;/jaxb:bindings&gt;
 * </pre>
 * 
 * @author Eric Dalquist
 */
public final class JodaTypeConverter {
    private JodaTypeConverter() {
    }
    
    public static String printDate(DateMidnight val) {
        final GregorianCalendar cal = val.toGregorianCalendar();
        return DatatypeConverter.printDate(cal);
    }

    public static String printDateTime(DateTime val) {
        final GregorianCalendar cal = val.toGregorianCalendar();
        return DatatypeConverter.printDateTime(cal);
    }

    public static String printTime(LocalTime localTime) {
        final DateTime dateTime = localTime.toDateTimeToday(DateTimeZone.UTC);
        final GregorianCalendar cal = dateTime.toGregorianCalendar();
        return DatatypeConverter.printTime(cal);
    }

    public static DateMidnight parseDate(String lexicalXSDDate) {
        final Calendar cal = DatatypeConverter.parseDate(lexicalXSDDate);
        return new DateMidnight(cal);
    }

    public static DateTime parseDateTime(String lexicalXSDDateTime) {
        final Calendar cal = DatatypeConverter.parseDateTime(lexicalXSDDateTime);
        return new DateTime(cal);
    }

    public static LocalTime parseTime(String lexicalXSDTime) {
        final Calendar cal = DatatypeConverter.parseTime(lexicalXSDTime);
        return new DateTime(cal).withZone(DateTimeZone.UTC).toLocalTime();
    }
}
