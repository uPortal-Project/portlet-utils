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
package org.jasig.portlet.utils.mvc;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.jasig.portlet.utils.mvc.IViewSelector;

public class UserAgentViewSelector implements IViewSelector {
  
  private static final String DEFAULT_MOBILE_REGEX = "(.*iPhone.*)|(.*Android.*)|(.*IEMobile.*)|(.*Safari.*Pre.*)|(.*Nokia.*AppleWebKit.*)|(.*Black[Bb]erry.*)|(.*Opera Mobile.*)|(.*Windows Phone.*)|(.*Fennec.*)|(.*Minimo.*)";

  private String mobileRegex;
  
  public String getMobileRegex() {
    return mobileRegex;
  }
  
  public void setMobileRegex(String mobileRegex) {
    this.mobileRegex = mobileRegex;
  }
  
  @Override
  public boolean isMobile(PortletRequest request) {
    String property = request.getProperty("user-agent");
    if(StringUtils.isBlank(property))
      return false;
    else {
      return property.matches(StringUtils.isBlank(mobileRegex) ? DEFAULT_MOBILE_REGEX : mobileRegex);
    }
    
  }

}
