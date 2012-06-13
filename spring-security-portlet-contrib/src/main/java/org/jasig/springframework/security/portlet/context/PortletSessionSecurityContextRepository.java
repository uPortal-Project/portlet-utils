package org.jasig.springframework.security.portlet.context;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.util.Assert;

/**
 * A {@code SecurityContextRepository} implementation which stores the security context in the {@code PortletSession}
 * between requests.
 * <p>
 * The {@code PortletSession} will be queried to retrieve the {@code SecurityContext} in the <tt>loadContext</tt>
 * method (using the key {@link #SPRING_SECURITY_CONTEXT_KEY} by default). If a valid {@code SecurityContext} cannot be
 * obtained from the {@code PortletSession} for whatever reason, a fresh {@code SecurityContext} will be created
 * by calling by {@link SecurityContextHolder#createEmptyContext()} and this instance will be returned instead.
 * <p>
 * When <tt>saveContext</tt> is called, the context will be stored under the same key, provided
 * <ol>
 * <li>The value has changed</li>
 * <li>The configured <tt>AuthenticationTrustResolver</tt> does not report that the contents represent an anonymous
 * user</li>
 * </ol>
 * <p>
 * With the standard configuration, no {@code PortletSession} will be created during <tt>loadContext</tt> if one does
 * not already exist. When <tt>saveContext</tt> is called at the end of the web request, and no session exists, a new
 * {@code PortletSession} will <b>only</b> be created if the supplied {@code SecurityContext} is not equal
 * to an empty {@code SecurityContext} instance. This avoids needless <code>PortletSession</code> creation,
 * but automates the storage of changes made to the context during the request. Note that if
 * {@link SecurityContextPersistenceFilter} is configured to eagerly create sessions, then the session-minimisation
 * logic applied here will not make any difference. If you are using eager session creation, then you should
 * ensure that the <tt>allowSessionCreation</tt> property of this class is set to <tt>true</tt> (the default).
 * <p>
 * If for whatever reason no {@code PortletSession} should <b>ever</b> be created (for example, if
 * Basic authentication is being used or similar clients that will never present the same {@code jsessionid}), then
 * {@link #setAllowSessionCreation(boolean) allowSessionCreation} should be set to <code>false</code>.
 * Only do this if you really need to conserve server memory and ensure all classes using the
 * {@code SecurityContextHolder} are designed to have no persistence of the {@code SecurityContext}
 * between web requests.
 *
 * @author Eric Dalquist
 * @since 3.0
 */
public class PortletSessionSecurityContextRepository implements PortletSecurityContextRepository {
    /**
     * The default key under which the security context will be stored in the session.
     */
    public static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";

    protected final Log logger = LogFactory.getLog(this.getClass());

    /** SecurityContext instance used to check for equality with default (unauthenticated) content */
    private final Object contextObject = SecurityContextHolder.createEmptyContext();
    private boolean allowSessionCreation = true;
    private String springSecurityContextKey = SPRING_SECURITY_CONTEXT_KEY;

    private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();
    private final int sessionScope;
    
    public PortletSessionSecurityContextRepository() {
        this.sessionScope = PortletSession.PORTLET_SCOPE;
    }

    /**
     * Set the PortletSession scope under which to store the security context
     */
    public PortletSessionSecurityContextRepository(int sessionScope) {
        this.sessionScope = sessionScope;
    }

