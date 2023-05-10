package nablarch.fw.jaxrs;

import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JaxRsBodyLogTargetMatcherTest {

    private final JaxRsBodyLogTargetMatcher sut = new JaxRsBodyLogTargetMatcher();

    private final HttpRequest request = mock(HttpRequest.class);

    private final HttpResponse response = mock(HttpResponse.class);

    /**
     * 初期化が正常終了する。
     */
    @Test
    public void testInitialize() {
        try {
            sut.initialize(new AppLogPropertyBuilder().build());
        } catch (Throwable t) {
            fail();
        }
    }

    /**
     * リクエストのContent-Typeがapplication/jsonの場合、出力対象とする。
     */
    @Test
    public void testIsTargetRequestIfJson() {
        when(request.getHeader("Content-Type")).thenReturn(MediaType.APPLICATION_JSON);

        boolean actual = sut.isTargetRequest(request, null);

        assertThat(actual, is(true));
    }

    /**
     * リクエストのContent-Typeがapplication/xmlの場合、出力対象とする。
     */
    @Test
    public void testIsTargetRequestIfXml() {
        when(request.getHeader("Content-Type")).thenReturn(MediaType.APPLICATION_XML);

        boolean actual = sut.isTargetRequest(request, null);

        assertThat(actual, is(true));
    }

    /**
     * リクエストのContent-Typeがサポート対象外の場合、出力対象にしない。
     */
    @Test
    public void testIsTargetRequestIfUnSupport() {
        when(request.getHeader("Content-Type")).thenReturn(MediaType.TEXT_HTML);

        boolean actual = sut.isTargetRequest(request, null);

        assertThat(actual, is(false));
    }

    /**
     * リクエストのContent-Typeが設定されていない場合、出力対象にしない。
     */
    @Test
    public void testIsTargetRequestIfNoContentType() {
        when(request.getHeader("Content-Type")).thenReturn(null);

        boolean actual = sut.isTargetRequest(request, null);

        assertThat(actual, is(false));
    }

    /**
     * リクエストが無い場合、出力対象にしない。
     */
    @Test
    public void testIsTargetRequestIfNoRequest() {
        boolean actual = sut.isTargetRequest(null, null);

        assertThat(actual, is(false));
    }

    /**
     * レスポンスのContent-Typeがapplication/jsonの場合、出力対象とする。
     */
    @Test
    public void testIsTargetResponseIfJson() {
        when(response.getHeader("Content-Type")).thenReturn(MediaType.APPLICATION_JSON);

        boolean actual = sut.isTargetResponse(request, response, null);

        assertThat(actual, is(true));
    }

    /**
     * レスポンスのContent-Typeがapplication/xmlの場合、出力対象とする。
     */
    @Test
    public void testIsTargetResponseIfXml() {
        when(response.getHeader("Content-Type")).thenReturn(MediaType.APPLICATION_XML);

        boolean actual = sut.isTargetResponse(request, response, null);

        assertThat(actual, is(true));
    }

    /**
     * レスポンスのContent-Typeがサポート対象外の場合、出力対象にしない。
     */
    @Test
    public void testIsTargetResponseIfUnSupport() {
        when(response.getHeader("Content-Type")).thenReturn(MediaType.TEXT_HTML);

        boolean actual = sut.isTargetResponse(request, response, null);

        assertThat(actual, is(false));
    }

    /**
     * レスポンスのContent-Typeが設定されていない場合、出力対象にしない。
     */
    @Test
    public void testIsTargetResponseIfNoContentType() {
        when(response.getHeader("Content-Type")).thenReturn(null);

        boolean actual = sut.isTargetResponse(request, response, null);

        assertThat(actual, is(false));
    }

    /**
     * レスポンスが無い場合、出力対象にしない。
     */
    @Test
    public void testIsTargetResponseIfNoRequest() {
        boolean actual = sut.isTargetResponse(null, null, null);

        assertThat(actual, is(false));
    }
}
