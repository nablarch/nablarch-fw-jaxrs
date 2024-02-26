package nablarch.fw.jaxrs;

import nablarch.fw.web.MockHttpCookie;
import nablarch.fw.web.MockHttpRequest;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * {@link JaxRsHttpRequest}のテスト。
 */
public class JaxRsHttpRequestTest {

    /**
     * リクエストのメソッドを取得できることを確認
     */
    @Test
    public void testGetMethod() {
        JaxRsHttpRequest sut = new JaxRsHttpRequest(new MockHttpRequest());
        assertThat(sut.getMethod(), is("GET"));
    }

    /**
     * HTTPバージョンを取得できることを確認
     */
    @Test
    public void testGetHttpVersion() {
        JaxRsHttpRequest sut = new JaxRsHttpRequest(new MockHttpRequest());
        assertThat(sut.getHttpVersion(), is("HTTP/1.1"));
    }

    /**
     * リクエストパラメータのMapを取得できることを確認
     */
    @Test
    public void testGetParamMap() {
        JaxRsHttpRequest sut = new JaxRsHttpRequest( new MockHttpRequest()
                .setParam("paramName1", "param1-1", "param1-2")
                .setParam("paramName2", "param2"));
        Map<String, String[]> result = sut.getParamMap();
        assertEquals(result.size(), 2);
        assertArrayEquals(result.get("paramName1"), new String[]{"param1-1", "param1-2"});
        assertArrayEquals(result.get("paramName2"), new String[]{"param2"});
    }

    /**
     * リクエストパラメータを取得できることを確認
     */
    @Test
    public void testGetParam() {
        JaxRsHttpRequest sut = new JaxRsHttpRequest(new MockHttpRequest()
                .setParam("paramName", "param1", "param2"));
        assertArrayEquals(sut.getParam("paramName"), new String[]{"param1", "param2"});

    }

    /**
     * HTTPリクエストから指定したnameのパラメータ（配列）の先頭要素が取得できること
     */
    @Test
    public void testGetPathParam_Success () {
        JaxRsHttpRequest sut = new JaxRsHttpRequest(new MockHttpRequest()
                .setParam("paramName", "param1", "param2"));
        assertThat(sut.getPathParam("paramName"),  CoreMatchers.is("param1"));
    }

    /**
     * HTTPリクエストで指定したnameのパラメータ（配列）が空だった場合、nullが返却されること
     */
    @Test
    public void testGetPathParam_ParamIsEmpty () {
        JaxRsHttpRequest sut = new JaxRsHttpRequest(new MockHttpRequest().setParam("paramName"));
        assertThat(sut.getPathParam("paramName"),  nullValue());
    }

    /**
     * 指定したnameのパラメータがHTTPリクエストに存在しない場合、nullが返却されること
     */
    @Test
    public void testGetPathParam_ParamIsNotExist () {
        JaxRsHttpRequest sut = new JaxRsHttpRequest(new MockHttpRequest());
        assertThat(sut.getPathParam("paramName"),  nullValue());
    }

    /**
     * リクエストパラメータを設定できることを確認
     */
    @Test
    public void testSetParam() {
        JaxRsHttpRequest sut = new JaxRsHttpRequest(new MockHttpRequest());
        assertThat(sut.getParamMap().size(), is(0));
        sut.setParam("paramName", "param1");
        assertArrayEquals(sut.getParam("paramName"), new String[]{"param1"});
    }

    /**
     * リクエストパラメータのMapを設定できることを確認
     */
    @Test
    public void testSetParamMap() {
        JaxRsHttpRequest sut = new JaxRsHttpRequest(new MockHttpRequest());
        assertThat(sut.getParamMap().size(), is(0));
        final Map<String, String[]> params = new HashMap<String, String[]>() {{
            put("paramName1", new String[]{"param1-1", "param1-2"});
        }};
        sut.setParamMap(params);
        assertThat(sut.getParamMap(), sameInstance(params));
    }


    /**
     * ヘッダのMapを取得できることを確認
     */
    @Test
    public void testGetHeaderMap() {
        JaxRsHttpRequest sut = new JaxRsHttpRequest(new MockHttpRequest()
                .setHeaderMap(new HashMap<String, String>() {{
                    put("key", "value");
                }}));
        assertThat(sut.getHeaderMap().get("key"), is("value"));
    }


    /**
     * ヘッダを取得できることを確認
     */
    @Test
    public void testGetHeader() {
        JaxRsHttpRequest sut = new JaxRsHttpRequest(new MockHttpRequest()
                .setHeaderMap(new HashMap<String, String>() {{
                    put("key", "value");
                }}));
        assertThat(sut.getHeader("key"), is("value"));
    }

    /**
     * Cookieを取得できることを確認
     */
    @Test
    public void testGetCookie() {
        MockHttpCookie cookie = new MockHttpCookie();
        cookie.put("key", "value");
        JaxRsHttpRequest sut = new JaxRsHttpRequest(new MockHttpRequest().setCookie(cookie));
        assertThat(sut.getCookie().get("key"), is("value"));
    }

}
