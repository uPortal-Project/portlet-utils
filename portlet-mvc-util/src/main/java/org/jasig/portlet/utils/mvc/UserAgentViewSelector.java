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
