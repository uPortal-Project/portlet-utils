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

    @Override
    protected ApplicationContext createPortletApplicationContext(ApplicationContext parent) {
        //Right here parent is actually the root servlet application context, we will ignore it
        
        final PortletContext portletContext = this.getPortletContext();
        final PortletContextLoader portletContextLoader = (PortletContextLoader)portletContext.getAttribute(PortletApplicationContextUtils2.ROOT_PORTLET_APPLICATION_CONTEXT_LOADER_ATTRIBUTE);
        
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
