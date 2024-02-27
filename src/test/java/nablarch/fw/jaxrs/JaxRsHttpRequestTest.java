package nablarch.fw.jaxrs;

import nablarch.fw.web.MockHttpCookie;
import nablarch.fw.web.MockHttpRequest;
import nablarch.fw.web.upload.PartInfo;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * {@link JaxRsHttpRequest}のテスト。
 */
public class JaxRsHttpRequestTest {

    /**
     * HTTPリクエストと同一のメソッドを取得できることを確認
     */
    @Test
    public void testGetMethod() {
        MockHttpRequest delegate = new MockHttpRequest();
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getMethod(), is(delegate.getMethod()));
    }

    /**
     * HTTPリクエストと同一のHTTPリクエストURIを取得できることを確認
     */
    @Test
    public void testGetRequestUri() {
        MockHttpRequest delegate = new MockHttpRequest();
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getRequestUri(), is(delegate.getRequestUri()));
    }

    /**
     * HTTPリクエストにHTTPリクエストURIを設定できることを確認
     */
    @Test
    public void testSetRequestUri() {
        MockHttpRequest delegate = new MockHttpRequest();
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        sut.setRequestUri("/requestUri");
        assertThat(sut.getRequestUri(), is(delegate.getRequestUri()));
    }

    /**
     * HTTPリクエストと同一のHTTPリクエストURIのパス部分が取得できることを確認
     */
    @Test
    public void testGetRequestPath() {
        MockHttpRequest delegate = (MockHttpRequest) new MockHttpRequest().setRequestUri("/requestPath?paramName1=param1&paramName2=param2");
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getRequestPath(), is(delegate.getRequestPath()));
    }

    /**
     * HTTPリクエストにリクエストパスを設定できることを確認
     */
    @Test
    public void testSetRequestPath() {
        MockHttpRequest delegate = (MockHttpRequest) new MockHttpRequest().setRequestUri("/requestPath?paramName1=param1&paramName2=param2");
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        sut.setRequestPath("/requestPath");
        assertThat(sut.getRequestUri(), is(delegate.getRequestUri()));
    }
    
    /**
     * HTTPリクエストと同一のHTTPバージョンを取得できることを確認
     */
    @Test
    public void testGetHttpVersion() {
        MockHttpRequest delegate = new MockHttpRequest();
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getHttpVersion(), is(delegate.getHttpVersion()));
    }

    /**
     * HTTPリクエストと同一のリクエストパラメータのMapを取得できることを確認
     */
    @Test
    public void testGetParamMap() {
        MockHttpRequest delegate = (MockHttpRequest) new MockHttpRequest()
                .setParam("paramName1", "param1-1", "param1-2")
                .setParam("paramName2", "param2");
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getParamMap(), is(delegate.getParamMap()));
    }

    /**
     * HTTPリクエストと同一のリクエストパラメータを取得できることを確認
     */
    @Test
    public void testGetParam() {
        MockHttpRequest delegate = (MockHttpRequest) new MockHttpRequest()
                .setParam("paramName", "param1", "param2");
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getParam("paramName"), is(delegate.getParam("paramName")));
    }

    /**
     * HTTPリクエストから指定したnameのパラメータ（配列）の先頭要素が取得できることを確認
     */
    @Test
    public void testGetPathParam_Success () {
        MockHttpRequest delegate = (MockHttpRequest) new MockHttpRequest().setParam("paramName", "param1", "param2");
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getPathParam("paramName"),  CoreMatchers.is("param1"));
    }

    /**
     * HTTPリクエストで指定したnameのパラメータ（配列）が空だった場合、nullが返却されることを確認
     */
    @Test
    public void testGetPathParam_ParamIsEmpty () {
        MockHttpRequest delegate = (MockHttpRequest) new MockHttpRequest().setParam("paramName");
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getPathParam("paramName"),  nullValue());
    }

    /**
     * 指定したnameのパラメータがHTTPリクエストに存在しない場合、nullが返却されることを確認
     */
    @Test
    public void testGetPathParam_ParamIsNotExist () {
        MockHttpRequest delegate = new MockHttpRequest();
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getPathParam("paramName"),  nullValue());
    }

    /**
     * HTTPリクエストにリクエストパラメータを設定できることを確認
     */
    @Test
    public void testSetParam() {
        MockHttpRequest delegate = new MockHttpRequest();
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getParamMap().size(), is(0));
        sut.setParam("paramName", "param1");
        assertThat(sut.getParamMap().size(), is(1));
        assertThat(sut.getParam("paramName"), is(delegate.getParam("paramName")));
    }

    /**
     * HTTPリクエストにリクエストパラメータのMapを設定できることを確認
     */
    @Test
    public void testSetParamMap() {
        MockHttpRequest delegate = new MockHttpRequest();
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getParamMap().size(), is(0));
        sut.setParamMap(new HashMap<String, String[]>() {{
            put("paramName1", new String[]{"param1-1", "param1-2"});
        }});
        assertThat(sut.getParamMap().size(), is(1));
        assertThat(sut.getParamMap(), is(delegate.getParamMap()));
    }

    /**
     * HTTPリクエストと同一のヘッダのMapを取得できることを確認
     */
    @Test
    public void testGetHeaderMap() {
        MockHttpRequest delegate = (MockHttpRequest) new MockHttpRequest()
                .setHeaderMap(new HashMap<String, String>() {{
                    put("key", "value");
                }});
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getHeaderMap(), is(delegate.getHeaderMap()));
    }

    /**
     * HTTPリクエストと同一のヘッダを取得できることを確認
     */
    @Test
    public void testGetHeader() {
        MockHttpRequest delegate = (MockHttpRequest) new MockHttpRequest()
                .setHeaderMap(new HashMap<String, String>() {{
                    put("key", "value");
                }});
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getHeader("key"), is(delegate.getHeader("key")));
    }

    /**
     * HTTPリクエストと同一のホストヘッダを取得できることを確認
     */
    @Test
    public void testGetHost() {
        MockHttpRequest delegate = (MockHttpRequest) new MockHttpRequest().setHost("host");
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getHost(), is(delegate.getHost()));
    }

    /**
     * HTTPリクエストと同一のCookieを取得できることを確認
     */
    @Test
    public void testGetCookie() {
        MockHttpCookie cookie = new MockHttpCookie();
        cookie.put("key", "value");
        MockHttpRequest delegate = (MockHttpRequest) new MockHttpRequest().setCookie(cookie);
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getCookie(), is(delegate.getCookie()));
    }

    /**
     * HTTPリクエストと同一のマルチパートの一部を取得できることを確認
     */
    @Test
    public void testGetPart() {
        MockHttpRequest delegate = new MockHttpRequest();
        delegate.setMultipart(new HashMap<String, List<PartInfo>>() {{
            put("key", new ArrayList<PartInfo>() {{
                add(PartInfo.newInstance("name"));
            }});
        }});
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getPart("key"), is(delegate.getPart("key")));
    }

    /**
     * HTTPリクエストのマルチパートを設定できることを確認
     */
    @Test
    public void testSetMultipart() {
        MockHttpRequest delegate = new MockHttpRequest();
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getMultipart().size(), is(0));
        sut.setMultipart(new HashMap<String, List<PartInfo>>() {{
            put("key", new ArrayList<PartInfo>() {{
                add(PartInfo.newInstance("name"));
            }});
        }});
        assertThat(sut.getMultipart().size(), is(1));
        assertThat(sut.getMultipart(), is(delegate.getMultipart()));
    }

    /**
     * HTTPリクエストと同一の全マルチパートを取得できることを確認
     */
    @Test
    public void testGetMultipart() {
        MockHttpRequest delegate = new MockHttpRequest();
        delegate.setMultipart(new HashMap<String, List<PartInfo>>() {{
            put("key", new ArrayList<PartInfo>() {{
                add(PartInfo.newInstance("name"));
            }});
        }});
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getMultipart(), is(delegate.getMultipart()));
    }

    /**
     * HTTPリクエストと同一のUserAgent情報を取得できることを確認
     */
    @Test
    public void testGetUserAgent() {
        MockHttpRequest delegate = new MockHttpRequest();
        JaxRsHttpRequest sut = new JaxRsHttpRequest(delegate);
        assertThat(sut.getUserAgent().getBrowserType(), is(delegate.getUserAgent().getBrowserType()));
        assertThat(sut.getUserAgent().getBrowserName(), is(delegate.getUserAgent().getBrowserName()));
        assertThat(sut.getUserAgent().getBrowserVersion(), is(delegate.getUserAgent().getBrowserVersion()));
        assertThat(sut.getUserAgent().getOsType(), is(delegate.getUserAgent().getOsType()));
        assertThat(sut.getUserAgent().getOsName(), is(delegate.getUserAgent().getOsName()));
        assertThat(sut.getUserAgent().getOsVersion(), is(delegate.getUserAgent().getOsVersion()));
    }
}
