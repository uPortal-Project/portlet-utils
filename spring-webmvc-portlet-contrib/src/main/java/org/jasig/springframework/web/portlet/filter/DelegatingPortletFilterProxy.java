package org.jasig.springframework.web.portlet.filter;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletException;
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

import org.springframework.context.ApplicationContext;
import org.springframework.web.portlet.context.PortletApplicationContextUtils;

/**
 * @author Eric Dalquist
 * @version $Revision: 23744 $
 */
public class DelegatingPortletFilterProxy extends GenericPortletFilterBean implements
    ActionFilter, EventFilter, RenderFilter, ResourceFilter {

    private String targetBeanName;

    private boolean targetFilterLifecycle = false;

    private PortletFilter delegate;
    private ActionFilter actionDelegate;
    private EventFilter eventDelegate;
    private RenderFilter renderDelegate;
    private ResourceFilter resourceDelegate;

    private final ReadWriteLock delegateLock = new ReentrantReadWriteLock();
    private final Lock delegateReadLock = this.delegateLock.readLock();
    private final Lock delegateWriteLock = this.delegateLock.writeLock();


    /**
     * Set the name of the target bean in the Spring application context.
     * The target bean must implement the standard Portlet 2.3 PortletFilter interface.
     * <p>By default, the <code>filter-name</code> as specified for the
     * DelegatingPortletFilterProxy in <code>web.xml</code> will be used.
     */
    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = targetBeanName;
    }

    /**
     * Return the name of the target bean in the Spring application context.
     */
    protected String getTargetBeanName() {
        return this.targetBeanName;
    }

    /**
     * Set whether to invoke the <code>PortletFilter.init</code> and
     * <code>PortletFilter.destroy</code> lifecycle methods on the target bean.
     * <p>Default is "false"; target beans usually rely on the Spring application
     * context for managing their lifecycle. Setting this flag to "true" means
     * that the portlet container will control the lifecycle of the target
     * PortletFilter, with this proxy delegating the corresponding calls.
     */
    public void setTargetFilterLifecycle(boolean targetFilterLifecycle) {
        this.targetFilterLifecycle = targetFilterLifecycle;
    }

    /**
     * Return whether to invoke the <code>PortletFilter.init</code> and
     * <code>PortletFilter.destroy</code> lifecycle methods on the target bean.
     */
    protected boolean isTargetFilterLifecycle() {
        return this.targetFilterLifecycle;
    }


    protected void initFilterBean() throws PortletException {
        // If no target bean name specified, use filter name.
        if (this.targetBeanName == null) {
            this.targetBeanName = getFilterName();
        }

        // Fetch Spring root application context and initialize the delegate early,
        // if possible. If the root application context will be started after this
        // filter proxy, we'll have to resort to lazy initialization.
        initDelegate(false);
    }

    
    public void doFilter(ResourceRequest request, ResourceResponse response, FilterChain chain) throws IOException,
            PortletException {
        
        // Lazily initialize the delegate if necessary.
        initDelegate(true);
        
        if (this.resourceDelegate == null) {
            throw new IllegalStateException("The delegate PortletFilter does not implement ResourceFilter but " + this.getFilterName() + " is configured with the RESOURCE_PHASE lifecycle.");
        }

        // Let the delegate perform the actual doFilter operation.
        invokeDelegate(this.resourceDelegate, request, response, chain);
    }

    public void doFilter(EventRequest request, EventResponse response, FilterChain chain) throws IOException,
            PortletException {
        
        // Lazily initialize the delegate if necessary.
        initDelegate(true);
        
        if (this.eventDelegate == null) {
            throw new IllegalStateException("The delegate PortletFilter does not implement EventFilter but " + this.getFilterName() + " is configured with the EVENT_PHASE lifecycle.");
        }

        // Let the delegate perform the actual doFilter operation.
        invokeDelegate(this.eventDelegate, request, response, chain);
    }

    public void doFilter(RenderRequest request, RenderResponse response, FilterChain chain) throws IOException,
            PortletException {
        
        // Lazily initialize the delegate if necessary.
        initDelegate(true);
        
        if (this.renderDelegate == null) {
            throw new IllegalStateException("The delegate PortletFilter does not implement RenderFilter but " + this.getFilterName() + " is configured with the RENDER_PHASE lifecycle.");
        }

        // Let the delegate perform the actual doFilter operation.
        invokeDelegate(this.renderDelegate, request, response, chain);
    }

    public void doFilter(ActionRequest request, ActionResponse response, FilterChain chain) throws IOException,
            PortletException {
        
        // Lazily initialize the delegate if necessary.
        initDelegate(true);
        
        if (this.actionDelegate == null) {
            throw new IllegalStateException("The delegate PortletFilter does not implement ActionFilter but " + this.getFilterName() + " is configured with the ACTION_PHASE lifecycle.");
        }

        // Let the delegate perform the actual doFilter operation.
        invokeDelegate(this.actionDelegate, request, response, chain);
    }

    public void destroy() {
        PortletFilter delegateToUse = null;
        delegateReadLock.lock();
        try {
            delegateToUse = this.delegate;
        }
        finally {
            delegateReadLock.unlock();
        }
        
        if (delegateToUse != null) {
            destroyDelegate(delegateToUse);
        }
    }


    /**
     * Retrieve a <code>WebApplicationContext</code> from the <code>PortletContext</code>
     * attribute with the {@link #setContextAttribute configured name}. The
     * <code>WebApplicationContext</code> must have already been loaded and stored in the
     * <code>PortletContext</code> before this filter gets initialized (or invoked).
     * <p>Subclasses may override this method to provide a different
     * <code>WebApplicationContext</code> retrieval strategy.
     * @return the WebApplicationContext for this proxy, or <code>null</code> if not found
     * @see #getContextAttribute()
     */
    protected ApplicationContext findWebApplicationContext() {
        return PortletApplicationContextUtils.getWebApplicationContext(getPortletContext());
    }

    /**
     * Initialize the PortletFilter delegate, defined as bean the given Spring
     * application context.
     * <p>The default implementation fetches the bean from the application context
     * and calls the standard <code>PortletFilter.init</code> method on it, passing
     * in the FilterConfig of this PortletFilter proxy.
     * @param wac the root application context
     * @return the initialized delegate PortletFilter
     * @throws PortletException if thrown by the PortletFilter
     * @see #getTargetBeanName()
     * @see #isTargetFilterLifecycle()
     * @see #getFilterConfig()
     * @see javax.portlet.PortletFilter#init(javax.portlet.FilterConfig)
     */
    protected void initDelegate(boolean require) throws PortletException {
        final ApplicationContext wac = findWebApplicationContext();
        PortletFilter delegate = null;
        
        //Check if initialization is complete
        this.delegateReadLock.lock();
        try {
            delegate = this.delegate;
        }
        finally {
            this.delegateReadLock.unlock();
        }
        
        //Return if the delegate filter was found
        if (delegate != null) {
            return;
        }
        
        this.delegateWriteLock.lock();
        try {
            //Already initialized
            if (this.delegate != null) {
                return;
            }
            
            //Verify app context is available
            if (wac == null) {
                //If required init throw an exception for a missing app context
                if (require) {
                    throw new IllegalStateException("No ApplicationContext found: no ContextLoaderListener registered?");
                }

                //No app context and not required init, just ignore the init request
                return;
            }
    
            //Load and init the delegate filter
            delegate = wac.getBean(getTargetBeanName(), PortletFilter.class);
            if (isTargetFilterLifecycle()) {
                delegate.init(getFilterConfig());
            }
            
            //init local fields
            this.delegate = delegate;
            if (delegate instanceof ActionFilter) {
                actionDelegate = (ActionFilter)delegate;
            }
            if (delegate instanceof EventFilter) {
                eventDelegate = (EventFilter)delegate;
            }
            if (delegate instanceof RenderFilter) {
                renderDelegate = (RenderFilter)delegate;
            }
            if (delegate instanceof ResourceFilter) {
                resourceDelegate = (ResourceFilter)delegate;
            }
        }
        finally {
            this.delegateWriteLock.unlock();
        }
    }

    /**
     * Actually invoke the delegate ActionFilter with the given request and response.
     * @param delegate the delegate ActionFilter
     * @param request the current action request
     * @param response the current action response
     * @param filterChain the current FilterChain
     * @throws PortletException if thrown by the PortletFilter
     * @throws IOException if thrown by the PortletFilter
     */
    protected void invokeDelegate(
            ActionFilter delegate, ActionRequest request, ActionResponse response, FilterChain filterChain)
            throws PortletException, IOException {

        delegate.doFilter(request, response, filterChain);
    }

    /**
     * Actually invoke the delegate EventFilter with the given request and response.
     * @param delegate the delegate EventFilter
     * @param request the current Event request
     * @param response the current Event response
     * @param filterChain the current FilterChain
     * @throws PortletException if thrown by the PortletFilter
     * @throws IOException if thrown by the PortletFilter
     */
    protected void invokeDelegate(
            EventFilter delegate, EventRequest request, EventResponse response, FilterChain filterChain)
            throws PortletException, IOException {

        delegate.doFilter(request, response, filterChain);
    }

    /**
     * Actually invoke the delegate RenderFilter with the given request and response.
     * @param delegate the delegate RenderFilter
     * @param request the current Render request
     * @param response the current Render response
     * @param filterChain the current FilterChain
     * @throws PortletException if thrown by the PortletFilter
     * @throws IOException if thrown by the PortletFilter
     */
    protected void invokeDelegate(
            RenderFilter delegate, RenderRequest request, RenderResponse response, FilterChain filterChain)
            throws PortletException, IOException {

        delegate.doFilter(request, response, filterChain);
    }

    /**
     * Actually invoke the delegate ResourceFilter with the given request and response.
     * @param delegate the delegate ResourceFilter
     * @param request the current Resource request
     * @param response the current Resource response
     * @param filterChain the current FilterChain
     * @throws PortletException if thrown by the PortletFilter
     * @throws IOException if thrown by the PortletFilter
     */
    protected void invokeDelegate(
            ResourceFilter delegate, ResourceRequest request, ResourceResponse response, FilterChain filterChain)
            throws PortletException, IOException {

        delegate.doFilter(request, response, filterChain);
    }

    /**
     * Destroy the PortletFilter delegate.
     * Default implementation simply calls <code>PortletFilter.destroy</code> on it.
     * @param delegate the PortletFilter delegate (never <code>null</code>)
     * @see #isTargetFilterLifecycle()
     * @see javax.portlet.PortletFilter#destroy()
     */
    protected void destroyDelegate(PortletFilter delegate) {
        if (isTargetFilterLifecycle()) {
            delegate.destroy();
        }
    }
}