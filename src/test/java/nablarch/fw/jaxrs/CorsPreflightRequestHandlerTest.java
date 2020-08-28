package nablarch.fw.jaxrs;

import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.ExecutionContext;
import nablarch.fw.jaxrs.cors.Cors;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpRequestHandler;
import nablarch.fw.web.HttpResponse;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link CorsPreflightRequestHandler}のテスト。
 */
public class CorsPreflightRequestHandlerTest {

    @Mocked
    private Cors cors;

    @Test
    public void preflightRequest() {

        final ExecutionContext context = new ExecutionContext();
        final HttpRequest request = null;

        new Expectations() {{
            cors.isPreflightRequest(request, context);
            result = true;
            cors.createPreflightResponse(request, context);
            result = new HttpResponse(204);
        }};

        CorsPreflightRequestHandler sut = new CorsPreflightRequestHandler();
        sut.setCors(cors);

        context.addHandler(sut);
        HttpResponse response = context.handleNext(request);

        assertThat(response.getStatusCode(), is(204));
    }

    @Test
    public void notPreflightRequest() {

        final ExecutionContext context = new ExecutionContext();
        final HttpRequest request = null;

        new Expectations() {{
            cors.isPreflightRequest(request, context);
            result = false;
        }};

        CorsPreflightRequestHandler sut = new CorsPreflightRequestHandler();
        sut.setCors(cors);

        context.addHandler(sut)
                .addHandler(new HttpRequestHandler() {
                    @Override
                    public HttpResponse handle(HttpRequest request, ExecutionContext context) {
                        return new HttpResponse(404);
                    }
                });
        HttpResponse response = context.handleNext(request);

        assertThat(response.getStatusCode(), is(404));
    }
}