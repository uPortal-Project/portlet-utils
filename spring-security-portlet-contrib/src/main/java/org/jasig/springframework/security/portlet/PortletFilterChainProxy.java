package org.jasig.springframework.security.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.PortletFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.springframework.security.portlet.util.RequestMatcher;
import org.jasig.springframework.web.portlet.filter.DelegatingPortletFilterProxy;
import org.jasig.springframework.web.portlet.filter.GenericPortletFilterBean;
import org.jasig.springframework.web.portlet.filter.PortletFilterUtils;

/**
 * Delegates {@code PortletFilter} requests to a list of Spring-managed filter beans.
 * As of version 2.0, you shouldn't need to explicitly configure a {@code PortletFilterChainProxy} bean in your application
 * context unless you need very fine control over the filter chain contents. Most cases should be adequately covered
 * by the default {@code &lt;security:http /&gt;} namespace configuration options.
 * <p>
 * The {@code PortletFilterChainProxy} is linked into the portlet container filter chain by adding a standard
 * Spring {@link DelegatingPortletFilterProxy} declaration in the application {@code portlet.xml} file.
 *
 * <h2>Configuration</h2>
 * <p>
 * As of version 3.1, {@code PortletFilterChainProxy} is configured using a list of {@link PortletSecurityFilterChain} instances,
 * each of which contains a {@link RequestMatcher} and a list of filters which should be applied to matching requests.
 * Most applications will only contain a single filter chain, and if you are using the namespace, you don't have to
 * set the chains explicitly. If you require finer-grained control, you can make use of the {@code &lt;filter-chain&gt;}
 * namespace element. This defines a URI pattern and the list of filters (as comma-separated bean names) which should be
 * applied to requests which match the pattern. An example configuration might look like this:
 *
 * <pre>
 &lt;bean id="myfilterChainProxy" class="org.jasig.springframework.security.portlet.PortletFilterChainProxy">
     &lt;constructor-arg>
         &lt;util:list>
             &lt;security:filter-chain pattern="/do/not/filter*" filters="none"/>
             &lt;security:filter-chain pattern="/**" filters="filter1,filter2,filter3"/>
         &lt;/util:list>
     &lt;/constructor-arg>
 &lt;/bean>
 * </pre>
 *
 * The names "filter1", "filter2", "filter3" should be the bean names of {@code PortletFilter} instances defined in the
 * application context. The order of the names defines the order in which the filters will be applied. As shown above,
 * use of the value "none" for the "filters" can be used to exclude a request pattern from the security filter chain
 * entirely. Please consult the security namespace schema file for a full list of available configuration options.
 *
 * <h2>Request Handling</h2>
 * <p>
 * Each possible pattern that the {@code PortletFilterChainProxy} should service must be entered.
 * The first match for a given request will be used to define all of the {@code PortletFilter}s that apply to that
 * request. This means you must put most specific matches at the top of the list, and ensure all {@code PortletFilter}s
 * that should apply for a given matcher are entered against the respective entry.
 * The {@code PortletFilterChainProxy} will not iterate through the remainder of the map entries to locate additional
 * {@code PortletFilter}s.
 * <p>
 * {@code PortletFilterChainProxy} respects normal handling of {@code PortletFilter}s that elect not to call {@link
 * javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
 * javax.servlet.FilterChain)}, in that the remainder of the original or {@code PortletFilterChainProxy}-declared filter
 * chain will not be called.
 *
 * <h2>Filter Lifecycle</h2>
 * <p>
 * Note the {@code PortletFilter} lifecycle mismatch between the portlet container and IoC
 * container. As described in the {@link DelegatingPortletFilterProxy} Javadocs, we recommend you allow the IoC
 * container to manage the lifecycle instead of the servlet container. {@code PortletFilterChainProxy} does not invoke the
 * standard filter lifecycle methods on any filter beans that you add to the application context.
 *
 * @author Carlos Sanchez
 * @author Ben Alex
 * @author Luke Taylor
 * @author Rob Winch
 * @author Eric Dalquist
 */
public class PortletFilterChainProxy extends GenericPortletFilterBean {
    //~ Static fields/initializers =====================================================================================

    private static final Log logger = LogFactory.getLog(PortletFilterChainProxy.class);

    //~ Instance fields ================================================================================================

    private List<PortletSecurityFilterChain> filterChains;

    private FilterChainValidator filterChainValidator = new NullFilterChainValidator();

    //~ Methods ========================================================================================================

    public PortletFilterChainProxy() {
    }

    public PortletFilterChainProxy(PortletSecurityFilterChain chain) {
        this(Arrays.asList(chain));
    }

    public PortletFilterChainProxy(List<PortletSecurityFilterChain> filterChains) {
        this.filterChains = filterChains;
    }

    @Override
    public void afterPropertiesSet() {
        filterChainValidator.validate(this);
    }

    @Override
    protected void doCommonFilter(PortletRequest request, PortletResponse response, FilterChain chain)
            throws IOException, PortletException {

        List<PortletFilter> filters = getFilters(request);

        if (filters == null || filters.size() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug(request.getParameterMap() +
                        (filters == null ? " has no matching filters" : " has an empty filter list"));
            }

            PortletFilterUtils.doFilter(request, response, chain);

            return;
        }

