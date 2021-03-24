package nablarch.fw.jaxrs.cors;

import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * {@link BasicCors}のテスト。
 */
public class BasicCorsTest {

    @Mocked
    private HttpRequest request;

    /**
     * リソースへのアクセスを許可するオリジンが設定されていない場合は、
     *　設定するように伝える実行時例外が送出されること。
     */
    @Test
    public void requiredSetting() {

        new Expectations() {{
            request.getHeader("Origin");
            result = "https://foo.example";
            request.getHeader("Access-Control-Request-Method");
            result = "POST";
            request.getHeader("Access-Control-Request-Headers");
            result = "TEST";
        }};

        BasicCors sut = new BasicCors();
        try {
            sut.createPreflightResponse(request, null);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("The allowOrigins property of CORS must be set."));
        }
    }

    /**
     * デフォルト設定を確認するテスト。
     */
    @Test
    public void defaultSettings() {

        new Expectations() {{
            request.getHeader("Origin");
            result = "https://foo.example";
            request.getHeader("Access-Control-Request-Method");
            result = "POST";
            request.getHeader("Access-Control-Request-Headers");
            result = "TEST";
        }};

        BasicCors sut = new BasicCors();
        sut.setAllowOrigins(Arrays.asList("https://foo.example"));
        HttpResponse response = sut.createPreflightResponse(request, null);

        assertThat(response.getStatusCode(), is(204));
        assertThat(response.getHeader("Access-Control-Allow-Methods"),
                is("OPTIONS, GET, POST, PUT, DELETE, PATCH"));
        assertThat(response.getHeader("Access-Control-Allow-Headers"),
                is("Content-Type, X-CSRF-TOKEN"));
        assertThat(response.getHeader("Access-Control-Max-Age"), is("-1"));
        assertThat(response.getHeader("Access-Control-Allow-Origin"), is("https://foo.example"));
        assertThat(response.getHeader("Vary"), is("Origin"));
        assertThat(response.getHeader("Access-Control-Allow-Credentials"), is("true"));
    }

    /**
     * カスタム設定を確認するテスト。
     */
    @Test
    public void customSettings() {
        new Expectations() {{
            request.getHeader("Origin");
            result = "https://foo.example";
            request.getHeader("Access-Control-Request-Method");
            result = "POST";
            request.getHeader("Access-Control-Request-Headers");
            result = "TEST";
        }};

        BasicCors sut = new BasicCors();
        sut.setAllowOrigins(Arrays.asList("https://foo.example"));
        sut.setAllowCredentials(false);
        sut.setAllowHeaders(Arrays.asList("TEST", "TEST2"));
        sut.setAllowMethods(Arrays.asList("GET", "POST"));
        sut.setMaxAge(60);
        HttpResponse response = sut.createPreflightResponse(request, null);

        assertThat(response.getStatusCode(), is(204));
        assertThat(response.getHeader("Access-Control-Allow-Methods"), is("GET, POST"));
        assertThat(response.getHeader("Access-Control-Allow-Headers"), is("TEST, TEST2"));
        assertThat(response.getHeader("Access-Control-Max-Age"), is("60"));
        assertThat(response.getHeader("Access-Control-Allow-Origin"), is("https://foo.example"));
        assertThat(response.getHeader("Vary"), is("Origin"));
        assertThat(response.getHeader("Access-Control-Allow-Credentials"), is(nullValue()));
    }

    /**
     * 許可しないオリジンからのリクエストの場合は
     * Access-Control-Allow-OriginヘッダとVaryヘッダが設定されないこと。
     */
    @Test
    public void requestsFromDisallowedOrigin() {

        new Expectations() {{
            request.getHeader("Origin");
            result = "https://bar.example";
            request.getHeader("Access-Control-Request-Method");
            result = "POST";
            request.getHeader("Access-Control-Request-Headers");
            result = "TEST";
        }};

        BasicCors sut = new BasicCors();
        sut.setAllowOrigins(Arrays.asList("https://foo.example"));
        HttpResponse response = sut.createPreflightResponse(request, null);

        assertThat(response.getStatusCode(), is(204));
        assertThat(response.getHeader("Access-Control-Allow-Methods"),
                is("OPTIONS, GET, POST, PUT, DELETE, PATCH"));
        assertThat(response.getHeader("Access-Control-Allow-Headers"),
                is("Content-Type, X-CSRF-TOKEN"));
        assertThat(response.getHeader("Access-Control-Max-Age"), is("-1"));
        assertThat(response.getHeader("Access-Control-Allow-Origin"), is(nullValue()));
        assertThat(response.getHeader("Vary"), is(nullValue()));
        assertThat(response.getHeader("Access-Control-Allow-Credentials"), is("true"));
    }

    /**
     * プリフライトリクエスト判定のテスト。
     */
    @Test
    public void isPreflightRequestForMethodNG() {
        new Expectations() {{
            request.getMethod();                                 result = "GET";
        }};
        assertThat(new BasicCors().isPreflightRequest(request, null), is(false));
    }
    @Test
    public void isPreflightRequestForOriginNG() {
        new Expectations() {{
            request.getMethod();                                 result = "OPTIONS";
            request.getHeader("Origin");                         result = null;
        }};
        assertThat(new BasicCors().isPreflightRequest(request, null), is(false));
    }
    @Test
    public void isPreflightRequestForAccessControlRequestMethodNG() {
        new Expectations() {{
            request.getMethod();                                 result = "OPTIONS";
            request.getHeader("Origin");                         result = "OK";
            request.getHeader("Access-Control-Request-Method");  result = null;
        }};
        assertThat(new BasicCors().isPreflightRequest(request, null), is(false));
    }
    @Test
    public void isPreflightRequestForAllOK() {
        new Expectations() {{
            request.getMethod();                                 result = "OPTIONS";
            request.getHeader("Origin");                         result = "OK";
            request.getHeader("Access-Control-Request-Method");  result = "OK";
        }};
        assertThat(new BasicCors().isPreflightRequest(request, null), is(true));
    }
}