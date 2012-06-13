package org.jasig.springframework.security.portlet.context;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

/**
 * Used to pass the incoming request to {@link PortletSecurityContextRepository#loadContext(PortletRequestResponseHolder)}
 *
 * @author Eric Dalquist
 * @since 3.0
 */
public final class PortletRequestResponseHolder {
    private final PortletRequest request;
    private final PortletResponse response;
    
    private boolean portletSessionExistedAtStartOfRequest;
    private SecurityContext contextBeforeExecution;
    private Authentication authBeforeExecution;


    public PortletRequestResponseHolder(PortletRequest request, PortletResponse response) {
        this.request = request;
        this.response = response;
    }

    public PortletRequest getRequest() {
        return request;
    }

    public PortletResponse getResponse() {
        return response;
    }

    
    boolean isPortletSessionExistedAtStartOfRequest() {
        return portletSessionExistedAtStartOfRequest;
    }

    void setPortletSessionExistedAtStartOfRequest(boolean httpSessionExistedAtStartOfRequest) {
        this.portletSessionExistedAtStartOfRequest = httpSessionExistedAtStartOfRequest;
    }

    SecurityContext getContextBeforeExecution() {
        return contextBeforeExecution;
    }

    void setContextBeforeExecution(SecurityContext contextBeforeExecution) {
        this.contextBeforeExecution = contextBeforeExecution;
    }

    Authentication getAuthBeforeExecution() {
        return authBeforeExecution;
    }

    void setAuthBeforeExecution(Authentication authBeforeExecution) {
        this.authBeforeExecution = authBeforeExecution;
    }
}
