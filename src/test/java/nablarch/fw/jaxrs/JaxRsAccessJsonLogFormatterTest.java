package nablarch.fw.jaxrs;

import com.sun.xml.bind.StringInputStream;
import nablarch.core.ThreadContext;
import nablarch.fw.jaxrs.JaxRsAccessLogFormatter.JaxRsAccessLogContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.NablarchHttpServletRequestWrapper;
import nablarch.fw.web.servlet.NablarchHttpServletRequestWrapper.HttpSessionWrapper;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class JaxRsAccessJsonLogFormatterTest {

    /**
     * リクエスト処理開始時の出力有無
     */
    public static class BeginOutputEnabled {

        private final JaxRsAccessJsonLogFormatter sut = new JaxRsAccessJsonLogFormatter();

        /**
         * リクエスト処理開始時の出力有無が未定義であれば有効になる。
         */
        @Test
        public void testBeginOutputEnabledIfUndefined() {
            sut.initialize(new AppLogPropertyBuilder().build());

            assertThat(sut.isBeginOutputEnabled(), is(true));
        }

        /**
         * リクエスト処理開始時の出力有無が空定義であれば無効になる。
         */
        @Test
        public void testBeginOutputEnabledIfEmpty() {
            sut.initialize(new AppLogPropertyBuilder().beginOutputEnabled("").build());

            assertThat(sut.isBeginOutputEnabled(), is(false));
        }

        /**
         * リクエスト処理開始時の出力有無が"true"であれば有効になる。
         */
        @Test
        public void testBeginOutputEnabledIfTrue() {
            sut.initialize(new AppLogPropertyBuilder().beginOutputEnabled("true").build());

            assertThat(sut.isBeginOutputEnabled(), is(true));
        }

        /**
         * リクエスト処理開始時の出力有無が"false"であれば無効になる。
         */
        @Test
        public void testBeginOutputEnabledIfFalse() {
            sut.initialize(new AppLogPropertyBuilder().beginOutputEnabled("false").build());

            assertThat(sut.isBeginOutputEnabled(), is(false));
        }
    }

    /**
     * リクエスト処理終了時の出力有無
     */
    public static class EndOutputEnabled {

        private final JaxRsAccessJsonLogFormatter sut = new JaxRsAccessJsonLogFormatter();

        /**
         * リクエスト処理終了時の出力有無が未定義であれば有効になる。
         */
        @Test
        public void testEndOutputEnabledIfUndefined() {
            sut.initialize(new AppLogPropertyBuilder().build());

            assertThat(sut.isEndOutputEnabled(), is(true));
        }

        /**
         * リクエスト処理終了時の出力有無が空定義であれば無効になる。
         */
        @Test
        public void testEndOutputEnabledIfEmpty() {
            sut.initialize(new AppLogPropertyBuilder().endOutputEnabled("").build());

            assertThat(sut.isEndOutputEnabled(), is(false));
        }

        /**
         * リクエスト処理終了時の出力有無が"true"であれば有効になる。
         */
        @Test
        public void testEndOutputEnabledIfTrue() {
            sut.initialize(new AppLogPropertyBuilder().endOutputEnabled("true").build());

            assertThat(sut.isEndOutputEnabled(), is(true));
        }

        /**
         * リクエスト処理終了時の出力有無が"false"であれば無効になる。
         */
        @Test
        public void testEndOutputEnabledIfFalse() {
            sut.initialize(new AppLogPropertyBuilder().endOutputEnabled("false").build());

            assertThat(sut.isEndOutputEnabled(), is(false));
        }
    }

    /**
     * ログ出力項目のフォーマット
     */
    public static class LogItem {

        private final JaxRsAccessJsonLogFormatter sut = new JaxRsAccessJsonLogFormatter();

        private final JaxRsAccessLogContext logContext = new JaxRsAccessLogContext();

        private final ServletExecutionContext executionContextMock = mock(ServletExecutionContext.class);

        private final HttpRequest httpRequestMock = mock(HttpRequest.class);

        private final NablarchHttpServletRequestWrapper servletRequestMock = mock(NablarchHttpServletRequestWrapper.class);

        private final HttpResponse httpResponseMock = mock(HttpResponse.class);

        @Before
        public void setUp() {
            when(executionContextMock.getServletRequest()).thenReturn(servletRequestMock);
            logContext.setContext(executionContextMock);
            logContext.setRequest(httpRequestMock);
            logContext.setResponse(httpResponseMock);
        }

        @After
        public void tearDown() {
            ThreadContext.clear();
        }

        /**
         * リクエスト処理開始時のメッセージにリクエストIDを出力できる。
         */
        @Test
        public void testBeginFormatRequestId() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("requestId")
                    .messagePrefix("$").build());
            ThreadContext.setRequestId("test");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"requestId\":\"test\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにリクエストIDを出力できる。
         */
        @Test
        public void testEndFormatRequestId() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("requestId")
                    .messagePrefix("$").build());
            ThreadContext.setRequestId("test");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"requestId\":\"test\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにユーザーIDを出力できる。
         */
        @Test
        public void testBeginFormatUserId() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("userId")
                    .messagePrefix("$").build());
            ThreadContext.setUserId("test");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"userId\":\"test\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにユーザーIDを出力できる。
         */
        @Test
        public void testEndFormatUserId() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("userId")
                    .messagePrefix("$").build());
            ThreadContext.setUserId("test");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"userId\":\"test\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにURLを出力できる。
         */
        @Test
        public void testBeginFormatUrl() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("url")
                    .messagePrefix("$").build());
            when(servletRequestMock.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"url\":\"http://localhost\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにURLを出力できる。
         */
        @Test
        public void testEndFormatUrl() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("url")
                    .messagePrefix("$").build());
            when(servletRequestMock.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"url\":\"http://localhost\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにクエリ文字列を出力できる。
         */
        @Test
        public void testBeginFormatQuery() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("queryString")
                    .messagePrefix("$").build());
            when(servletRequestMock.getQueryString()).thenReturn("param=value");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"queryString\":\"?param=value\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにクエリ文字列を出力できる。
         */
        @Test
        public void testEndFormatQuery() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("queryString")
                    .messagePrefix("$").build());
            when(servletRequestMock.getQueryString()).thenReturn("param=value");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"queryString\":\"?param=value\"}"));
        }

        /**
         * クエリ文字列が設定されていなければ、空文字を出力する。
         */
        @Test
        public void testQueryIfEmpty() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("queryString")
                    .messagePrefix("$").build());

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"queryString\":\"\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにポート番号を出力できる。
         */
        @Test
        public void testBeginFormatPort() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("port")
                    .messagePrefix("$").build());
            when(servletRequestMock.getServerPort()).thenReturn(8080);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"port\":8080}"));
        }

        /**
         * リクエスト処理終了時のメッセージにポート番号を出力できる。
         */
        @Test
        public void testEndFormatPort() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("port")
                    .messagePrefix("$").build());
            when(servletRequestMock.getServerPort()).thenReturn(8080);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"port\":8080}"));
        }

        /**
         * リクエスト処理開始時のメッセージにHTTPメソッドを出力できる。
         */
        @Test
        public void testBeginFormatMethod() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("method")
                    .messagePrefix("$").build());
            when(httpRequestMock.getMethod()).thenReturn("GET");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"method\":\"GET\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにHTTPメソッドを出力できる。
         */
        @Test
        public void testEndFormatMethod() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("method")
                    .messagePrefix("$").build());
            when(httpRequestMock.getMethod()).thenReturn("GET");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"method\":\"GET\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにHTTPセッションIDを出力できる。
         */
        @Test
        public void testBeginFormatSessionId() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("sessionId")
                    .messagePrefix("$").build());
            HttpSessionWrapper sessionMock = mock(HttpSessionWrapper.class);
            when(sessionMock.getId()).thenReturn("id");
            when(servletRequestMock.getSession(false)).thenReturn(sessionMock);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"sessionId\":\"id\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにHTTPセッションIDを出力できる。
         */
        @Test
        public void testEndFormatSessionId() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("sessionId")
                    .messagePrefix("$").build());
            HttpSessionWrapper sessionMock = mock(HttpSessionWrapper.class);
            when(sessionMock.getId()).thenReturn("id");
            when(servletRequestMock.getSession(false)).thenReturn(sessionMock);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"sessionId\":\"id\"}"));
        }

        /**
         * HTTPセッションを使用していなければ、HTTPセッションIDには空文字を出力する。
         */
        @Test
        public void testSessionIdIfEmpty() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("sessionId")
                    .messagePrefix("$").build());

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"sessionId\":\"\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにセッションストアIDを出力できる。
         */
        @Test
        public void testBeginFormatSessionStoreId() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("sessionStoreId")
                    .messagePrefix("$").build());
            when(executionContextMock.getRequestScopedVar("nablarch_internal_session_store_id")).thenReturn("id");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"sessionStoreId\":\"id\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにセッションストアIDを出力できる。
         */
        @Test
        public void testEndFormatSessionStoreId() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("sessionStoreId")
                    .messagePrefix("$").build());
            when(executionContextMock.getRequestScopedVar("nablarch_internal_session_store_id")).thenReturn("id");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"sessionStoreId\":\"id\"}"));
        }

        /**
         * セッションストアを使用していなければ、セッションストアIDを出力しない。
         */
        @Test
        public void testSessionStoreIdIfEmpty() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("sessionStoreId")
                    .messagePrefix("$").build());

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${}"));
        }

        /**
         * リクエスト処理開始時のメッセージにクライアント端末IPアドレスを出力できる。
         */
        @Test
        public void testBeginFormatClientIpAddress() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("clientIpAddress")
                    .messagePrefix("$").build());
            when(servletRequestMock.getRemoteAddr()).thenReturn("192.168.0.0");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"clientIpAddress\":\"192.168.0.0\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにクライアント端末IPアドレスを出力できる。
         */
        @Test
        public void testEndFormatClientIpAddress() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("clientIpAddress")
                    .messagePrefix("$").build());
            when(servletRequestMock.getRemoteAddr()).thenReturn("192.168.0.0");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"clientIpAddress\":\"192.168.0.0\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにクライアント端末ホストを出力できる。
         */
        @Test
        public void testBeginFormatClientHost() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("clientHost")
                    .messagePrefix("$").build());
            when(servletRequestMock.getRemoteHost()).thenReturn("localhost");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"clientHost\":\"localhost\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにクライアント端末ホストを出力できる。
         */
        @Test
        public void testEndFormatClientHost() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("clientHost")
                    .messagePrefix("$").build());
            when(servletRequestMock.getRemoteHost()).thenReturn("localhost");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"clientHost\":\"localhost\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにHTTPヘッダのUser-Agentを出力できる。
         */
        @Test
        public void testBeginFormatUserAgent() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("clientUserAgent")
                    .messagePrefix("$").build());
            when(servletRequestMock.getHeader("User-Agent")).thenReturn("agent");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"clientUserAgent\":\"agent\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにHTTPヘッダのUser-Agentを出力できる。
         */
        @Test
        public void testEndFormatUserAgent() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("clientUserAgent")
                    .messagePrefix("$").build());
            when(servletRequestMock.getHeader("User-Agent")).thenReturn("agent");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"clientUserAgent\":\"agent\"}"));
        }

        /**
         * User-Agentが設定されていなければ、空文字を出力する。
         */
        @Test
        public void testUserAgentIfEmpty() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("clientUserAgent")
                    .messagePrefix("$").build());

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${}"));
        }

        /**
         * リクエスト処理開始時のメッセージにリクエストパラメータを出力できる。
         */
        @Test
        public void testBeginFormatParameter() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("parameters")
                    .messagePrefix("$").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            parameter.put("param1", new String[]{"value1"});
            parameter.put("param2", new String[]{"value2"});
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"parameters\":{\"param1\":[\"value1\"],\"param2\":[\"value2\"]}}"));
        }

        /**
         * リクエスト処理終了時のメッセージにリクエストパラメータを出力できる。
         */
        @Test
        public void testEndFormatParameter() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("parameters")
                    .messagePrefix("$").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            parameter.put("param1", new String[]{"value1"});
            parameter.put("param2", new String[]{"value2"});
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"parameters\":{\"param1\":[\"value1\"],\"param2\":[\"value2\"]}}"));
        }

        /**
         * リクエストパラメータが設定されていなければ、空表現を出力する。
         */
        @Test
        public void testParameterIfEmpty() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("parameters")
                    .messagePrefix("$").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"parameters\":{}}"));
        }

        /**
         * マスク文字とマスク対象パターンを指定してリクエストパラメータをマスクできる。
         */
        @Test
        public void testMaskParameter() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("parameters").messagePrefix("$")
                    .maskingChar("x").maskingPatterns("param").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            parameter.put("para", new String[]{"value"});
            parameter.put("param", new String[]{"value"});
            parameter.put("1param", new String[]{"value"});
            parameter.put("param2", new String[]{"value"});
            parameter.put("3param3", new String[]{"value"});
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, containsString("\"para\":[\"value\"]"));
            assertThat(actual, containsString("\"param\":[\"xxxxx\"]"));
            assertThat(actual, containsString("\"1param\":[\"xxxxx\"]"));
            assertThat(actual, containsString("\"param2\":[\"xxxxx\"]"));
            assertThat(actual, containsString("\"3param3\":[\"xxxxx\"]"));
        }

        /**
         * マスク対象パターンを複数指定してリクエストパラメータをマスクできる。
         */
        @Test
        public void testMaskParameterWithMultiplePattern() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("parameters").messagePrefix("$")
                    .maskingChar("x").maskingPatterns("1pa,am2,3param3").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            parameter.put("para", new String[]{"value"});
            parameter.put("param", new String[]{"value"});
            parameter.put("1param", new String[]{"value"});
            parameter.put("param2", new String[]{"value"});
            parameter.put("3param3", new String[]{"value"});
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, containsString("\"para\":[\"value\"]"));
            assertThat(actual, containsString("\"param\":[\"value\"]"));
            assertThat(actual, containsString("\"1param\":[\"xxxxx\"]"));
            assertThat(actual, containsString("\"param2\":[\"xxxxx\"]"));
            assertThat(actual, containsString("\"3param3\":[\"xxxxx\"]"));
        }

        /**
         * マスク文字を指定しなければデフォルトのマスク文字でリクエストパラメータをマスクできる。
         */
        @Test
        public void testMaskParameterWithDefaultMaskingChar() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("parameters").messagePrefix("$")
                    .maskingPatterns("param").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            parameter.put("param", new String[]{"value"});
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, containsString("\"param\":[\"*****\"]"));
        }

        /**
         * マスク文字に空白が含まれていた場合、空白部分は除去される。
         */
        @Test
        public void testMaskPatternsIfSpace() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("parameters").messagePrefix("$")
                    .maskingPatterns(" param ,, ").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            parameter.put("param", new String[]{"value"});
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"parameters\":{\"param\":[\"*****\"]}}"));

        }

        /**
         * マスク文字が1文字を超えている場合、例外を送出する。
         */
        @Test(expected = IllegalArgumentException.class)
        public void testMaskCharIfOverLength() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("parameters").messagePrefix("$")
                    .maskingChar("12").build());
        }

        /**
         * リクエスト処理開始時のメッセージにセッションスコープ情報を出力できる。
         */
        @Test
        public void testBeginFormatSessionScope() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("sessionScope")
                    .messagePrefix("$").build());
            Map<String, Object> scope = new HashMap<String, Object>();
            scope.put("param1", "value1");
            scope.put("param2", "value2");
            when(executionContextMock.hasSession()).thenReturn(true);
            when(executionContextMock.getSessionScopeMap()).thenReturn(scope);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"sessionScope\":{\"param1\":\"value1\",\"param2\":\"value2\"}}"));
        }

        /**
         * リクエスト処理終了時のメッセージにセッションスコープ情報を出力できる。
         */
        @Test
        public void testEndFormatSessionScope() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("sessionScope")
                    .messagePrefix("$").build());
            Map<String, Object> scope = new HashMap<String, Object>();
            scope.put("param1", "value1");
            scope.put("param2", "value2");
            when(executionContextMock.hasSession()).thenReturn(true);
            when(executionContextMock.getSessionScopeMap()).thenReturn(scope);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"sessionScope\":{\"param1\":\"value1\",\"param2\":\"value2\"}}"));
        }

        /**
         * セッションスコープ情報が設定されていなければ、空表現を出力する。
         */
        @Test
        public void testSessionScopeIfEmpty() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("sessionScope")
                    .messagePrefix("$").build());
            Map<String, Object> scope = new HashMap<String, Object>();
            when(executionContextMock.hasSession()).thenReturn(true);
            when(executionContextMock.getSessionScopeMap()).thenReturn(scope);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"sessionScope\":{}}"));
        }

        /**
         * マスク文字とマスク対象パターンを指定してセッションスコープ情報をマスクできる。
         */
        @Test
        public void testMaskSessionScope() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("sessionScope").messagePrefix("$")
                    .maskingChar("x").maskingPatterns("param").build());
            Map<String, Object> scope = new HashMap<String, Object>();
            scope.put("para", "value");
            scope.put("param", "value");
            scope.put("1param", "value");
            scope.put("param2", "value");
            scope.put("3param3", "value");
            when(executionContextMock.hasSession()).thenReturn(true);
            when(executionContextMock.getSessionScopeMap()).thenReturn(scope);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, containsString("\"para\":\"value\""));
            assertThat(actual, containsString("\"param\":\"xxxxx\""));
            assertThat(actual, containsString("\"1param\":\"xxxxx\""));
            assertThat(actual, containsString("\"param2\":\"xxxxx\""));
            assertThat(actual, containsString("\"3param3\":\"xxxxx\""));
        }

        /**
         * マスク対象パターンを複数指定してセッションスコープ情報をマスクできる。
         */
        @Test
        public void testMaskSessionScopeWithMultiplePattern() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("sessionScope").messagePrefix("$")
                    .maskingChar("x").maskingPatterns("1pa,am2,3param3").build());
            Map<String, Object> scope = new HashMap<String, Object>();
            scope.put("para", "value");
            scope.put("param", "value");
            scope.put("1param", "value");
            scope.put("param2", "value");
            scope.put("3param3", "value");
            when(executionContextMock.hasSession()).thenReturn(true);
            when(executionContextMock.getSessionScopeMap()).thenReturn(scope);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, containsString("\"para\":\"value\""));
            assertThat(actual, containsString("\"param\":\"value\""));
            assertThat(actual, containsString("\"1param\":\"xxxxx\""));
            assertThat(actual, containsString("\"param2\":\"xxxxx\""));
            assertThat(actual, containsString("\"3param3\":\"xxxxx\""));
        }

        /**
         * マスク文字を指定しなければデフォルトのマスク文字でセッションスコープ情報をマスクできる。
         */
        @Test
        public void testMaskSessionScopeWithDefaultMaskingChar() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("sessionScope").messagePrefix("$")
                    .maskingPatterns("param").build());
            Map<String, Object> scope = new HashMap<String, Object>();
            scope.put("param", "value");
            when(executionContextMock.hasSession()).thenReturn(true);
            when(executionContextMock.getSessionScopeMap()).thenReturn(scope);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, containsString("\"param\":\"*****\""));
        }

        /**
         * リクエスト処理終了時のメッセージにステータスコードを出力できる。
         */
        @Test
        public void testEndFormatStatusCode() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("statusCode")
                    .messagePrefix("$").build());
            when(httpResponseMock.getStatusCode()).thenReturn(200);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"statusCode\":200}"));
        }

        /**
         * レスポンスが設定されていない場合、ステータスコードを出力しない。
         */
        @Test
        public void testStatusCodeIfEmpty() {
            logContext.setResponse(null);
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("statusCode")
                    .messagePrefix("$").build());
            when(httpResponseMock.getStatusCode()).thenReturn(200);
            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${}"));
        }

        /**
         * リクエスト処理終了時のメッセージに開始日時を出力できる。
         */
        @Test
        public void testEndFormatStartTime() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("startTime").messagePrefix("$")
                    .datePattern("yyyy/MM/dd").build());
            long startTime = new GregorianCalendar(2023, Calendar.JANUARY, 31).getTimeInMillis();
            logContext.setStartTime(startTime);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"startTime\":\"2023/01/31\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージに開始日時をデフォルト書式で出力できる。
         */
        @Test
        public void testEndFormatStartTimeWithDefaultFormat() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("startTime")
                    .messagePrefix("$").build());
            long startTime = new GregorianCalendar(2023, Calendar.JANUARY, 31, 9, 59, 0).getTimeInMillis();
            logContext.setStartTime(startTime);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"startTime\":\"2023-01-31 09:59:00.000\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージに終了日時を出力できる。
         */
        @Test
        public void testEndFormatEndTime() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("endTime").messagePrefix("$")
                    .datePattern("yyyy/MM/dd").build());
            long endTime = new GregorianCalendar(2023, Calendar.JANUARY, 31).getTimeInMillis();
            logContext.setEndTime(endTime);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"endTime\":\"2023/01/31\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージに終了日時をデフォルト書式で出力できる。
         */
        @Test
        public void testEndFormatEndTimeWithDefaultFormat() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("endTime")
                    .messagePrefix("$").build());
            long endTime = new GregorianCalendar(2023, Calendar.JANUARY, 31, 9, 59, 0).getTimeInMillis();
            logContext.setEndTime(endTime);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"endTime\":\"2023-01-31 09:59:00.000\"}"));
        }


        /**
         * リクエスト処理終了時のメッセージに実行時間を出力できる。
         */
        @Test
        public void testEndFormatExecutionTime() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("executionTime")
                    .messagePrefix("$").build());
            long startTime = new GregorianCalendar(2023, Calendar.JANUARY, 1, 12, 0).getTimeInMillis();
            logContext.setStartTime(startTime);
            long endTime = new GregorianCalendar(2023, Calendar.JANUARY, 1, 13, 0).getTimeInMillis();
            logContext.setEndTime(endTime);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"executionTime\":" + 1000 * 60 * 60 + "}"));
        }

        /**
         * リクエスト処理終了時のメッセージに最大メモリ量を出力できる。
         */
        @Test
        public void testEndFormatMaxMemory() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("maxMemory")
                    .messagePrefix("$").build());
            logContext.setMaxMemory(100);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"maxMemory\":100}"));
        }

        /**
         * リクエスト処理終了時のメッセージに空きメモリ量を出力できる。
         */
        @Test
        public void testEndFormatFreeMemory() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("freeMemory")
                    .messagePrefix("$").build());
            logContext.setFreeMemory(100);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"freeMemory\":100}"));
        }

        /**
         * リクエスト処理開始時のメッセージにラベルを出力できる。
         */
        @Test
        public void testBeginFormatLabel() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("label").messagePrefix("$")
                    .beginLabel("BEGIN").build());

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"label\":\"BEGIN\"}"));
        }

        /**
         * リクエスト処理開始時のラベルが設定されていない場合、デフォルトラベルを出力できる。
         */
        @Test
        public void testDefaultBeginLabel() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("label")
                    .messagePrefix("$").build());

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"label\":\"HTTP ACCESS BEGIN\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにラベルを出力できる。
         */
        @Test
        public void testEndFormatLabel() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("label").messagePrefix("$")
                    .endLabel("END").build());

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"label\":\"END\"}"));
        }

        /**
         * リクエスト処理終了時のラベルが設定されていない場合、デフォルトラベルを出力できる。
         */
        @Test
        public void testDefaultEndLabel() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("label")
                    .messagePrefix("$").build());

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"label\":\"HTTP ACCESS END\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにリクエストボディを出力できる。
         */
        @Test
        public void testBeginFormatRequestBody() throws Exception {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("requestBody")
                    .messagePrefix("$").build());
            String requestBody = "{\"id\":\"test\"}";
            when(servletRequestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
            when(servletRequestMock.getContentLength()).thenReturn(requestBody.length());
            when(httpRequestMock.getHeader("Content-Type")).thenReturn("application/json; charset=UTF-8");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"requestBody\":{\"id\":\"test\"}}"));
        }

        /**
         * リクエスト処理終了時のメッセージにリクエストボディを出力できる。
         */
        @Test
        public void testEndFormatRequestBody() throws Exception {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("requestBody")
                    .messagePrefix("$").build());
            String requestBody = "{\"id\":\"test\"}";
            when(servletRequestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
            when(servletRequestMock.getContentLength()).thenReturn(requestBody.length());
            when(httpRequestMock.getHeader("Content-Type")).thenReturn("application/json; charset=UTF-8");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"requestBody\":{\"id\":\"test\"}}"));
        }

        /**
         * リクエストボディのルート要素が配列のJSON文字列の場合、リクエストボディを出力できる。
         */
        @Test
        public void testRequestBodyIfJsonArray() throws Exception {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("requestBody")
                    .messagePrefix("$").build());
            String requestBody = "[{\"id\":\"test\"},{\"id\":\"test\"}]";
            when(servletRequestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
            when(servletRequestMock.getContentLength()).thenReturn(requestBody.length());
            when(httpRequestMock.getHeader("Content-Type")).thenReturn("application/json; charset=UTF-8");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"requestBody\":[{\"id\":\"test\"},{\"id\":\"test\"}]}"));
        }

        /**
         * リクエストボディが空の場合、リクエストボディを出力しない。
         */
        @Test
        public void testRequestBodyIfEmpty() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("requestBody")
                    .messagePrefix("$").build());
            when(servletRequestMock.getContentLength()).thenReturn(0);
            when(httpRequestMock.getHeader("Content-Type")).thenReturn("application/json; charset=UTF-8");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${}"));
        }

        /**
         * リクエストボディの出力対象でないコンテンツタイプであれば、リクエストボディを出力しない。
         */
        @Test
        public void testRequestBodyIfOtherContentType() throws Exception {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("requestBody")
                    .messagePrefix("$").build());
            String requestBody = "{\"id\":\"test\"}";
            when(servletRequestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
            when(servletRequestMock.getContentLength()).thenReturn(requestBody.length());
            when(httpRequestMock.getHeader("Content-Type")).thenReturn("text/html");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${}"));
        }

        /**
         * リクエストボディがJSON形式の文字列でなければ、文字列として出力する。
         */
        @Test
        public void testRequestBodyIfDisabledFormat() throws Exception {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("requestBody")
                    .messagePrefix("$").build());
            String requestBody = "({\"id\":\"test\"})";
            when(servletRequestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
            when(servletRequestMock.getContentLength()).thenReturn(requestBody.length());
            when(httpRequestMock.getHeader("Content-Type")).thenReturn("application/json; charset=UTF-8");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"requestBody\":\"({\\\"id\\\":\\\"test\\\"})\"}"));
        }

        /**
         * リクエストのコンテンツタイプが設定されていなければ、リクエストボディを出力しない。
         */
        @Test
        public void testRequestBodyIfNoContentType() throws Exception {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("requestBody")
                    .messagePrefix("$").build());
            String requestBody = "{\"id\":\"test\"}";
            when(servletRequestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
            when(servletRequestMock.getContentLength()).thenReturn(requestBody.length());

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${}"));
        }

        /**
         * リクエストボディの読込でエラーが発生した場合、異常終了せずリクエストボディを出力しない。
         */
        @Test
        public void testRequestBodyIfReadError() throws Exception {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("requestBody")
                    .messagePrefix("$").build());
            // thenThrowでは明示されていない検査例外を送出できないためthenAnswerで代替
            when(servletRequestMock.getReader()).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    throw new IOException();
                }
            });
            when(servletRequestMock.getContentLength()).thenReturn(1);
            when(httpRequestMock.getHeader("Content-Type")).thenReturn("application/json; charset=UTF-8");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${}"));
        }

        /**
         * リクエスト処理終了時のメッセージにレスポンスボディを出力できる。
         */
        @Test
        public void testEndFormatResponseBody() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("responseBody")
                    .messagePrefix("$").build());
            String responseBody = "{\"id\":\"test\"}";
            when(httpResponseMock.getBodyStream()).thenReturn(new StringInputStream(responseBody));
            when(httpResponseMock.getCharset()).thenReturn(Charset.forName("UTF-8"));
            when(httpResponseMock.getHeader("Content-Type")).thenReturn("application/json");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"responseBody\":{\"id\":\"test\"}}"));
        }

        /**
         * レスポンスボディのルート要素が配列のJSON文字列の場合、レスポンスボディを出力できる。
         */
        @Test
        public void testResponseBodyIfJsonArray() throws Exception {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("responseBody")
                    .messagePrefix("$").build());
            String responseBody = "[{\"id\":\"test\"},{\"id\":\"test\"}]";
            when(httpResponseMock.getBodyStream()).thenReturn(new StringInputStream(responseBody));
            when(httpResponseMock.getCharset()).thenReturn(Charset.forName("UTF-8"));
            when(httpResponseMock.getHeader("Content-Type")).thenReturn("application/json");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"responseBody\":[{\"id\":\"test\"},{\"id\":\"test\"}]}"));
        }

        /**
         * レスポンスボディが空の場合、レスポンスボディを出力しない。
         */
        @Test
        public void testResponseBodyIfEmpty() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("responseBody")
                    .messagePrefix("$").build());
            when(httpResponseMock.isBodyEmpty()).thenReturn(true);
            when(httpResponseMock.getHeader("Content-Type")).thenReturn("application/json");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${}"));
        }

        /**
         * レスポンスボディの出力対象でないコンテンツタイプであれば、レスポンスボディを出力しない。
         */
        @Test
        public void testResponseBodyIfOtherContentType() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("responseBody")
                    .messagePrefix("$").build());
            String responseBody = "{\"id\":\"test\"}";
            when(httpResponseMock.getBodyStream()).thenReturn(new StringInputStream(responseBody));
            when(httpResponseMock.getCharset()).thenReturn(Charset.forName("UTF-8"));
            when(httpResponseMock.getHeader("Content-Type")).thenReturn("text/html");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${}"));
        }

        /**
         * レスポンスのコンテンツタイプが設定されていなければ、レスポンスボディを出力しない。
         */
        @Test
        public void testResponseBodyIfNoContentType() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("responseBody")
                    .messagePrefix("$").build());
            String responseBody = "{\"id\":\"test\"}";
            when(httpResponseMock.getBodyStream()).thenReturn(new StringInputStream(responseBody));
            when(httpResponseMock.getCharset()).thenReturn(Charset.forName("UTF-8"));

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${}"));
        }

        /**
         * レスポンスボディがJSON形式の文字列でなければ、文字列として出力する。
         */
        @Test
        public void testResponseBodyIfDisabledFormat() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("responseBody")
                    .messagePrefix("$").build());
            String responseBody = "({\"id\":\"test\"})";
            when(httpResponseMock.getBodyStream()).thenReturn(new StringInputStream(responseBody));
            when(httpResponseMock.getCharset()).thenReturn(Charset.forName("UTF-8"));
            when(httpResponseMock.getHeader("Content-Type")).thenReturn("application/json");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"responseBody\":\"({\\\"id\\\":\\\"test\\\"})\"}"));
        }

        /**
         * レスポンスボディの読込でエラーが発生した場合、異常終了せずレスポンスボディを出力しない。
         */
        @Test
        public void testResponseBodyIfReadError() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("responseBody")
                    .messagePrefix("$").build());
            // thenThrowでは明示されていない検査例外を送出できないためthenAnswerで代替
            when(httpResponseMock.getBodyStream()).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    throw new IOException();
                }
            });
            when(httpResponseMock.getCharset()).thenReturn(Charset.forName("UTF-8"));
            when(httpResponseMock.getHeader("Content-Type")).thenReturn("application/json");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${}"));
        }

        /**
         * リクエスト処理開始時のメッセージに未定義の項目があれば例外を送出する。
         */
        @Test(expected = IllegalArgumentException.class)
        public void testBeginFormatIfUnknownItem() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("hoge")
                    .messagePrefix("$").build());

            sut.formatBegin(logContext);
        }

        /**
         * リクエスト処理終了時のメッセージに未定義の項目があれば例外を送出する。
         */
        @Test(expected = IllegalArgumentException.class)
        public void testEndFormatIfUnknownItem() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("hoge")
                    .messagePrefix("$").build());

            sut.formatEnd(logContext);
        }

        /**
         * リクエスト処理開始時のメッセージに項目が重複している場合、重複分を削除して出力できる。
         */
        @Test
        public void testBeginFormatIfDuplicatedTarget() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("requestId,requestId")
                    .messagePrefix("$").build());
            ThreadContext.setRequestId("test");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"requestId\":\"test\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージに項目が重複している場合、重複分を削除して出力できる。
         */
        @Test
        public void testEndFormatIfDuplicatedTarget() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("requestId,requestId")
                    .messagePrefix("$").build());
            ThreadContext.setRequestId("test");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"requestId\":\"test\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージに項目名が指定されていない場合、出力されない。
         */
        @Test
        public void testBeginFormatIfEmptyTarget() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets(", ")
                    .messagePrefix("$").build());

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${}"));
        }

        /**
         * リクエスト処理終了時のメッセージに項目名が指定されていない場合、出力されない。
         */
        @Test
        public void testEndFormatIfEmptyTarget() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled(  "true").endTargets(", ")
                    .messagePrefix("$").build());

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${}"));
        }

        /**
         * リクエスト処理開始時のメッセージに未定義の項目があれば例外を送出する。
         */
        @Test(expected = IllegalArgumentException.class)
        public void testBeginFormatIfUnknownTarget() {
            sut.initialize(new AppLogPropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("hoge")
                    .messagePrefix("$").build());

            sut.formatBegin(logContext);
        }

        /**
         * リクエスト処理終了時のメッセージに未定義の項目があれば例外を送出する。
         */
        @Test(expected = IllegalArgumentException.class)
        public void testEndFormatIfUnknownTarget() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("hoge")
                    .messagePrefix("$").build());

            sut.formatEnd(logContext);
        }
    }

    public static class ContainsMemoryItem {

        private final JaxRsAccessJsonLogFormatter sut = new JaxRsAccessJsonLogFormatter();

        /**
         * リクエスト処理終了時のメッセージに最大メモリ量が含まれている場合はtrue。
         */
        @Test
        public void testContainsMemoryItemIfMaxMemory() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("maxMemory").build());

            boolean actual = sut.containsMemoryItem();

            assertThat(actual, is(true));
        }

        /**
         * リクエスト処理終了時のメッセージに空きメモリ量が含まれている場合はtrue。
         */
        @Test
        public void testContainsMemoryItemIfFreeMemory() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("freeMemory").build());

            boolean actual = sut.containsMemoryItem();

            assertThat(actual, is(true));
        }

        /**
         * リクエスト処理終了時のメッセージにメモリ情報が含まれていない場合はfalse。
         */
        @Test
        public void testContainsMemoryItemIfNothing() {
            sut.initialize(new AppLogPropertyBuilder()
                    .endOutputEnabled("true").endTargets("requestId").build());

            boolean actual = sut.containsMemoryItem();

            assertThat(actual, is(false));
        }
    }
}
