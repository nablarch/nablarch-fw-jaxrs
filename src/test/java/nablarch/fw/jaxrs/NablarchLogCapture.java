package nablarch.fw.jaxrs;

import nablarch.core.log.basic.LogLevel;
import nablarch.core.log.basic.StandardOutputLogWriter;
import nablarch.core.util.StringUtil;
import org.junit.rules.ExternalResource;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Nablarchログの出力内容を検証するためのルール。
 * <p>
 * {@link StandardOutputLogWriter}を使用して出力されたログの内容を保持し、
 * テストクラスから参照可能にする。
 */
public class NablarchLogCapture extends ExternalResource {

    /** 標準出力。 */
    private PrintStream systemOut;

    /** 標準出力への出力内容をキャプチャするためのラッパー。 */
    private CapturingStream capturingStream;

    @Override
    protected void before() throws Throwable {
        systemOut = System.out;
        capturingStream = new CapturingStream(System.out, createLogLinePattern());
        System.setOut(capturingStream);
    }

    @Override
    protected void after() {
        System.setOut(systemOut);
    }

    /**
     * ログ出力内容を取得する。
     *
     * @return ログ出力内容
     */
    public List<String> getLogLines() {
        return Collections.unmodifiableList(capturingStream.logLines);
    }

    /**
     * TRACEレベルのログに指定されたメッセージが含まれているかチェックする。
     *
     * @param message メッセージ
     * @return 含まれている場合は {@code true}
     */
    public boolean containsTraceMessage(String message) {
        return containsMessage(message, LogLevel.TRACE);
    }

    /**
     * DEBUGレベルのログに指定されたメッセージが含まれているかチェックする。
     *
     * @param message メッセージ
     * @return 含まれている場合は {@code true}
     */
    public boolean containsDebugMessage(String message) {
        return containsMessage(message, LogLevel.DEBUG);
    }

    /**
     * INFOレベルのログに指定されたメッセージが含まれているかチェックする。
     *
     * @param message メッセージ
     * @return 含まれている場合は {@code true}
     */
    public boolean containsInfoMessage(String message) {
        return containsMessage(message, LogLevel.INFO);
    }

    /**
     * WARNレベルのログに指定されたメッセージが含まれているかチェックする。
     *
     * @param message メッセージ
     * @return 含まれている場合は {@code true}
     */
    public boolean containsWarnMessage(String message) {
        return containsMessage(message, LogLevel.WARN);
    }

    /**
     * ERRORレベルのログに指定されたメッセージが含まれているかチェックする。
     *
     * @param message メッセージ
     * @return 含まれている場合は {@code true}
     */
    public boolean containsErrorMessage(String message) {
        return containsMessage(message, LogLevel.ERROR);
    }

    /**
     * FATALレベルのログに指定されたメッセージが含まれているかチェックする。
     *
     * @param message メッセージ
     * @return 含まれている場合は {@code true}
     */
    public boolean containsFatalMessage(String message) {
        return containsMessage(message, LogLevel.FATAL);
    }

    /**
     * 出力内容がログであるか判定するための正規表現を生成する。
     *
     * @return 正規表現
     */
    protected Pattern createLogLinePattern() {
        return createLogLevelPattern(LogLevel.values());
    }

    /**
     * 出力内容のログレベルを判定するための正規表現を生成する。
     *
     * @param levels ログレベル
     * @return 正規表現
     */
    protected Pattern createLogLevelPattern(LogLevel... levels) {
        if (levels.length == 0) {
            throw new IllegalArgumentException("Log level not specified.");
        }
        StringBuilder buf = new StringBuilder();
        buf.append(" -");
        if (levels.length == 1) {
            buf.append(levels[0]);
        } else {
            buf.append("(");
            List<String> list = new ArrayList<String>();
            for (LogLevel level : levels) {
                list.add(level.toString());
            }
            buf.append(StringUtil.join("|", list));
            buf.append(")");

        }
        buf.append("- ");
        return Pattern.compile(buf.toString());
    }

    /**
     * ログに指定されたレベルのメッセージが含まれているかチェックする。
     *
     * @param message メッセージ
     * @param level ログレベル
     * @return 含まれている場合は {@code true}
     */
    protected boolean containsMessage(String message, LogLevel level) {
        Pattern logLevelPattern = createLogLevelPattern(level);
        for (String log : capturingStream.logLines) {
            if (logLevelPattern.matcher(log).find() && log.contains(message)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ログ出力内容をキャプチャするためのラッパー。
     */
    public static class CapturingStream extends PrintStream {

        /** ログ出力内容 */
        private final List<String> logLines = new ArrayList<String>();

        /** ログであるか判定するための正規表現 */
        private final Pattern logLinePattern;

        /**
         * コンストラクタ
         *
         * @param out キャプチャ対象のStream
         * @param logLinePattern ログであるか判定するための正規表現
         */
        public CapturingStream(PrintStream out, Pattern logLinePattern) {
            super(out);
            this.logLinePattern = logLinePattern;
        }

        @Override
        public void print(String message) {
            super.print(message);
            if (logLinePattern.matcher(message).find()) {
                logLines.add(message);
            }
        }
    }
}
