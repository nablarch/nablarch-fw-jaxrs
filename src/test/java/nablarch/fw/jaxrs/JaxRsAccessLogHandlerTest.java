package nablarch.fw.jaxrs;

import nablarch.core.log.LogUtil;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

/**
 * {@link JaxRsAccessLogHandler}のテスト。
 */
public class JaxRsAccessLogHandlerTest {

    @Rule
    public RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public NablarchLogCapture logCapture = new NablarchLogCapture();

    @Before
    public void setUp() throws Exception {
        LogUtil.removeAllObjectsBoundToContextClassLoader();
    }

    /**
     * アクセスログ出力が有効であれば、開始および終了ログがINFOレベルで出力される。
     */
    @Test
    public void testOutputLog() {
        System.setProperty("jaxRsAccessLogFormatter.className", LogOutputMock.class.getName());
        JaxRsAccessLogHandler sut = new JaxRsAccessLogHandler();

        sut.handle(null, mock(ServletExecutionContext.class));

        assertThat(logCapture.containsInfoMessage("formatBegin"), is(true));
        assertThat(logCapture.containsInfoMessage("formatEnd"), is(true));
    }

    /**
     * アクセスログ出力が無効であれば、開始および終了ログが出力されない。
     */
    @Test
    public void testNoOutputLog() {
        System.setProperty("jaxRsAccessLogFormatter.className", LogNoOutputMock.class.getName());
        JaxRsAccessLogHandler sut = new JaxRsAccessLogHandler();

        sut.handle(null, mock(ServletExecutionContext.class));

        assertThat(logCapture.containsInfoMessage("formatBegin"), is(false));
        assertThat(logCapture.containsInfoMessage("formatEnd"), is(false));
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
            return "formatEnd";
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
