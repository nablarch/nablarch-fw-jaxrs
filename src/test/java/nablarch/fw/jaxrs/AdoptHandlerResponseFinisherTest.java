package nablarch.fw.jaxrs;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.handler.SecureHandler;
import nablarch.fw.web.handler.secure.ContentTypeOptionsHeader;
import nablarch.fw.web.handler.secure.ReferrerPolicyHeader;
import nablarch.fw.web.handler.secure.XssProtectionHeader;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link AdoptHandlerResponseFinisher}のテスト。
 */
public class AdoptHandlerResponseFinisherTest {

    private final HttpServletRequest mockServletRequest = mock(HttpServletRequest.class, RETURNS_DEEP_STUBS);

    private final HttpServletResponse mockServletResponse = mock(HttpServletResponse.class);

    private final ServletContext mockServletContext = mock(ServletContext.class);

    @Test
    public void testSecureHandler() {

        when(mockServletRequest.getContextPath()).thenReturn("dummy");
        when(mockServletRequest.getRequestURI()).thenReturn("dummy/test");

        SecureHandler handler = new SecureHandler();
        handler.setSecureResponseHeaderList(Arrays.asList(
                new XssProtectionHeader(),
                new ContentTypeOptionsHeader(),
                new ReferrerPolicyHeader()));

        AdoptHandlerResponseFinisher sut = new AdoptHandlerResponseFinisher();
        sut.setHandler(handler);

        HttpRequest request = null;
        HttpResponse response = new HttpResponse();
        ExecutionContext context = new ServletExecutionContext(
                mockServletRequest, mockServletResponse, mockServletContext);
        sut.finish(request, response, context);

        assertThat(response.getHeader("X-XSS-Protection"), notNullValue());
        assertThat(response.getHeader("X-Content-Type-Options"), notNullValue());
        assertThat(response.getHeader("Referrer-Policy"), notNullValue());
    }
}