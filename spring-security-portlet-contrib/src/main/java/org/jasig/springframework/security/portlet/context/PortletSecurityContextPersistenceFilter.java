package org.jasig.springframework.security.portlet.context;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.filter.ActionFilter;
import javax.portlet.filter.EventFilter;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.RenderFilter;
import javax.portlet.filter.ResourceFilter;

import org.jasig.springframework.portlet.filter.GenericPortletFilterBean;
import org.jasig.springframework.portlet.filter.PortletFilterUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class PortletSecurityContextPersistenceFilter
        extends GenericPortletFilterBean
        implements ActionFilter, EventFilter, RenderFilter, ResourceFilter {

    static final String FILTER_APPLIED = "__spring_security_pscpf_applied";

    private PortletSecurityContextRepository repo;

    private boolean forceEagerSessionCreation = false;

    public PortletSecurityContextPersistenceFilter() {
        this(new PortletSessionSecurityContextRepository());
    }

    public PortletSecurityContextPersistenceFilter(PortletSecurityContextRepository repo) {
        this.repo = repo;
    }
    
    @Override
    public void doFilter(ActionRequest request, ActionResponse response, FilterChain chain) throws IOException,
            PortletException {
        doSecurityFilter(request, response, chain);
    }
    
    @Override
    public void doFilter(EventRequest request, EventResponse response, FilterChain chain) throws IOException,
            PortletException {
        doSecurityFilter(request, response, chain);        
    }
    
    @Override
    public void doFilter(ResourceRequest request, ResourceResponse response, FilterChain chain) throws IOException,
            PortletException {
        doSecurityFilter(request, response, chain);        
    }

    @Override
    public void doFilter(RenderRequest request, RenderResponse response, FilterChain chain) throws IOException,
            PortletException {
        doSecurityFilter(request, response, chain);        
    }

    public void doSecurityFilter(PortletRequest request, PortletResponse response, FilterChain chain) throws IOException,
            PortletException {
        if (request.getAttribute(FILTER_APPLIED) != null) {
            // ensure that filter is only applied once per request
            PortletFilterUtils.doFilter(request, response, chain);
            return;
        }

        final boolean debug = logger.isDebugEnabled();

        request.setAttribute(FILTER_APPLIED, Boolean.TRUE);

        if (forceEagerSessionCreation) {
            PortletSession session = request.getPortletSession();

            if (debug && session.isNew()) {
                logger.debug("Eagerly created session: " + session.getId());
            }
        }

        PortletRequestResponseHolder holder = new PortletRequestResponseHolder(request, response);
        SecurityContext contextBeforeChainExecution = repo.loadContext(holder);

        try {
            SecurityContextHolder.setContext(contextBeforeChainExecution);

            PortletFilterUtils.doFilter(holder.getRequest(), holder.getResponse(), chain);

        } finally {
            SecurityContext contextAfterChainExecution = SecurityContextHolder.getContext();
            // Crucial removal of SecurityContextHolder contents - do this before anything else.
            SecurityContextHolder.clearContext();
            repo.saveContext(contextAfterChainExecution, holder);
            request.removeAttribute(FILTER_APPLIED);

            if (debug) {
                logger.debug("SecurityContextHolder now cleared, as request processing completed");
            }
        }
    }

    public void setForceEagerSessionCreation(boolean forceEagerSessionCreation) {
        this.forceEagerSessionCreation = forceEagerSessionCreation;
    }
}
