package org.jasig.springframework.security.portlet.authentication;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.portlet.PortletRequest;
import javax.portlet.filter.FilterChain;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.portlet.MockRenderRequest;
import org.springframework.mock.web.portlet.MockRenderResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.ImmutableMap;

public class PortletAuthenticationProcessingFilterTest {

    @After
    @Before
    public void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void defaultsToUsingRemoteUser() throws Exception {
        MockRenderRequest request = new MockRenderRequest();
        request.setRemoteUser("cat");
        MockRenderResponse response = new MockRenderResponse();
        FilterChain chain = mock(FilterChain.class);
        PortletAuthenticationProcessingFilter filter = new PortletAuthenticationProcessingFilter();
        filter.setAuthenticationManager(createAuthenticationManager());

        filter.doFilter(request, response, chain);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("cat", SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals("N/A", SecurityContextHolder.getContext().getAuthentication().getCredentials());
    }

    @Test
    public void userInfoUserNameIsSupported() throws Exception {
        MockRenderRequest request = new MockRenderRequest();
        request.setAttribute(PortletRequest.USER_INFO, ImmutableMap.of("myUsernameHeader", "wolfman"));
        MockRenderResponse response = new MockRenderResponse();
        FilterChain chain = mock(FilterChain.class);
        PortletAuthenticationProcessingFilter filter = new PortletAuthenticationProcessingFilter();
        filter.setAuthenticationManager(createAuthenticationManager());
        filter.setUserNameAttributes(Arrays.asList("myUsernameHeader"));

        filter.doFilter(request, response, chain);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("wolfman", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    public void credentialsAreNotRetrievedIfHeaderNameIsSet() throws Exception {
        MockRenderRequest request = new MockRenderRequest();
        MockRenderResponse response = new MockRenderResponse();
        FilterChain chain = mock(FilterChain.class);
        PortletAuthenticationProcessingFilter filter = new PortletAuthenticationProcessingFilter();
        filter.setAuthenticationManager(createAuthenticationManager());
        filter.setUserNameAttributes(Arrays.asList("myCredentialsHeader"));
        request.setRemoteUser("cat");
        request.setAttribute(PortletRequest.USER_INFO, ImmutableMap.of("myCredentialsHeader", "catspassword"));

        filter.doFilter(request, response, chain);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("N/A", SecurityContextHolder.getContext().getAuthentication().getCredentials());
    }

    @Test
    public void userIsReauthenticatedIfPrincipalChangesAndCheckForPrincipalChangesIsSet() throws Exception {
        MockRenderRequest request = new MockRenderRequest();
        MockRenderResponse response = new MockRenderResponse();
        FilterChain chain = mock(FilterChain.class);
        PortletAuthenticationProcessingFilter filter = new PortletAuthenticationProcessingFilter();
        filter.setAuthenticationManager(createAuthenticationManager());
        filter.setCheckForPrincipalChanges(true);
        request.setRemoteUser("cat");
        filter.doFilter(request, response, chain);
        request = new MockRenderRequest();
        request.setRemoteUser("dog");
        filter.doFilter(request, response, chain);
        Authentication dog = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(dog);
        assertEquals("dog", dog.getName());
        // Make sure authentication doesn't occur every time (i.e. if the header *doesn't change)
        filter.setAuthenticationManager(mock(AuthenticationManager.class));
        filter.doFilter(request, response, chain);
        assertSame(dog, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void anonymousUserSupport() throws Exception {
        MockRenderRequest request = new MockRenderRequest();
        MockRenderResponse response = new MockRenderResponse();
        FilterChain chain = mock(FilterChain.class);
        PortletAuthenticationProcessingFilter filter = new PortletAuthenticationProcessingFilter();
        filter.setAuthenticationManager(createAuthenticationManager());
        filter.doFilter(request, response, chain);
    }

    /**
     * Create an authentication manager which returns the passed in object.
     */
    private AuthenticationManager createAuthenticationManager() {
        AuthenticationManager am = mock(AuthenticationManager.class);
        when(am.authenticate(any(Authentication.class))).thenAnswer(new Answer<Authentication>() {
            public Authentication answer(InvocationOnMock invocation) throws Throwable {
                return (Authentication) invocation.getArguments()[0];
            }
        });

        return am;
    }
}
