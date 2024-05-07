package nablarch.fw.jaxrs;

import nablarch.core.beans.BeanUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * "application/x-www-form-urlencoded"に対するリクエスト/レスポンスの変換を行うクラス。
 *
 * @author Kiyohito Itoh
 */
@Published(tag = "architect")
public class FormUrlEncodedConverter extends BodyConverterSupport {

    @Override
    protected Object convertRequest(HttpRequest request, ExecutionContext context) {
        final JaxRsContext jaxRsContext = JaxRsContext.get(context);
        return BeanUtil.createAndCopy(jaxRsContext.getRequestClass(), request.getParamMap());
    }

    @Override
    protected HttpResponse convertResponse(Object response, ExecutionContext context) {

        final JaxRsContext jaxRsContext = JaxRsContext.get(context);
        final MultivaluedMap<String, String> multivaluedMap = castResponse(response, jaxRsContext);

        if (multivaluedMap.isEmpty()) {
            return new HttpResponse(HttpResponse.Status.NO_CONTENT.getStatusCode());
        }

        final ContentType contentType = getContentType(jaxRsContext.getProducesMediaType());

        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : multivaluedMap.entrySet()) {
            final String key = encode(entry.getKey(), contentType.getEncoding());
            for (String value : entry.getValue()) {
                if (sb.length() != 0) {
                    sb.append('&');
                }
                sb.append(key).append('=');
                if (value != null) {
                    sb.append(encode(value, contentType.getEncoding()));
                }
            }
        }

        final HttpResponse httpResponse = new HttpResponse();
        httpResponse.setContentType(contentType.getValue());
        httpResponse.write(sb);

        return httpResponse;
    }

    /**
     * 指定された文字列に対してURLエンコードを行う。
     * @param str 文字列
     * @param encoding エンコーディング
     * @return URLエンコード後の文字列
     */
    private String encode(String str, Charset encoding) {
        try {
            return URLEncoder.encode(str, encoding.name());
        } catch (UnsupportedEncodingException ignore) {
            throw new RuntimeException(ignore); // not happened.
        }
    }

    /**
     * レスポンスオブジェクトを{@link MultivaluedMap<String, String>}にキャストする。
     *
     * キャストできない場合は{@link IllegalStateException}をスローする。
     *
     * @param response レスポンスオブジェクト
     * @param context {@link JaxRsContext}
     * @return キャスト後のオブジェクト
     */
    private MultivaluedMap<String, String> castResponse(Object response, JaxRsContext context) {
        try {
            return (MultivaluedMap<String, String>) response;
        } catch (ClassCastException e) {

            throw new IllegalStateException(
                String.format("return type of resource method that specified "
                        + "@Produces({ \"application/x-www-form-urlencoded\" }) "
                        + "should be MultivaluedMap<String, String>. "
                        + "resource class = [%s], resource method = [%s], return type = [%s]",
                        context.getResourceMethod().getDeclaringClass().getSimpleName(),
                        context.getResourceMethod().getName(),
                        response.getClass().getSimpleName()), e);
        }
    }

    @Override
    public boolean isConvertible(String mediaType) {
        return mediaType.toLowerCase().startsWith(MediaType.APPLICATION_FORM_URLENCODED);
    }
}
