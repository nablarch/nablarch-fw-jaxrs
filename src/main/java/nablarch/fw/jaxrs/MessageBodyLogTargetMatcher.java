package nablarch.fw.jaxrs;

import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

import java.util.Map;

/**
 * メッセージボディがログ出力対象であるか判定するためのインタフェース。
 */
@Published(tag = "architect")
public interface MessageBodyLogTargetMatcher {

    /**
     * 初期化する。
     *
     * @param props 各種ログ出力の設定情報
     */
    void initialize(Map<String, String> props);

    /**
     * ログ出力対象のリクエストボディであるか判定する。
     *
     * @param request {@link HttpRequest}
     * @param context {@link ExecutionContext}
     * @return 出力対象であれば {@code true}
     */
    boolean isTargetRequest(HttpRequest request, ExecutionContext context);

    /**
     * ログ出力対象のレスポンスボディであるか判定する。
     *
     * @param request {@link HttpRequest}
     * @param response {@link HttpResponse}
     * @param context {@link ExecutionContext}
     * @return 出力対象であれば {@code true}
     */
    boolean isTargetResponse(HttpRequest request, HttpResponse response, ExecutionContext context);
}
