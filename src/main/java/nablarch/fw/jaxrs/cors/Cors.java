package nablarch.fw.jaxrs.cors;

import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

/**
 * CORSの処理を行うインタフェース。
 *
 * @author Kiyohito Itoh
 */
@Published(tag = "architect")
public interface Cors {
    /**
     * リクエストがプリフライトリクエストであるか否かを判定する。
     * @param request リクエスト
     * @param context コンテキスト
     * @return リクエストがプリフライトリクエストの場合はtrue
     */
    boolean isPreflightRequest(HttpRequest request, ExecutionContext context);

    /**
     * プリフライトリクエストに対するレスポンスを作成する。
     * @param request リクエスト
     * @param context コンテキスト
     * @return プリフライトリクエストに対するレスポンス
     */
    HttpResponse createPreflightResponse(HttpRequest request, ExecutionContext context);

    /**
     * プリフライトリクエスト後の実際のリクエストのレスポンスに対する処理を行う。
     * @param request リクエスト
     * @param context コンテキスト
     * @param response レスポンス
     */
    void postProcess(HttpRequest request, HttpResponse response, ExecutionContext context);
}
