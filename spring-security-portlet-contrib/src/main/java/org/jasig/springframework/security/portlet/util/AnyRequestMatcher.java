package org.jasig.springframework.security.portlet.util;

import javax.portlet.PortletRequest;

/**
 * Matches all {@link PortletRequest}s
 * 
 * @author Eric Dalquist
 */
public class AnyRequestMatcher implements RequestMatcher {

    @Override
    public boolean matches(PortletRequest request) {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AnyRequestMatcher;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
