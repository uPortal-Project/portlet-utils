package org.jasig.springframework.security.portlet.context;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.FilterChain;

import org.junit.After;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.portlet.MockRenderRequest;
import org.springframework.mock.web.portlet.MockRenderResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

public class PortletSecurityContextPersistenceFilterTests {
    TestingAuthenticationToken testToken = new TestingAuthenticationToken("someone", "passwd", "ROLE_A");

    @After
    public void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void contextIsClearedAfterChainProceeds() throws Exception {
        final FilterChain chain = mock(FilterChain.class);
        final MockRenderRequest request = new MockRenderRequest();
        final MockRenderResponse response = new MockRenderResponse();
        PortletSecurityContextPersistenceFilter filter = new PortletSecurityContextPersistenceFilter();
        SecurityContextHolder.getContext().setAuthentication(testToken);

        filter.doFilter(request, response, chain);
        verify(chain).doFilter(any(RenderRequest.class), any(RenderResponse.class));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void contextIsStillClearedIfExceptionIsThrowByFilterChain() throws Exception {
        final FilterChain chain = mock(FilterChain.class);
        final MockRenderRequest request = new MockRenderRequest();
        final MockRenderResponse response = new MockRenderResponse();
        PortletSecurityContextPersistenceFilter filter = new PortletSecurityContextPersistenceFilter();
        SecurityContextHolder.getContext().setAuthentication(testToken);
        doThrow(new IOException()).when(chain).doFilter(any(RenderRequest.class), any(RenderResponse.class));
        try {
            filter.doFilter(request, response, chain);
            fail();
        } catch(IOException expected) {
        }

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void loadedContextContextIsCopiedToSecurityContextHolderAndUpdatedContextIsStored() throws Exception {
        final MockRenderRequest request = new MockRenderRequest();
        final MockRenderResponse response = new MockRenderResponse();
        final PortletSecurityContextRepository repo = mock(PortletSecurityContextRepository.class);
        PortletSecurityContextPersistenceFilter filter = new PortletSecurityContextPersistenceFilter(repo);
        final TestingAuthenticationToken beforeAuth = new TestingAuthenticationToken("someoneelse", "passwd", "ROLE_B");
        final SecurityContext scBefore = new SecurityContextImpl();
        final SecurityContext scExpectedAfter = new SecurityContextImpl();
        scExpectedAfter.setAuthentication(testToken);
        scBefore.setAuthentication(beforeAuth);
        when(repo.loadContext(any(PortletRequestResponseHolder.class))).thenReturn(scBefore);

        final FilterChain chain = mock(FilterChain.class);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                assertEquals(beforeAuth, SecurityContextHolder.getContext().getAuthentication());
                // Change the context here
                SecurityContextHolder.setContext(scExpectedAfter);
                return null;
            }
        }).when(chain).doFilter(any(RenderRequest.class), any(RenderResponse.class));

        filter.doFilter(request, response, chain);

        verify(repo).saveContext(eq(scExpectedAfter), any(PortletRequestResponseHolder.class));
    }

    @Test
    public void filterIsNotAppliedAgainIfFilterAppliedAttributeIsSet() throws Exception {
        final FilterChain chain = mock(FilterChain.class);
        final MockRenderRequest request = new MockRenderRequest();
        final MockRenderResponse response = new MockRenderResponse();
        final PortletSecurityContextRepository repo = mock(PortletSecurityContextRepository.class);
        PortletSecurityContextPersistenceFilter filter = new PortletSecurityContextPersistenceFilter(repo);

        request.setAttribute(PortletSecurityContextPersistenceFilter.FILTER_APPLIED, Boolean.TRUE);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    public void sessionIsEagerlyCreatedWhenConfigured() throws Exception {
        final FilterChain chain = mock(FilterChain.class);
        final MockRenderRequest request = new MockRenderRequest();
        final MockRenderResponse response = new MockRenderResponse();
        PortletSecurityContextPersistenceFilter filter = new PortletSecurityContextPersistenceFilter();
        filter.setForceEagerSessionCreation(true);
        filter.doFilter(request, response, chain);
        assertNotNull(request.getPortletSession(false));
    }

    @Test
    public void nullSecurityContextRepoDoesntSaveContextOrCreateSession() throws Exception {
        final FilterChain chain = mock(FilterChain.class);
        final MockRenderRequest request = new MockRenderRequest();
        final MockRenderResponse response = new MockRenderResponse();
        PortletSecurityContextRepository repo = new NullPortletSecurityContextRepository();
        PortletSecurityContextPersistenceFilter filter = new PortletSecurityContextPersistenceFilter(repo);
        filter.doFilter(request, response, chain);
        assertFalse(repo.containsContext(request));
        assertNull(request.getPortletSession(false));
    }
}
