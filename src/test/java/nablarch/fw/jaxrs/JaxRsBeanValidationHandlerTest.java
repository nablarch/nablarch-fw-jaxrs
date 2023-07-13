package nablarch.fw.jaxrs;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.MessageInterpolator;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;

import nablarch.core.message.ApplicationException;
import nablarch.core.message.Message;
import nablarch.core.repository.SystemRepository;
import nablarch.core.validation.ValidationResultMessage;
import nablarch.core.validation.ee.Digits;
import nablarch.core.validation.ee.Domain;
import nablarch.core.validation.ee.DomainManager;
import nablarch.core.validation.ee.Length;
import nablarch.core.validation.ee.MultiLanguageMessageInterpolator;
import nablarch.core.validation.ee.Required;
import nablarch.core.validation.ee.ValidatorFactoryBuilder;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.web.HttpRequest;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import mockit.Injectable;

/**
 * {@link JaxRsBeanValidationHandler}のテストクラス。
 */
public class JaxRsBeanValidationHandlerTest {

    /** テスト対象 */
    private final JaxRsBeanValidationHandler sut = new JaxRsBeanValidationHandler();

    /** mock http request */
    @Injectable
    private HttpRequest mockRequest;

    @ClassRule
    public static SystemRepositoryResource resource = new SystemRepositoryResource(
            "nablarch/fw/jaxrs/JaxRsBeanValidationHandler.xml");

