package nablarch.fw.jaxrs;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.HandlerWrapper;
import nablarch.fw.Interceptor;
import nablarch.fw.handler.MethodBinding;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link JaxRsMethodBinder}のテストクラス。
 */
public class JaxRsMethodBinderTest {

    @SuppressWarnings("deprecation")
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /** テスト対象 */
    JaxRsMethodBinder sut;

    public final HttpRequest req = mock(HttpRequest.class);

    public ExecutionContext ctx = new ExecutionContext();

    private static final List<Handler<HttpRequest, ?>> dummyHandlers = new ArrayList<Handler<HttpRequest, ?>>() {{
        add(new JaxRsBeanValidationHandler());
    }};

    /**
     * 指定したメソッドがバインドされて正常に実行されるケース。
     */
    @Test
    public void testBind_success() throws Exception {

        sut = new JaxRsMethodBinder("nothing", dummyHandlers);
        HandlerWrapper<HttpRequest, Object> wrapper = sut.bind(new TestAction());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("指定したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("nothing"));

        sut = new JaxRsMethodBinder("requestOnly", dummyHandlers);
        wrapper = sut.bind(new TestAction());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("指定したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("request only"));

        sut = new JaxRsMethodBinder("jaxRsRequestOnly", dummyHandlers);
        wrapper = sut.bind(new TestAction());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("指定したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("jaxRsRequest only"));

        sut = new JaxRsMethodBinder("contextOnly", dummyHandlers);
        wrapper = sut.bind(new TestAction());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("指定したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("context only"));

        sut = new JaxRsMethodBinder("formOnly", dummyHandlers);
        wrapper = sut.bind(new TestAction());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("指定したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("form only"));

        sut = new JaxRsMethodBinder("requestAndForm", dummyHandlers);
        wrapper = sut.bind(new TestAction());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("指定したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("request and form"));

        sut = new JaxRsMethodBinder("formAndRequest", dummyHandlers);
        wrapper = sut.bind(new TestAction());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("指定したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("form and request"));

        sut = new JaxRsMethodBinder("requestAndContextAndForm", dummyHandlers);
        wrapper = sut.bind(new TestAction());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("指定したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(),
                containsString("request and context and form"));
    }

    /**
     * JAX-RSリソースクラスの継承、インターフェース実装に関する呼び分けを確認する
     */
    @Test
    public void testBind_jaxRsInheritVariation_success() throws Exception {

        sut = new JaxRsMethodBinder("list", dummyHandlers);
        HandlerWrapper<HttpRequest, Object> wrapper = sut.bind(new SimpleResource());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("指定したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("ok"));

        sut = new JaxRsMethodBinder("list", dummyHandlers);
        wrapper = sut.bind(new ResourceImpl());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("インターフェース定義を実装したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("ok"));

        sut = new JaxRsMethodBinder("list", dummyHandlers);
        wrapper = sut.bind(new ResourceExtendsAndImplements());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("インターフェース定義を実装したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("ok"));

        sut = new JaxRsMethodBinder("list", dummyHandlers);
        wrapper = sut.bind(new ResourceExtendsAndImplements2());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("親クラスで実装したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("ok"));

        sut = new JaxRsMethodBinder("list", dummyHandlers);
        wrapper = sut.bind(new ResourceExtendsGrandParentResourceAndIMplementsPlainInterface());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("親クラスで実装したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("ok"));

        sut = new JaxRsMethodBinder("list", dummyHandlers);
        wrapper = sut.bind(new ResourceInheritDefaultMethod());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("引き継いだデフォルトメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("ok"));

        sut = new JaxRsMethodBinder("list", dummyHandlers);
        wrapper = sut.bind(new ResourceOverrideDefaultMethod());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("オーバーライドしたメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("test"));
    }

    /**
     * カスタムの{@link JaxRsHandlerListFactory}を使用するケース。
     */
    @Test
    public void testBind_customJaxRsHandlerListFactory() throws Exception {

        // カスタムのハンドラが実行されていることを確認
        sut = new JaxRsMethodBinder("nothing", new ArrayList<Handler<HttpRequest, ?>>() {{add(new TestHandler());}});
        HandlerWrapper<HttpRequest, Object> wrapper = sut.bind(new TestAction());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("指定したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("nothing"));
        assertThat("カスタムで指定したハンドラが実行されていること", (String) ctx.getRequestScopedVar("handler"), is("testHandler called."));

        // ハンドラリストが空であっても正常に動作が完了することを確認
        sut = new JaxRsMethodBinder("nothing", new ArrayList<Handler<HttpRequest, ?>>());
        wrapper = sut.bind(new TestAction());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("指定したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("nothing"));

        // ハンドラリストがnullであっても正常に動作が完了することを確認
        sut = new JaxRsMethodBinder("nothing", null);
        wrapper = sut.bind(new TestAction());
        assertThat("戻り値の型がMethodBindingを継承していること", wrapper, instanceOf(MethodBinding.class));
        assertThat("指定したメソッドが実行されていること", ((HttpResponse) wrapper.handle(req, ctx)).getBodyString(), containsString("nothing"));
    }

    /**
     * 指定したメソッド名と同一の名前のメソッドがクラス内に複数定義されているケース。
     */
    @Test
    public void testBind_methodNameDuplicated() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                "method name is duplicated. class = [nablarch.fw.jaxrs.JaxRsMethodBinderTest$TestAction], method = [duplicated]");

        sut = new JaxRsMethodBinder("duplicated", dummyHandlers);
        HandlerWrapper<HttpRequest, Object> wrapper = sut.bind(new TestAction());
        wrapper.handle(req, ctx);
    }

    /**
     * 指定したメソッドがクラス内に定義されていないケース。
     */
    @Test
    public void testBind_methodUndefined() throws Exception {

        expectedException.expect(HttpErrorResponse.class);
        expectedException.expect(new TypeSafeMatcher<HttpErrorResponse>() {
            @Override
            protected boolean matchesSafely(HttpErrorResponse errorResponse) {
                return errorResponse.getResponse()
                        .getStatusCode() == HttpResponse.Status.NOT_FOUND.getStatusCode();
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("error response with status code 404");
            }
        });

        when(req.getRequestUri()).thenReturn("/test");

        sut = new JaxRsMethodBinder("test", dummyHandlers);
        HandlerWrapper<HttpRequest, Object> wrapper = sut.bind(new TestAction());
        wrapper.handle(req, ctx);
    }

    /**
     * 指定したメソッドが不正なケース。
     */
    @Test
    public void testBind_signatureInvalid_combination() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("argument definition is invalid. method = [formAndForm");

        sut = new JaxRsMethodBinder("formAndForm", dummyHandlers);
        HandlerWrapper<HttpRequest, Object> wrapper = sut.bind(new TestAction());
        wrapper.handle(req, ctx);
    }

    /**
     * リソースメソッドがアクセス不可の場合、{@link RuntimeException}が発生すること。
     * また元例外は、{@link IllegalAccessException}であること。
     */
    @Test
    public void testBind_methodAccessError() throws Exception {
        PrivateResource resource = new PrivateResource();
        JaxRsMethodBinder.ResourceMethod resourceMethod = new JaxRsMethodBinder.ResourceMethod(
                PrivateResource.class.getDeclaredMethod("method"));
        try {
            resourceMethod.invoke(resource, null, new ExecutionContext());
            fail("ここはこない。");
        } catch (RuntimeException e) {
            assertThat("元例外は、IllegalAccessExceptionであること", e.getCause(), is(instanceOf(IllegalAccessException.class)));
        }
    }

    /**
     * 指定したメソッドが不正なケース。
     */
    @Test
    public void testBind_signatureInvalid_size() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("argument definition is invalid. method = [requestAndFormAndForm");

        sut = new JaxRsMethodBinder("requestAndFormAndForm", dummyHandlers);
        HandlerWrapper<HttpRequest, Object> wrapper = sut.bind(new TestAction());
        wrapper.handle(req, ctx);
    }

    /**
     * 指定したメソッド実行時、{@link RuntimeException}が送出されるケース。
     */
    @Test
    public void testBind_throwRuntimeException() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(containsString("runtime exception."));

        sut = new JaxRsMethodBinder("throwRuntimeException", dummyHandlers);
        HandlerWrapper<HttpRequest, Object> wrapper = sut.bind(new TestAction());
        wrapper.handle(req, ctx);
    }

    /**
     * 指定したメソッド実行時、{@link Error}が送出されるケース。
     */
    @Test
    public void testBind_throwError() throws Exception {
        expectedException.expect(Error.class);
        expectedException.expectMessage(containsString("error."));

        sut = new JaxRsMethodBinder("throwError", dummyHandlers);
        HandlerWrapper<HttpRequest, Object> wrapper = sut.bind(new TestAction());
        wrapper.handle(req, ctx);
    }

    /**
     * 指定したメソッド実行時、{@link Exception}が送出されるケース。
     */
    @Test
    public void testBind_throwException() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage(containsString("exception."));

        sut = new JaxRsMethodBinder("throwException", dummyHandlers);
        HandlerWrapper<HttpRequest, Object> wrapper = sut.bind(new TestAction());
        wrapper.handle(req, ctx);
    }

    @Test
    public void userInterceptor() throws Exception {
        sut = new JaxRsMethodBinder("interceptorTest", Collections.<Handler<HttpRequest, ?>>emptyList());
        final TestAction action = new TestAction();
        final HandlerWrapper<HttpRequest, Object> wrapper = sut.bind(action);
        final HttpResponse result = (HttpResponse) wrapper.handle(req, new ExecutionContext());
        
        assertThat("Interceptorで設定した値が戻されること", result.getContentPath().getPath(), is("TestInterceptorの戻り"));
    }

    @Test
    public void testBind_resourceClassAndMethodAreSavedInRequestScope() throws Exception {
        sut = new JaxRsMethodBinder("nothing", dummyHandlers);
        HandlerWrapper<HttpRequest, Object> wrapper = sut.bind(new TestAction());
        wrapper.handle(req, ctx);

        Class<?> clazz = ctx.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS);
        assertThat(clazz, is((Object)TestAction.class));

        Method method = ctx.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD);
        Method nothingMethod = TestAction.class.getMethod("nothing");
        assertThat(method, is(nothingMethod));
    }

    @Test
    public void testBind_delegateObjectClassIsSetToScopeAsBoundClass() {
        sut = new JaxRsMethodBinder("nothing", dummyHandlers);
        HandlerWrapper<HttpRequest, Object> wrapper = sut.bind(new TestSubAction());
        wrapper.handle(req, ctx);

        Class<?> clazz = ctx.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS);
        assertThat(clazz, is((Object)TestSubAction.class));
    }

    /**
     * テスト用のActionクラス。
     */
    private static class TestAction {

        public HttpResponse nothing() {
            return new HttpResponse("nothing");
        }

        public HttpResponse requestOnly(HttpRequest req) {
            return new HttpResponse("request only");
        }

        public HttpResponse jaxRsRequestOnly(JaxRsHttpRequest req) {
            return new HttpResponse("jaxRsRequest only");
        }

        public HttpResponse contextOnly(ExecutionContext ctx) {
            return new HttpResponse("context only");
        }

        public HttpResponse formOnly(TestForm form) {
            return new HttpResponse("form only");
        }

        public HttpResponse requestAndForm(HttpRequest req, TestForm form) {
            return new HttpResponse("request and form");
        }

        public HttpResponse formAndRequest(TestForm form, HttpRequest req) {
            return new HttpResponse("form and request");
        }

        public HttpResponse requestAndContextAndForm(HttpRequest req, ExecutionContext ctx, TestForm form) {
            return new HttpResponse("request and context and form");
        }

        public HttpResponse formAndForm(TestForm form1, TestForm form2) {
            return new HttpResponse("form and form");
        }

        public HttpResponse requestAndFormAndForm(HttpRequest req, TestForm form1, TestForm form2) {
            return new HttpResponse("request and form and form");
        }

        public HttpResponse duplicated(HttpRequest req) {
            return new HttpResponse("duplicated");
        }

        public HttpResponse duplicated(TestForm form) {
            return new HttpResponse("duplicated");
        }

        public HttpResponse throwRuntimeException() {
            throw new RuntimeException("runtime exception.");
        }

        public HttpResponse throwError() {
            throw new Error("error.");
        }

        public HttpResponse throwException() throws Exception {
            throw new Exception("exception.");
        }
        
        @TestInterceptor
        public void interceptorTest() {
            // NOP
        }
    }

    private static class TestSubAction extends TestAction {}

    @Interceptor(TestInterceptor.TestImpl.class)
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestInterceptor {
        
        class TestImpl extends Interceptor.Impl<Object, HttpResponse, TestInterceptor> {
            @Override
            public HttpResponse handle(final Object o, final ExecutionContext context) {
                final Object result = getOriginalHandler().handle(o, context);
                return new HttpResponse("TestInterceptorの戻り");
            }
        }
    }

    /**
     * テスト用のFormクラス。
     */
    private static class TestForm {

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * テスト用の{@link Handler}クラス。
     */
    public static class TestHandler implements Handler<HttpRequest, HttpResponse> {

        @Override
        public HttpResponse handle(HttpRequest httpRequest, ExecutionContext executionContext) {
            executionContext.setRequestScopedVar("handler", "testHandler called.");
            return executionContext.handleNext(httpRequest);
        }
    }


    public static class PrivateResource {

        @SuppressWarnings("SameReturnValue")
        private HttpResponse method() {
            return null;
        }
    }


    @Path("/path")
    public static class SimpleResource {
        @GET
        @Path("/list")
        @Produces(MediaType.TEXT_PLAIN)
        public HttpResponse list() {
            HttpResponse response = new HttpResponse(200);
            response.write("ok");
            return response;
        }
    }

    @Path("/path")
    public interface ResourceInterface {
        @GET
        @Path("/list")
        @Produces(MediaType.APPLICATION_JSON)
        HttpResponse list();
    }

    public interface PlainInterface {
    }

    public static abstract class ParentPlainClass {
    }

    public static abstract class ParentPlainExtendsClass extends ParentPlainClass {
    }

    @Path("/path2")
    public static abstract class ParentResource {
        @GET
        @Path("/list2")
        @Produces(MediaType.APPLICATION_JSON)
        public HttpResponse list() {
            HttpResponse response = new HttpResponse(200);
            response.write("ok");
            return response;
        }
    }

    public static abstract class ParentResourceExtendsClass extends ParentResource {
    }

    public static class ResourceImpl implements ResourceInterface {
        @Override
        public HttpResponse list() {
            HttpResponse response = new HttpResponse(200);
            response.write("ok");
            return response;
        }
    }

    public static class ResourceExtendsAndImplements extends ParentPlainExtendsClass implements ResourceInterface {
        @Override
        public HttpResponse list() {
            HttpResponse response = new HttpResponse(200);
            response.write("ok");
            return response;
        }
    }

    public static class ResourceExtendsAndImplements2 extends ParentResource implements ResourceInterface {
    }

    public static class ResourceExtendsGrandParentResourceAndIMplementsPlainInterface extends ParentResourceExtendsClass implements PlainInterface {
    }

    @Path("/path")
    public interface ResourceDefaultMethodInterface {
        @GET
        @Path("/list")
        @Produces(MediaType.APPLICATION_JSON)
        default HttpResponse list() {
            HttpResponse response = new HttpResponse(200);
            response.write("ok");
            return response;
        }
    }

    public static class ResourceInheritDefaultMethod implements ResourceDefaultMethodInterface {
    }

    public static class ResourceOverrideDefaultMethod implements ResourceDefaultMethodInterface {
        @Override
        public HttpResponse list() {
            HttpResponse response = new HttpResponse(200);
            response.write("test");
            return response;
        }
    }
}

