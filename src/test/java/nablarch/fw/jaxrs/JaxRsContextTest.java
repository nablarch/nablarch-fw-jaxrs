package nablarch.fw.jaxrs;

import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
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

    @Test
    public void getConsumesMediaType() throws Exception {

        JaxRsContext jaxRsContext = new JaxRsContext(TestAction.class.getMethod("emptyConsumes"));
        assertThat(jaxRsContext.getConsumesMediaType(), is(nullValue()));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("oneValueConsumes"));
        assertThat(jaxRsContext.getConsumesMediaType(), is("application/json"));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("multiValuesConsumes"));
        assertThat(jaxRsContext.getConsumesMediaType(), is("application/xml"));
    }

    @Test
    public void getProducesMediaType() throws Exception {

        JaxRsContext jaxRsContext = new JaxRsContext(TestAction.class.getMethod("emptyProduces"));
        assertThat(jaxRsContext.getProducesMediaType(), is(nullValue()));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("oneValueProduces"));
        assertThat(jaxRsContext.getProducesMediaType(), is("application/xml"));

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("multiValuesProduces"));
        assertThat(jaxRsContext.getProducesMediaType(), is("application/json"));
    }

    @Test
    public void getValidationAnnotation() throws Exception {

        JaxRsContext jaxRsContext = new JaxRsContext(TestAction.class.getMethod("nothing"));
        assertThat(jaxRsContext.hasValidAnnotation(), is(false));
        assertThat(jaxRsContext.hasConvertGroupAnnotation(), is(false));
        try {
            jaxRsContext.getToAttributesOfConvertGroupAnnotation();
            fail("IllegalStateExceptionが送出されるはず");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("ConvertGroup annotation is not set for the resource method."));
        }

        jaxRsContext = new JaxRsContext(TestAction.class.getMethod("validAndConvertGroups"));
        assertThat(jaxRsContext.hasValidAnnotation(), is(true));
        assertThat(jaxRsContext.hasConvertGroupAnnotation(), is(true));
        assertThat(jaxRsContext.getToAttributesOfConvertGroupAnnotation(), ClassMatcher.isClassOf(TestAction.Test1.class));
    }

    private static class ClassMatcher extends BaseMatcher<Class<?>> {

        private final Class<?> expected;
        private Class<?> actual;

        private ClassMatcher(Class<?> expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object actual) {

            if (!(actual instanceof Class<?>)) {
                return false;
            }

            this.actual = (Class<?>)actual;

            return this.actual.equals(this.expected);

        }

        @Override
        public void describeTo(Description description) {
            description.appendText("is  ");
            description.appendValue(expected);
        }

        public static ClassMatcher isClassOf(Class<?> clazz) {
            return new ClassMatcher(clazz);
        }

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
