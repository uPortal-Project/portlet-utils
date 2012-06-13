package org.jasig.springframework.security.portlet.context;

import javax.portlet.PortletRequest;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Eric Dalquist
 */
public class NullPortletSecurityContextRepository implements PortletSecurityContextRepository {

    @Override
    public SecurityContext loadContext(PortletRequestResponseHolder requestResponseHolder) {
        return SecurityContextHolder.createEmptyContext();
    }

    @Override
    public void saveContext(SecurityContext context, PortletRequestResponseHolder requestResponseHolder) {
    }

    @Override
    public boolean containsContext(PortletRequest request) {
        return false;
    }

}
