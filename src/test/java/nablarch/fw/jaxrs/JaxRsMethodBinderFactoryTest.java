package nablarch.fw.jaxrs;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import mockit.Deencapsulation;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.MethodBinder;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link JaxRsMethodBinderFactory}のテストクラス。
 */
public class JaxRsMethodBinderFactoryTest {

    /** テスト対象 */
    private JaxRsMethodBinderFactory sut = new JaxRsMethodBinderFactory();

    /**
     * {@link JaxRsMethodBinderFactory#create(String)}のテスト。
     */
    @Test
    public void testCreate() throws Exception {
        MethodBinder<HttpRequest, ?> methodBinder = sut.create("list");

        assertThat("JaxRsMethodBinderが生成されること", methodBinder, is(instanceOf(JaxRsMethodBinder.class)));

        // ファクトリに指定したメソッドが呼び出せていることを確認する。
        HttpResponse response = (HttpResponse) methodBinder.bind(new Resource()).handle(null, new ExecutionContext());
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.getBodyString(), is("ok"));

        // デフォルトで空のハンドラリストが設定されていることを確認する。
        List<Handler<HttpRequest, ?>> handlerList = Deencapsulation.getField(methodBinder, "handlerList");
        assertThat(handlerList.size(), is(0));
    }

    /**
     * {@link JaxRsMethodBinderFactory#create(String)}のテスト。
     */
    @Test
    public void testCreate_custom() throws Exception {
        sut.setHandlerList(new CustomHandlerListFactory().createObject());
        MethodBinder<HttpRequest, ?> methodBinder = sut.create("list");

        assertThat("JaxRsMethodBinderが生成されること", methodBinder, is(instanceOf(JaxRsMethodBinder.class)));

        // ファクトリに指定したメソッドが呼び出せていることを確認する。
        HttpResponse response = (HttpResponse) methodBinder.bind(new Resource()).handle(null, new ExecutionContext());
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.getBodyString(), is("ok"));

        // カスタムのハンドラリストが設定されていることを確認する。
        List<Handler<HttpRequest, ?>> handlerList = Deencapsulation.getField(methodBinder, "handlerList");
        assertThat(handlerList.size(), is(1));
        assertThat(handlerList.get(0), instanceOf(CustomHandler.class));
    }

    public static class Resource {

        public HttpResponse list() {
            HttpResponse response = new HttpResponse(200);
            response.write("ok");
            return response;
        }
    }

    public static class CustomHandlerListFactory implements JaxRsHandlerListFactory {
        @Override
        public List<Handler<HttpRequest, ?>> createObject() {
            return new ArrayList<Handler<HttpRequest, ?>>() {{
               add(new CustomHandler());
            }};
        }
    }

    public static class CustomHandler implements Handler<HttpRequest, HttpResponse> {
        @Override
        public HttpResponse handle(HttpRequest httpRequest, ExecutionContext executionContext) {
            return executionContext.handleNext(httpRequest);
        }
    }
}
