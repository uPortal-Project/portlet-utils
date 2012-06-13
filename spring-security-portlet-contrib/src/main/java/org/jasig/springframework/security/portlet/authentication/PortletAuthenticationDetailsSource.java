package org.jasig.springframework.security.portlet.authentication;

import javax.portlet.PortletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;

public class PortletAuthenticationDetailsSource implements AuthenticationDetailsSource<PortletRequest, PortletAuthenticationDetails> {

    //~ Methods ========================================================================================================

    /**
     * @param context the {@code PortletRequest} object.
     * @return the {@code PortletAuthenticationDetails} containing information about the current request
     */
    public PortletAuthenticationDetails buildDetails(PortletRequest context) {
        return new PortletAuthenticationDetails(context);
    }
}
