package org.jasig.springframework.web.portlet.context;

import javax.portlet.PortletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.portlet.DispatcherPortlet;

/**
 * Extension to {@link DispatcherPortlet} that adds support for the portlet app level
 * context
 * 
 * @author Eric Dalquist
 */
public class ContribDispatcherPortlet extends DispatcherPortlet {

    /**
     * Uses {@link PortletApplicationContextUtils2#getPortletApplicationContext(PortletContext)} to see if
     * the portlet application level context has been loaded. If not it is loaded by the {@link PortletContextLoader}
     * retrieved from the {@link PortletContext}. The portlet applications's context is then used as the parent for the
     * portlet's context.
     * <p>
     * The check/load of the portlet app context is done within a synchronized block on the {@link PortletContextLoader}.
     * <p>
     * If no {@link PortletContextLoader} exists in the {@link PortletContext} (the web.xml listener was not configured)
     * the portlet app level context loading is skipped.
     */
    @Override
    protected ApplicationContext createPortletApplicationContext(ApplicationContext parent) {
        //Right here parent is actually the root servlet application context, we will ignore it
        
        final PortletContext portletContext = this.getPortletContext();
        final PortletContextLoader portletContextLoader = (PortletContextLoader)portletContext.getAttribute(PortletApplicationContextUtils2.ROOT_PORTLET_APPLICATION_CONTEXT_LOADER_ATTRIBUTE);
        if (portletContextLoader == null) {
            logger.info("No PortletContextLoader found, skipping load of portlet-app level context. See org.jasig.springframework.web.portlet.context.PortletContextLoaderListener for more information");
            return super.createPortletApplicationContext(parent);
        }
        
        
        WebApplicationContext parentPortletApplicationContext;
        synchronized (portletContextLoader) {
            parentPortletApplicationContext = PortletApplicationContextUtils2.getPortletApplicationContext(portletContext);
            if (parentPortletApplicationContext == null) {
                parentPortletApplicationContext = portletContextLoader.initWebApplicationContext(portletContext);
            }
        }
        
        return super.createPortletApplicationContext(parentPortletApplicationContext);
    }
}
