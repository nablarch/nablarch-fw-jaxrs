package nablarch.fw.jaxrs.cors;

import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * {@link CorsResponseFinisher}のテスト。
 */
public class CorsResponseFinisherTest {

    private final Cors cors = mock(Cors.class);

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

        verify(cors).postProcess(request, response, context);
    }
}