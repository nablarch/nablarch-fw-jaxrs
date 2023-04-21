package nablarch.fw.jaxrs;

import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.NablarchHttpServletRequestWrapper;
import nablarch.fw.web.servlet.ServletExecutionContext;
import nablarch.test.support.log.app.OnMemoryLogWriter;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockedStatic;

import java.nio.charset.UnsupportedCharsetException;

import static nablarch.fw.jaxrs.HttpResponseMatcher.isStatusCode;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private final HttpRequest mockRequest = mock(HttpRequest.class);

    private final NablarchHttpServletRequestWrapper mockServletRequest = mock(NablarchHttpServletRequestWrapper.class);

    private final ServletExecutionContext mockContext = mock(ServletExecutionContext.class);

    private final JaxRsContext mockJaxRsContext = mock(JaxRsContext.class);
    
    private MockedStatic<JaxRsContext> jaxRsContextMockStatic;

    @Before
    public void setUp() throws Exception {
        OnMemoryLogWriter.clear();
        jaxRsContextMockStatic = mockStatic(JaxRsContext.class);
        jaxRsContextMockStatic.when(() -> JaxRsContext.get(mockContext)).thenReturn(mockJaxRsContext);
        
        when(mockContext.getServletRequest()).thenReturn(mockServletRequest);
    }

    @After
    public void tearDown() {
        OnMemoryLogWriter.clear();
        jaxRsContextMockStatic.close();
    }

    /**
     * 実装クラスで変換されたオブジェクトが返されること。
     */
    @Test
    public void readSuccess_shouldReturnObjectConvertedInImplementationClass() throws Exception {
        doReturn(TestBean.class).when(mockJaxRsContext).getRequestClass();
        when(mockJaxRsContext.getConsumesMediaType()).thenReturn("application/json");

        String result = (String) sut.read(mockRequest, mockContext);
        assertThat(result, is("read object"));

        // デフォルトのcharsetが ServletRequest
        verify(mockServletRequest).setCharacterEncoding("UTF-8");
    }

    /**
     * リソースメソッドの引数にBeanが存在しない場合例外が送出されること。
     */
    @Test
    public void resourceMethodNotHaveBeanParameter_shouldThrowException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("consumes media type and resource method signature is mismatch.");

        when(mockJaxRsContext.getRequestClass()).thenReturn(null);
        sut.read(mockRequest, mockContext);
    }

    /**
     * クライアントからのCharsetがサポートされていないものの場合{@link HttpErrorResponse}が送出されること。
     */
    @Test
    public void unsupportedCharset_shouldThrowException() throws Exception {
        doReturn(TestBean.class).when(mockJaxRsContext).getRequestClass();
        when(mockJaxRsContext.getConsumesMediaType()).thenReturn("application/json");
        when(mockServletRequest.getCharacterEncoding()).thenReturn("test");
        
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
        when(mockJaxRsContext.getProducesMediaType()).thenReturn("application/json");

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
        when(mockJaxRsContext.getProducesMediaType()).thenReturn("application/json");

        sut.write(new HttpResponse(), mockContext);
    }

    /**
     * デフォルトのエンコーディングを変更した場合、{@link jakarta.servlet.ServletRequest#setCharacterEncoding(String)}に
     * そのエンコーディングが指定されて呼び出されること。
     */
    @Test
    public void changeCharset_shouldChangeCharsetOfServletRequest() throws Exception {
        when(mockJaxRsContext.getConsumesMediaType()).thenReturn("application/json");
        doReturn(TestBean.class).when(mockJaxRsContext).getRequestClass();

        sut.setDefaultEncoding("Windows-31j");

        String result = (String) sut.read(mockRequest, mockContext);
        assertThat(result, is("read object"));

        verify(mockServletRequest).setCharacterEncoding("windows-31j");
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
