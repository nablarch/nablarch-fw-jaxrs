package nablarch.fw.jaxrs;

import nablarch.core.log.LogUtil;
import nablarch.fw.web.servlet.ServletExecutionContext;
import nablarch.test.support.log.app.OnMemoryLogWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

/**
 * {@link JaxRsAccessLogHandler}のテスト。
 */
public class JaxRsAccessLogHandlerTest {

    @Rule
    public RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Before
    public void setUp() throws Exception {
        OnMemoryLogWriter.clear();
        LogUtil.removeAllObjectsBoundToContextClassLoader();
    }

    @After
    public void tearDown() throws Exception {
        OnMemoryLogWriter.clear();
    }

    /**
     * リクエスト処理開始時のログ出力対象であれば開始ログが出力される
     */
    @Test
    public void testOutputLog() {
        System.setProperty("jaxRsAccessLogFormatter.className", LogOutputMock.class.getName());
        JaxRsAccessLogHandler sut = new JaxRsAccessLogHandler();

        sut.handle(null, mock(ServletExecutionContext.class));

        OnMemoryLogWriter.assertLogContains("writer.memory", "formatBegin", "formatEnd");
    }

    /**
     * リクエスト処理終了時のログ出力対象であれば終了ログが出力される
     */
    @Test
    public void testNoOutputLog() {
        System.setProperty("jaxRsAccessLogFormatter.className", NoLogOutputMock.class.getName());
        JaxRsAccessLogHandler sut = new JaxRsAccessLogHandler();

        sut.handle(null, mock(ServletExecutionContext.class));

        List<String> messages = OnMemoryLogWriter.getMessages("writer.memory");
        assertThat(messages.size(), is(0));
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
    public static class NoLogOutputMock extends JaxRsAccessLogFormatter {

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
