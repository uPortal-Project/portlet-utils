package org.jasig.springframework.security.portlet.authentication;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility to access the primary attribute of the current user
 * 
 * @author Eric Dalquist
 */
public final class PrimaryAttributeUtils {
    /**
     * @return The current user's primary attribute
     */
    public static String getPrimaryId() {
        final SecurityContext context = SecurityContextHolder.getContext();
        final Authentication authentication = context.getAuthentication();
        
        final PrimaryAttributePortletAuthenticationDetails authenticationDetails = (PrimaryAttributePortletAuthenticationDetails)authentication.getDetails();
        return authenticationDetails.getPrimaryAttribute();
    }
}