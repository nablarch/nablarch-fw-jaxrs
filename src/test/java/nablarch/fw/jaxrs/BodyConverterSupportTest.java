package nablarch.fw.jaxrs;

import static nablarch.fw.jaxrs.HttpResponseMatcher.isStatusCode;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.nio.charset.UnsupportedCharsetException;

import org.hamcrest.CoreMatchers;

import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.NablarchHttpServletRequestWrapper;
import nablarch.fw.web.servlet.ServletExecutionContext;
import nablarch.test.support.log.app.OnMemoryLogWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;

/**
 * {@link BodyConverterSupport}のテストクラス。
 */
public class BodyConverterSupportTest {

    /**
     * テスト用の実装クラス。
     */
    public static class Impl extends BodyConverterSupport {

        @Override
        public boolean isConvertible(String mediaType) {
            return true;
        }

        @Override
        protected Object convertRequest(HttpRequest request, ExecutionContext context) {
            return "read object";
        }

        @Override
        protected HttpResponse convertResponse(Object response, ExecutionContext context) {
            return new HttpResponse(200).write(response.toString());
        }
    }

    private BodyConverterSupport sut = new Impl();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mocked
    private HttpRequest mockRequest;

    @Mocked
    private NablarchHttpServletRequestWrapper mockServletRequest;

    @Mocked
    private ServletExecutionContext mockContext;

    @Mocked
    private JaxRsContext mockJaxRsContext;

    @Before
    public void setUp() throws Exception {
        OnMemoryLogWriter.clear();
    }

    @After
    public void tearDown() {
        OnMemoryLogWriter.clear();
    }

    /**
     * 実装クラスで変換されたオブジェクトが返されること。
     */
    @Test
    public void readSuccess_shouldReturnObjectConvertedInImplementationClass() throws Exception {
        new Expectations() {{
            mockJaxRsContext.getRequestClass();
            result = TestBean.class;
            minTimes = 0;
            mockJaxRsContext.getConsumesMediaType();
            result = "application/json";
            minTimes = 0;
        }};

        String result = (String) sut.read(mockRequest, mockContext);
        assertThat(result, is("read object"));

        // デフォルトのcharsetが ServletRequest
        new Verifications() {{
            mockServletRequest.setCharacterEncoding("UTF-8");
            times = 1;
        }};
    }

    /**
     * リソースメソッドの引数にBeanが存在しない場合例外が送出されること。
     */
    @Test
    public void resourceMethodNotHaveBeanParameter_shouldThrowException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("consumes media type and resource method signature is mismatch.");

        new Expectations() {{
            mockJaxRsContext.getRequestClass();
            result = null;
        }};
        sut.read(mockRequest, mockContext);
    }

    /**
     * クライアントからのCharsetがサポートされていないものの場合{@link HttpErrorResponse}が送出されること。
     */
    @Test
    public void unsupportedCharset_shouldThrowException() throws Exception {
            new Expectations() {{
                mockJaxRsContext.getRequestClass();
                result = TestBean.class;
                minTimes = 0;
                mockJaxRsContext.getConsumesMediaType();
                result = "application/json";
                minTimes = 0;
                mockServletRequest.getCharacterEncoding();
                result = "test";
                minTimes = 0;
            }};
        try {
            sut.read(mockRequest, mockContext);
            fail("例外がスローされるはず");
        }catch(HttpErrorResponse e){
            assertThat(e.getCause(), CoreMatchers.<Throwable>instanceOf(UnsupportedCharsetException.class));
            assertThat(e.getResponse().getStatusCode(), is(400));
            OnMemoryLogWriter.assertLogContains("writer.memory", "consumes charset is invalid. charset = [test]");

        }
    }

    /**
     * 実装クラスで変換されたオブジェクトが返されること。
     */
    @Test
    public void writeSuccess_shouldReturnObjectConvertedInImplementationClass() throws Exception {
        new Expectations() {{
            mockJaxRsContext.getProducesMediaType();
            result = "application/json";
            minTimes = 0;
        }};

        HttpResponse response = sut.write("response object", mockContext);
        assertThat(response, isStatusCode(200).withBody("response object"));
    }

    /**
     * {@link BodyConverterSupport#write(Object, ExecutionContext)}にHttpResponseを指定した場合、例外が発生すること。
     */
    @Test
    public void specifiesHttpResponseToWriteMethod_shouldThrowException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("produces media type and resource method signature is mismatch.");
        new Expectations() {{
            mockJaxRsContext.getProducesMediaType();
            result = "application/json";
        }};

        sut.write(new HttpResponse(), mockContext);
    }

    /**
     * デフォルトのエンコーディングを変更した場合、{@link javax.servlet.ServletRequest#setCharacterEncoding(String)}に
     * そのエンコーディングが指定されて呼び出されること。
     */
    @Test
    public void changeCharset_shouldChangeCharsetOfServletRequest() throws Exception {
        new Expectations() {{
            mockJaxRsContext.getConsumesMediaType();
            result = "application/json";
            minTimes = 0;
            mockJaxRsContext.getRequestClass();
            result = TestBean.class;
            minTimes = 0;
        }};

        sut.setDefaultEncoding("Windows-31j");

        String result = (String) sut.read(mockRequest, mockContext);
        assertThat(result, is("read object"));

        new Verifications() {{
            mockServletRequest.setCharacterEncoding("windows-31j");
            times = 1;
        }};
    }

    /**
     * 指定されたメディアタイプに応じてContent-Typeを取得できること。
     */
    @Test
    public void getContentType_shouldGetContentType() {

        BodyConverterSupport.ContentType contentType = sut.getContentType("application/json; charset=UTF-8");
        assertThat("charsetありのメディアタイプの場合、そのまま返されること",
                contentType.getValue(),
                is("application/json; charset=UTF-8"));
        assertThat(contentType.getEncoding().name(), is("UTF-8"));

        contentType = sut.getContentType("application/json");
        assertThat("charsetなしのメディアタイプの場合、デフォルトのエンコーディングがつくこと",
                contentType.getValue(),
                is("application/json;charset=UTF-8"));
        assertThat(contentType.getEncoding().name(), is("UTF-8"));

        sut.setDefaultEncoding("Windows-31j");

        contentType = sut.getContentType("application/json");
        assertThat("charsetなしのメディアタイプの場合、設定されたデフォルトのエンコーディングがつくこと",
                contentType.getValue(),
                is("application/json;charset=windows-31j"));
        assertThat(contentType.getEncoding().name(), is("windows-31j"));
    }

    /**
     * 指定されたメディアタイプの文字コードが不正な場合、実行時例外がスローされること。
     */
    @Test
    public void getContentType_shouldThrowRuntimeException() {
        try {
            sut.getContentType("application/json; charset=unknown");
            fail("例外がスローされるはず");
        } catch (IllegalArgumentException e) {
            assertThat(e.getCause(), instanceOf(UnsupportedCharsetException.class));
            assertThat(e.getMessage(), is("produces charset is invalid. charset = [unknown]"));
        }
    }

    private static class TestBean {

    }
}
