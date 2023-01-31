package nablarch.fw.jaxrs;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.handler.SecureHandler;
import nablarch.fw.web.handler.secure.ContentTypeOptionsHeader;
import nablarch.fw.web.handler.secure.ReferrerPolicyHeader;
import nablarch.fw.web.handler.secure.XssProtectionHeader;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.Test;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * {@link AdoptHandlerResponseFinisher}のテスト。
 */
public class AdoptHandlerResponseFinisherTest {

    @Mocked
    private HttpServletRequest mockServletRequest;

    @Mocked
    private HttpServletResponse mockServletResponse;

    @Mocked
    private ServletContext mockServletContext;

    @Test
    public void testSecureHandler() {

        new Expectations() {{
            mockServletRequest.getContextPath();
            result = "dummy";
            mockServletRequest.getRequestURI();
            result = "dummy/test";
        }};

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