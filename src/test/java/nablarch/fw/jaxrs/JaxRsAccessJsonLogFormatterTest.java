package nablarch.fw.jaxrs;

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

import java.util.Collections;
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
            sut.initialize(new PropertyBuilder().build());

            assertThat(sut.isBeginOutputEnabled(), is(true));
        }

        /**
         * リクエスト処理開始時の出力有無が空定義であれば無効になる。
         */
        @Test
        public void testBeginOutputEnabledIfEmpty() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("").build());

            assertThat(sut.isBeginOutputEnabled(), is(false));
        }

        /**
         * リクエスト処理開始時の出力有無が"true"であれば有効になる。
         */
        @Test
        public void testBeginOutputEnabledIfTrue() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").build());

            assertThat(sut.isBeginOutputEnabled(), is(true));
        }

        /**
         * リクエスト処理開始時の出力有無が"false"であれば無効になる。
         */
        @Test
        public void testBeginOutputEnabledIfFalse() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("false").build());

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
            sut.initialize(new PropertyBuilder().build());

            assertThat(sut.isEndOutputEnabled(), is(true));
        }

        /**
         * リクエスト処理終了時の出力有無が空定義であれば無効になる。
         */
        @Test
        public void testEndOutputEnabledIfEmpty() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("").build());

            assertThat(sut.isEndOutputEnabled(), is(false));
        }

        /**
         * リクエスト処理終了時の出力有無が"true"であれば有効になる。
         */
        @Test
        public void testEndOutputEnabledIfTrue() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").build());

            assertThat(sut.isEndOutputEnabled(), is(true));
        }

        /**
         * リクエスト処理終了時の出力有無が"false"であれば無効になる。
         */
        @Test
        public void testEndOutputEnabledIfFalse() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("false").build());

            assertThat(sut.isEndOutputEnabled(), is(false));
        }
    }

    /**
     * ログ出力項目のフォーマット
     */
    public static class LogItemInRequest {

        private final JaxRsAccessJsonLogFormatter sut = new JaxRsAccessJsonLogFormatter();

        private final JaxRsAccessLogContext logContext = new JaxRsAccessLogContext();

        private final ServletExecutionContext executionContextMock = mock(ServletExecutionContext.class);

        private final HttpRequest httpRequestMock = mock(HttpRequest.class);

        private final NablarchHttpServletRequestWrapper servletRequestMock = mock(NablarchHttpServletRequestWrapper.class);

        private final HttpResponse httpResponseMock = mock(HttpResponse.class);

        @Before
        public void setUp() throws Exception {
            when(executionContextMock.getServletRequest()).thenReturn(servletRequestMock);
            logContext.setContext(executionContextMock);
            logContext.setRequest(httpRequestMock);
            logContext.setResponse(httpResponseMock);
        }

        @After
        public void tearDown() throws Exception {
            ThreadContext.clear();
        }

        /**
         * リクエスト処理開始時のメッセージにリクエストIDを出力できる。
         */
        @Test
        public void testBeginFormatRequestId() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("requestId").messagePrefix("$").build());
            ThreadContext.setRequestId("test");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"requestId\":\"test\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにリクエストIDを出力できる。
         */
        @Test
        public void testEndFormatRequestId() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("requestId").messagePrefix("$").build());
            ThreadContext.setRequestId("test");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"requestId\":\"test\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにユーザーIDを出力できる。
         */
        @Test
        public void testBeginFormatUserId() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("userId").messagePrefix("$").build());
            ThreadContext.setUserId("test");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"userId\":\"test\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにユーザーIDを出力できる。
         */
        @Test
        public void testEndFormatUserId() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("userId").messagePrefix("$").build());
            ThreadContext.setUserId("test");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"userId\":\"test\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにURLを出力できる。
         */
        @Test
        public void testBeginFormatUrl() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("url").messagePrefix("$").build());
            when(servletRequestMock.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"url\":\"http://localhost\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにURLを出力できる。
         */
        @Test
        public void testEndFormatUrl() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("url").messagePrefix("$").build());
            when(servletRequestMock.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));

            String actual = sut.formatEnd(logContext);
        }

        /**
         * リクエスト処理開始時のメッセージにクエリ文字列を出力できる。
         */
        @Test
        public void testBeginFormatQuery() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("queryString").messagePrefix("$").build());
            when(servletRequestMock.getQueryString()).thenReturn("param=value");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"queryString\":\"?param=value\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにクエリ文字列を出力できる。
         */
        @Test
        public void testEndFormatQuery() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("queryString").messagePrefix("$").build());
            when(servletRequestMock.getQueryString()).thenReturn("param=value");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"queryString\":\"?param=value\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにポート番号を出力できる。
         */
        @Test
        public void testBeginFormatPort() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("port").messagePrefix("$").build());
            when(servletRequestMock.getServerPort()).thenReturn(8080);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"port\":8080}"));
        }

        /**
         * リクエスト処理終了時のメッセージにポート番号を出力できる。
         */
        @Test
        public void testEndFormatPort() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("port").messagePrefix("$").build());
            when(servletRequestMock.getServerPort()).thenReturn(8080);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"port\":8080}"));
        }

        /**
         * リクエスト処理開始時のメッセージにHTTPメソッドを出力できる。
         */
        @Test
        public void testBeginFormatMethod() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("method").messagePrefix("$").build());
            when(httpRequestMock.getMethod()).thenReturn("GET");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"method\":\"GET\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにHTTPメソッドを出力できる。
         */
        @Test
        public void testEndFormatMethod() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("method").messagePrefix("$").build());
            when(httpRequestMock.getMethod()).thenReturn("GET");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"method\":\"GET\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにHTTPセッションIDを出力できる。
         */
        @Test
        public void testBeginFormatSessionId() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("sessionId").messagePrefix("$").build());
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
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("sessionId").messagePrefix("$").build());
            HttpSessionWrapper sessionMock = mock(HttpSessionWrapper.class);
            when(sessionMock.getId()).thenReturn("id");
            when(servletRequestMock.getSession(false)).thenReturn(sessionMock);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"sessionId\":\"id\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにセッションストアIDを出力できる。
         */
        @Test
        public void testBeginFormatSessionStoreId() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("sessionStoreId").messagePrefix("$").build());
            when(executionContextMock.getRequestScopedVar("nablarch_internal_session_store_id")).thenReturn("id");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"sessionStoreId\":\"id\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにセッションストアIDを出力できる。
         */
        @Test
        public void testEndFormatSessionStoreId() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("sessionStoreId").messagePrefix("$").build());
            when(executionContextMock.getRequestScopedVar("nablarch_internal_session_store_id")).thenReturn("id");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"sessionStoreId\":\"id\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにクライアント端末IPアドレスを出力できる。
         */
        @Test
        public void testBeginFormatClientIpAddress() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("clientIpAddress").messagePrefix("$").build());
            when(servletRequestMock.getRemoteAddr()).thenReturn("192.168.0.0");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"clientIpAddress\":\"192.168.0.0\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにクライアント端末IPアドレスを出力できる。
         */
        @Test
        public void testEndFormatClientIpAddress() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("clientIpAddress").messagePrefix("$").build());
            when(servletRequestMock.getRemoteAddr()).thenReturn("192.168.0.0");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"clientIpAddress\":\"192.168.0.0\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにクライアント端末ホストを出力できる。
         */
        @Test
        public void testBeginFormatClientHost() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("clientHost").messagePrefix("$").build());
            when(servletRequestMock.getRemoteHost()).thenReturn("localhost");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"clientHost\":\"localhost\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにクライアント端末ホストを出力できる。
         */
        @Test
        public void testEndFormatClientHost() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("clientHost").messagePrefix("$").build());
            when(servletRequestMock.getRemoteHost()).thenReturn("localhost");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"clientHost\":\"localhost\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにHTTPヘッダのUser-Agentを出力できる。
         */
        @Test
        public void testBeginFormatUserAgent() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("clientUserAgent").messagePrefix("$").build());
            when(servletRequestMock.getHeader("User-Agent")).thenReturn("agent");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("${\"clientUserAgent\":\"agent\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージにHTTPヘッダのUser-Agentを出力できる。
         */
        @Test
        public void testEndFormatUserAgent() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("clientUserAgent").messagePrefix("$").build());
            when(servletRequestMock.getHeader("User-Agent")).thenReturn("agent");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"clientUserAgent\":\"agent\"}"));
        }

        /**
         * リクエスト処理開始時のメッセージにリクエストパラメータを出力できる。
         */
        @Test
        public void testBeginFormatParameter() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("parameters").messagePrefix("$").build());
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
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("parameters").messagePrefix("$").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            parameter.put("param1", new String[]{"value1"});
            parameter.put("param2", new String[]{"value2"});
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"parameters\":{\"param1\":[\"value1\"],\"param2\":[\"value2\"]}}"));
        }

        /**
         * マスク文字とマスク対象パターンを指定してリクエストパラメータをマスクできる。
         */
        @Test
        public void testMaskParameter() {
            sut.initialize(new PropertyBuilder()
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
            sut.initialize(new PropertyBuilder()
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
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("parameters").messagePrefix("$")
                    .maskingPatterns("param").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            parameter.put("param", new String[]{"value"});
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, containsString("\"param\":[\"*****\"]"));
        }

        /**
         * リクエスト処理開始時のメッセージにセッションスコープ情報を出力できる。
         */
        @Test
        public void testBeginFormatSessionScope() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginTargets("sessionScope").messagePrefix("$").build());
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
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("sessionScope").messagePrefix("$").build());
            Map<String, Object> scope = new HashMap<String, Object>();
            scope.put("param1", "value1");
            scope.put("param2", "value2");
            when(executionContextMock.hasSession()).thenReturn(true);
            when(executionContextMock.getSessionScopeMap()).thenReturn(scope);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"sessionScope\":{\"param1\":\"value1\",\"param2\":\"value2\"}}"));
        }

        /**
         * マスク文字とマスク対象パターンを指定してセッションスコープ情報をマスクできる。
         */
        @Test
        public void testMaskSessionScope() {
            sut.initialize(new PropertyBuilder()
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
            sut.initialize(new PropertyBuilder()
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
            sut.initialize(new PropertyBuilder()
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
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("statusCode").messagePrefix("$").build());
            when(httpResponseMock.getStatusCode()).thenReturn(200);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"statusCode\":200}"));
        }

        /**
         * リクエスト処理終了時のメッセージに開始日時を出力できる。
         */
        @Test
        public void testEndFormatStartTime() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("startTime").messagePrefix("$")
                    .datePattern("yyyy/MM/dd").build());
            long time = new GregorianCalendar(2023, 0, 31).getTimeInMillis();
            logContext.setStartTime(time);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"startTime\":\"2023/01/31\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージに開始日時をデフォルト書式で出力できる。
         */
        @Test
        public void testEndFormatStartTimeWithDefaultFormat() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("startTime").messagePrefix("$").build());
            long time = new GregorianCalendar(2023, 0, 31, 9, 59, 0).getTimeInMillis();
            logContext.setStartTime(time);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"startTime\":\"2023-01-31 09:59:00.000\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージに終了日時を出力できる。
         */
        @Test
        public void testEndFormatEndTime() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("endTime").messagePrefix("$")
                    .datePattern("yyyy/MM/dd").build());
            long time = new GregorianCalendar(2023, 0, 31).getTimeInMillis();
            logContext.setEndTime(time);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"endTime\":\"2023/01/31\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージに終了日時をデフォルト書式で出力できる。
         */
        @Test
        public void testEndFormatEndTimeWithDefaultFormat() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("endTime").messagePrefix("$").build());
            long time = new GregorianCalendar(2023, 0, 31, 9, 59, 0).getTimeInMillis();
            logContext.setEndTime(time);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"endTime\":\"2023-01-31 09:59:00.000\"}"));
        }

        /**
         * リクエスト処理終了時のメッセージに最大メモリ量を出力できる。
         */
        @Test
        public void testEndFormatMaxMemory() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("maxMemory").messagePrefix("$").build());
            logContext.setMaxMemory(100);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"maxMemory\":100}"));
        }

        /**
         * リクエスト処理終了時のメッセージに空きメモリ量を出力できる。
         */
        @Test
        public void testEndFormatFreeMemory() {
            sut.initialize(new PropertyBuilder()
                    .endOutputEnabled("true").endTargets("freeMemory").messagePrefix("$").build());
            logContext.setFreeMemory(100);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("${\"freeMemory\":100}"));
        }

    }

    public static class ContainsMemoryItem {

        private final JaxRsAccessJsonLogFormatter sut = new JaxRsAccessJsonLogFormatter();

        /**
         * リクエスト処理終了時のメッセージに最大メモリ量が含まれている場合はtrue。
         */
        @Test
        public void testContainsMemoryItemIfMaxMemory() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endTargets("maxMemory").build());

            boolean actual = sut.containsMemoryItem();

            assertThat(actual, is(true));
        }

        /**
         * リクエスト処理終了時のメッセージに空きメモリ量が含まれている場合はtrue。
         */
        @Test
        public void testContainsMemoryItemIfFreeMemory() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endTargets("freeMemory").build());

            boolean actual = sut.containsMemoryItem();

            assertThat(actual, is(true));
        }

        /**
         * リクエスト処理終了時のメッセージにメモリ情報が含まれていない場合はfalse。
         */
        @Test
        public void testContainsMemoryItemIfNothing() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endTargets("requestId").build());

            boolean actual = sut.containsMemoryItem();

            assertThat(actual, is(false));
        }
    }

    /**
     * ログ出力プロパティ情報の作成補助。
     */
    private static class PropertyBuilder {

        private final Map<String, String> props = new HashMap<String, String>();

        public PropertyBuilder beginOutputEnabled(String value) {
            props.put("jaxRsAccessLogFormatter.beginOutputEnabled", value);
            return this;
        }

        public PropertyBuilder endOutputEnabled(String value) {
            props.put("jaxRsAccessLogFormatter.endOutputEnabled", value);
            return this;
        }

        public PropertyBuilder beginTargets(String value) {
            props.put("jaxRsAccessLogFormatter.beginTargets", value);
            return this;
        }

        public PropertyBuilder endTargets(String value) {
            props.put("jaxRsAccessLogFormatter.endTargets", value);
            return this;
        }

        public PropertyBuilder datePattern(String value) {
            props.put("jaxRsAccessLogFormatter.datePattern", value);
            return this;
        }

        public PropertyBuilder maskingChar(String value) {
            props.put("jaxRsAccessLogFormatter.maskingChar", value);
            return this;
        }

        public PropertyBuilder maskingPatterns(String value) {
            props.put("jaxRsAccessLogFormatter.maskingPatterns", value);
            return this;
        }

        public PropertyBuilder messagePrefix(String value) {
            props.put("jaxRsAccessLogFormatter.structuredMessagePrefix", value);
            return this;
        }


        public Map<String, String> build() {
            return Collections.unmodifiableMap(props);
        }
    }
}
