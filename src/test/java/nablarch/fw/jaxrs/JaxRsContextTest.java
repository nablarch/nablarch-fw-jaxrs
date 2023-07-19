package nablarch.fw.jaxrs;

import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import org.junit.Test;

import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link JaxRsContext}のテスト。
 */
public class JaxRsContextTest {

    /**
     * リソースパスの情報が取得できること
     */
    @Test
    public void toResourcePath() throws Exception {
        JaxRsContext jaxRsContext = new JaxRsContext(TestAction.class.getMethod("nothing"));
        assertThat(jaxRsContext.toResourcePath(), is(TestAction.class.getName() + "#nothing"));
    }

    /**
     * コンテキストの設定/取得ができること。
     */
    @Test
    public void getContextAndSetContext() {

        ExecutionContext context = new ExecutionContext();
        assertThat(JaxRsContext.get(context), is(nullValue()));

        JaxRsContext jaxRsContext = new JaxRsContext(null);
        JaxRsContext.set(context, jaxRsContext);
        assertThat(JaxRsContext.get(context), is(jaxRsContext));
    }

    /**
     * リクエストメソッドの引数の型の設定/取得ができること
     */
    @Test
    public void getRequestClass() throws Exception {
        JaxRsContext jaxRsContext = new JaxRsContext(TestAction.class.getMethod("nothing"));
        assertThat(jaxRsContext.getRequestClass(), is(nullValue()));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("bean", TestBean1.class));
        assertThat(jaxRsContext.getRequestClass(), typeCompatibleWith(TestBean1.class));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("request", HttpRequest.class, TestBean1.class));
        assertThat(jaxRsContext.getRequestClass(), typeCompatibleWith(TestBean1.class));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("context", HttpRequest.class, ExecutionContext.class, TestBean2.class));
        assertThat(jaxRsContext.getRequestClass(), typeCompatibleWith(TestBean2.class));
    }

    /**
     * Consumesメディアタイプの設定/取得ができること
     */
    @Test
    public void getConsumesMediaType() throws Exception {

        JaxRsContext jaxRsContext = new JaxRsContext(TestAction.class.getMethod("emptyConsumes"));
        assertThat(jaxRsContext.getConsumesMediaType(), is(nullValue()));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("oneValueConsumes"));
        assertThat(jaxRsContext.getConsumesMediaType(), is("application/json"));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("multiValuesConsumes"));
        assertThat(jaxRsContext.getConsumesMediaType(), is("application/xml"));
    }

    /**
     * Producesメディアタイプの設定/取得ができること
     */
    @Test
    public void getProducesMediaType() throws Exception {

        JaxRsContext jaxRsContext = new JaxRsContext(TestAction.class.getMethod("emptyProduces"));
        assertThat(jaxRsContext.getProducesMediaType(), is(nullValue()));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("oneValueProduces"));
        assertThat(jaxRsContext.getProducesMediaType(), is("application/xml"));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("multiValuesProduces"));
        assertThat(jaxRsContext.getProducesMediaType(), is("application/json"));
    }

    /**
     * リクエストメソッドに{@link Valid}アノテーションが設定されているか判定できること。
     */
    @Test
    public void checkValidAnnotation() throws Exception {

        JaxRsContext jaxRsContext = new JaxRsContext(TestAction.class.getMethod("nothing"));
        assertThat(jaxRsContext.hasValidAnnotation(), is(false));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("validAndConvertGroups"));
        assertThat(jaxRsContext.hasValidAnnotation(), is(true));
    }

    /**
     * リクエストメソッドに{@link ConvertGroup}アノテーションが設定されているか判定できること。
     */
    @Test
    public void checkConvertGroupAnnotation() throws Exception {

        JaxRsContext jaxRsContext = new JaxRsContext(TestAction.class.getMethod("nothing"));
        assertThat(jaxRsContext.hasConvertGroupAnnotation(), is(false));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("validAndConvertGroups"));
        assertThat(jaxRsContext.hasConvertGroupAnnotation(), is(true));
    }

    /**
     * リクエストメソッド設定した{@link ConvertGroup}アノテーションから、{@code from}属性が取得できること
     */
    @Test
    public void getFromAttributeOfConvertGroupAnnotation() throws Exception {

        JaxRsContext jaxRsContext = new JaxRsContext(TestAction.class.getMethod("nothing"));
        assertThat(jaxRsContext.getFromOfConvertGroupAnnotation(), is(nullValue()));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("validAndConvertGroups"));
        assertThat(jaxRsContext.getFromOfConvertGroupAnnotation(), typeCompatibleWith(Default.class));
    }

    /**
     * リクエストメソッド設定した{@link ConvertGroup}アノテーションから、{@code to}属性が取得できること
     */
    @Test
    public void getToAttributeOfConvertGroupAnnotation() throws Exception {

        JaxRsContext jaxRsContext = new JaxRsContext(TestAction.class.getMethod("nothing"));
        assertThat(jaxRsContext.getToOfConvertGroupAnnotation(), is(nullValue()));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("validAndConvertGroups"));
        assertThat(jaxRsContext.getToOfConvertGroupAnnotation(), typeCompatibleWith(TestAction.Test1.class));
    }

    public static class TestAction {

        public HttpResponse nothing() {
            return new HttpResponse();
        }

        public HttpResponse bean(TestBean1 bean) {
            return new HttpResponse();
        }

        public HttpResponse request(HttpRequest req, TestBean1 bean) {
            return new HttpResponse();
        }

        public HttpResponse context(HttpRequest req, ExecutionContext ctx, TestBean2 bean) {
            return new HttpResponse();
        }

        @Consumes("")
        public HttpResponse emptyConsumes() {
            return new HttpResponse();
        }

        @Consumes("application/json")
        public HttpResponse oneValueConsumes() {
            return new HttpResponse();
        }

        @Consumes({"application/xml", "application/json"})
        public HttpResponse multiValuesConsumes() {
            return new HttpResponse();
        }

        @Produces("")
        public HttpResponse emptyProduces() {
            return new HttpResponse();
        }

        @Produces("application/xml")
        public HttpResponse oneValueProduces() {
            return new HttpResponse();
        }

        @Produces({"application/json", "application/xml"})
        public HttpResponse multiValuesProduces() {
            return new HttpResponse();
        }

        @Valid
        @ConvertGroup(from = Default.class, to = Test1.class)
        public HttpResponse validAndConvertGroups() {
            return new HttpResponse();
        }

        public interface Test1{}
    }

    public static class TestBean1 {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class TestBean2 {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
