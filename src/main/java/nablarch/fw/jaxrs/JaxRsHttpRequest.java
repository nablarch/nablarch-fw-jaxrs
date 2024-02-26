package nablarch.fw.jaxrs;


import nablarch.core.util.annotation.Published;
import nablarch.fw.web.HttpCookie;
import nablarch.fw.web.HttpRequest;

import java.util.Map;

/**
 * JAX-RS用の{@link HttpRequest}クラス。
 */
public class JaxRsHttpRequest  extends HttpRequest {

    private final HttpRequest request;

    public JaxRsHttpRequest(HttpRequest request) {
        this.request = request;
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getHttpVersion() {
        return request.getHttpVersion();
    }

    @Override
    @Published
    public Map<String, String[]> getParamMap() {
        return request.getParamMap();
    }

    @Override
    @Published
    public String[] getParam(String name) {
        return request.getParam(name);
    }

    /**
     * HTTPリクエストからパスパラメータを取得する。
     *
     * @param name パラメータ名
     * @return パラメータの値
     */
    @Published
    public String getPathParam(String name) {
        String[] params = request.getParam(name);
        return params == null || params.length == 0 ? null : params[0];
    }
    
    @Override
    public HttpRequest setParam(String name, String... params) {
        return request.setParam(name, params);
    }

    @Override
    public HttpRequest setParamMap(Map<String, String[]> params) {
        return request.setParamMap(params);
    }

    @Override
    public Map<String, String> getHeaderMap() {
        return request.getHeaderMap();
    }

    @Override
    public String getHeader(String headerName) {
        return request.getHeader(headerName);
    }

    @Override
    public HttpCookie getCookie() {
        return request.getCookie();
    }
}

