package org.jasig.springframework.web.portlet.context;

import javax.portlet.PortletContext;

import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.portlet.DispatcherPortlet;
import org.springframework.web.portlet.FrameworkPortlet;
import org.springframework.web.portlet.context.ConfigurablePortletApplicationContext;

/**
 * Convenience methods for retrieving the root
 * {@link org.springframework.web.context.WebApplicationContext} for a given
 * <code>PortletContext</code>. This is e.g. useful for accessing a Spring
 * context from within custom web views or Struts actions.
 *
 * <p>Note that there are more convenient ways of accessing the root context for
 * many web frameworks, either part of Spring or available as external library.
 * This helper class is just the most generic way to access the root context.
 *
 * @author Juergen Hoeller
 * @author Eric Dalquist
 * @see ContextLoaderFilter
 * @see FrameworkPortlet
 * @see DispatcherPortlet
 */
public class PortletApplicationContextUtils2 {
    
    /**
     * Context attribute to bind root WebApplicationContext to on successful startup.
     * <p>Note: If the startup of the root context fails, this attribute can contain
     * an exception or error as value. Use PortletApplicationContextUtils2 for convenient
     * lookup of the root portlet WebApplicationContext.
     * @see PortletApplicationContextUtils2#getPortletApplicationContext(PortletContext)
     * @see PortletApplicationContextUtils2#getRequiredPortletApplicationContext(PortletContext)
     */
    public static final String ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE = ConfigurablePortletApplicationContext.class.getName() + ".ROOT";
    
    /**
     * Find the root WebApplicationContext for this portlet application, which is
     * typically loaded via {@link ContextLoaderFilter}.
     * <p>Will rethrow an exception that happened on root context startup,
     * to differentiate between a failed context startup and no context at all.
     * @param sc PortletContext to find the web application context for
     * @return the root WebApplicationContext for this portlet app
     * @throws IllegalStateException if the root WebApplicationContext could not be found
     * @see PortletApplicationContextUtils2#ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE
     */
    public static WebApplicationContext getRequiredPortletApplicationContext(PortletContext sc)
            throws IllegalStateException {

        WebApplicationContext wac = getPortletApplicationContext(sc);
        if (wac == null) {
            throw new IllegalStateException("No WebApplicationContext found: no ContextLoaderListener registered?");
        }
        return wac;
    }

    /**
     * Find the root WebApplicationContext for this web application, which is
     * typically loaded via {@link ContextLoaderFilter}.
     * <p>Will rethrow an exception that happened on root context startup,
     * to differentiate between a failed context startup and no context at all.
     * @param sc PortletContext to find the web application context for
     * @return the root WebApplicationContext for this portlet app, or <code>null</code> if none
     * @see PortletApplicationContextUtils2#ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE
     */
    public static WebApplicationContext getPortletApplicationContext(PortletContext sc) {
        return getPortletApplicationContext(sc, WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    }

    /**
     * Find a custom WebApplicationContext for this web application.
     * @param sc PortletContext to find the web application context for
     * @param attrName the name of the PortletContext attribute to look for
     * @return the desired WebApplicationContext for this web app, or <code>null</code> if none
     */
    public static WebApplicationContext getPortletApplicationContext(PortletContext sc, String attrName) {
        Assert.notNull(sc, "PortletContext must not be null");
        Object attr = sc.getAttribute(attrName);
        if (attr == null) {
            return null;
        }
        if (attr instanceof RuntimeException) {
            throw (RuntimeException) attr;
        }
        if (attr instanceof Error) {
            throw (Error) attr;
        }
        if (attr instanceof Exception) {
            throw new IllegalStateException((Exception) attr);
        }
        if (!(attr instanceof WebApplicationContext)) {
            throw new IllegalStateException("Context attribute is not of type WebApplicationContext: " + attr);
        }
        return (WebApplicationContext) attr;
    }
}
