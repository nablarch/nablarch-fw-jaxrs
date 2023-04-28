package nablarch.fw.jaxrs;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import nablarch.common.web.WebConfig;
import nablarch.core.message.ApplicationException;
import nablarch.core.message.Message;
import nablarch.core.message.MessageLevel;
import nablarch.core.message.StringResource;
import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;
import nablarch.core.validation.ValidationResultMessage;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.ServletExecutionContext;
import nablarch.test.support.log.app.OnMemoryLogWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static nablarch.fw.jaxrs.HttpResponseMatcher.isStatusCode;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link JaxRsResponseHandler}のテストクラス。
 */
public class JaxRsResponseHandlerTest {

    /** テスト対象 */
    private JaxRsResponseHandler sut = new JaxRsResponseHandler();

    private final HttpServletRequest mockServletRequest = mock(HttpServletRequest.class, RETURNS_DEEP_STUBS);

    private final HttpServletResponse mockServletResponse = mock(HttpServletResponse.class);

    private final ServletOutputStream mockOutputStream = mock(ServletOutputStream.class);

    private final ServletContext mockServletContext = mock(ServletContext.class);

    private final HttpRequest mockHttpRequest = mock(HttpRequest.class);

    private ByteArrayOutputStream responseBody = new ByteArrayOutputStream();

    private ServletExecutionContext context;

    @Before
    public void setUp() throws Exception {
        SystemRepository.clear();
        OnMemoryLogWriter.clear();
        
        when(mockServletRequest.getContextPath()).thenReturn("dummy");
        when(mockServletRequest.getRequestURI()).thenReturn("dummy/users");

        when(mockServletResponse.getOutputStream()).thenReturn(mockOutputStream);

        doAnswer(context -> {
            byte[] b = context.getArgument(0);
            int offset = context.getArgument(1);
            int length = context.getArgument(2);
            responseBody.write(b, offset, length);
            return null;
        }).when(mockOutputStream).write(any(byte[].class), anyInt(), anyInt());
        
        context = new ServletExecutionContext(mockServletRequest, mockServletResponse, mockServletContext);
    }

