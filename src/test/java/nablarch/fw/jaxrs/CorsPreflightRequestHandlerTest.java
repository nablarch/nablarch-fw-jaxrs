package nablarch.fw.jaxrs;

import nablarch.fw.ExecutionContext;
import nablarch.fw.jaxrs.cors.Cors;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpRequestHandler;
import nablarch.fw.web.HttpResponse;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link CorsPreflightRequestHandler}のテスト。
 */
public class CorsPreflightRequestHandlerTest {

    private final Cors cors = mock(Cors.class);

    @Test
    public void preflightRequest() {

        final ExecutionContext context = new ExecutionContext();
        final HttpRequest request = null;

        when(cors.isPreflightRequest(request, context)).thenReturn(true);
        when(cors.createPreflightResponse(request, context)).thenReturn(new HttpResponse(204));

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

        when(cors.isPreflightRequest(request, context)).thenReturn(false);

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