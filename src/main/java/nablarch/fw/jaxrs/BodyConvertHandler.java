package nablarch.fw.jaxrs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.util.StringUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpRequestHandler;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.HttpResponse.Status;

/**
 * {@link BodyConverter}によるリクエスト/レスポンスの変換を行うハンドラ。
 *
 * @author Kiyohito Itoh
 */
public class BodyConvertHandler implements HttpRequestHandler {

    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(BodyConvertHandler.class);

    /** {@link BodyConverter} */
    private List<BodyConverter> bodyConverters = new ArrayList<BodyConverter>();

    @Override
    public HttpResponse handle(final HttpRequest request, final ExecutionContext context) {

        final JaxRsContext jaxRsContext = JaxRsContext.get(context);

        final String contentType = request.getHeader("Content-Type");
        final String consumesMediaType = jaxRsContext.getConsumesMediaType();
        if (!supportsMediaType(contentType, consumesMediaType)) {
            LOGGER.logInfo("unsupported media type requested. "
                    + "request method = [" + request.getMethod() + "], "
                    + "request uri = [" + request.getRequestUri() + "], "
                    + "content type = [" + contentType + "], "
                    + "resource method = [" + jaxRsContext.toResourcePath() + "], "
                    + "consumes media type = [" + consumesMediaType + "]"
            );
            throw new HttpErrorResponse(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
        }

        if (StringUtil.hasValue(consumesMediaType)) {
            jaxRsContext.setRequest(findConverter(consumesMediaType).read(request, context));
        }

        final Object response = context.handleNext(request);
        if (response == null) {
            return new HttpResponse(Status.NO_CONTENT.getStatusCode());
        }

        final EntityResponse entityResponse = response instanceof EntityResponse ? (EntityResponse) response : null;
        final Object entity = entityResponse != null ? entityResponse.getEntity() : response;

        String producesMediaType = jaxRsContext.getProducesMediaType();
        final String entityResponseContentType = entityResponse != null ? entityResponse.getHeader("Content-Type") : null;
        if (StringUtil.hasValue(entityResponseContentType)) {
            if (StringUtil.hasValue(producesMediaType)) {
                throw new IllegalStateException(
                        "Content-Type is specified in both @Produces and EntityResponse. "
                                + "Specify the Content-Type in either @Produces or EntityResponse. "
                                + "resource method = [" + jaxRsContext.toResourcePath() + "]");
            }
            producesMediaType = entityResponseContentType;
        }

        if (StringUtil.hasValue(producesMediaType)) {
            HttpResponse convertedResponse = findConverter(producesMediaType).write(entity, context);
            if (entityResponse != null) {
                copy(entityResponse, convertedResponse);
            }
            return convertedResponse;
        }

        return cast(response);
    }

    /**
     * {@link EntityResponse}からコンバートされた{@link HttpResponse}にコピーする。
     *
     * レスポンスヘッダとステータスコードをコピーする。
     * レスポンスヘッダは上書きしない。
     * ステータスコードは指定された場合のみコピーする。
     *
     * @param from {@link EntityResponse}
     * @param to コンバートされた{@link HttpResponse}
     */
    private void copy(EntityResponse from, HttpResponse to) {

        // response header
        final Map<String, String> toHeaderMap = to.getHeaderMap();
        for (Map.Entry<String, String> fromHeader : from.getHeaderMap().entrySet()) {
            if (!toHeaderMap.containsKey(fromHeader.getKey())) {
                toHeaderMap.put(fromHeader.getKey(), fromHeader.getValue());
            }
        }

        // status code
        if (from.isStatusCodeSet()) {
            to.setStatusCode(from.getStatusCode());
        }
    }

    /**
     * メディアタイプを変換するための{@link BodyConverter}を取得する。
     *
     * 変換対象の{@link BodyConverter}が存在しない場合は、{@link Status#UNSUPPORTED_MEDIA_TYPE}を持つ{@link HttpErrorResponse}を送出する。
     *
     * @param mediaType メディアタイプ
     * @return {@link BodyConverter}
     */
    private BodyConverter findConverter(final String mediaType) {
        for (BodyConverter converter : bodyConverters) {
            if (converter.isConvertible(mediaType)) {
                return converter;
            }
        }
        throw new HttpErrorResponse(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    }

    /**
     * レスポンスのキャストを行う。
     * <p/>
     * レスポンスが{@code null}でなく、{@link HttpResponse}にキャストできない場合は、
     * {@link IllegalStateException}をスローする。
     *
     * @param response レスポンス
     * @return {@link HttpResponse}
     */
    private HttpResponse cast(final Object response) {
        if (response != null && !(response instanceof HttpResponse)) {
            throw new IllegalStateException(
                String.format(
                    "unsupported response type found. must be return %s. response type = [%s]",
                    HttpResponse.class.getName(), response.getClass().getName()));
        }
        return (HttpResponse) response;
    }

    /**
     * HTTPヘッダーのContent-Typeに指定されたメディアタイプをサポートしているかを判定する。
     *
     * 以下の場合のみサポートしていると判定する。
     * <pre>
     * ・Content-Typeが指定され、かつメディアタイプと一致する場合。（GET以外の場合を想定）
     * ・Content-Typeが未指定で、かつメディアタイプも未指定の場合。（GETの場合を想定）
     * </pre>
     *
     * @param contentType リクエストされたContent-Type
     * @param consumesMediaType {@link jakarta.ws.rs.Consumes}アノテーションに指定されたメディアタイプ
     * @return サポートしている場合は<code>true</code>
     */
    protected boolean supportsMediaType(final String contentType, final String consumesMediaType) {
        if (StringUtil.hasValue(contentType)) {
            return consumesMediaType != null && contentType.toLowerCase().contains(consumesMediaType);
        } else {
            return StringUtil.isNullOrEmpty(consumesMediaType);
        }
    }

    /**
     * {@link BodyConverter}のリストを設定する。
     *
     * 既に設定されていた{@link BodyConverter}のリストは破棄される。
     *
     * @param bodyConverters {@link BodyConverter}
     */
    public void setBodyConverters(final List<BodyConverter> bodyConverters) {
        this.bodyConverters = Collections.unmodifiableList(bodyConverters);
    }

    /**
     * {@link BodyConverter}を追加する。
     *
     * @param bodyConverter 追加する{@link BodyConverter}
     */
    public void addBodyConverter(final BodyConverter bodyConverter) {
        bodyConverters.add(bodyConverter);
    }
}
