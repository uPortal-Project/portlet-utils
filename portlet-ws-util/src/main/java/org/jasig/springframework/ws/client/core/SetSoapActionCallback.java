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
package org.jasig.springframework.ws.client.core;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

/**
 * Calls {@link SaajSoapMessage#setSoapAction(String)} with the configured String if the {@link WebServiceMessage} is an
 * instance of {@link SaajSoapMessage}
 * 
 * @author Eric Dalquist
 * @version $Revision: 1.1 $
 */
public class SetSoapActionCallback implements WebServiceMessageCallback {
    private final String soapAction;
    
    public SetSoapActionCallback(String soapAction) {
        this.soapAction = soapAction;
    }
    
    public String getSoapAction() {
        return soapAction;
    }

    @Override
    public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
        if (message instanceof SaajSoapMessage) {
            final SaajSoapMessage casted = (SaajSoapMessage) message;
            casted.setSoapAction(this.soapAction);
        }
    }
}