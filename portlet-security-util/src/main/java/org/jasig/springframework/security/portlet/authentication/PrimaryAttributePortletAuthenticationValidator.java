package org.jasig.springframework.security.portlet.authentication;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.jasig.springframework.security.portlet.util.AuthenticationValidator;
import org.springframework.security.core.Authentication;

public class PrimaryAttributePortletAuthenticationValidator implements AuthenticationValidator {
	
	private PrimaryAttributePortletPreAuthenticatedAuthenticationDetailsSource preAuthenticationDetailsSource;
	
	public void setPreAuthenticationDetailsSource(PrimaryAttributePortletPreAuthenticatedAuthenticationDetailsSource value) {
		preAuthenticationDetailsSource = value;
	}
	
	@Override
	public boolean validate(Authentication authentication, PortletRequest request) {
		
		//get current request value
		String currentValue = preAuthenticationDetailsSource.getPrimaryUserAttribute(request);
		
		//get cached value
		final PrimaryAttributePortletAuthenticationDetails authenticationDetails = (PrimaryAttributePortletAuthenticationDetails)authentication.getDetails();
        String cachedValue = authenticationDetails.getPrimaryAttribute();
		
        //return if those values changed or not
        return StringUtils.equals(currentValue, cachedValue);
	}

}
