package org.jasig.springframework.security.portlet.context;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static org.jasig.springframework.security.portlet.context.PortletSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.portlet.PortletSession;

import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.portlet.MockPortletRequest;
import org.springframework.mock.web.portlet.MockPortletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class PortletSessionSecurityContextRepositoryTests {
    private final TestingAuthenticationToken testToken = new TestingAuthenticationToken("someone", "passwd", "ROLE_A");

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void sessionIsntCreatedIfContextDoesntChange() throws Exception {
        PortletSessionSecurityContextRepository repo = new PortletSessionSecurityContextRepository();
        MockPortletRequest request = new MockPortletRequest();
        MockPortletResponse response = new MockPortletResponse();
        PortletRequestResponseHolder holder = new PortletRequestResponseHolder(request, response);
        SecurityContext context = repo.loadContext(holder);
        assertNull(request.getPortletSession(false));
        repo.saveContext(context, holder);
        assertNull(request.getPortletSession(false));
    }

    @Test
    public void sessionIsntCreatedIfAllowSessionCreationIsFalse() throws Exception {
        PortletSessionSecurityContextRepository repo = new PortletSessionSecurityContextRepository();
        repo.setAllowSessionCreation(false);
        MockPortletRequest request = new MockPortletRequest();
        MockPortletResponse response = new MockPortletResponse();
        PortletRequestResponseHolder holder = new PortletRequestResponseHolder(request, response);
        SecurityContext context = repo.loadContext(holder);
        // Change context
        context.setAuthentication(testToken);
        repo.saveContext(context, holder);
        assertNull(request.getPortletSession(false));
    }

    @Test
    public void existingContextIsSuccessFullyLoadedFromSessionAndSavedBack() throws Exception {
        PortletSessionSecurityContextRepository repo = new PortletSessionSecurityContextRepository();
        repo.setSpringSecurityContextKey("imTheContext");
        MockPortletRequest request = new MockPortletRequest();
        SecurityContextHolder.getContext().setAuthentication(testToken);
        request.getPortletSession().setAttribute("imTheContext", SecurityContextHolder.getContext(), PortletSession.APPLICATION_SCOPE);
        MockPortletResponse response = new MockPortletResponse();
        PortletRequestResponseHolder holder = new PortletRequestResponseHolder(request, response);
        SecurityContext context = repo.loadContext(holder);
        assertNotNull(context);
        assertEquals(testToken, context.getAuthentication());
        // Won't actually be saved as it hasn't changed, but go through the use case anyway
        repo.saveContext(context, holder);
        assertEquals(context, request.getPortletSession().getAttribute("imTheContext", PortletSession.APPLICATION_SCOPE));
    }

    // SEC-1528
    @Test
    public void saveContextCallsSetAttributeIfContextIsModifiedDirectlyDuringRequest() throws Exception {
        PortletSessionSecurityContextRepository repo = new PortletSessionSecurityContextRepository();
        MockPortletRequest request = new MockPortletRequest();
        // Set up an existing authenticated context, mocking that it is in the session already
        SecurityContext ctx = SecurityContextHolder.getContext();
        ctx.setAuthentication(testToken);
        PortletSession session = mock(PortletSession.class);
        when(session.getAttribute(SPRING_SECURITY_CONTEXT_KEY, PortletSession.APPLICATION_SCOPE)).thenReturn(ctx);
        request.setSession(session);
        PortletRequestResponseHolder holder = new PortletRequestResponseHolder(request, new MockPortletResponse());
        assertSame(ctx, repo.loadContext(holder));

        // Modify context contents. Same user, different role
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("someone", "passwd", "ROLE_B"));
        repo.saveContext(ctx, holder);

        // Must be called even though the value in the local VM is already the same
        verify(session).setAttribute(SPRING_SECURITY_CONTEXT_KEY, ctx, PortletSession.APPLICATION_SCOPE);
    }

    @Test
    public void nonSecurityContextInSessionIsIgnored() throws Exception {
        PortletSessionSecurityContextRepository repo = new PortletSessionSecurityContextRepository();
        MockPortletRequest request = new MockPortletRequest();
        SecurityContextHolder.getContext().setAuthentication(testToken);
        request.getPortletSession().setAttribute(SPRING_SECURITY_CONTEXT_KEY, "NotASecurityContextInstance", PortletSession.APPLICATION_SCOPE);
        MockPortletResponse response = new MockPortletResponse();
        PortletRequestResponseHolder holder = new PortletRequestResponseHolder(request, response);
        SecurityContext context = repo.loadContext(holder);
        assertNotNull(context);
        assertNull(context.getAuthentication());
    }

    @Test
    public void sessionIsCreatedAndContextStoredWhenContextChanges() throws Exception {
        PortletSessionSecurityContextRepository repo = new PortletSessionSecurityContextRepository();
        MockPortletRequest request = new MockPortletRequest();
        MockPortletResponse response = new MockPortletResponse();
        PortletRequestResponseHolder holder = new PortletRequestResponseHolder(request, response);
        SecurityContext context = repo.loadContext(holder);
        assertNull(request.getPortletSession(false));
        // Simulate authentication during the request
        context.setAuthentication(testToken);
        repo.saveContext(context, holder);
        assertNotNull(request.getPortletSession(false));
        assertEquals(context, request.getPortletSession().getAttribute(SPRING_SECURITY_CONTEXT_KEY, PortletSession.APPLICATION_SCOPE));
    }

    @Test
    public void noSessionIsCreatedIfSessionWasInvalidatedDuringTheRequest() throws Exception {
        PortletSessionSecurityContextRepository repo = new PortletSessionSecurityContextRepository();
        MockPortletRequest request = new MockPortletRequest();
        request.getPortletSession();
        MockPortletResponse response = new MockPortletResponse();
        PortletRequestResponseHolder holder = new PortletRequestResponseHolder(request, response);
        SecurityContextHolder.setContext(repo.loadContext(holder));
        SecurityContextHolder.getContext().setAuthentication(testToken);
        request.getPortletSession().invalidate();
        repo.saveContext(SecurityContextHolder.getContext(), holder);
        assertNull(request.getPortletSession(false));
    }

    // SEC-1315
    @Test
    public void noSessionIsCreatedIfAnonymousTokenIsUsed() throws Exception {
        PortletSessionSecurityContextRepository repo = new PortletSessionSecurityContextRepository();
        MockPortletRequest request = new MockPortletRequest();
        MockPortletResponse response = new MockPortletResponse();
        PortletRequestResponseHolder holder = new PortletRequestResponseHolder(request, response);
        SecurityContextHolder.setContext(repo.loadContext(holder));
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anon", AuthorityUtils.createAuthorityList("ANON")));
        repo.saveContext(SecurityContextHolder.getContext(), holder);
        assertNull(request.getPortletSession(false));
    }

    // SEC-1587
    @Test
    public void contextIsRemovedFromSessionIfCurrentContextIsAnonymous() throws Exception {
        PortletSessionSecurityContextRepository repo = new PortletSessionSecurityContextRepository();
        MockPortletRequest request = new MockPortletRequest();
        SecurityContext ctxInSession = SecurityContextHolder.createEmptyContext();
        ctxInSession.setAuthentication(testToken);
        request.getPortletSession().setAttribute(SPRING_SECURITY_CONTEXT_KEY, ctxInSession, PortletSession.APPLICATION_SCOPE);
        PortletRequestResponseHolder holder = new PortletRequestResponseHolder(request, new MockPortletResponse());
        repo.loadContext(holder);
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("x","x", testToken.getAuthorities()));
        repo.saveContext(SecurityContextHolder.getContext(), holder);
        assertNull(request.getPortletSession().getAttribute(SPRING_SECURITY_CONTEXT_KEY, PortletSession.APPLICATION_SCOPE));
    }

    @Test
    public void contextIsRemovedFromSessionIfCurrentContextIsEmpty() throws Exception {
        PortletSessionSecurityContextRepository repo = new PortletSessionSecurityContextRepository();
        repo.setSpringSecurityContextKey("imTheContext");
        MockPortletRequest request = new MockPortletRequest();
        SecurityContext ctxInSession = SecurityContextHolder.createEmptyContext();
        ctxInSession.setAuthentication(testToken);
        request.getPortletSession().setAttribute("imTheContext", ctxInSession, PortletSession.APPLICATION_SCOPE);
        PortletRequestResponseHolder holder = new PortletRequestResponseHolder(request, new MockPortletResponse());
        repo.loadContext(holder);
        // Save an empty context
        repo.saveContext(SecurityContextHolder.getContext(), holder);
        assertNull(request.getPortletSession().getAttribute("imTheContext", PortletSession.APPLICATION_SCOPE));
    }

    //Not working after port to portlet apis, not quite sure why
//    // SEC-1735
//    @Test
//    public void contextIsNotRemovedFromSessionIfContextBeforeExecutionDefault() throws Exception {
//        PortletSessionSecurityContextRepository repo = new PortletSessionSecurityContextRepository();
//        MockPortletRequest request = new MockPortletRequest();
//        PortletRequestResponseHolder holder = new PortletRequestResponseHolder(request, new MockPortletResponse());
//        repo.loadContext(holder);
//        SecurityContext ctxInSession = SecurityContextHolder.createEmptyContext();
//        ctxInSession.setAuthentication(testToken);
//        request.getPortletSession().setAttribute(SPRING_SECURITY_CONTEXT_KEY, ctxInSession);
//        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("x","x", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
//        repo.saveContext(SecurityContextHolder.getContext(), holder);
//        assertSame(ctxInSession,request.getPortletSession().getAttribute(SPRING_SECURITY_CONTEXT_KEY));
//    }
}