    /**
     * ボディー部が空(ステータスコードのみ)のレスポンスのテスト。
     * <p/>
     * ボディ部が空で、指定したステータスコードのレスポンスが返ること
     */
    @Test
    public void testStatusCodeOnlyResponse() throws Exception {
        // -------------------------------------------------- setup
        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                return new HttpResponse(HttpResponse.Status.CREATED.getStatusCode());
            }
        });
        when(mockHttpRequest.getMethod()).thenReturn("GET");
        when(mockHttpRequest.getRequestUri()).thenReturn("/api/user");

        // -------------------------------------------------- execute
        HttpResponse response = sut.handle(mockHttpRequest, context);

        // -------------------------------------------------- assert
        assertThat("201でボディが空のHttpResponseが戻される", response, isStatusCode(201).withEmptyBody());
        assertThat("ServletOutputStreamに書き込まれたボティの長さも0であること",
                getBodyString(), is(""));
        // servlet responseに201ステータスコードが設定されていること
        verify(mockServletResponse, atLeastOnce()).setStatus(201);
    }

    /**
     * ボディを持たないレスポンスでもContent-Typeを設定する場合のテスト。
     * <p/>
     * HttpResponse#getContentTypeは、システムリポジトリ中のWebConfigオブジェクトにフラグが設定されている場合は、
     * ボディが空の状態でもContent-Typeが設定されていない場合にtext/plain;charset=UTF-8が設定されるようになっている。
     * text/plain;charset=UTF-8が設定されること。
     */
    @Test
    public void testStatusCodeOnlyResponseWithSetContentTypeForResponseWithNoBody() throws Exception {
        // -------------------------------------------------- setup
        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                return new HttpResponse(HttpResponse.Status.CREATED.getStatusCode());
            }
        });
        when(mockHttpRequest.getMethod()).thenReturn("GET");
        when(mockHttpRequest.getRequestUri()).thenReturn("/api/user");

        final WebConfig webConfig = new WebConfig();
        webConfig.setAddDefaultContentTypeForNoBodyResponse(true);
        SystemRepository.load(new ObjectLoader() {
            @Override
            public Map<String, Object> load() {
                final Map<String, Object> result = new HashMap<String, Object>();
                result.put("webConfig", webConfig);
                return result;
            }
        });

        // -------------------------------------------------- execute
        HttpResponse response = sut.handle(mockHttpRequest, context);

        // -------------------------------------------------- assert
        assertThat("201でボディが空のHttpResponseが戻される", response, isStatusCode(201).withEmptyBody());
        assertThat("ServletOutputStreamに書き込まれたボティの長さも0であること",
                getBodyString(), is(""));
        
        // servlet responseに201ステータスコードが設定されていること
        verify(mockServletResponse, atLeastOnce()).setStatus(201);
        verify(mockServletResponse, atLeastOnce()).setContentType("text/plain;charset=UTF-8");
    }

    /**
     * JSONレスポンスの書き込みができることを確認するテスト。
     */
    @Test
    public void testJsonResponse() throws Exception {
        // -------------------------------------------------- setup
        final String json = "{\"key\": 100}";

        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                HttpResponse response = new HttpResponse(200);
                response.setContentType("application/json; charset=utf-8");
                response.write(json);
                return response;
            }
        });

        // -------------------------------------------------- execute
        HttpResponse response = sut.handle(mockHttpRequest, context);

        // -------------------------------------------------- assert
        assertThat("201でボディが空のHttpResponseが戻される", response, isStatusCode(200).withBody(json));
        assertThat("ServletOutputStreamにHttpResponseのbodyが書き込まれていること",
                getBodyString(), is(json));
        
        // servlet responseに200のステータスコードが設定されていること
        verify(mockServletResponse, atLeastOnce()).setStatus(200);
        // Content-Length及びContent-Typeが設定されていること
        verify(mockServletResponse, atLeastOnce()).setContentLength(json.length());
        verify(mockServletResponse, atLeastOnce()).setContentType("application/json; charset=utf-8");
    }

    /**
     * 任意のヘッダーをレスポンスに設定した場合もその値が返却されること。
     */
    @Test
    public void testAddResponseHeader() throws Exception {
        // -------------------------------------------------- setup
        final String text = "hello!!";

        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                HttpResponse response = new HttpResponse(200);
                response.setContentType("text/plain");
                response.setHeader("option", "value");
                response.write(text);
                return response;
            }
        });

        // -------------------------------------------------- execute
        HttpResponse response = sut.handle(mockHttpRequest, context);

        // -------------------------------------------------- assert
        assertThat("200で書き込んだボディであること", response, isStatusCode(200).withBody(text));
        assertThat("ServletOutputStreamにHttpResponseのbodyが書き込まれていること",
                getBodyString(), is(text));
        
        // servlet responseに200のステータスコードが設定されていること
        verify(mockServletResponse, atLeastOnce()).setStatus(200);
        // Content-Length及びContent-Typeが正しいこと
        verify(mockServletResponse, atLeastOnce()).setContentLength(text.length());
        verify(mockServletResponse, atLeastOnce()).setContentType("text/plain");
        // 任意のヘッダーの書き込みも行われていいること
        verify(mockServletResponse, atLeastOnce()).setHeader("option", "value");
    }

    /**
     * 後続ハンドラで{@link nablarch.fw.web.HttpErrorResponse}が創出された場合、
     * {@link HttpErrorResponse#getResponse()}がレスポンスとして戻されること
     */
    @Test
    public void testHttpErrorResponse() throws Exception {
        // -------------------------------------------------- setup
        final String errorMessage = "エラーが発生しました。";

        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                HttpResponse response = new HttpResponse(400);
                response.setContentType(MediaType.TEXT_PLAIN);
                response.write(errorMessage);
                throw new HttpErrorResponse(response);
            }
        });

        // -------------------------------------------------- execute
        HttpResponse response = sut.handle(mockHttpRequest, context);

        assertThat("ステータスコードが400でハンドラで設定したボディを保持していること",
                response, isStatusCode(400).withBody(errorMessage));

        // -------------------------------------------------- assert
        assertThat("400でボディが空のHttpResponseが戻される", response, isStatusCode(400).withBody(errorMessage));
        assertThat("ServletOutputStreamにHttpResponseのbodyが書き込まれていること",
                getBodyString(), is(errorMessage));

        // servlet responseに200のステータスコードが設定されていること
        verify(mockServletResponse, atLeastOnce()).setStatus(400);
        // Content-Length及びContent-Typeが正しいこと
        verify(mockServletResponse, atLeastOnce()).setContentLength(errorMessage.getBytes(Charset.forName("utf-8")).length);
        verify(mockServletResponse, atLeastOnce()).setContentType("text/plain");
    }

    /**
     * {@link ApplicationException}発生時のテスト。
     * <p/>
     * デフォルト設定の場合、ボディが空の400がレスポンスとなること。
     */
    @Test
    public void testApplicationException_DefaultSetting() throws Exception {

        // -------------------------------------------------- setup
        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                List<Message> messages = new ArrayList<Message>();
                messages.add(new ValidationResultMessage("name", new StringResource() {
                    @Override
                    public String getId() {
                        return "id";
                    }

                    @Override
                    public String getValue(Locale locale) {
                        return "error-message";
                    }
                }, new Object[0]));
                messages.add(new Message(MessageLevel.ERROR, new StringResource() {
                    @Override
                    public String getId() {
                        return "id2";
                    }

                    @Override
                    public String getValue(Locale locale) {
                        return "error-message2";
                    }
                }));
                throw new ApplicationException(messages);
            }
        });

        // -------------------------------------------------- execute
        HttpResponse response = sut.handle(mockHttpRequest, context);

        // -------------------------------------------------- assert
        assertThat("400でボディが空のHttpResponseが戻される", response, isStatusCode(400).withEmptyBody());
        assertThat("ServletOutputStreamに書き込まれたボティの長さも0であること",
                getBodyString(), is(""));

        // servlet responseに400のステータスコードが設定されていること
        verify(mockServletResponse, atLeastOnce()).setStatus(400);
    }

    /**
     * {@link ApplicationException}が発生した場合で、例外内容のログ出力に失敗するケース
     * <p>
     * ApplicationExceptionに対応した400のステータスが戻されるが、
     * ログ出力の失敗を示すFATALログが出力されること。
     */
    @Test
    public void testApplicationException_writeFail() throws Exception {
        // -------------------------------------------------- setup
        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                List<Message> messages = new ArrayList<Message>();
                messages.add(new ValidationResultMessage("name", new StringResource() {
                    @Override
                    public String getId() {
                        return "id";
                    }

                    @Override
                    public String getValue(Locale locale) {
                        return "error-message";
                    }
                }, new Object[0]));
                throw new ApplicationException(messages);
            }
        });

        sut.setErrorLogWriter(new JaxRsErrorLogWriter() {
            @Override
            protected void writeApplicationExceptionLog(
                    HttpRequest request, HttpResponse response,
                    ExecutionContext context, ApplicationException exception) {
                throw new IllegalArgumentException("write fail... ");
            }
        });

        // -------------------------------------------------- execute
        HttpResponse response = sut.handle(mockHttpRequest, context);

        // -------------------------------------------------- assert
        assertThat("400でボディが空のHttpResponseが戻される", response, isStatusCode(400).withEmptyBody());
        assertThat("ServletOutputStreamに書き込まれたボティの長さも0であること",
                getBodyString(), is(""));

        // servlet responseに400のステータスコードが設定されていること
        verify(mockServletResponse, atLeastOnce()).setStatus(400);

        // ログ書き込みに失敗したのでFATALログが出力されること。
        OnMemoryLogWriter.assertLogContains("writer.memory",
                "FATAL monitor",
                "FATAL ROO",
                "write fail... ");
    }

    /**
     * 内部サーバエラー発生時のテスト。
     * <p/>
     * デフォルト設定の場合、ボディが空の500がレスポンスとなること。
     * <p/>
     * エラーの情報が障害通知レベルでログ出力されること
     */
    @Test
    public void testInternalServerError_DefaultSetting() throws Exception {

        // -------------------------------------------------- setup
        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                throw new NullPointerException("ぬるぽ");
            }
        });

        // -------------------------------------------------- execute
        HttpResponse response = sut.handle(mockHttpRequest, context);

        // -------------------------------------------------- assert
        assertThat("500でボディが空のHttpResponseが戻される", response, isStatusCode(500).withEmptyBody());
        assertThat("ServletOutputStreamに書き込まれたボティの長さも0であること",
                getBodyString(), is(""));

        // servlet responseに500のステータスコードが設定されていること
        verify(mockServletResponse, atLeastOnce()).setStatus(500);

        // ログに障害通知と障害解析のログが出力されること
        OnMemoryLogWriter.assertLogContains("writer.memory", "FATAL monitor", "FATAL ROO");
    }

    /**
     * ボディの書き込み時に{@link java.io.IOException}が発生した場合、
     * ワーニングログが出力されること
     */
    @Test
    public void testIOExceptionInBodyWrite() throws Exception {
        // -------------------------------------------------- setup
        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                return new HttpResponse(201).write("body");
            }
        });

        when(mockServletResponse.getOutputStream()).thenReturn(mockOutputStream);
        doThrow(new IOException("io error")).when(mockOutputStream).write(any(byte[].class), anyInt(), anyInt());

        // -------------------------------------------------- execute
        HttpResponse response = sut.handle(mockHttpRequest, context);

        // -------------------------------------------------- assert
        assertThat("後続のハンドラで生成したレスポンスが戻されること", response, isStatusCode(201).withBody("body"));
        OnMemoryLogWriter.assertLogContains("writer.memory", "WARN ROO failed to write response.");
    }

    /**
     * カスタムの{@link ErrorResponseBuilder}が使用できること。
     */
    @Test
    public void testCustomErrorResponseBuilder() throws Exception {
        // -------------------------------------------------- setup
        final String body = "{\"result\": \"error\"}";
        sut.setErrorResponseBuilder(new ErrorResponseBuilder() {
            @Override
            public HttpResponse build(HttpRequest request, ExecutionContext context, Throwable throwable) {
                HttpResponse response = new HttpResponse(500);
                response.setContentType(MediaType.APPLICATION_JSON);
                response.write(body);
                return response;
            }
        });
        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                throw new NullPointerException("error");
            }
        });

        // -------------------------------------------------- execute
        HttpResponse response = sut.handle(mockHttpRequest, context);

        // -------------------------------------------------- assert
        assertThat("500でビルダーが作成したレスポンスが書き込まれること", response, isStatusCode(500).withBody(body));
        assertThat("ServletOutputStreamに書き込まれたボティの長さも0であること",
                getBodyString(), is(body));

        // servlet responseに500のステータスコードが設定されていること
        verify(mockServletResponse, atLeastOnce()).setStatus(500);
        verify(mockServletResponse, atLeastOnce()).setContentType(MediaType.APPLICATION_JSON);

        // ログに障害通知と障害解析のログが出力されること
        OnMemoryLogWriter.assertLogContains("writer.memory", "FATAL monitor", "FATAL ROO");
    }

    /**
     * カスタムの{@link ErrorResponseBuilder}で例外が発生した場合に500が返ること。
     */
    @Test
    public void testCustomErrorResponseBuilderThrownException() throws Exception {
        // -------------------------------------------------- setup
        final String body = "{\"result\": \"error\"}";
        sut.setErrorResponseBuilder(new ErrorResponseBuilder() {
            @Override
            public HttpResponse build(HttpRequest request, ExecutionContext context, Throwable throwable) {
                throw new RuntimeException("test");
            }
        });
        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                throw new NullPointerException("error");
            }
        });

        // -------------------------------------------------- execute
        HttpResponse response = sut.handle(mockHttpRequest, context);

        // -------------------------------------------------- assert
        assertThat("500でボディが空のHttpResponseが戻される", response, isStatusCode(500).withEmptyBody());
        assertThat("ServletOutputStreamに書き込まれたボティの長さも0であること",
                getBodyString(), is(""));

        // servlet responseに500のステータスコードが設定されていること
        verify(mockServletResponse, atLeastOnce()).setStatus(500);

        // ログに障害通知と障害解析のログが出力されること
        OnMemoryLogWriter.assertLogContains("writer.memory", "FATAL monitor", "FATAL ROO", "WARN");
    }

    /**
     * カスタムの{@link JaxRsErrorLogWriter}が使用できること。
     */
    @Test
    public void testCustomErrorLogWriter() {
        // -------------------------------------------------- setup
        sut.setErrorLogWriter(new JaxRsErrorLogWriter() {
            @Override
            public void write(
                    HttpRequest request, HttpResponse response, ExecutionContext context, Throwable throwable) {
                LOGGER.logWarn("エラーが発生しました: " + throwable.getMessage(), throwable);
            }
        });
        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                throw new IllegalStateException("invalid");
            }
        });

        // -------------------------------------------------- execute
        HttpResponse response = sut.handle(mockHttpRequest, context);

        // -------------------------------------------------- assert
        assertThat("500であること", response, isStatusCode(500).withEmptyBody());
        OnMemoryLogWriter.assertLogContains("writer.memory", "WARN ROO エラーが発生しました: invalid");
    }

    /**
     * レスポンスフィニッシャーのテスト。
     * <p/>
     * 指定されたレスポンスフィニッシャーによりレスポンスが変更されること
     */
    @Test
    public void testResponseFinisher() throws Exception {
        // -------------------------------------------------- setup
        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                return new HttpResponse(HttpResponse.Status.CREATED.getStatusCode());
            }
        });

        when(mockHttpRequest.getMethod()).thenReturn("GET");
        when(mockHttpRequest.getRequestUri()).thenReturn("/api/user");

        // -------------------------------------------------- execute
        sut.setResponseFinishers(Arrays.asList(
                new ResponseFinisher() {
                    @Override
                    public void finish(HttpRequest request, HttpResponse response, ExecutionContext context) {
                        response.setHeader("test1", "aaa");
                    }
                },
                new ResponseFinisher() {
                    @Override
                    public void finish(HttpRequest request, HttpResponse response, ExecutionContext context) {
                        response.setHeader("test2", "bbb");
                    }
                }
        ));
        HttpResponse response = sut.handle(mockHttpRequest, context);

        // -------------------------------------------------- assert
        assertThat("201でボディが空のHttpResponseが戻される", response, isStatusCode(201).withEmptyBody());
        assertThat("ServletOutputStreamに書き込まれたボティの長さも0であること",
                getBodyString(), is(""));
        assertThat(response.getHeader("test1"), is("aaa"));
        assertThat(response.getHeader("test2"), is("bbb"));

        // servlet responseに201ステータスコードが設定されていること
        verify(mockServletResponse, atLeastOnce()).setStatus(201);
        verify(mockServletResponse, never()).setContentType(null);
    }

    /**
     * {@link HttpResponse#getContentLength()}がnullを返した場合でも、例外が送出されず処理が正常に終わること。
     */
    @Test
    public void testContentLengthIsNull() throws Exception {
        // -------------------------------------------------- setup
        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                return new HttpResponse(200) {
                    @Override
                    public String getContentLength() {
                        return null;
                    }
                };
            }
        });

        // -------------------------------------------------- execute
        HttpResponse response = sut.handle(mockHttpRequest, context);

        // -------------------------------------------------- assert
        assertThat("ボディが空の200であること", response, isStatusCode(200).withEmptyBody());
        assertThat("ボディが空であること", getBodyString(), is(""));

        // servlet responseに500のステータスコードが設定されていること
        verify(mockServletResponse, atLeastOnce()).setStatus(200);
    }

    /**
     * {@link HttpResponse#getBodyStream()}がnullの場合、bodyは書き込まれないこと
     */
    @Test
    public void testBodyStreamIsNull() throws Exception {
        // -------------------------------------------------- setup
        context.addHandler(new Handler<Object, Object>() {
            @Override
            public Object handle(Object o, ExecutionContext context) {
                return new HttpResponse(200) {
                    @Override
                    public InputStream getBodyStream() {
                        return null;
                    }
                };
            }
        });

        // -------------------------------------------------- execute
        HttpResponse response = sut.handle(mockHttpRequest, context);

        // -------------------------------------------------- assert
        assertThat("ボディが空の200であること", response, isStatusCode(200).withEmptyBody());
        assertThat("ボディが空であること", getBodyString(), is(""));

    }

    private String getBodyString() throws UnsupportedEncodingException {
        return new String(responseBody.toByteArray(), "utf-8");
    }

}