    /**
     * {@link Valid}アノテーションが存在しない場合、バリデーションは実行されないこと。
     */
    @Test
    public void testWithoutValidAnnotation() throws Exception {
        // ---------------------------------------- setup
        Method method = TestResource.class.getMethod("withoutValid");
        JaxRsContext jaxRsContext = new JaxRsContext(method);
        jaxRsContext.setRequest(new Person(1L, null));      // バリデーションエラーとなるオブジェクトを生成

        ExecutionContext context = new ExecutionContext();
        JaxRsContext.set(context, jaxRsContext);

        Handler<Object, Object> handler = new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                return "ok";
            }
        };
        context.addHandler(handler);

        // ---------------------------------------- execute
        String result = (String) sut.handle(mockRequest, context);

        // ---------------------------------------- assert
        assertThat("", result, is("ok"));
    }

    /**
     * {@link Valid}アノテーションが設定されていてエラーが発生しない場合、後続のハンドラが呼び出されること。
     */
    @Test
    public void testWithValidAnnotation_ValidationSuccess() throws Exception {
        // ---------------------------------------- setup
        Method method = TestResource.class.getMethod("withValid");
        JaxRsContext jaxRsContext = new JaxRsContext(method);
        jaxRsContext.setRequest(new Person(2L, "name"));

        ExecutionContext context = new ExecutionContext();
        JaxRsContext.set(context, jaxRsContext);

        Handler<Object, String> handler = new Handler<Object, String>() {
            @Override
            public String handle(Object o, ExecutionContext context) {
                return "ok";
            }
        };
        context.addHandler(handler);

        // ---------------------------------------- execute
        String result = (String) sut.handle(mockRequest, context);

        // ---------------------------------------- assert
        assertThat("バリデーションエラーは発生しないので後続のハンドラの結果が戻されること", result, is("ok"));
    }

    /**
     * {@link Valid}アノテーションと{@link ConvertGroup}アノテーションが設定されていてエラーが発生しない場合、後続のハンドラが呼び出されること。
     * デフォルトグループで検証される{@link Groups#id}は検証スキップされる。
     */
    @Test
    public void testWithValidAnnotation_ValidationSuccess_WithGroup() throws Exception {
        // ---------------------------------------- setup
        Method method = TestResource.class.getMethod("withValidAndGroup");
        JaxRsContext jaxRsContext = new JaxRsContext(method);
        jaxRsContext.setRequest(new Groups("123456", "hogehoge"));

        ExecutionContext context = new ExecutionContext();
        JaxRsContext.set(context, jaxRsContext);

        Handler<Object, String> handler = new Handler<Object, String>() {
            @Override
            public String handle(Object o, ExecutionContext context) {
                return "ok";
            }
        };
        context.addHandler(handler);

        // ---------------------------------------- execute
        String result = (String) sut.handle(mockRequest, context);

        // ---------------------------------------- assert
        assertThat("バリデーションエラーは発生しないので後続のハンドラの結果が戻されること", result, is("ok"));
    }

    /**
     * {@link Valid}アノテーションが設定されていてエラーが発生しない場合、後続のハンドラが呼び出されること。
     * {@link Groups.Test1}グループで検証される{@link Groups#name}は検証スキップされる。
     */
    @Test
    public void testWithValidAnnotation_ValidationSuccess_WithoutGroup() throws Exception {
        // ---------------------------------------- setup
        Method method = TestResource.class.getMethod("withValid");
        JaxRsContext jaxRsContext = new JaxRsContext(method);
        jaxRsContext.setRequest(new Groups("1234", "hoge"));

        ExecutionContext context = new ExecutionContext();
        JaxRsContext.set(context, jaxRsContext);

        Handler<Object, String> handler = new Handler<Object, String>() {
            @Override
            public String handle(Object o, ExecutionContext context) {
                return "ok";
            }
        };
        context.addHandler(handler);

        // ---------------------------------------- execute
        String result = (String) sut.handle(mockRequest, context);

        // ---------------------------------------- assert
        assertThat("バリデーションエラーは発生しないので後続のハンドラの結果が戻されること", result, is("ok"));
    }

    /**
     * {@link Valid}アノテーションと{@link ConvertGroup}アノテーションが設定されていてエラーがする場合、{@link ApplicationException}が送出されること。
     */
    @Test
    public void testWithValidAnnotation_ValidationError_WithGroup() throws Exception {
        // ---------------------------------------- setup
        Method method = TestResource.class.getMethod("withValidAndGroup");
        JaxRsContext jaxRsContext = new JaxRsContext(method);
        jaxRsContext.setRequest(new Groups("1234", "hoge"));

        ExecutionContext context = new ExecutionContext();
        JaxRsContext.set(context, jaxRsContext);

        Handler<Object, String> handler = new Handler<Object, String>() {
            @Override
            public String handle(Object o, ExecutionContext context) {
                return "ok";
            }
        };
        context.addHandler(handler);

        // ---------------------------------------- execute
        System.out.println(SystemRepository.get("messageInterpolator"));
        try {
            sut.handle(mockRequest, context);
            Assert.fail("とおらない");
        } catch (ApplicationException e) {
            List<Message> messages = e.getMessages();
            assertThat("エラーは1件", messages, hasSize(1));
            ValidationResultMessage message = (ValidationResultMessage) messages.get(0);
            assertThat("エラーが発生したのはnameプロパティ", message.getPropertyName(), is("name"));
            assertThat("エラー内容は文字列長に関するもの", message.formatMessage(), is("文字列は固定長です"));
        }
    }

    /**
     * {@link Valid}アノテーションが設定されているが、リクエストオブジェクトがnullの場合
     * 予期せぬエラーなど発生せずに後続のハンドラが呼び出されること。
     */
    @Test
    public void testWithValidAnnotation_ObjectIsNull() throws Exception {
        // ---------------------------------------- setup
        Method method = TestResource.class.getMethod("withValid");
        JaxRsContext jaxRsContext = new JaxRsContext(method);
        jaxRsContext.setRequest(null);

        ExecutionContext context = new ExecutionContext();
        JaxRsContext.set(context, jaxRsContext);

        Handler<Object, String> handler = new Handler<Object, String>() {
            @Override
            public String handle(Object o, ExecutionContext context) {
                return "ok";
            }
        };
        context.addHandler(handler);

        // ---------------------------------------- execute
        String result = (String) sut.handle(mockRequest, context);

        // ---------------------------------------- assert
        assertThat("後続のハンドラの結果が戻されること", result, is("ok"));
    }

    /**
     * {@link Valid}アノテーションが設定されていてエラーが発生する場合、{@link nablarch.core.message.ApplicationException}が送出されること。
     */
    @Test
    public void testWithValidAnnotation_ValidationError() throws Exception {
        // ---------------------------------------- setup
        Method method = TestResource.class.getMethod("withValid");
        JaxRsContext jaxRsContext = new JaxRsContext(method);
        jaxRsContext.setRequest(new Person(3L, ""));

        ExecutionContext context = new ExecutionContext();
        JaxRsContext.set(context, jaxRsContext);

        // ---------------------------------------- execute
        System.out.println(SystemRepository.get("messageInterpolator"));
        try {
            sut.handle(mockRequest, context);
            Assert.fail("とおらない");
        } catch (ApplicationException e) {
            List<Message> messages = e.getMessages();
            assertThat("エラーは1件", messages, hasSize(1));
            ValidationResultMessage message = (ValidationResultMessage) messages.get(0);
            assertThat("エラーが発生したのはnameプロパティ", message.getPropertyName(), is("name"));
            assertThat("エラー内容は必須に関するもの", message.formatMessage(), is("必須の項目です"));
        }
    }

    /**
     * テスト用のリソースクラス
     */
    private static class TestResource {

        public void withoutValid() {
        }

        @Valid
        public void withValid() {
        }

        @Valid
        @ConvertGroup(from = Default.class, to = Groups.Test1.class)
        public void withValidAndGroup(){}

    }

    /**
     * テスト用のデータクラス
     */
    public static class Person {

        private final Long id;

        private final String name;

        public Person(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Domain("id")
        @Required
        public Long getId() {
            return id;
        }

        @Domain("name")
        @Required
        public String getName() {
            return name;
        }

    }

    public static class Bean {

        @Digits(integer = 15)
        Long id;

        @Length(max = 5)
        String name;

        public static class Manager implements DomainManager<Bean> {

            @Override
            public Class<Bean> getDomainBean() {
                return Bean.class;
            }
        }
    }

    public static class Groups {

        @Length(max = 4, min = 4)
        String id;

        @Length(max = 8, min = 8, groups = Test1.class)
        String name;

        public Groups(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public interface Test1{}

    }

    public static class CustomMessageInterpolator implements MessageInterpolator {

        private static Map<String, String> messageTable = new HashMap<String, String>() {{
            put("{nablarch.core.validation.ee.Required.message}", "必須の項目です");
            put("{nablarch.core.validation.ee.Length.fixed.message}", "文字列は固定長です");
        }};

        @Override
        public String interpolate(String messageTemplate, Context context) {
            System.out.println(
                    "----------------------------------------------------------------------------------------------------");
            return interpolate(messageTemplate, context, Locale.getDefault());
        }

        @Override
        public String interpolate(String messageTemplate, Context context, Locale locale) {
            System.out.println(
                    "----------------------------------------------------------------------------------------------------" + messageTemplate);
            return messageTable.get(messageTemplate);
        }
    }
    
    public static class CustomValidationFactoryBuilder extends ValidatorFactoryBuilder {
        @Override
        protected ValidatorFactory build() {
            return Validation.byDefaultProvider()
                             .configure()
                             .messageInterpolator(new MultiLanguageMessageInterpolator())
                             .buildValidatorFactory();
        }
    }
}
