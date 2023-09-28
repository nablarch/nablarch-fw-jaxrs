package nablarch.fw.jaxrs;

import nablarch.core.log.LogUtil;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link JaxRsAccessLogHandler}のテスト。
 */
public class JaxRsAccessLogHandlerTest {

    @Rule
    public RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public NablarchLogCapture logCapture = new NablarchLogCapture();

    @Before
    public void setUp() {
        LogUtil.removeAllObjectsBoundToContextClassLoader();
    }

    /**
     * アクセスログ出力が有効であれば、開始および終了ログがINFOレベルで出力される。
     */
    @Test
    public void testOutputLog() {
        System.setProperty("jaxRsAccessLogFormatter.className", LogOutputMock.class.getName());
        ServletExecutionContext contextMock = mock(ServletExecutionContext.class);
        when(contextMock.handleNext(null)).thenReturn(new HttpResponse());
        JaxRsAccessLogHandler sut = new JaxRsAccessLogHandler();

        sut.handle(null, contextMock);

        assertThat(logCapture.containsInfoMessage("formatBegin"), is(true));
        assertThat(logCapture.containsInfoMessage("formatEnd:200"), is(true));
    }

    /**
     * エラーレスポンスが送出された場合、終了ログはエラーレスポンスから出力する。
     */
    @Test
    public void testErrorResponse() {
        System.setProperty("jaxRsAccessLogFormatter.className", LogOutputMock.class.getName());
        ServletExecutionContext contextMock = mock(ServletExecutionContext.class);
        when(contextMock.handleNext(null)).thenThrow(new HttpErrorResponse());
        JaxRsAccessLogHandler sut = new JaxRsAccessLogHandler();

        try {
            sut.handle(null, contextMock);
            fail();
        } catch (HttpErrorResponse e) {
            assertThat(logCapture.containsInfoMessage("formatEnd:400"), is(true));
        }
    }

    /**
     * アクセスログ出力が無効であれば、開始および終了ログが出力されない。
     */
    @Test
    public void testNoOutputLog() {
        System.setProperty("jaxRsAccessLogFormatter.className", LogNoOutputMock.class.getName());
        ServletExecutionContext contextMock = mock(ServletExecutionContext.class);
        when(contextMock.handleNext(null)).thenReturn(new HttpResponse());
        JaxRsAccessLogHandler sut = new JaxRsAccessLogHandler();

        sut.handle(null, contextMock);

        assertThat(logCapture.containsInfoMessage("formatBegin"), is(false));
        assertThat(logCapture.containsInfoMessage("formatEnd:200"), is(false));
    }

    /**
     * フォーマッターが定義されていない場合、デフォルトのフォーマッターが生成される。
     */
    @Test
    public void testCreateDefaultLogFormatter() {
        JaxRsAccessLogHandler sut = new JaxRsAccessLogHandler();

        JaxRsAccessLogFormatter logFormatter = sut.createLogFormatter(new HashMap<String, String>());

        assertThat(logFormatter, instanceOf(JaxRsAccessLogFormatter.class));
    }

    /**
     * アクセスログ出力を検証するためのモック。
     */
    public static class LogOutputMock extends JaxRsAccessLogFormatter {

        @Override
        public boolean isBeginOutputEnabled() {
            return true;
        }

        @Override
        public boolean isEndOutputEnabled() {
            return true;
        }

        @Override
        public String formatBegin(JaxRsAccessLogContext context) {
            return "formatBegin";
        }

        @Override
        public String formatEnd(JaxRsAccessLogContext context) {
            return "formatEnd:" + context.getResponse().getStatusCode();
        }
    }

    /**
     * アクセスログ出力の無効化を検証するためのモック。
     */
    public static class LogNoOutputMock extends JaxRsAccessLogFormatter {

        @Override
        public boolean isBeginOutputEnabled() {
            return false;
        }

        @Override
        public boolean isEndOutputEnabled() {
            return false;
        }
    }
}
