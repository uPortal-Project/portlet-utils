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
package org.jasig.portlet.data;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.jasig.portlet.spring.PortletApplicationContextLocator;
import org.springframework.context.ApplicationContext;

public class Importer {
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

        JAXBContext jc = JAXBContext.newInstance(modelClass);

        File folder = new File(dir);
        File[] files = folder.listFiles(new ImportFileFilter());

        for(File f : files) {
            StreamSource xml = new StreamSource(f.getAbsoluteFile());
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            JAXBElement je1 = unmarshaller.unmarshal(xml, modelClass);
            Object object = je1.getValue();
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();

            Method method = service.getClass().getMethod(serviceBeanMethodName,modelClass);
            method.invoke(service,object);
            transaction.commit();
        }
    }

    private static class ImportFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            return (pathname.isFile() && pathname.getName().toLowerCase().endsWith(".xml"));
        }

    }
}
