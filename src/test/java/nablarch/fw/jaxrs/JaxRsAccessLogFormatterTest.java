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

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.Charset;
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
public class JaxRsAccessLogFormatterTest {

    /**
     * リクエスト処理開始時の出力有無
     */
    public static class BeginOutputEnabled {

        private final JaxRsAccessLogFormatter sut = new JaxRsAccessLogFormatter();

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

        private final JaxRsAccessLogFormatter sut = new JaxRsAccessLogFormatter();

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
    public static class LogItem {

        private final JaxRsAccessLogFormatter sut = new JaxRsAccessLogFormatter();

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
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$requestId$]").build());
            ThreadContext.setRequestId("test");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[test]"));
        }

        /**
         * リクエスト処理終了時のメッセージにリクエストIDを出力できる。
         */
        @Test
        public void testEndFormatRequestId() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$requestId$]").build());
            ThreadContext.setRequestId("test");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[test]"));
        }

        /**
         * スレッドコンテキストにリクエストIDが設定されていない場合はnullを出力する。
         */
        @Test
        public void testFormatRequestIdIfUndefined() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$requestId$]").build());

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[null]"));
        }

        /**
         * リクエスト処理開始時のメッセージにユーザーIDを出力できる。
         */
        @Test
        public void testBeginFormatUserId() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$userId$]").build());
            ThreadContext.setUserId("test");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[test]"));
        }

        /**
         * リクエスト処理終了時のメッセージにユーザーIDを出力できる。
         */
        @Test
        public void testEndFormatUserId() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$userId$]").build());
            ThreadContext.setUserId("test");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[test]"));
        }

        /**
         * スレッドコンテキストにリクエストIDが設定されていない場合はnullを出力する。
         */
        @Test
        public void testFormatUserIdIfUndefined() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$userId$]").build());

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[null]"));
        }

        /**
         * リクエスト処理開始時のメッセージにURLを出力できる。
         */
        @Test
        public void testBeginFormatUrl() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$url$]").build());
            when(servletRequestMock.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[http://localhost]"));
        }

        /**
         * リクエスト処理終了時のメッセージにURLを出力できる。
         */
        @Test
        public void testEndFormatUrl() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$url$]").build());
            when(servletRequestMock.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[http://localhost]"));
        }

        /**
         * リクエスト処理開始時のメッセージにクエリ文字列を出力できる。
         */
        @Test
        public void testBeginFormatQuery() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$query$]").build());
            when(servletRequestMock.getQueryString()).thenReturn("param=value");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[?param=value]"));
        }

        /**
         * リクエスト処理終了時のメッセージにクエリ文字列を出力できる。
         */
        @Test
        public void testEndFormatQuery() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$query$]").build());
            when(servletRequestMock.getQueryString()).thenReturn("param=value");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[?param=value]"));
        }

        /**
         * リクエスト処理開始時のメッセージにポート番号を出力できる。
         */
        @Test
        public void testBeginFormatPort() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$port$]").build());
            when(servletRequestMock.getServerPort()).thenReturn(8080);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[8080]"));
        }

        /**
         * リクエスト処理終了時のメッセージにポート番号を出力できる。
         */
        @Test
        public void testEndFormatPort() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$port$]").build());
            when(servletRequestMock.getServerPort()).thenReturn(8080);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[8080]"));
        }

        /**
         * リクエスト処理開始時のメッセージにHTTPメソッドを出力できる。
         */
        @Test
        public void testBeginFormatMethod() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$method$]").build());
            when(httpRequestMock.getMethod()).thenReturn("GET");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[GET]"));
        }

        /**
         * リクエスト処理終了時のメッセージにHTTPメソッドを出力できる。
         */
        @Test
        public void testEndFormatMethod() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$method$]").build());
            when(httpRequestMock.getMethod()).thenReturn("GET");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[GET]"));
        }

        /**
         * リクエスト処理開始時のメッセージにHTTPセッションIDを出力できる。
         */
        @Test
        public void testBeginFormatSessionId() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$sessionId$]").build());
            HttpSessionWrapper sessionMock = mock(HttpSessionWrapper.class);
            when(sessionMock.getId()).thenReturn("id");
            when(servletRequestMock.getSession(false)).thenReturn(sessionMock);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[id]"));
        }

        /**
         * リクエスト処理終了時のメッセージにHTTPセッションIDを出力できる。
         */
        @Test
        public void testEndFormatSessionId() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$sessionId$]").build());
            HttpSessionWrapper sessionMock = mock(HttpSessionWrapper.class);
            when(sessionMock.getId()).thenReturn("id");
            when(servletRequestMock.getSession(false)).thenReturn(sessionMock);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[id]"));
        }

        /**
         * リクエスト処理開始時のメッセージにセッションストアIDを出力できる。
         */
        @Test
        public void testBeginFormatSessionStoreId() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$sessionStoreId$]").build());
            when(executionContextMock.getRequestScopedVar("nablarch_internal_session_store_id")).thenReturn("id");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[id]"));
        }

        /**
         * リクエスト処理終了時のメッセージにセッションストアIDを出力できる。
         */
        @Test
        public void testEndFormatSessionStoreId() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$sessionStoreId$]").build());
            when(executionContextMock.getRequestScopedVar("nablarch_internal_session_store_id")).thenReturn("id");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[id]"));
        }

        /**
         * リクエスト処理開始時のメッセージにクライアント端末IPアドレスを出力できる。
         */
        @Test
        public void testBeginFormatClientIpAddress() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$clientIpAddress$]").build());
            when(servletRequestMock.getRemoteAddr()).thenReturn("192.168.0.0");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[192.168.0.0]"));
        }

        /**
         * リクエスト処理終了時のメッセージにクライアント端末IPアドレスを出力できる。
         */
        @Test
        public void testEndFormatClientIpAddress() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$clientIpAddress$]").build());
            when(servletRequestMock.getRemoteAddr()).thenReturn("192.168.0.0");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[192.168.0.0]"));
        }

        /**
         * リクエスト処理開始時のメッセージにクライアント端末ホストを出力できる。
         */
        @Test
        public void testBeginFormatClientHost() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$clientHost$]").build());
            when(servletRequestMock.getRemoteHost()).thenReturn("localhost");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[localhost]"));
        }

        /**
         * リクエスト処理終了時のメッセージにクライアント端末ホストを出力できる。
         */
        @Test
        public void testEndFormatClientHost() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$clientHost$]").build());
            when(servletRequestMock.getRemoteHost()).thenReturn("localhost");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[localhost]"));
        }

        /**
         * リクエスト処理開始時のメッセージにHTTPヘッダのUser-Agentを出力できる。
         */
        @Test
        public void testBeginFormatUserAgent() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$clientUserAgent$]").build());
            when(servletRequestMock.getHeader("User-Agent")).thenReturn("agent");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[agent]"));
        }

        /**
         * リクエスト処理終了時のメッセージにHTTPヘッダのUser-Agentを出力できる。
         */
        @Test
        public void testEndFormatUserAgent() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$clientUserAgent$]").build());
            when(servletRequestMock.getHeader("User-Agent")).thenReturn("agent");

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[agent]"));
        }

        /**
         * リクエスト処理開始時のメッセージにリクエストパラメータを出力できる。
         */
        @Test
        public void testBeginFormatParameter() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$parameters$]")
                    .parametersSeparator("@").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            parameter.put("param1", new String[]{"value1"});
            parameter.put("param2", new String[]{"value2"});
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[{param1 = [value1],@param2 = [value2]}]"));
        }

        /**
         * リクエスト処理終了時のメッセージにリクエストパラメータを出力できる。
         */
        @Test
        public void testEndFormatParameter() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$parameters$]")
                    .parametersSeparator("@").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            parameter.put("param1", new String[]{"value1"});
            parameter.put("param2", new String[]{"value2"});
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[{param1 = [value1],@param2 = [value2]}]"));
        }

        /**
         * リクエストパラメータのセパレータが未設定の場合、デフォルトセパレータで出力する。
         */
        @Test
        public void testDefaultParameterSeparator() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$parameters$]").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            parameter.put("param1", new String[]{"value1"});
            parameter.put("param2", new String[]{"value2"});
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[{\n\t\tparam1 = [value1],\n\t\tparam2 = [value2]}]"));
        }

        /**
         * マスク文字とマスク対象パターンを指定してリクエストパラメータをマスクできる。
         */
        @Test
        public void testMaskParameter() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginFormat("[$parameters$]")
                    .maskingChar("x").maskingPatterns("param").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            parameter.put("para", new String[]{"value"});
            parameter.put("param", new String[]{"value"});
            parameter.put("1param", new String[]{"value"});
            parameter.put("param2", new String[]{"value"});
            parameter.put("3param3", new String[]{"value"});
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, containsString("para = [value]"));
            assertThat(actual, containsString("param = [xxxxx]"));
            assertThat(actual, containsString("1param = [xxxxx]"));
            assertThat(actual, containsString("param2 = [xxxxx]"));
            assertThat(actual, containsString("3param3 = [xxxxx]"));
        }

        /**
         * マスク対象パターンを複数指定してリクエストパラメータをマスクできる。
         */
        @Test
        public void testMaskParameterWithMultiplePattern() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginFormat("[$parameters$]")
                    .maskingChar("x").maskingPatterns("1pa,am2,3param3").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            parameter.put("para", new String[]{"value"});
            parameter.put("param", new String[]{"value"});
            parameter.put("1param", new String[]{"value"});
            parameter.put("param2", new String[]{"value"});
            parameter.put("3param3", new String[]{"value"});
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, containsString("para = [value]"));
            assertThat(actual, containsString("param = [value]"));
            assertThat(actual, containsString("1param = [xxxxx]"));
            assertThat(actual, containsString("param2 = [xxxxx]"));
            assertThat(actual, containsString("3param3 = [xxxxx]"));
        }

        /**
         * マスク文字を指定しなければデフォルトのマスク文字でリクエストパラメータをマスクできる。
         */
        @Test
        public void testMaskParameterWithDefaultMaskingChar() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginFormat("[$parameters$]")
                    .maskingPatterns("param").build());
            Map<String, String[]> parameter = new HashMap<String, String[]>();
            parameter.put("param", new String[]{"value"});
            when(httpRequestMock.getParamMap()).thenReturn(parameter);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, containsString("param = [*****]"));
        }

        /**
         * リクエスト処理開始時のメッセージにセッションスコープ情報を出力できる。
         */
        @Test
        public void testBeginFormatSessionScope() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$sessionScope$]")
                    .sessionScopeSeparator("@").build());
            Map<String, Object> scope = new HashMap<String, Object>();
            scope.put("param1", "value1");
            scope.put("param2", "value2");
            when(executionContextMock.hasSession()).thenReturn(true);
            when(executionContextMock.getSessionScopeMap()).thenReturn(scope);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[{param1 = [value1],@param2 = [value2]}]"));
        }

        /**
         * リクエスト処理終了時のメッセージにセッションスコープ情報を出力できる。
         */
        @Test
        public void testEndFormatSessionScope() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$sessionScope$]")
                    .sessionScopeSeparator("@").build());
            Map<String, Object> scope = new HashMap<String, Object>();
            scope.put("param1", "value1");
            scope.put("param2", "value2");
            when(executionContextMock.hasSession()).thenReturn(true);
            when(executionContextMock.getSessionScopeMap()).thenReturn(scope);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[{param1 = [value1],@param2 = [value2]}]"));
        }

        /**
         * セッションスコープ情報のセパレータが未設定の場合、デフォルトセパレータで出力する。
         */
        @Test
        public void testDefaultSessionSeparator() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$sessionScope$]").build());
            Map<String, Object> scope = new HashMap<String, Object>();
            scope.put("param1", "value1");
            scope.put("param2", "value2");
            when(executionContextMock.hasSession()).thenReturn(true);
            when(executionContextMock.getSessionScopeMap()).thenReturn(scope);

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[{\n\t\tparam1 = [value1],\n\t\tparam2 = [value2]}]"));
        }

        /**
         * マスク文字とマスク対象パターンを指定してセッションスコープ情報をマスクできる。
         */
        @Test
        public void testMaskSessionScope() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginFormat("[$sessionScope$]")
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

            assertThat(actual, containsString("para = [value]"));
            assertThat(actual, containsString("param = [xxxxx]"));
            assertThat(actual, containsString("1param = [xxxxx]"));
            assertThat(actual, containsString("param2 = [xxxxx]"));
            assertThat(actual, containsString("3param3 = [xxxxx]"));
        }

        /**
         * マスク対象パターンを複数指定してセッションスコープ情報をマスクできる。
         */
        @Test
        public void testMaskSessionScopeWithMultiplePattern() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginFormat("[$sessionScope$]")
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

            assertThat(actual, containsString("para = [value]"));
            assertThat(actual, containsString("param = [value]"));
            assertThat(actual, containsString("1param = [xxxxx]"));
            assertThat(actual, containsString("param2 = [xxxxx]"));
            assertThat(actual, containsString("3param3 = [xxxxx]"));
        }

        /**
         * マスク文字を指定しなければデフォルトのマスク文字でセッションスコープ情報をマスクできる。
         */
        @Test
        public void testMaskSessionScopeWithDefaultMaskingChar() {
            sut.initialize(new PropertyBuilder()
                    .beginOutputEnabled("true").beginFormat("[$sessionScope$]")
                    .maskingPatterns("param").build());
            Map<String, Object> scope = new HashMap<String, Object>();
            scope.put("param", "value");
            when(executionContextMock.hasSession()).thenReturn(true);
            when(executionContextMock.getSessionScopeMap()).thenReturn(scope);


            String actual = sut.formatBegin(logContext);

            assertThat(actual, containsString("param = [*****]"));
        }

        /**
         * リクエスト処理終了時のメッセージにステータスコードを出力できる。
         */
        @Test
        public void testEndFormatStatusCode() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$statusCode$]").build());
            when(httpResponseMock.getStatusCode()).thenReturn(200);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[200]"));
        }

        /**
         * リクエスト処理終了時のメッセージに開始日時を出力できる。
         */
        @Test
        public void testEndFormatStartTime() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$startTime$]")
                    .datePattern("yyyy/MM/dd").build());
            long startTime = new GregorianCalendar(2023, 0, 31).getTimeInMillis();
            logContext.setStartTime(startTime);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[2023/01/31]"));
        }

        /**
         * リクエスト処理終了時のメッセージに開始日時をデフォルト書式で出力できる。
         */
        @Test
        public void testEndFormatStartTimeWithDefaultFormat() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$startTime$]").build());
            long startTime = new GregorianCalendar(2023, 0, 31, 9, 59, 0).getTimeInMillis();
            logContext.setStartTime(startTime);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[2023-01-31 09:59:00.000]"));
        }

        /**
         * リクエスト処理終了時のメッセージに終了日時を出力できる。
         */
        @Test
        public void testEndFormatEndTime() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$endTime$]")
                    .datePattern("yyyy/MM/dd").build());
            long endTime = new GregorianCalendar(2023, 0, 31).getTimeInMillis();
            logContext.setEndTime(endTime);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[2023/01/31]"));
        }

        /**
         * リクエスト処理終了時のメッセージに終了日時をデフォルト書式で出力できる。
         */
        @Test
        public void testEndFormatEndTimeWithDefaultFormat() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$endTime$]").build());
            long endTime = new GregorianCalendar(2023, 0, 31, 9, 59, 0).getTimeInMillis();
            logContext.setEndTime(endTime);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[2023-01-31 09:59:00.000]"));
        }

        /**
         * リクエスト処理終了時のメッセージに実行時間を出力できる。
         */
        @Test
        public void testEndFormatExecutionTime() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$executionTime$]").build());
            long startTime = new GregorianCalendar(2023, 0, 1, 12, 0).getTimeInMillis();
            logContext.setStartTime(startTime);
            long endTime = new GregorianCalendar(2023, 0, 1, 13, 0).getTimeInMillis();
            logContext.setEndTime(endTime);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("["+ 1000 * 60 * 60 + "]"));
        }

        /**
         * リクエスト処理終了時のメッセージに最大メモリ量を出力できる。
         */
        @Test
        public void testEndFormatMaxMemory() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$maxMemory$]").build());
            logContext.setMaxMemory(100);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[100]"));
        }

        /**
         * リクエスト処理終了時のメッセージに空きメモリ量を出力できる。
         */
        @Test
        public void testEndFormatFreeMemory() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$freeMemory$]").build());
            logContext.setFreeMemory(200);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[200]"));
        }

        /**
         * リクエスト処理開始時のメッセージにリクエストボディを出力できる。
         */
        @Test
        public void testBeginFormatRequestBody() throws Exception {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").beginFormat("[$requestBody$]").build());
            String requestBody = "{\"id\":\"test\"}";
            when(servletRequestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
            when(servletRequestMock.getContentLength()).thenReturn(requestBody.length());

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("[{\"id\":\"test\"}]"));
        }

        /**
         * リクエスト処理終了時のメッセージにレスポンスボディを出力できる。
         */
        @Test
        public void testEndFormatResponseBody() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$responseBody$]").build());
            String responseBody = "{\"id\":\"test\"}";
            when(httpResponseMock.getBodyStream()).thenReturn(new StringInputStream(responseBody));
            when(httpResponseMock.getCharset()).thenReturn(Charset.forName("UTF-8"));

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("[{\"id\":\"test\"}]"));
        }

        /**
         * リクエスト処理開始時のフォーマットを指定していない場合、デフォルトフォーマットで出力する。
         */
        @Test
        public void testDefaultBeginFormat() {
            sut.initialize(new PropertyBuilder().beginOutputEnabled("true").build());

            ThreadContext.setRequestId("testRequestId");
            ThreadContext.setUserId("testUserId");

            HttpSessionWrapper sessionMock = mock(HttpSessionWrapper.class);
            when(sessionMock.getId()).thenReturn("testSessionId");
            when(servletRequestMock.getSession(false)).thenReturn(sessionMock);

            when(servletRequestMock.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));
            when(servletRequestMock.getQueryString()).thenReturn("param=value");
            when(servletRequestMock.getServerPort()).thenReturn(8080);
            when(servletRequestMock.getRemoteAddr()).thenReturn("192.168.0.0");
            when(servletRequestMock.getRemoteHost()).thenReturn("localhost");
            when(httpRequestMock.getMethod()).thenReturn("GET");

            String actual = sut.formatBegin(logContext);

            assertThat(actual, is("@@@@ BEGIN @@@@ rid = [testRequestId] uid = [testUserId] sid = [testSessionId]"
                    + "\n\turl         = [http://localhost]"
                    + "\n\tmethod      = [GET]"
                    + "\n\tport        = [8080]"
                    + "\n\tclient_ip   = [192.168.0.0]"
                    + "\n\tclient_host = [localhost]"));
        }

        /**
         * リクエスト処理終了時のフォーマットを指定していない場合、デフォルトフォーマットで出力する。
         */
        @Test
        public void testDefaultEndFormat() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").build());

            ThreadContext.setRequestId("testRequestId");
            ThreadContext.setUserId("testUserId");

            HttpSessionWrapper sessionMock = mock(HttpSessionWrapper.class);
            when(sessionMock.getId()).thenReturn("testSessionId");
            when(servletRequestMock.getSession(false)).thenReturn(sessionMock);

            when(servletRequestMock.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));
            when(httpResponseMock.getStatusCode()).thenReturn(200);

            long startTime = new GregorianCalendar(2023, 11, 31).getTimeInMillis();
            logContext.setStartTime(startTime);
            long endTime = new GregorianCalendar(2024, 0, 1).getTimeInMillis();
            logContext.setEndTime(endTime);

            logContext.setMaxMemory(999);
            logContext.setFreeMemory(1);

            String actual = sut.formatEnd(logContext);

            assertThat(actual, is("@@@@ END @@@@ rid = [testRequestId] uid = [testUserId] sid = [testSessionId] url = [http://localhost] status_code = [200]"
                    + "\n\tstart_time     = [2023-12-31 00:00:00.000]"
                    + "\n\tend_time       = [2024-01-01 00:00:00.000]"
                    + "\n\texecution_time = [" + 1000 * 60 * 60 * 24 + "]"
                    + "\n\tmax_memory     = [999]"
                    + "\n\tfree_memory    = [1]"));
        }

    }

    public static class ContainsMemoryItem {

        private final JaxRsAccessLogFormatter sut = new JaxRsAccessLogFormatter();

        /**
         * リクエスト処理終了時のメッセージに最大メモリ量が含まれている場合はtrue。
         */
        @Test
        public void testContainsMemoryItemIfMaxMemory() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$maxMemory$]").build());

            boolean actual = sut.containsMemoryItem();

            assertThat(actual, is(true));
        }

        /**
         * リクエスト処理終了時のメッセージに空きメモリ量が含まれている場合はtrue。
         */
        @Test
        public void testContainsMemoryItemIfFreeMemory() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$freeMemory$]").build());

            boolean actual = sut.containsMemoryItem();

            assertThat(actual, is(true));
        }

        /**
         * リクエスト処理終了時のメッセージにメモリ情報が含まれていない場合はfalse。
         */
        @Test
        public void testContainsMemoryItemIfNothing() {
            sut.initialize(new PropertyBuilder().endOutputEnabled("true").endFormat("[$requestId$]").build());

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

        public PropertyBuilder beginFormat(String value) {
            props.put("jaxRsAccessLogFormatter.beginFormat", value);
            return this;
        }

        public PropertyBuilder endFormat(String value) {
            props.put("jaxRsAccessLogFormatter.endFormat", value);
            return this;
        }

        public PropertyBuilder parametersSeparator(String value) {
            props.put("jaxRsAccessLogFormatter.parametersSeparator", value);
            return this;
        }

        public PropertyBuilder sessionScopeSeparator(String value) {
            props.put("jaxRsAccessLogFormatter.sessionScopeSeparator", value);
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

        public Map<String, String> build() {
            return Collections.unmodifiableMap(props);
        }
    }
}
