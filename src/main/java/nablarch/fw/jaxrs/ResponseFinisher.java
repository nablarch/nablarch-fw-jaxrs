package nablarch.fw.jaxrs;

import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

/**
 * レスポンスを仕上げるインタフェース。
 *
 * {@link JaxRsResponseHandler}が作成したレスポンス(エラーレスポンス含む)に共通する処理を行う。
 * 共通処理としてはセキュリティやCORSに対応したレスポンスヘッダの設定などを想定している。
 *
 * レスポンスの作成処理の後に実行する処理のため、このインタフェースの実装クラスでは例外を発生させてはならない。
 *
 * @author Kiyohito Itoh
 */
public interface ResponseFinisher {
    /**
     * レスポンスを仕上げる。
     *
     * @param request リクエスト
     * @param response レスポンス
     * @param context コンテキスト
     */
    void finish(HttpRequest request, HttpResponse response, ExecutionContext context);
}
