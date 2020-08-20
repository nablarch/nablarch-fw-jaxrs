package nablarch.fw.jaxrs;

import nablarch.fw.ExecutionContext;
import nablarch.fw.jaxrs.cors.BasicCors;
import nablarch.fw.jaxrs.cors.Cors;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpRequestHandler;
import nablarch.fw.web.HttpResponse;

/**
 * CORSのプリフライトリクエストを処理するハンドラ。
 *
 * @author Kiyohito Itoh
 */
public class CorsPreflightRequestHandler implements HttpRequestHandler {

    private Cors cors = new BasicCors();

    @Override
    public HttpResponse handle(HttpRequest request, ExecutionContext context) {
        if (cors.isPreflightRequest(request, context)) {
            return cors.createPreflightResponse(request, context);
        }
        return context.handleNext(request);
    }

    /**
     * CORSの処理を行うインタフェースを設定する。
     * @param cors CORSの処理を行うインタフェース
     */
    public void setCors(Cors cors) {
        this.cors = cors;
    }
}
