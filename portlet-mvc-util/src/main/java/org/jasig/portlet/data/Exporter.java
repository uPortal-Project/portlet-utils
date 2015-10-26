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
package org.jasig.portlet.data;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.jasig.portlet.spring.PortletApplicationContextLocator;
import org.springframework.context.ApplicationContext;

public class Exporter {
    public static void main(String[] args) throws Exception
    {
        String dir = args[0];
        String importExportContext = args[1];
        String sessionFactoryBeanName = args[2];
        String modelClassName = args[3];
        String serviceBeanName = args[4];
        String serviceBeanMethodName = args[5];

        ApplicationContext context = PortletApplicationContextLocator.getApplicationContext(importExportContext);
        SessionFactory sessionFactory = context.getBean(sessionFactoryBeanName, SessionFactory.class);
        Class<?> modelClass = Class.forName(modelClassName);

        Object service = context.getBean(serviceBeanName);
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();

        JAXBContext jc = JAXBContext.newInstance(modelClass);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        Method method = service.getClass().getMethod(serviceBeanMethodName);
        List<?> objects = (List<?>)method.invoke(service,null);

        for(Object o : objects)
        {
            session.lock(o, LockMode.NONE);
            JAXBElement je2 = new JAXBElement(new QName(modelClass.getSimpleName().toLowerCase()), modelClass, o);
            String output = dir + File.separator + UUID.randomUUID().toString() + ".xml";
            try {
                marshaller.marshal(je2,new FileOutputStream(output));
            } catch(Exception exception) {
                exception.printStackTrace();
            }
        }
        transaction.commit();
    }
}
