package nablarch.fw.jaxrs;

import jakarta.ws.rs.core.MediaType;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

/**
 * {@code multipart/form-data}形式のリクエストを後続のハンドラを呼び出すためにPass-Throughする{@link BodyConverter}の実装クラス。
 */
public class MultipartFormDataBodyConverter implements BodyConverter {
    /**
     * マルチパートリクエストは後続のハンドラで処理するため、処理自体は行わず常に{@code null}を返却する。
     *
     * @param request HTTPリクエスト
     * @param executionContext 実行コンテキスト
     * @return 常に{@code null}
     */
    @Override
    public Object read(HttpRequest request, ExecutionContext executionContext) {
        // なにもしない（マルチパートリクエストは後続のハンドラで処理する）
        return null;
    }

    /**
     * マルチパートのメディアタイプがレスポンスとなることはないため、なにもしない
     *
     * @param response Beanオブジェクト
     * @param executionContext 実行コンテキスト
     * @return 常に{@code null}
     */
    @Override
    public HttpResponse write(Object response, ExecutionContext executionContext) {
        // なにもしない
        return null;
    }

    /**
     * メディアタイプが{@code multipart/form-data}の場合、後続のハンドラで処理するため{@code true}を返却する
     *
     * @param mediaType メディアタイプ
     * @return メディアタイプが{@code multipart/form-data}の場合は{@code true}
     */
    @Override
    public boolean isConvertible(String mediaType) {
        return mediaType.startsWith(MediaType.MULTIPART_FORM_DATA);
    }
}
