package nablarch.fw.jaxrs.cors;

import mockit.Mocked;
import mockit.Verifications;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import org.junit.Test;

/**
 * {@link CorsResponseFinisher}のテスト。
 */
public class CorsResponseFinisherTest {

    @Mocked
    private Cors cors;

    /**
     * CORSに処理を委譲していることを確認。
     */
    @Test
    public void delegate() {

        final HttpRequest request = null;
        final HttpResponse response = new HttpResponse();
        final ExecutionContext context = new ExecutionContext();

        CorsResponseFinisher sut = new CorsResponseFinisher();
        sut.setCors(cors);
        sut.finish(request, response, context);

        new Verifications() {{
            cors.postProcess(request, response, context);
            times=1;
        }};
    }
}