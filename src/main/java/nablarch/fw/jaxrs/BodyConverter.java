package nablarch.fw.jaxrs;

import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

/**
 * リクエスト/レスポンスの変換を行うインタフェース。
 *
 * @author Naoki Yamamoto
 */
public interface BodyConverter {

    /**
     * メディアタイプに応じてリクエストボディ部を読み込み、Beanオブジェクトに変換する。
     *
     * @param request HTTPリクエスト
     * @param executionContext 実行コンテキスト
     * @return Beanオブジェクト
     */
    Object read(HttpRequest request, ExecutionContext executionContext);

    /**
     * Beanオブジェクトをメディアタイプに応じて変換し、レスポンスボディ部へ書き込む。
     *
     * @param response Beanオブジェクト
     * @param executionContext 実行コンテキスト
     * @return HTTPレスポンス
     */
    HttpResponse write(Object response, ExecutionContext executionContext);

    /**
     * 指定されたメディアタイプを変換できるかどうか。
     *
     * @param mediaType メディアタイプ
     * @return 変換できる場合は{@code true}
     */
    boolean isConvertible(String mediaType);
}

