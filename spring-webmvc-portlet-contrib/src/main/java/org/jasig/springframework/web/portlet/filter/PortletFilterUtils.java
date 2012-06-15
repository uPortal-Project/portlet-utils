package org.jasig.springframework.web.portlet.filter;

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
import javax.portlet.filter.ActionFilter;
import javax.portlet.filter.EventFilter;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.PortletFilter;
import javax.portlet.filter.RenderFilter;
import javax.portlet.filter.ResourceFilter;

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
    
    /**
     * Call doFilter on the specified {@link PortletFilter}, determines the right PortletFilter interface to use by
     * looking at the {@link PortletRequest#LIFECYCLE_PHASE} attribute
     */
    public static void doFilter(PortletFilter filter, PortletRequest request, PortletResponse response, FilterChain chain)
            throws IOException, PortletException {
        
        final Object phase = request.getAttribute(PortletRequest.LIFECYCLE_PHASE);

        if (PortletRequest.ACTION_PHASE.equals(phase)) {
            if (filter instanceof ActionFilter) {
                ((ActionFilter) filter).doFilter((ActionRequest) request, (ActionResponse) response, chain);
            }
            else {
                throw new IllegalArgumentException("Provided filter does not implement ActionFilter as required by : " + phase + " - " + filter); 
            }
        }
        else if (PortletRequest.EVENT_PHASE.equals(phase)) {
            if (filter instanceof EventFilter) {
                ((EventFilter) filter).doFilter((EventRequest) request, (EventResponse) response, chain);
            }
            else {
                throw new IllegalArgumentException("Provided filter does not implement EventFilter as required by : " + phase + " - " + filter); 
            }
        }
        else if (PortletRequest.RENDER_PHASE.equals(phase)) {
            if (filter instanceof RenderFilter) {
                ((RenderFilter) filter).doFilter((RenderRequest) request, (RenderResponse) response, chain);
            }
            else {
                throw new IllegalArgumentException("Provided filter does not implement RenderFilter as required by : " + phase + " - " + filter); 
            }
        }
        else if (PortletRequest.RESOURCE_PHASE.equals(phase)) {
            if (filter instanceof ResourceFilter) {
                ((ResourceFilter) filter).doFilter((ResourceRequest) request, (ResourceResponse) response, chain);
            }
            else {
                throw new IllegalArgumentException("Provided filter does not implement ResourceFilter as required by : " + phase + " - " + filter); 
            }
        }
        else {
            throw new IllegalArgumentException("Unknown Portlet Lifecycle Phase: " + phase);
        }
    }
}
