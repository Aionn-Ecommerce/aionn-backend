package com.aionn.identity.adapter.rest.support.session;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import com.aionn.identity.infrastructure.security.web.SecurityRequestAttributeKeys;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrentSessionIdArgumentResolverTest {

    private final CurrentSessionIdArgumentResolver resolver = new CurrentSessionIdArgumentResolver();

    @SuppressWarnings("unused")
    static class Sample {
        public void handle(@CurrentSessionId String sessionId, String otherParam) {
        }
    }

    private static MethodParameter param(int index) throws Exception {
        Method method = Sample.class.getMethod("handle", String.class, String.class);
        return new MethodParameter(method, index);
    }

    @Test
    void supportsAnnotatedStringParameter() throws Exception {
        assertTrue(resolver.supportsParameter(param(0)));
        assertFalse(resolver.supportsParameter(param(1)));
    }

    @Test
    void resolvesSessionIdFromRequestAttribute() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SecurityRequestAttributeKeys.SESSION_ID, "session-1");
        var webRequest = new ServletWebRequest(request);

        Object result = resolver.resolveArgument(param(0), null, webRequest, null);

        assertEquals("session-1", result);
    }

    @Test
    void returnsNullWhenAttributeAbsent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        var webRequest = new ServletWebRequest(request);

        Object result = resolver.resolveArgument(param(0), null, webRequest, null);

        assertNull(result);
    }

    @Test
    void returnsNullWhenAttributeNotString() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SecurityRequestAttributeKeys.SESSION_ID, 123);
        var webRequest = new ServletWebRequest(request);

        Object result = resolver.resolveArgument(param(0), null, webRequest, null);

        assertNull(result);
    }
}
