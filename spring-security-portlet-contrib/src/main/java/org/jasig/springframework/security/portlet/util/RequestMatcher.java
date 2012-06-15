package org.jasig.springframework.security.portlet.util;

import javax.portlet.PortletRequest;

/**
 * Simple strategy to match an <tt>PortletRequest</tt>.
 *
 * @author Eric Dalquist
 */
public interface RequestMatcher {

    /**
     * Decides whether the rule implemented by the strategy matches the supplied request.
     *
     * @param request the request to check for a match
     * @return true if the request matches, false otherwise
     */
    boolean matches(PortletRequest request);

}