        VirtualFilterChain vfc = new VirtualFilterChain(request, chain, filters);
        vfc.doCommonFilter(request, response);
    }

    /**
     * Returns the first filter chain matching the supplied URL.
     *
     * @param request the request to match
     * @return an ordered array of Filters defining the filter chain
     */
    private List<PortletFilter> getFilters(PortletRequest request)  {
        for (PortletSecurityFilterChain chain : filterChains) {
            if (chain.matches(request)) {
                return chain.getFilters();
            }
        }

        return null;
    }

    /**
     * Sets the mapping of URL patterns to filter chains.
     *
     * The map keys should be the paths and the values should be arrays of {@code PortletFilter} objects.
     * It's VERY important that the type of map used preserves ordering - the order in which the iterator
     * returns the entries must be the same as the order they were added to the map, otherwise you have no way
     * of guaranteeing that the most specific patterns are returned before the more general ones. So make sure
     * the Map used is an instance of {@code LinkedHashMap} or an equivalent, rather than a plain {@code HashMap}, for
     * example.
     *
     * @param filterChainMap the map of path Strings to {@code List&lt;PortletFilter&gt;}s.
     * @deprecated Use the constructor which takes a {@code List&lt;PortletSecurityFilterChain&gt;} instead.
     */
    @Deprecated
    public void setFilterChainMap(Map<RequestMatcher, List<PortletFilter>> filterChainMap) {
        filterChains = new ArrayList<PortletSecurityFilterChain>(filterChainMap.size());

        for (Map.Entry<RequestMatcher,List<PortletFilter>> entry : filterChainMap.entrySet()) {
            filterChains.add(new DefaultPortletSecurityFilterChain(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Returns a copy of the underlying filter chain map. Modifications to the map contents
     * will not affect the PortletFilterChainProxy state.
     *
     * @return the map of path pattern Strings to filter chain lists (with ordering guaranteed).
     *
     * @deprecated use the list of {@link PortletSecurityFilterChain}s instead
     */
    @Deprecated
    public Map<RequestMatcher, List<PortletFilter>> getFilterChainMap() {
        LinkedHashMap<RequestMatcher, List<PortletFilter>> map =  new LinkedHashMap<RequestMatcher, List<PortletFilter>>();

        for (PortletSecurityFilterChain chain : filterChains) {
            map.put(((DefaultPortletSecurityFilterChain)chain).getRequestMatcher(), chain.getFilters());
        }

        return map;
    }

    /**
     * @return the list of {@code PortletSecurityFilterChain}s which will be matched against and
     *         applied to incoming requests.
     */
    public List<PortletSecurityFilterChain> getFilterChains() {
        return Collections.unmodifiableList(filterChains);
    }

    /**
     * Used (internally) to specify a validation strategy for the filters in each configured chain.
     *
     * @param filterChainValidator the validator instance which will be invoked on during initialization
     * to check the {@code PortletFilterChainProxy} instance.
     */
    public void setFilterChainValidator(FilterChainValidator filterChainValidator) {
        this.filterChainValidator = filterChainValidator;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PortletFilterChainProxy[");
        sb.append("PortletFilter Chains: ");
        sb.append(filterChains);
        sb.append("]");

        return sb.toString();
    }

    //~ Inner Classes ==================================================================================================

    /**
     * Internal {@code FilterChain} implementation that is used to pass a request through the additional
     * internal list of filters which match the request.
     */
    private static class VirtualFilterChain implements FilterChain {
        private final FilterChain originalChain;
        private final List<PortletFilter> additionalFilters;
        private final PortletRequest portletRequest;
        private final int size;
        private int currentPosition = 0;

        private VirtualFilterChain(PortletRequest portletRequest, FilterChain chain, List<PortletFilter> additionalFilters) {
            this.originalChain = chain;
            this.additionalFilters = additionalFilters;
            this.size = additionalFilters.size();
            this.portletRequest = portletRequest;
        }
        
        @Override
        public void doFilter(ActionRequest request, ActionResponse response) throws IOException, PortletException {
            doCommonFilter(request, response);
        }

        @Override
        public void doFilter(EventRequest request, EventResponse response) throws IOException, PortletException {
            doCommonFilter(request, response); 
        }

        @Override
        public void doFilter(RenderRequest request, RenderResponse response) throws IOException, PortletException {
            doCommonFilter(request, response);
        }

        @Override
        public void doFilter(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
            doCommonFilter(request, response);
        }

        public void doCommonFilter(PortletRequest request, PortletResponse response) throws IOException, PortletException {
            if (currentPosition == size) {
                if (logger.isDebugEnabled()) {
                    logger.debug(portletRequest
                            + " reached end of additional filter chain; proceeding with original chain");
                }

                // Deactivate path stripping as we exit the security filter chain
                PortletFilterUtils.doFilter(request, response, originalChain);
            } else {
                currentPosition++;

                PortletFilter nextFilter = additionalFilters.get(currentPosition - 1);

                if (logger.isDebugEnabled()) {
                    logger.debug(portletRequest + " at position " + currentPosition + " of "
                        + size + " in additional filter chain; firing PortletFilter: '"
                        + nextFilter.getClass().getSimpleName() + "'");
                }

                PortletFilterUtils.doFilter(nextFilter, request, response, this);
            }
        }
    }

    public interface FilterChainValidator {
        void validate(PortletFilterChainProxy filterChainProxy);
    }

    private class NullFilterChainValidator implements FilterChainValidator {
        public void validate(PortletFilterChainProxy filterChainProxy) {
        }
    }

}
