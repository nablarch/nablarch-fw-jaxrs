package nablarch.fw.jaxrs;

import java.util.Map;

/**
 * アクセスログ出力時のメッセージボディのマスク処理を差し替えるためのモック。
 */
public class MessageBodyMaskingFilterMock implements LogContentMaskingFilter {

    @Override
    public void initialize(Map<String, String> props) {
    }

    @Override
    public String mask(String content) {
        return "mock";
    }
}
