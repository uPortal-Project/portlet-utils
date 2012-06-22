package org.jasig.springframework.web.portlet.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.ContextCleanupListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * Bootstrap listener to start up and shut down Spring's root {@link WebApplicationContext}.
 * Simply delegates to {@link PortletContextLoader} as well as to {@link ContextCleanupListener}.
 *
 * <p>This listener should be registered after
 * {@link org.springframework.web.util.Log4jConfigListener}
 * in <code>web.xml</code>, if the latter is used.
 *
 * <p>As of Spring 3.1, {@code ContextLoaderListener} supports injecting the root web
 * application context via the {@link #ContextLoaderListener(WebApplicationContext)}
 * constructor, allowing for programmatic configuration in Servlet 3.0+ environments. See
 * {@link org.springframework.web.WebApplicationInitializer} for usage examples.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 17.02.2003
 * @see org.springframework.web.WebApplicationInitializer
 * @see org.springframework.web.util.Log4jConfigListener
 */
public class PortletContextLoaderListener implements ServletContextListener {
    private PortletContextLoader contextLoader;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final ServletContext servletContext = sce.getServletContext();
        if (servletContext.getAttribute(PortletApplicationContextUtils2.ROOT_PORTLET_APPLICATION_CONTEXT_LOADER_ATTRIBUTE) != null) {
            throw new IllegalStateException(
                    "Cannot initialize root portlet ContextLoader context because there is already a root portlet ContextLoader present - " +
                    "check whether you have multiple PortletContextLoaderListener definitions in your web.xml!");
        }
        
        //Register the portlet context loader with the servlet context
        contextLoader = new PortletContextLoader();
        servletContext.setAttribute(PortletApplicationContextUtils2.ROOT_PORTLET_APPLICATION_CONTEXT_LOADER_ATTRIBUTE, contextLoader);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //destroy the root portlet app context
        final ServletContext servletContext = sce.getServletContext();
        contextLoader.closeWebApplicationContext(servletContext);
        
        servletContext.removeAttribute(PortletApplicationContextUtils2.ROOT_PORTLET_APPLICATION_CONTEXT_LOADER_ATTRIBUTE);
        contextLoader = null;
    }
}
