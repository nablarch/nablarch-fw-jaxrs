package nablarch.fw.jaxrs;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpRequestHandler;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.HttpResponse.Status;
import nablarch.test.support.log.app.OnMemoryLogWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import static nablarch.fw.jaxrs.HttpResponseMatcher.isStatusCode;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link BodyConvertHandler}のテスト。
 */
public class BodyConvertHandlerTest {

    private final HttpRequest mockRequest = mock(HttpRequest.class);

    private BodyConvertHandler sut;

    private TestBodyConverter testBodyConverter;

    @Before
    public void setUp() {
        sut = new BodyConvertHandler();
        testBodyConverter = new TestBodyConverter();
        sut.addBodyConverter(testBodyConverter);
        OnMemoryLogWriter.clear();
    }

    @After
    public void tearDown() {
        OnMemoryLogWriter.clear();
    }

    /**
     * {@link JaxRsContext}とsutを設定した{@link ExecutionContext}を作る。
     *
     * @param methodName {@link JaxRsContext}に設定する{@link TestAction}クラスのメソッドに対するメソッド名
     * @return {@link ExecutionContext}
     */
    private ExecutionContext executionContext(final String methodName) {
        for (Method method : TestAction.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                final ExecutionContext context = new ExecutionContext();
                JaxRsContext.set(context, new JaxRsContext(method));
                context.addHandler(sut);
                return context;
            }
        }
        throw new IllegalArgumentException(
                "method not found from TestAction. methodName =[" + methodName + ']');
    }

    /**
     * Consumes/Producesが付いていない場合。
     *
     * {@link BodyConverter#read(HttpRequest, ExecutionContext)}、
     * {@link BodyConverter#write(Object, ExecutionContext)}が呼ばれないこと。
     */
    @Test
    public void noAnnotations() {

        final ExecutionContext context = executionContext("noAnnotations");

        // add resource method invoking
        context.addHandler(new HttpRequestHandler() {
            @Override
            public HttpResponse handle(final HttpRequest request, final ExecutionContext exeContext) {
                return new TestAction().noAnnotations();
            }
        });

        HttpResponse response = context.handleNext(mockRequest);

        assertThat(response, isStatusCode(200).withEmptyBody());
        assertThat(testBodyConverter.readCount, is(0));
        assertThat(testBodyConverter.writeCount, is(0));
    }

    /**
     * Consumesのみが付いている場合。
     *
     * {@link BodyConverter#read(HttpRequest, ExecutionContext)}のみ呼ばれること。
     */
    @Test
    public void consumes() {
        when(mockRequest.getHeader("Content-Type")).thenReturn("application/json; charset=utf-8");

        final ExecutionContext context = executionContext("consumes");

        // add resource method invoking
        context.addHandler(new HttpRequestHandler() {
            @Override
            public HttpResponse handle(final HttpRequest request, final ExecutionContext exeContext) {
                JaxRsContext jaxRsContext = JaxRsContext.get(exeContext);
                return new TestAction().consumes((TestForm) jaxRsContext.getRequest());
            }
        });

        HttpResponse response = context.handleNext(mockRequest);

        assertThat(response, is(isStatusCode(201).withEmptyBody()));
        assertThat(testBodyConverter.readCount, is(1));
        assertThat(testBodyConverter.writeCount, is(0));
    }

    /**
     * Producesのみが付いている場合。
     *
     * {@link BodyConverter#write(Object, ExecutionContext)}のみ呼ばれること。
     */
    @Test
    public void produces() {

        final ExecutionContext context = executionContext("produces");

        // add resource method invoking
        context.addHandler(new Handler<HttpRequest, Object>() {
            @Override
            public Object handle(final HttpRequest request, final ExecutionContext exeContext) {
                return new TestAction().produces(request);
            }
        });

        HttpResponse response = context.handleNext(mockRequest);

        assertThat(response, isStatusCode(202).withBody("TestForm:0")); // not counted up
        assertThat(testBodyConverter.readCount, is(0));
        assertThat(testBodyConverter.writeCount, is(1));
    }

    /**
     * EntityResponseを使い、レスポンスヘッダとステータスコードが指定された場合。
     *
     * 指定されたレスポンスヘッダとステータスコードがレスポンスに設定されていること。
     */
    @Test
    public void entityResponse() {

        final ExecutionContext context = executionContext("entityResponse");

        // add resource method invoking
        context.addHandler(new Handler<HttpRequest, Object>() {
            @Override
            public Object handle(final HttpRequest request, final ExecutionContext exeContext) {
                return new TestAction().entityResponse(request);
            }
        });

        HttpResponse response = context.handleNext(mockRequest);

        assertThat(response, isStatusCode(505).withBody("TestForm:0")); // not counted up
        assertThat(testBodyConverter.readCount, is(0));
        assertThat(testBodyConverter.writeCount, is(1));
        assertThat(response.getHeaderMap().size(), is(3));
        assertThat(response.getHeader("test-name"), is("test-value"));
    }

    /**
     * EntityResponseを使い、Content-Typeが指定された場合。
     *
     * EntityResponseに指定されたエンティティとContent-Typeでボディが変換されること。
     */
    @Test
    public void entityResponseWithContentType() {

        final ExecutionContext context = executionContext("entityResponseWithContentType");

        // add resource method invoking
        context.addHandler(new Handler<HttpRequest, Object>() {
            @Override
            public Object handle(final HttpRequest request, final ExecutionContext exeContext) {
                return new TestAction().entityResponseWithContentType(request);
            }
        });

        HttpResponse response = context.handleNext(mockRequest);

        assertThat(response, isStatusCode(505).withBody("TestForm:0")); // not counted up
        assertThat(testBodyConverter.readCount, is(0));
        assertThat(testBodyConverter.writeCount, is(1));
        assertThat(response.getHeaderMap().size(), is(3));
        assertThat(response.getHeader("test-name"), is("test-value"));
    }

    /**
     * ProducesアノテーションとEntityResponseでContent-Typeが指定された場合。
     *
     * どちらか一方でContent-Typeを指定するように実行時例外が送出されること。
     */
    @Test
    public void invalidContentType() {

        final ExecutionContext context = executionContext("invalidContentType");

        // add resource method invoking
        context.addHandler(new Handler<HttpRequest, Object>() {
            @Override
            public Object handle(final HttpRequest request, final ExecutionContext exeContext) {
                return new TestAction().invalidContentType(request);
            }
        });

        try {
            context.handleNext(mockRequest);
            fail("IllegalStateExceptionがスローされるはず");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is(
                    "Content-Type is specified in both @Produces and EntityResponse. "
                    + "Specify the Content-Type in either @Produces or EntityResponse. "
                    + "resource method = [nablarch.fw.jaxrs.BodyConvertHandlerTest$TestAction#invalidContentType]"));
        }
    }

    /**
     * EntityResponseを使い、レスポンスヘッダとステータスコードを指定しなかった場合。
     */
    @Test
    public void entityResponseNoneSpecified() {

        final ExecutionContext context = executionContext("entityResponseNoneSpecified");

        // add resource method invoking
        context.addHandler(new Handler<HttpRequest, Object>() {
            @Override
            public Object handle(final HttpRequest request, final ExecutionContext exeContext) {
                return new TestAction().entityResponseNoneSpecified(request);
            }
        });

        HttpResponse response = context.handleNext(mockRequest);

        assertThat(response, isStatusCode(202).withBody("TestForm:0")); // not counted up
        assertThat(testBodyConverter.readCount, is(0));
        assertThat(testBodyConverter.writeCount, is(1));
        assertThat(response.getHeaderMap().size(), is(2));
    }

    /**
     * Consumes/Producesが付いている場合。
     *
     * {@link BodyConverter#read(HttpRequest, ExecutionContext)}、
     * {@link BodyConverter#write(Object, ExecutionContext)}が呼ばれること。
     */
    @Test
    public void consumesAndProduces() {

        when(mockRequest.getHeader("Content-Type")).thenReturn("application/json; charset=utf-8");

        final ExecutionContext context = executionContext("consumesAndProduces");

        // add resource method invoking
        context.addHandler(new Handler<HttpRequest, Object>() {
            @Override
            public Object handle(final HttpRequest request, final ExecutionContext exeContext) {
                JaxRsContext jaxRsContext = JaxRsContext.get(exeContext);
                return new TestAction().consumesAndProduces((TestForm) jaxRsContext.getRequest());
            }
        });

        HttpResponse response = context.handleNext(mockRequest);

        assertThat(response, isStatusCode(202).withBody("TestForm:1")); // count up by TestAction#consumesAndProduces
        assertThat(testBodyConverter.readCount, is(1));
        assertThat(testBodyConverter.writeCount, is(1));
    }

    /**
     * Consumesが付いていて、そのメディアタイプがcontent-typeと一致しない場合。
     *
     * 415が返却されること。
     * {@link BodyConverter#read(HttpRequest, ExecutionContext)}、
     * {@link BodyConverter#write(Object, ExecutionContext)}が呼ばれないこと。
     */
    @Test
    public void unsupportedMediaTypeForMediaTypeMismatch() {

        when(mockRequest.getHeader("Content-Type")).thenReturn("application/xml; charset=utf-8");
        when(mockRequest.getRequestUri()).thenReturn("/api/user");
        when(mockRequest.getMethod()).thenReturn("GET");

        final ExecutionContext context = executionContext("consumes"); // application/json

        try {
            context.handleNext(mockRequest);
        } catch (HttpErrorResponse e) {
            assertThat(e.getResponse(), isStatusCode(415).withEmptyBody());
            assertThat(testBodyConverter.readCount, is(0));
            assertThat(testBodyConverter.writeCount, is(0));
        }

        OnMemoryLogWriter.assertLogContains(
                "writer.memory",
                "unsupported media type requested. request method = [GET],"
                        + " request uri = [/api/user],"
                        + " content type = [application/xml; charset=utf-8],"
                        + " resource method = [" + TestAction.class.getName() + "#consumes],"
                        + " consumes media type = [application/json]");
    }

    /**
     * Content-Typeがあるが、Consumesアノテーションがない場合
     *
     * 415が返されること
     */
    @Test
    public void withContentTypeAndWithoutConsumes() {
        when(mockRequest.getHeader("Content-Type")).thenReturn("application/json");
        when(mockRequest.getMethod()).thenReturn("POST");
        when(mockRequest.getRequestUri()).thenReturn("/api/user");

        final ExecutionContext context = executionContext("noAnnotations"); // application/json

        try {
            context.handleNext(mockRequest);
        } catch (HttpErrorResponse e) {
            assertThat(e.getResponse(), isStatusCode(415).withEmptyBody());
            assertThat(testBodyConverter.readCount, is(0));
            assertThat(testBodyConverter.writeCount, is(0));
        }

        OnMemoryLogWriter.assertLogContains(
                "writer.memory",
                "unsupported media type requested."
                        + " request method = [POST],"
                        + " request uri = [/api/user],"
                        + " content type = [application/json],"
                        + " resource method = [" + TestAction.class.getName() + "#noAnnotations],"
                        + " consumes media type = [null]");
    }

    /**
     * Consumesが付いていて、そのメディアタイプがcontent-typeと一致しない場合、
     * （content-typeヘッダが未指定の場合）
     *
     * 415が返却されること。
     */
    @Test
    public void unsupportedMediaTypeForNoContentType() {

        when(mockRequest.getHeader("Content-Type")).thenReturn(null);
        when(mockRequest.getMethod()).thenReturn("PUT");
        when(mockRequest.getRequestUri()).thenReturn("/api/person");
        
        final ExecutionContext context = executionContext("consumes"); // application/json

        try {
            context.handleNext(mockRequest);
        } catch (HttpErrorResponse e) {
            assertThat(e.getResponse(), isStatusCode(415).withEmptyBody());
            assertThat(testBodyConverter.readCount, is(0));
            assertThat(testBodyConverter.writeCount, is(0));
        }

        OnMemoryLogWriter.assertLogContains(
                "writer.memory",
                "unsupported media type requested."
                        + " request method = [PUT],"
                        + " request uri = [/api/person],"
                        + " content type = [null],"
                        + " resource method = [" + TestAction.class.getName() + "#consumes],"
                        + " consumes media type = [application/json]");
    }

    /**
     * 最終的なレスポンスのタイプがHttpResponseでない場合。
     *
     * 実行時例外がスローされること。
     */
    @Test
    public void unsupportedResponseType() {

        final ExecutionContext context = executionContext("unsupportedResponseType");

        // add resource method invoking
        context.addHandler(new Handler<HttpRequest, Object>() {
            @Override
            public Object handle(final HttpRequest request, final ExecutionContext exeContext) {
                return new TestAction().unsupportedResponseType();
            }
        });

        try {
            context.handleNext(mockRequest);
            fail("throws IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(),
                    is("unsupported response type found. "
                     + "must be return nablarch.fw.web.HttpResponse. "
                     + "response type = [nablarch.fw.jaxrs.BodyConvertHandlerTest$TestForm]"));
        }
    }

    /**
     * 最終的なレスポンスがnullの場合。
     *
     * 204:NO_CONTENTが返されること。
     */
    @Test
    public void nullResponse() {

        final ExecutionContext context = executionContext("nullResponse");

        // add resource method invoking
        context.addHandler(new Handler<HttpRequest, Object>() {
            @Override
            public Object handle(final HttpRequest request, final ExecutionContext exeContext) {
                return new TestAction().nullResponse();
            }
        });

        final HttpResponse response = context.handleNext(mockRequest);

        assertThat(response, isStatusCode(204).withEmptyBody());
        assertThat(testBodyConverter.readCount, is(0));
        assertThat(testBodyConverter.writeCount, is(0));
    }

    /**
     * レスポンスの型が{@link Void}の場合。
     *
     * 204:NO_CONTENTが返されること。
     */
    @Test
    public void returnTypeNull() {

        final ExecutionContext context = executionContext("returnTypeVoid");

        // add resource method invoking
        context.addHandler(new Handler<HttpRequest, Object>() {
            @Override
            public Object handle(final HttpRequest request, final ExecutionContext exeContext) {
                return new TestAction().returnTypeVoid();
            }
        });

        final HttpResponse response = context.handleNext(mockRequest);

        assertThat(response, isStatusCode(204).withEmptyBody());
        assertThat(testBodyConverter.readCount, is(0));
        assertThat(testBodyConverter.writeCount, is(0));
    }

    /**
     * 複数の{@link BodyConverter}が設定されている場合、{@link BodyConverter#isConvertible(String)}で{@code true}
     * を返すコンバーターでボディの変換処理が行われること。
     */
    @Test
    public void multipleBodyConverter_shouldExecuteConvertibleConverter() {
        sut.setBodyConverters(new ArrayList<BodyConverter>() {{
            add(new BodyConverter() {
                @Override
                public Object read(HttpRequest request, ExecutionContext executionContext) {
                    throw new UnsupportedOperationException("このクラスは実行されないはず");
                }

                @Override
                public HttpResponse write(Object response, ExecutionContext executionContext) {
                    return new HttpResponse().write("first-converter");
                }

                @Override
                public boolean isConvertible(String mediaType) {
                    return mediaType.equals("application/xml");
                }
            });
            add(new BodyConverter() {
                @Override
                public Object read(HttpRequest request, ExecutionContext executionContext) {
                    return "second-converter";
                }

                @Override
                public HttpResponse write(Object response, ExecutionContext executionContext) {
                    throw new UnsupportedOperationException("このクラスは実行されないはず");
                }

                @Override
                public boolean isConvertible(String mediaType) {
                    return mediaType.equals("application/json");
                }
            });
        }});

        when(mockRequest.getHeader("Content-Type")).thenReturn("application/json");

        ExecutionContext context = executionContext("consumesAndProduces");
        context.setHandlerQueue(Arrays.asList(new Handler<HttpRequest, String>() {
            @Override
            public String handle(HttpRequest s, ExecutionContext context) {
                JaxRsContext jaxRsContext = JaxRsContext.get(context);
                String request = jaxRsContext.getRequest();
                if (!request.equals("second-converter")) {
                    throw new IllegalArgumentException("パラメータが想定と違うよ");
                }
                return "ok";
            }
        }));
        HttpResponse response = sut.handle(mockRequest, context);
        assertThat(response, isStatusCode(200).withBody("first-converter"));
    }

    /**
     *
     */
    @Test
    public void multipleConvertibleBodyConverter_shouldExecuteFirstConvertibleConverter() {
        sut.setBodyConverters(new ArrayList<BodyConverter>() {{
            add(new BodyConverter() {
                @Override
                public Object read(HttpRequest request, ExecutionContext executionContext) {
                    return "first-converter";
                }

                @Override
                public HttpResponse write(Object response, ExecutionContext executionContext) {
                    return new HttpResponse(200).write("first-converter: " + response);
                }

                @Override
                public boolean isConvertible(String mediaType) {
                    return true;
                }
            });
            add(new BodyConverter() {
                @Override
                public Object read(HttpRequest request, ExecutionContext executionContext) {
                    throw new UnsupportedOperationException("このクラスは実行されないはず");
                }

                @Override
                public HttpResponse write(Object response, ExecutionContext executionContext) {
                    throw new UnsupportedOperationException("このクラスは実行されないはず");
                }

                @Override
                public boolean isConvertible(String mediaType) {
                    return true;
                }
            });
        }});

        when(mockRequest.getHeader("Content-Type")).thenReturn("application/json");

        ExecutionContext context = executionContext("consumesAndProduces");
        context.setHandlerQueue(Arrays.asList(new Handler<HttpRequest, String>() {
            @Override
            public String handle(HttpRequest s, ExecutionContext context) {
                JaxRsContext jaxRsContext = JaxRsContext.get(context);
                String request = jaxRsContext.getRequest();
                if (!request.equals("first-converter")) {
                    throw new IllegalArgumentException("パラメータが想定と違うよ");
                }
                return "ok";
            }
        }));
        HttpResponse response = sut.handle(mockRequest, context);
        assertThat(response, isStatusCode(200).withBody("first-converter: ok"));
    }

    /**
     * メディアタイプを処理する{@link BodyConverter}が存在しない場合、
     * {@link Status#UNSUPPORTED_MEDIA_TYPE}を持つ{@link HttpErrorResponse}が送出されること
     */
    @Test
    public void readConverterNotFound_shouldThrowUnsupportedMediaType() {
        sut.setBodyConverters(new ArrayList<BodyConverter>() {{
            add(new BodyConverter() {
                @Override
                public Object read(HttpRequest request, ExecutionContext executionContext) {
                    return null;
                }

                @Override
                public HttpResponse write(Object response, ExecutionContext executionContext) {
                    return null;
                }

                @Override
                public boolean isConvertible(String mediaType) {
                    return false;
                }
            });
        }});

        when(mockRequest.getHeader("Content-Type")).thenReturn("application/json");

        try {
            sut.handle(mockRequest, executionContext("consumes"));
            fail("ここは通過しない");
        } catch (HttpErrorResponse e) {
            HttpResponse response = e.getResponse();
            assertThat(response,
                    isStatusCode(HttpResponse.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode())
                            .withEmptyBody());
        }
    }

    public static final class TestAction {

        public HttpResponse noAnnotations() {
            return new HttpResponse(200);
        }

        @Consumes(MediaType.APPLICATION_JSON)
        public HttpResponse consumes(TestForm testForm) {
            return new HttpResponse(201);
        }

        @Produces(MediaType.APPLICATION_XML)
        public TestForm produces(HttpRequest request) {
            return new TestForm();
        }

        @Produces(MediaType.APPLICATION_XML)
        public EntityResponse<TestForm> entityResponse(HttpRequest request) {
            EntityResponse<TestForm> response = new EntityResponse<>();
            response.setEntity(new TestForm());
            response.setStatusCode(505);
            response.setHeader("test-name", "test-value");
            return response;
        }

        public EntityResponse<TestForm> entityResponseWithContentType(HttpRequest request) {
            EntityResponse<TestForm> response = new EntityResponse<>();
            response.setEntity(new TestForm());
            response.setContentType(MediaType.APPLICATION_JSON);
            response.setStatusCode(505);
            response.setHeader("test-name", "test-value");
            return response;
        }

        @Produces(MediaType.APPLICATION_XML)
        public EntityResponse<?> invalidContentType(HttpRequest request) {
            EntityResponse<?> response = new EntityResponse<>();
            response.setContentType(MediaType.APPLICATION_JSON);
            return response;
        }

        @Produces(MediaType.APPLICATION_XML)
        public EntityResponse entityResponseNoneSpecified(HttpRequest request) {
            // 以前はEntityResponseがジェネリクスに対応していなかったため、互確認のために残している
            EntityResponse response = new EntityResponse();
            response.setEntity(new TestForm());
            return response;
        }

        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_XML)
        public TestForm consumesAndProduces(TestForm testForm) {
            testForm.countUp();
            return testForm;
        }

        public TestForm unsupportedResponseType() {
            return new TestForm();
        }

        public TestForm nullResponse() {
            return null;
        }

        public Void returnTypeVoid() {
            return null;
        }
    }

    public static final class TestForm {
        int count = 0;
        void countUp() { count++; }
    }

    public static final class TestBodyConverter implements BodyConverter {
        int readCount = 0;
        int writeCount = 0;
        @Override
        public Object read(HttpRequest request, ExecutionContext executionContext) {
            readCount++;
            return new TestForm();
        }
        @Override
        public HttpResponse write(Object response, ExecutionContext executionContext) {
            writeCount++;
            return new HttpResponse(202).write("TestForm:" + ((TestForm) response).count);
        }

        @Override
        public boolean isConvertible(String mediaType) {
            return true;
        }
    }
}
