package org.jasig.springframework.portlet.filter;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.filter.FilterChain;

/**
 * Utilities for interacting with a portlet filter chain
 * 
 * @author Eric Dalquist
 */
public final class PortletFilterUtils {
    private PortletFilterUtils() {
    }
    
    /**
     * Call doFilter and use the {@link PortletRequest#LIFECYCLE_PHASE} attribute to figure out what
     * type of request/response are in use and call the appropriate doFilter on {@link FilterChain}
     */
    public static void doFilter(PortletRequest request, PortletResponse response, FilterChain chain)
            throws IOException, PortletException {
        
        final Object phase = request.getAttribute(PortletRequest.LIFECYCLE_PHASE);

        if (PortletRequest.ACTION_PHASE.equals(phase)) {
            chain.doFilter((ActionRequest) request, (ActionResponse) response);
        }
        else if (PortletRequest.EVENT_PHASE.equals(phase)) {
            chain.doFilter((EventRequest) request, (EventResponse) response);
        }
        else if (PortletRequest.RENDER_PHASE.equals(phase)) {
            chain.doFilter((RenderRequest) request, (RenderResponse) response);
        }
        else if (PortletRequest.RESOURCE_PHASE.equals(phase)) {
            chain.doFilter((ResourceRequest) request, (ResourceResponse) response);
        }
        else {
            throw new IllegalArgumentException("Unknown Portlet Lifecycle Phase: " + phase);
        }
    }
}
