package nablarch.fw.jaxrs;

import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

import java.util.Map;

/**
 * アクセスログ出力時のメッセージボディの出力対象判定を差し替えるためのモック。
 */
public class MessageBodyLogTargetMatcherMock implements MessageBodyLogTargetMatcher {

    @Override
    public void initialize(Map<String, String> props) {
    }

    @Override
    public boolean isTargetRequest(HttpRequest request, ExecutionContext context) {
        return false;
    }

    @Override
    public boolean isTargetResponse(HttpRequest request, HttpResponse response, ExecutionContext context) {
        return false;
    }
}
