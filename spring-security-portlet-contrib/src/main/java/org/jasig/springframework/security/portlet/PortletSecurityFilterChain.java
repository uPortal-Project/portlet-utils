package org.jasig.springframework.security.portlet;

import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.filter.PortletFilter;

/**
 * Defines a filter chain which is capable of being matched against an {@code PortletRequest}.
 * in order to decide whether it applies to that request.
 * <p>
 * Used to configure a {@code PortletFilterChainProxy}.
 *
 *
 * @author Eric Dalquist
 */
public interface PortletSecurityFilterChain {

    boolean matches(PortletRequest request);

    List<PortletFilter> getFilters();
}
