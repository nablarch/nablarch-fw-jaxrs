package nablarch.fw.jaxrs;

import nablarch.core.message.ApplicationException;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

/**
 * JAX-RS用のエラーレスポンスを生成するクラス。
 *
 * 例外の種類に応じて以下のレスポンスを生成する。
 *
 * <pre>
 * --------------------------------------------- --------------------
 * 例外クラス                                    ステータスコード
 * --------------------------------------------- --------------------
 * ApplicationException                          400
 * 上記以外                                      500
 * --------------------------------------------- --------------------
 *
 * </pre>
 *
 * @author Hisaaki Shioiri
 */
@Published(tag = "architect")
public class ErrorResponseBuilder {

    /**
     * エラーレスポンスを生成する。
     * <p/>
     * 発生したエラーが、{@link ApplicationException}の場合は、{@code 400}を生成する。
     * それ以外のエラーの場合には、{@code 500}を生成する。
     *
     * @param request {@link HttpRequest}
     * @param context {@link ExecutionContext}
     * @param throwable 発生したエラーの情報
     * @return エラーレスポンス
     */
    public HttpResponse build(HttpRequest request, ExecutionContext context, Throwable throwable) {
        if (throwable instanceof ApplicationException) {
            return new HttpResponse(400);
        } else {
            return new HttpResponse(500);
        }
    }
}
