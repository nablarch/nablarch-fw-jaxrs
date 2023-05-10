package nablarch.fw.jaxrs;

import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * JAX-RSのメッセージボディがログ出力対象であるか判定するクラス。
 */
public class JaxRsBodyLogTargetMatcher implements MessageBodyLogTargetMatcher {

    /** ログ出力対象のコンテンツタイプ */
    private static final List<String> TARGET_MEDIA_TYPES = Arrays.asList(
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML
    );

    @Override
    public void initialize(Map<String, String> props) {
        // NOOP
    }

    @Override
    public boolean isTargetRequest(HttpRequest request, ExecutionContext context) {
        if (request == null) {
            return false;
        }
        return isTargetContentType(request.getHeader("Content-Type"));
    }

    @Override
    public boolean isTargetResponse(HttpRequest request, HttpResponse response, ExecutionContext context) {
        if (response == null) {
            return false;
        }
        return isTargetContentType(response.getHeader("Content-Type"));
    }

    /**
     * ログ出力対象のコンテンツタイプであるか判定する。
     *
     * @param contentType コンテンツタイプ
     * @return 出力対象である場合は {@code true}
     */
    private boolean isTargetContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        String mediaType = contentType.split(";")[0];
        return TARGET_MEDIA_TYPES.contains(mediaType.trim().toLowerCase());
    }
}
