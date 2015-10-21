package org.jasig.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * Default behavior for this filter is to allow origins from * (ie anywhere). It is not reccomended to use this default 
 * behavior except for development testing. When configuring the filter, the filter configuration parameters will 
 * overwrite any value set in a spring or other configuration file doing dependency injection and initialization.  
 * 
 * Config within web.xml:
 * 
 * <filter>
 *   <filter-name>Simple CORS Filter</filter-name>
 *   <filter-class>org.jasig.web.filter.SimpleCorsFilter</filter-class>
 *   <init-param>
 *     <!-- Comma separated domain names -->
 *     <param-name>allowOrigin</param-name>
 *     <param-value>someDomain.org, another.com, jasig.org, apereo.org</param-value>
 *     
 *     <param-name>maxAge</param-name>
 *     <param-value>3600</param-value>
 *     
 *     <!-- Comma separated methods allowed -->
 *     <param-name>allowMethod</param-name>
 *     <param-value>POST, GET</param-value>
 *     
 *     <!--Comma separated list of allowed header values -->
 *     <param-name>allowHeaders</param-name>
 *     <param-value>Origin, X-Requested-With, Content-Type, Accept</param-value>
 *   </init-param>    
 * </filter>
 * 
 * @author chasegawa@unicon.net
 */
public class SimpleCorsFilter implements Filter {
    private String allowHeaders = "Origin, X-Requested-With, Content-Type, Accept";
    private String allowMethod = "POST, GET, PUT, OPTIONS, DELETE";
    private String allowOrigin = "*";
    private String maxAge = "3600";

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

    /**
     * Sets the headers to support CORS 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Origin", allowOrigin);
        response.setHeader("Access-Control-Allow-Methods", allowMethod);
        response.setHeader("Access-Control-Max-Age", maxAge);
        response.setHeader("Access-Control-Allow-Headers", allowHeaders);
        chain.doFilter(req, res);
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) {
        String value = filterConfig.getInitParameter("allowOrigin");
        if (!StringUtils.isEmpty(value)) {
            this.allowOrigin = value;
        }
        
        value = filterConfig.getInitParameter("allowMethod");
        if (!StringUtils.isEmpty(value)) {
            this.allowMethod = value;
        }
        
        value = filterConfig.getInitParameter("maxAge");
        if (!StringUtils.isEmpty(value)) {
            this.maxAge = value;
        }
        
        value = filterConfig.getInitParameter("allowHeaders");
        if (!StringUtils.isEmpty(value)) {
            this.allowHeaders = value;
        }
    }

    /** 
     * Defaults to "Origin, X-Requested-With, Content-Type, Accept"
     * This value can be overwritten by any value configured in filter config parameters of web.xml
     * @param allowHeaders
     */
    public void setAllowHeaders(String allowHeaders) {
        this.allowHeaders = allowHeaders;
    }

    /**
     * Defaults to "POST, GET, PUT, OPTIONS, DELETE"
     * This value can be overwritten by any value configured in filter config parameters of web.xml
     * @param allowMethod
     */
    public void setAllowMethod(String allowMethod) {
        this.allowMethod = allowMethod;
    }

    /**
     * Defaults to *
     * This value can be overwritten by any value configured in filter config parameters of web.xml
     * @param allowOrigin comma separated list of domains to use in setting "Access-Control-Allow-Origin"
     */
    public void setAllowOrigin(String allowOrigin) {
        this.allowOrigin = allowOrigin;
    }

    /**
     * Defaults to 3600
     * This value can be overwritten by any value configured in filter config parameters of web.xml
     * @param maxAge max time in seconds that a preflight request can be in cache 
     */
    public void setMaxAge(String maxAge) {
        this.maxAge = maxAge;
    }
}
