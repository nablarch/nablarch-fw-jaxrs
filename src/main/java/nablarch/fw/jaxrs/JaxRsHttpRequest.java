package nablarch.fw.jaxrs;


import nablarch.core.util.annotation.Published;
import nablarch.fw.web.HttpCookie;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.upload.PartInfo;
import nablarch.fw.web.useragent.UserAgent;

import java.util.List;
import java.util.Map;

/**
 * JAX-RS用の{@link HttpRequest}クラス。
 * <p/>
 * {@link JaxRsHttpRequest#getParamMap()}、{@link JaxRsHttpRequest#getParam(String)} を公開APIとし、それ以外のメソッドは保持するHttpRequestに委譲している。
 */
public class JaxRsHttpRequest extends HttpRequest {

    private final HttpRequest request;

    public JaxRsHttpRequest(HttpRequest request) {
        this.request = request;
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
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getRequestUri() {
        return request.getRequestUri();
    }

    @Override
    public HttpRequest setRequestUri(final String requestUri) {
        return request.setRequestUri(requestUri);
    }

    @Override
    public String getRequestPath() {
        return request.getRequestPath();
}

    @Override
    public HttpRequest setRequestPath(final String requestPath) {
        return request.setRequestPath(requestPath);
    }

    @Override
    public String getHttpVersion() {
        return request.getHttpVersion();
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
    public String getHost() {
        return request.getHost();
    }

    @Override
    public HttpCookie getCookie() {
        return request.getCookie();
    }

    @Override
    public List<PartInfo> getPart(final String name) {
       return request.getPart(name);
    }

    @Override
    public void setMultipart(Map<String, List<PartInfo>> multipart) {
        request.setMultipart(multipart);
    }

    @Override
    public Map<String, List<PartInfo>> getMultipart() {
        return request.getMultipart();
    }

    @Override
    public <UA extends UserAgent> UA getUserAgent() {
        return request.getUserAgent();
    }
}

