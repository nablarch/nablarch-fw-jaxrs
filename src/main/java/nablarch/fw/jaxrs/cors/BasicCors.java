package nablarch.fw.jaxrs.cors;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.util.StringUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

import java.util.Arrays;
import java.util.List;

/**
 * {@link Cors}の基本実装クラス。
 *
 * @author Kiyohito Itoh
 */
public class BasicCors implements Cors {

    private static final Logger LOGGER = LoggerManager.get(Cors.class);

    private List<String> allowOrigins = null;
    private String allowMethods = joinWithComma(Arrays.asList("OPTIONS", "GET", "POST", "PUT", "DELETE", "PATCH"));
    private String allowHeaders = joinWithComma(Arrays.asList("Content-Type", "X-CSRF-TOKEN"));
    private long maxAge = -1;
    private boolean allowCredentials = true;

    @Override
    public boolean isPreflightRequest(HttpRequest request, ExecutionContext context) {
        return request.getMethod().equals("OPTIONS") &&
                request.getHeader(Headers.ORIGIN) != null &&
                request.getHeader(Headers.ACCESS_CONTROL_REQUEST_METHOD) != null;
    }

    @Override
    public HttpResponse createPreflightResponse(HttpRequest request, ExecutionContext context) {
        if (LOGGER.isDebugEnabled()) {
            String message = String.format("Preflight request%nRequest-Path: %s%n%s: %s%n%s: %s%n%s: %s",
                    request.getRequestPath(),
                    Headers.ORIGIN, request.getHeader(Headers.ORIGIN),
                    Headers.ACCESS_CONTROL_REQUEST_METHOD, request.getHeader(Headers.ACCESS_CONTROL_REQUEST_METHOD),
                    Headers.ACCESS_CONTROL_REQUEST_HEADERS, request.getHeader(Headers.ACCESS_CONTROL_REQUEST_HEADERS));
            LOGGER.logDebug(message);
        }
        HttpResponse response = new HttpResponse(204);
        response.setHeader(Headers.ACCESS_CONTROL_ALLOW_METHODS, allowMethods);
        response.setHeader(Headers.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
        response.setHeader(Headers.ACCESS_CONTROL_MAX_AGE, String.valueOf(maxAge));
        postProcess(request, response, context);
        return response;
    }

    @Override
    public void postProcess(HttpRequest request, HttpResponse response, ExecutionContext context) {
        processOrigin(request, response);
        processCredentials(response);
    }

    private void processOrigin(HttpRequest request, HttpResponse response) {
        if (allowOrigins == null) {
            throw new IllegalStateException("The allowOrigins property of CORS must be set.");
        }
        String origin = request.getHeader(Headers.ORIGIN);
        if (allowOrigins.contains(origin)) {
            response.setHeader(Headers.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            response.setHeader(Headers.VARY, Headers.ORIGIN);
        }
    }

    private void processCredentials(HttpResponse response) {
        if (allowCredentials) {
            response.setHeader(Headers.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }
    }

    private static String joinWithComma(List<String> collection) {
        return StringUtil.join(", ", collection);
    }

    /**
     * リソースへのアクセスを許可するオリジンを設定する。
     * @param allowOrigins リソースへのアクセスを許可するオリジン
     */
    public void setAllowOrigins(List<String> allowOrigins) {
        this.allowOrigins = allowOrigins;
    }

    /**
     * リソースへのアクセス時に許可するメソッドを設定する。
     * @param allowMethods リソースへのアクセス時に許可するメソッド
     */
    public void setAllowMethods(List<String> allowMethods) {
        this.allowMethods = joinWithComma(allowMethods);
    }

    /**
     * 実際のリクエストで使用できるHTTPヘッダを設定する。
     * @param allowHeaders 実際のリクエストで使用できるHTTPヘッダ
     */
    public void setAllowHeaders(List<String> allowHeaders) {
        this.allowHeaders = joinWithComma(allowHeaders);
    }

    /**
     * プリフライトリクエストの結果をキャッシュしてよい時間（秒）を設定する。
     * @param maxAge プリフライトリクエストの結果をキャッシュしてよい時間（秒）
     */
    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * 実際のリクエストで資格情報を使用してよいか否かを設定する。
     * @param allowCredentials 実際のリクエストで資格情報を使用してよい場合はtrue
     */
    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    private static final class Headers {
        static final String ORIGIN = "Origin";
        static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
        static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
        static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
        static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
        static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
        static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
        static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
        static final String VARY = "Vary";
    }
}
