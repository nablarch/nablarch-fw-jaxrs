package nablarch.fw.jaxrs;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.PropertyException;
import jakarta.xml.bind.UnmarshalException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.hamcrest.CoreMatchers;

import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.NablarchHttpServletRequestWrapper;
import nablarch.fw.web.servlet.ServletExecutionContext;
import nablarch.test.support.log.app.OnMemoryLogWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mockit.Expectations;
import mockit.Mocked;

/**
 * {@link JaxbBodyConverter}のテストクラス。
 */
@Ignore("jacoco と jmockit が競合してエラーになるため")
public class JaxbBodyConverterTest {

    /** テスト対象 */
    JaxbBodyConverter sut = new JaxbBodyConverter();

    @Mocked
    public HttpRequest request;

    @Mocked
    public ServletExecutionContext executionContext;

    @Mocked
    public JaxRsContext jaxRsContext;

    @Mocked
    protected NablarchHttpServletRequestWrapper servletRequest;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        OnMemoryLogWriter.clear();
    }

    @After
    public void tearDown() {
        OnMemoryLogWriter.clear();
    }

    /**
     * デフォルト設定でリクエストを読み込むケース。
     *
     * @throws Exception
     */
    @Test
    public void test_read() throws Exception {

        final String xml =
                "<?xml version=\"1.0\"?>"
                + "<person>"
                + "  <age>12</age>"
                + "  <name>山田太郎</name>"
                + "</person>";

        new Expectations() {{
            jaxRsContext.getRequestClass();
            result = Person.class;
            minTimes = 0;
            jaxRsContext.getConsumesMediaType();
            result = "application/xml";
            minTimes = 0;
            servletRequest.getReader();
            result = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(xml.getBytes("utf-8")), "utf-8"));
            minTimes = 0;
        }};

        Person person = (Person) sut.read(request, executionContext);

        assertThat(person.getAge(), is(12));
        assertThat(person.getName(), is("山田太郎"));
    }

    /**
     * 文字コードに"windows-31j"を指定してリクエストを読み込むケース。
     *
     * @throws Exception
     */
    @Test
    public void test_read_windows31j() throws Exception {

        final String xml = "<?xml version=\"1.0\"?><person><age>34</age><name>山田花子</name></person>";

        new Expectations() {{
            jaxRsContext.getRequestClass();
            result = Person.class;
            minTimes = 0;
            jaxRsContext.getConsumesMediaType();
            result = "application/xml";
            minTimes = 0;
            servletRequest.getCharacterEncoding();
            result = "windows-31j";
            minTimes = 0;
            servletRequest.getReader();
            result = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(xml.getBytes("windows-31j")), "windows-31j"));
            minTimes = 0;
        }};

        Person person = (Person) sut.read(request, executionContext);

        assertThat(person.getAge(), is(34));
        assertThat(person.getName(), is("山田花子"));
    }

    /**
     * リクエストの読み込みに失敗するケース。
     *
     * @throws Exception
     */
    @Test
    public void test_read_failed() throws Exception {
        new Expectations() {{
            jaxRsContext.getRequestClass();
            result = Person.class;
            jaxRsContext.getConsumesMediaType();
            result = "application/xml";
            servletRequest.getReader();
            result = new BufferedReader(new InputStreamReader(new ByteArrayInputStream("failed.".getBytes("utf-8")), "utf-8"));
        }};

        try {
            sut.read(request, executionContext);
            fail("XMLのフォーマットと一致しないためエラーが発生。");
        } catch (HttpErrorResponse e) {
            assertThat(e.getResponse().getStatusCode(), is(400));
            assertThat(e.getCause(), instanceOf(UnmarshalException.class));
        }

        new Expectations() {{
            jaxRsContext.getRequestClass();
            result = Person.class;
            minTimes = 0;
            jaxRsContext.getConsumesMediaType();
            result = "application/xml";
            minTimes = 0;
            servletRequest.getReader();
            result = new IOException();
            minTimes = 0;
        }};

        try {
            sut.read(request, executionContext);
            fail("リクエストの読み込みに失敗してエラーが発生。");
        } catch (HttpErrorResponse e) {
            assertThat(e.getResponse().getStatusCode(), is(400));
            assertThat(e.getCause(), instanceOf(IOException.class));

            OnMemoryLogWriter.assertLogContains("writer.memory", "failed to read request. cause = [null]");

        }
    }

    /**
     * オプション設定を行ってリクエストの読み込みを行うケース。
     * <p/>
     * 未定義のオプションを設定し、例外が発生。
     * (現状、XML→Bean変換時に設定可能なオプションが存在しないため、オプション設定に失敗した場合のみ検証。)
     *
     * @throws Exception
     */
    @Test
    public void test_read_configure_failed() throws Exception {

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("failed to configure Unmarshaller."));
        expectedException.expectCause(CoreMatchers.<Throwable>instanceOf(JAXBException.class));

        final String xml =
                "<?xml version=\"1.0\"?>"
                        + "<person>"
                        + "  <age>12</age>"
                        + "  <name>山田太郎</name>"
                        + "</person>";

        new Expectations() {{
            jaxRsContext.getRequestClass();
            result = Person.class;
            jaxRsContext.getConsumesMediaType();
            result = "application/xml";
            servletRequest.getReader();
            result = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(xml.getBytes("utf-8")), "utf-8"));
        }};

        JaxbBodyConverter sut = new JaxbBodyConverter() {
            @Override
            protected void configure(Unmarshaller unmarshaller) throws JAXBException {
                unmarshaller.setProperty("test", "test");
            }
        };

        sut.read(request, executionContext);
    }

    /**
     * デフォルト設定でレスポンスの書き込みを行うケース。
     *
     * @throws Exception
     */
    @Test
    public void test_write() throws Exception {

        new Expectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/xml;charset=utf-8";
        }};

        HttpResponse response = sut.write(new Person(12, "山田太郎"), executionContext);

        final String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<person>\n" +
                "    <age>12</age>\n" +
                "    <name>山田太郎</name>\n" +
                "</person>\n";

        assertThat(response.getStatusCode(), is(200));
        assertThat(response.getContentType(), is("application/xml;charset=utf-8"));
        assertThat(response.getContentLength(), is("123"));
        assertThat(response.getBodyString(), is(xml));
    }

    /**
     * 文字コードに"windows-31j"を指定してレスポンスの書き込みを行うケース。
     *
     * @throws Exception
     */
    @Test
    public void test_write_windows31j() throws Exception {

        new Expectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/xml;charset=windows-31j";
        }};

        HttpResponse response = sut.write(new Person(34, "山田花子"), executionContext);

        final String xml =
                "<?xml version=\"1.0\" encoding=\"windows-31j\" standalone=\"yes\"?>\n" +
                        "<person>\n" +
                        "    <age>34</age>\n" +
                        "    <name>山田花子</name>\n" +
                        "</person>\n";

        assertThat(response.getStatusCode(), is(200));
        assertThat(response.getContentType(), is("application/xml;charset=windows-31j"));
        assertThat(response.getContentLength(), is("125"));
        assertThat(response.getBodyString(), is(xml));
    }

    /**
     * レスポンスの書き込みに失敗するケース。
     *
     * @throws Exception
     */
    @Test
    public void test_write_failed() throws Exception {

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("failed to write response."));
        expectedException.expectCause(CoreMatchers.<Throwable>instanceOf(JAXBException.class));

        new Expectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/xml;charset=windows-31j";
        }};

        sut.write("test", executionContext);
    }

    /**
     * オプションを設定してレスポンスの書き込みを行うケース。
     * <p/>
     * フォーマットせずにXMLを出力するようにオプションを設定。
     *
     * @throws Exception
     */
    @Test
    public void test_write_configure_success() throws Exception {

        new Expectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/xml";
        }};

        JaxbBodyConverter sut = new JaxbBodyConverter() {
            @Override
            protected void configure(Marshaller marshaller) throws JAXBException {
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            }
        };
        HttpResponse response = sut.write(new Person(12, "山田太郎"), executionContext);

        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><person><age>12</age><name>山田太郎</name></person>";

        assertThat(response.getStatusCode(), is(200));
        assertThat(response.getContentType(), is("application/xml;charset=UTF-8"));
        assertThat(response.getContentLength(), is("110"));
        assertThat(response.getBodyString(), is(xml));
    }

    /**
     * オプションを設定してレスポンスの書き込みを行うケース。
     * <p/>
     * 未定義のオプションを設定し、例外が発生。
     *
     * @throws Exception
     */
    @Test
    public void test_write_configure_failed() throws Exception {

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("failed to configure Marshaller."));
        expectedException.expectCause(CoreMatchers.<Throwable>instanceOf(PropertyException.class));

        new Expectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/xml";
        }};

        JaxbBodyConverter sut = new JaxbBodyConverter() {
            @Override
            protected void configure(Marshaller marshaller) throws JAXBException {
                marshaller.setProperty("test", "test");
            }
        };
        sut.write(new Person(12, "山田太郎"), executionContext);
    }

    /**
     * メディアタイプが変換可能なケース。
     *
     * @throws Exception
     */
    @Test
    public void test_isConvertible() throws Exception {
        assertThat("メディアタイプがxmlであれば変換可能であること", sut.isConvertible("application/xml"), is(true));
        assertThat("前方一致であれば変換可能であること", sut.isConvertible("application/xml;charset=UTF-8"), is(true));
        assertThat("大文字、小文字を問わないこと", sut.isConvertible("Application/XML"), is(true));
    }

    /**
     * メディアタイプが変換不可能なケース。
     *
     * @throws Exception
     */
    @Test
    public void test_isNotConvertible() throws Exception {
        assertThat("メディアタイプがxmlでなければ変換不可であること", sut.isConvertible("application/json"), is(false));
    }

    /**
     * テスト用のBeanクラス。
     */
    @XmlRootElement
    public static class Person {
        private Integer age;
        private String name;

        public Person() {
        }

        public Person(Integer age, String name) {
            this.age = age;
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}