    /**
     * Gets the security context for the current request (if available) and returns it.
     * <p>
     * If the session is null, the context object is null or the context object stored in the session
     * is not an instance of {@code SecurityContext}, a new context object will be generated and
     * returned.
     */
    @Override
    public SecurityContext loadContext(PortletRequestResponseHolder requestResponseHolder) {
        final PortletRequest request = requestResponseHolder.getRequest();
        final PortletSession portletSession = request.getPortletSession(false);
        
        SecurityContext context = readSecurityContextFromSession(portletSession);

        if (context == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No SecurityContext was available from the PortletSession: " + portletSession +". " +
                        "A new one will be created.");
            }
            context = generateNewContext();

        }

        //Capture pre-request state
        requestResponseHolder.setAuthBeforeExecution(context.getAuthentication());
        requestResponseHolder.setContextBeforeExecution(context);
        requestResponseHolder.setPortletSessionExistedAtStartOfRequest(portletSession != null);

        return context;
    }

    @Override
    public void saveContext(SecurityContext context, PortletRequestResponseHolder requestResponseHolder) {
        final Authentication authentication = context.getAuthentication();
        final PortletRequest request = requestResponseHolder.getRequest();
        PortletSession portletSession = request.getPortletSession(false);

        // See SEC-776
        if (authentication == null || authenticationTrustResolver.isAnonymous(authentication)) {
            if (logger.isDebugEnabled()) {
                logger.debug("SecurityContext is empty or contents are anonymous - context will not be stored in PortletSession.");
            }

            if (portletSession != null) {
                // SEC-1587 A non-anonymous context may still be in the session
                portletSession.removeAttribute(springSecurityContextKey, this.sessionScope);
            }
            return;
        }

        if (portletSession == null) {
            portletSession = createNewSessionIfAllowed(context, requestResponseHolder);
        }

        // If PortletSession exists, store current SecurityContext but only if it has
        // actually changed in this thread (see SEC-37, SEC-1307, SEC-1528)
        if (portletSession != null) {
            // We may have a new session, so check also whether the context attribute is set SEC-1561
            if (contextChanged(context, requestResponseHolder) || portletSession.getAttribute(springSecurityContextKey, this.sessionScope) == null) {
                portletSession.setAttribute(springSecurityContextKey, context, this.sessionScope);

                if (logger.isDebugEnabled()) {
                    logger.debug("SecurityContext stored to PortletSession: '" + context + "'");
                }
            }
        }
    }

    private boolean contextChanged(SecurityContext context, PortletRequestResponseHolder requestResponseHolder) {
        return context != requestResponseHolder.getContextBeforeExecution() || context.getAuthentication() != requestResponseHolder.getAuthBeforeExecution();
    }

    private PortletSession createNewSessionIfAllowed(SecurityContext context, PortletRequestResponseHolder requestResponseHolder) {
        if (requestResponseHolder.isPortletSessionExistedAtStartOfRequest()) {
            if (logger.isDebugEnabled()) {
                logger.debug("PortletSession is now null, but was not null at start of request; "
                        + "session was invalidated, so do not create a new session");
            }

            return null;
        }

        if (!allowSessionCreation) {
            if (logger.isDebugEnabled()) {
                logger.debug("The PortletSession is currently null, and the "
                                + PortletSessionSecurityContextRepository.class.getSimpleName()
                                + " is prohibited from creating an PortletSession "
                                + "(because the allowSessionCreation property is false) - SecurityContext thus not "
                                + "stored for next request");
            }

            return null;
        }
        // Generate a PortletSession only if we need to

        if (contextObject.equals(context)) {
            if (logger.isDebugEnabled()) {
                logger.debug("PortletSession is null, but SecurityContext has not changed from default empty context: ' "
                        + context
                        + "'; not creating PortletSession or storing SecurityContext");
            }

            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("PortletSession being created as SecurityContext is non-default");
        }

        try {
            final PortletRequest request = requestResponseHolder.getRequest();
            return request.getPortletSession(true);
        } catch (IllegalStateException e) {
            // Response must already be committed, therefore can't create a new session
            logger.warn("Failed to create a session, as response has been committed. Unable to store" +
                    " SecurityContext.");
        }

        return null;
    }

    @Override
    public boolean containsContext(PortletRequest request) {
        final PortletSession portletSession = request.getPortletSession(false);

        if (portletSession == null) {
            return false;
        }

        return portletSession.getAttribute(springSecurityContextKey, this.sessionScope) != null;
    }

    /**
     *
     * @param portletSession the session obtained from the request.
     */
    private SecurityContext readSecurityContextFromSession(PortletSession portletSession) {
        final boolean debug = logger.isDebugEnabled();

        if (portletSession == null) {
            if (debug) {
                logger.debug("No PortletSession currently exists");
            }

            return null;
        }

        // Session exists, so try to obtain a context from it.

        Object contextFromSession = portletSession.getAttribute(springSecurityContextKey, this.sessionScope);

        if (contextFromSession == null) {
            if (debug) {
                logger.debug("PortletSession returned null object for SPRING_SECURITY_CONTEXT");
            }

            return null;
        }

        // We now have the security context object from the session.
        if (!(contextFromSession instanceof SecurityContext)) {
            if (logger.isWarnEnabled()) {
                logger.warn(springSecurityContextKey + " did not contain a SecurityContext but contained: '"
                        + contextFromSession + "'; are you improperly modifying the PortletSession directly "
                        + "(you should always use SecurityContextHolder) or using the PortletSession attribute "
                        + "reserved for this class?");
            }

            return null;
        }

        if (debug) {
            logger.debug("Obtained a valid SecurityContext from " + springSecurityContextKey + ": '" + contextFromSession + "'");
        }

        // Everything OK. The only non-null return from this method.

        return (SecurityContext) contextFromSession;
    }

    /**
     * By default, calls {@link SecurityContextHolder#createEmptyContext()} to obtain a new context (there should be
     * no context present in the holder when this method is called). Using this approach the context creation
     * strategy is decided by the {@link SecurityContextHolderStrategy} in use. The default implementations
     * will return a new <tt>SecurityContextImpl</tt>.
     *
     * @return a new SecurityContext instance. Never null.
     */
    protected SecurityContext generateNewContext() {
        return SecurityContextHolder.createEmptyContext();
    }

    /**
     * If set to true (the default), a session will be created (if required) to store the security context if it is
     * determined that its contents are different from the default empty context value.
     * <p>
     * Note that setting this flag to false does not prevent this class from storing the security context. If your
     * application (or another filter) creates a session, then the security context will still be stored for an
     * authenticated user.
     *
     * @param allowSessionCreation
     */
    public void setAllowSessionCreation(boolean allowSessionCreation) {
        this.allowSessionCreation = allowSessionCreation;
    }

    /**
     * Allows the session attribute name to be customized for this repository instance.
     *
     * @param springSecurityContextKey the key under which the security context will be stored. Defaults to
     * {@link #SPRING_SECURITY_CONTEXT_KEY}.
     */
    public void setSpringSecurityContextKey(String springSecurityContextKey) {
        Assert.hasText(springSecurityContextKey, "springSecurityContextKey cannot be empty");
        this.springSecurityContextKey = springSecurityContextKey;
    }
}
