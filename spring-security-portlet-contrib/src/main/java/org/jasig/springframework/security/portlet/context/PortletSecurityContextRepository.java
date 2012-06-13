package org.jasig.springframework.security.portlet.context;

import javax.portlet.PortletRequest;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SaveContextOnUpdateOrErrorResponseWrapper;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

/**
 * Strategy used for persisting a {@link SecurityContext} between requests.
 * <p>
 * Used by {@link SecurityContextPersistenceFilter} to obtain the context which should be used for the current thread
 * of execution and to store the context once it has been removed from thread-local storage and the request has
 * completed.
 * <p>
 * The persistence mechanism used will depend on the implementation, but most commonly the <tt>HttpSession</tt> will
 * be used to store the context.
 *
 * @author Eric Dalquist
 * @since 3.0
 *
 * @see PortletSecurityContextPersistenceFilter
 * @see HttpSessionSecurityContextRepository
 * @see SaveContextOnUpdateOrErrorResponseWrapper TODO
 */
public interface PortletSecurityContextRepository {

    /**
     * Obtains the security context for the supplied request. For an unauthenticated user, an empty context
     * implementation should be returned. This method should not return null.
     * <p>
     * The use of the <tt>PortletRequestResponseHolder</tt> parameter allows implementations to return wrapped versions of
     * the request or response (or both), allowing them to access implementation-specific state for the request.
     * The values obtained from the holder will be passed on to the filter chain and also to the <tt>saveContext</tt>
     * method when it is finally called. Implementations may wish to return a subclass of
     * {@link SaveContextOnUpdateOrErrorResponseWrapper} as the response object, which guarantees that the context is
     * persisted when an error or redirect occurs.
     *
     * @param requestResponseHolder holder for the current request and response for which the context should be loaded.
     *
     * @return The security context which should be used for the current request, never null.
     */
    SecurityContext loadContext(PortletRequestResponseHolder requestResponseHolder);

    /**
     * Stores the security context on completion of a request.
     *
     * @param context the non-null context which was obtained from the holder.
     * @param requestResponseHolder holder for the current request and response for which the context should be loaded.
     */
    void saveContext(SecurityContext context, PortletRequestResponseHolder requestResponseHolder);

    /**
     * Allows the repository to be queried as to whether it contains a security context for the
     * current request.
     *
     * @param request the current request
     * @return true if a context is found for the request, false otherwise
     */
    boolean containsContext(PortletRequest request);
}
