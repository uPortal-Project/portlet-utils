package org.jasig.springframework.web.portlet.context;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.filter.ActionFilter;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;

import org.springframework.web.context.ContextCleanupListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * Bootstrap listener to start up and shut down Spring's root portlet {@link WebApplicationContext}.
 * Simply delegates to {@link ContextLoader} as well as to {@link ContextCleanupListener}.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Eric Dalquist
 */
public class ContextLoaderFilter extends ContextLoader implements ActionFilter {
    private PortletContext portletContext;
    
    @Override
    public void init(FilterConfig filterConfig) throws PortletException {
        portletContext = filterConfig.getPortletContext();
        this.initWebApplicationContext(portletContext);
    }

    @Override
    public void destroy() {
        if (portletContext != null) {
            this.closeWebApplicationContext(portletContext);
            portletContext = null;
        }
    }
    
    /**
     * Does nothing, just required by the portlet spec
     */
    @Override
    public void doFilter(ActionRequest request, ActionResponse response, FilterChain chain) throws IOException, PortletException {
        chain.doFilter(request, response);
    }
}
