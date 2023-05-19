package nablarch.fw.jaxrs;

import nablarch.common.web.session.InternalSessionUtil;
import nablarch.core.ThreadContext;
import nablarch.core.log.DateItemSupport;
import nablarch.core.log.LogItem;
import nablarch.core.log.LogUtil;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.log.MaskingMapItemSupport;
import nablarch.core.util.ObjectUtil;
import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.NablarchHttpServletRequestWrapper;
import nablarch.fw.web.servlet.ServletExecutionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * RESTfulウェブサービスのアクセスログのメッセージをフォーマットするクラス。
 */
@Published(tag = "architect")
public class JaxRsAccessLogFormatter {

    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(JaxRsAccessLogFormatter.class);

    /** デフォルトの日時フォーマット */
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    /** デフォルトのリクエスト処理開始時のフォーマット */
    private static final String DEFAULT_BEGIN_FORMAT = "@@@@ BEGIN @@@@ rid = [$requestId$] uid = [$userId$] sid = [$sessionId$]"
            + "\n\turl         = [$url$]"
            + "\n\tmethod      = [$method$]"
            + "\n\tport        = [$port$]"
            + "\n\tclient_ip   = [$clientIpAddress$]"
            + "\n\tclient_host = [$clientHost$]";

    /** デフォルトのリクエスト処理終了時のフォーマット */
    private static final String DEFAULT_END_FORMAT = "@@@@ END @@@@ rid = [$requestId$] uid = [$userId$] sid = [$sessionId$]"
            + " url = [$url$] status_code = [$statusCode$]"
            + "\n\tstart_time     = [$startTime$]"
            + "\n\tend_time       = [$endTime$]"
            + "\n\texecution_time = [$executionTime$]"
            + "\n\tmax_memory     = [$maxMemory$]"
            + "\n\tfree_memory    = [$freeMemory$]";

    /** デフォルトのマスク文字 */
    private static final String DEFAULT_MASKING_CHAR = "*";

    /** デフォルトのマスク対象のパターン */
    private static final Pattern[] DEFAULT_MASKING_PATTERNS = new Pattern[0];

    /** デフォルトのリクエストパラメータの区切り文字 */
    private static final String DEFAULT_PARAMETERS_SEPARATOR = Logger.LS + "\t\t";

    /** デフォルトのセッションスコープ情報の区切り文字 */
    private static final String DEFAULT_SESSION_SCOPE_SEPARATOR = Logger.LS + "\t\t";

    /** デフォルトのリクエスト処理開始時の出力が有効か否か。 */
    private static final String DEFAULT_BEGIN_OUTPUT_ENABLED = Boolean.TRUE.toString();

    /** デフォルトのリクエスト処理終了時の出力が有効か否か。 */
    private static final String DEFAULT_END_OUTPUT_ENABLED = Boolean.TRUE.toString();

    /** プロパティ名のプレフィックス */
    public static final String PROPS_PREFIX = "jaxRsAccessLogFormatter.";

    /** リクエスト処理開始時のフォーマットを取得する際に使用するプロパティ名 */
    private static final String PROPS_BEGIN_FORMAT = PROPS_PREFIX + "beginFormat";

    /** リクエスト処理終了時のフォーマットを取得する際に使用するプロパティ名 */
    private static final String PROPS_END_FORMAT = PROPS_PREFIX + "endFormat";

    /** 開始日時と終了日時のフォーマットに使用する日時パターンを取得する際に使用するプロパティ名 */
    private static final String PROPS_DATE_PATTERN = PROPS_PREFIX + "datePattern";

    /** マスク文字を取得する際に使用するプロパティ名 */
    private static final String PROPS_MASKING_CHAR = PROPS_PREFIX + "maskingChar";

    /** マスク対象のパターンを取得する際に使用するプロパティ名 */
    private static final String PROPS_MASKING_PATTERNS = PROPS_PREFIX + "maskingPatterns";

    /** リクエストパラメータの区切り文字を取得する際に使用するプロパティ名 */
    private static final String PROPS_PARAMETERS_SEPARATOR = PROPS_PREFIX + "parametersSeparator";

    /** セッションスコープ情報の区切り文字を取得する際に使用するプロパティ名 */
    private static final String PROPS_SESSION_SCOPE_SEPARATOR = PROPS_PREFIX + "sessionScopeSeparator";

    /** リクエスト処理開始時の出力が有効か否かを取得する際に使用するプロパティ名 */
    private static final String PROPS_BEGIN_OUTPUT_ENABLED = PROPS_PREFIX + "beginOutputEnabled";

    /** リクエスト処理終了時の出力が有効か否かを取得する際に使用するプロパティ名 */
    private static final String PROPS_END_OUTPUT_ENABLED = PROPS_PREFIX + "endOutputEnabled";

    /** ボディ出力時のマスク処理を行うクラス名を取得する際に使用するプロパティ名 */
    private static final String PROPS_BODY_MASKING_FILTER = PROPS_PREFIX + "bodyMaskingFilter";

    /** ボディ出力対象か判定するクラス名を取得する際に使用するプロパティ名 */
    private static final String PROPS_BODY_LOG_TARGET_MATCHER = PROPS_PREFIX + "bodyLogTargetMatcher";

    /** 多値指定(カンマ区切り)のプロパティを分割する際に使用するパターン */
    private static final Pattern MULTIVALUE_SEPARATOR_PATTERN = Pattern.compile(",");

    /** リクエスト処理開始時の出力が有効か否か。 */
    private boolean beginOutputEnabled;

    /** リクエスト処理終了時の出力が有効か否か。 */
    private boolean endOutputEnabled;

    /** 出力対象にメモリ項目が含まれているか否か。 */
    private boolean containsMemoryItem = false;

    /** リクエスト処理開始時のフォーマット済みのログ出力項目 */
    private LogItem<JaxRsAccessLogContext>[] beginLogItems;

    /** リクエスト処理終了時のフォーマット済みのログ出力項目 */
    private LogItem<JaxRsAccessLogContext>[] endLogItems;

    /**
     * 初期化する。
     *
     * @param props 各種ログ出力の設定情報
     */
    public void initialize(Map<String, String> props) {
        initializeEnabled(props);
        initializeLogItems(props);
    }

    /**
     * 各ログ出力が有効か否かを初期化する。
     * @param props 各種ログ出力の設定情報
     */
    protected void initializeEnabled(Map<String, String> props) {
        beginOutputEnabled = Boolean.parseBoolean(getProp(props, PROPS_BEGIN_OUTPUT_ENABLED, DEFAULT_BEGIN_OUTPUT_ENABLED));
        endOutputEnabled = Boolean.parseBoolean(getProp(props, PROPS_END_OUTPUT_ENABLED, DEFAULT_END_OUTPUT_ENABLED));
    }

    /**
     * フォーマット済みのログ出力項目を初期化する。
     * @param props 各種ログ出力の設定情報
     */
    protected void initializeLogItems(Map<String, String> props) {
        Map<String, LogItem<JaxRsAccessLogContext>> logItems = getLogItems(props);

        if (isBeginOutputEnabled()) {
            beginLogItems = LogUtil.createFormattedLogItems(logItems, getProp(props, PROPS_BEGIN_FORMAT, DEFAULT_BEGIN_FORMAT));
        }
        if (isEndOutputEnabled()) {
            endLogItems = LogUtil.createFormattedLogItems(logItems, getProp(props, PROPS_END_FORMAT, DEFAULT_END_FORMAT));
            containsMemoryItem = LogUtil.contains(endLogItems, MaxMemoryItem.class, FreeMemoryItem.class);
        }
    }

    /**
     * JaxRsAccessLogContextを生成する。
     * @return JaxRsAccessLogContext
     */
    public JaxRsAccessLogContext createAccessLogContext() {
        return new JaxRsAccessLogContext();
    }

    /**
     * 出力対象にメモリ項目が含まれているか否かを判定する。
     * @return 出力対象にメモリ項目が含まれている場合はtrue
     */
    public boolean containsMemoryItem() {
        return containsMemoryItem;
    }

    /**
     * フォーマット対象のログ出力項目を取得する。
     * @param props 各種ログの設定情報
     * @return フォーマット対象のログ出力項目
     */
    protected Map<String, LogItem<JaxRsAccessLogContext>> getLogItems(Map<String, String> props) {
        Map<String, LogItem<JaxRsAccessLogContext>> logItems = new HashMap<String, LogItem<JaxRsAccessLogContext>>();
        logItems.put("$requestId$", new RequestIdItem());
        logItems.put("$userId$", new UserIdItem());
        logItems.put("$url$", new UrlItem());
        logItems.put("$query$", new QueryStringItem());
        logItems.put("$port$", new PortItem());
        logItems.put("$method$", new MethodItem());

        char maskingChar = getMaskingChar(props);
        Pattern[] maskingPatterns = getMaskingPatterns(props);
        logItems.put("$parameters$", new ParametersItem(maskingChar, maskingPatterns,
                getSeparator(props, PROPS_PARAMETERS_SEPARATOR, DEFAULT_PARAMETERS_SEPARATOR)));
        logItems.put("$sessionScope$", new SessionScopeItem(maskingChar, maskingPatterns,
                getSeparator(props, PROPS_SESSION_SCOPE_SEPARATOR, DEFAULT_SESSION_SCOPE_SEPARATOR)));

        logItems.put("$sessionId$", new SessionIdItem());
        logItems.put("$sessionStoreId$", new SessionStoreIdItem());
        logItems.put("$statusCode$", new StatusCodeItem());
        logItems.put("$clientIpAddress$", new ClientIpAddressItem());
        logItems.put("$clientHost$", new ClientHostItem());
        logItems.put("$clientUserAgent$", new ClientUserAgentItem());

        DateFormat dateFormat = getDateFormat(props);
        logItems.put("$startTime$", new StartTimeItem(dateFormat));
        logItems.put("$endTime$", new EndTimeItem(dateFormat));
        logItems.put("$executionTime$", new ExecutionTimeItem());
        logItems.put("$maxMemory$", new MaxMemoryItem());
        logItems.put("$freeMemory$", new FreeMemoryItem());

        MessageBodyLogTargetMatcher bodyLogTargetMatcher = createBodyLogTargetMatcher(props);
        LogContentMaskingFilter bodyMaskingFilter = createBodyMaskingFilter(props);
        logItems.put("$requestBody$", new RequestBodyItem(bodyLogTargetMatcher, bodyMaskingFilter));
        logItems.put("$responseBody$", new ResponseBodyItem(bodyLogTargetMatcher, bodyMaskingFilter));

        return logItems;
    }

    /**
     * ボディ出力時のマスク処理を行うフィルタを生成します。
     *
     * @param props 各種ログの設定情報
     * @return マスク処理フィルタ
     */
    protected LogContentMaskingFilter createBodyMaskingFilter(Map<String, String> props) {
        String maskingFilterClassName = props.get(PROPS_BODY_MASKING_FILTER);
        LogContentMaskingFilter maskingFilter;
        if (maskingFilterClassName != null) {
            maskingFilter = ObjectUtil.createInstance(maskingFilterClassName);
        } else {
            maskingFilter = new JaxRsBodyMaskingFilter();
        }
        maskingFilter.initialize(props);
        return maskingFilter;
    }

    /**
     * ボディ出力対象であるか判定するMatcherを生成します。
     *
     * @param props 各種ログの設定情報
     * @return Matcher
     */
    protected MessageBodyLogTargetMatcher createBodyLogTargetMatcher(Map<String, String> props) {
        String bodyLogTargetMatcherClassName = props.get(PROPS_BODY_LOG_TARGET_MATCHER);
        MessageBodyLogTargetMatcher bodyLogTargetMatcher;
        if (bodyLogTargetMatcherClassName != null) {
            bodyLogTargetMatcher = ObjectUtil.createInstance(bodyLogTargetMatcherClassName);
        } else {
            bodyLogTargetMatcher = new JaxRsBodyLogTargetMatcher();
        }
        bodyLogTargetMatcher.initialize(props);
        return bodyLogTargetMatcher;
    }

    /**
     * 日時フォーマットを取得する。
     * プロパティの指定がない場合はデフォルトの日時フォーマットを返す。
     * @param props 各種ログの設定情報
     * @return 日時フォーマット
     */
    protected DateFormat getDateFormat(Map<String, String> props) {
        String datePattern = props.get(PROPS_DATE_PATTERN);
        return new SimpleDateFormat(datePattern != null ? datePattern : DEFAULT_DATE_PATTERN);
    }

    /**
     * プロパティを取得する。
     * プロパティの指定がない場合はデフォルト値を返す。
     * @param props 各種ログの設定情報
     * @param propName プロパティ名
     * @param defaultValue プロパティのデフォルト値
     * @return プロパティ
     */
    protected String getProp(Map<String, String> props, String propName, String defaultValue) {
        String value = props.get(propName);
        return value != null ? value : defaultValue;
    }

    /**
     * 区切り文字を取得する。
     * @param props 各種ログの設定情報
     * @param propName プロパティ名
     * @param defaultValue プロパティのデフォルト値
     * @return パラメータ間の区切り文字
     */
    protected String getSeparator(Map<String, String> props, String propName, String defaultValue) {
        String parametersSeparator = getProp(props, propName, defaultValue);
        return parametersSeparator.replace("\\n", Logger.LS).replace("\\t", "\t");
    }

    /**
     * マスク文字を取得する。
     * @param props 各種ログの設定情報
     * @return マスク文字
     */
    protected char getMaskingChar(Map<String, String> props) {
        String maskingChar = getProp(props, PROPS_MASKING_CHAR, DEFAULT_MASKING_CHAR);
        if (maskingChar.toCharArray().length != 1) {
            throw new IllegalArgumentException(
                    String.format("maskingChar was not char type. maskingChar = [%s]", maskingChar));
        }
        return maskingChar.charAt(0);
    }

    /**
     * マスク対象のパラメータ名を取得する。
     * プロパティの指定がない場合はデフォルト値を返す。
     * @param props 各種ログの設定情報
     * @return マスク対象のパラメータ名
     */
    protected Pattern[] getMaskingPatterns(Map<String, String> props) {
        String patterns = props.get(PROPS_MASKING_PATTERNS);
        if (patterns == null) {
            return DEFAULT_MASKING_PATTERNS;
        }
        String[] splitPatterns = MULTIVALUE_SEPARATOR_PATTERN.split(patterns);
        List<Pattern> maskingPatterns = new ArrayList<Pattern>();
        for (String regex : splitPatterns) {
            regex = regex.trim();
            if (StringUtil.isNullOrEmpty(regex)) {
                continue;
            }
            maskingPatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
        }
        return maskingPatterns.toArray(new Pattern[0]);
    }

    /**
     * リクエスト処理開始時のメッセージをフォーマットする。
     * @param context JaxRsAccessLogContext
     * @return フォーマット済みのメッセージ
     */
    public String formatBegin(JaxRsAccessLogContext context) {
        return LogUtil.formatMessage(beginLogItems, context);
    }

    /**
     * リクエスト処理終了時のメッセージをフォーマットする。
     * @param context JaxRsAccessLogContext
     * @return フォーマット済みのメッセージ
     */
    public String formatEnd(JaxRsAccessLogContext context) {
        return LogUtil.formatMessage(endLogItems, context);
    }

    /**
     * リクエスト処理開始時の出力が有効かを判定する。
     * @return リクエスト処理開始時の出力が有効な場合はtrue。
     */
    public boolean isBeginOutputEnabled() {
        return beginOutputEnabled;
    }

    /**
     * リクエスト処理終了時の出力が有効かを判定する。
     * @return リクエスト処理終了時の出力が有効な場合はtrue。
     */
    public boolean isEndOutputEnabled() {
        return endOutputEnabled;
    }

    public static class JaxRsAccessLogContext {

        /** {@link nablarch.fw.ExecutionContext } */
        private ServletExecutionContext context;

        /** HTTPリクエスト **/
        private HttpRequest request;

        /** HTTPレスポンス */
        private HttpResponse response;

        /** 開始日時 */
        private long startTime;

        /** 終了日時 */
        private long endTime;

        /** 最大メモリ量 */
        private long maxMemory;

        /** 空きメモリ量(開始時) */
        private long freeMemory;

        @SuppressWarnings("unchecked")
        private static final Map<String, Object> EMPTY_MAP = Collections.EMPTY_MAP;

        /**
         * {@link nablarch.fw.ExecutionContext}を設定する。
         * @return {@link nablarch.fw.ExecutionContext}
         */
        public ServletExecutionContext getContext() {
            return this.context;
        }
        /**
         * {@link nablarch.fw.ExecutionContext}を設定する。
         * @param context {@link nablarch.fw.ExecutionContext}
         */
        public void setContext(ServletExecutionContext context) {
            this.context = context;
        }

        /**
         * サーブレットリクエストを返す。
         * <p>
         * サーブレットコンテナ上で動作している場合は、サーブレットリクエストを返す。
         * そうでない場合(JUnit上で内蔵サーバーを使用せずにテストした場合など)は、
         * 実行時例外が送出される。
         *
         * @return サーブレットリクエスト
         * @throws ClassCastException
         *     サーブレットコンテナ上で動作していない場合。
         */
        public HttpServletRequest getServletRequest() throws ClassCastException {
            return context.getServletRequest();
        }

        /**
         * HTTPリクエストを取得する。
         * @return HTTPリクエスト
         */
        public HttpRequest getRequest() {
            return this.request;
        }

        /**
         * HTTPリクエストを設定する。
         * @param request HTTPリクエスト
         */
        public void setRequest(HttpRequest request) {
            this.request = request;
        }

        /**
         * HTTPレスポンスを取得する。
         * @return HTTPレスポンス
         */
        public HttpResponse getResponse() {
            return this.response;
        }

        /**
         * HTTPレスポンスを設定する。
         * @param response HTTPレスポンス
         */
        public void setResponse(HttpResponse response) {
            this.response = response;
        }

        /**
         * セッションIDを取得する。
         * @return セッションID
         */
        public String getSessionId() {
            HttpSession session = getServletRequest().getSession(false);
            return session == null ? "" : session.getId();
        }

        /**
         * URLを取得する。
         * @return URL
         */
        public String getUrl() {
            return getServletRequest().getRequestURL().toString();
        }

        /**
         * クエリ文字列を取得する。
         * @return クエリ文字列
         */
        public String getQueryString() {
            String queryString = getServletRequest().getQueryString();

            return queryString == null
                    ? ""
                    : "?" + queryString;
        }

        /**
         * ポート番号を取得する。
         * @return ポート番号
         */
        public int getPort() {
            return getServletRequest().getServerPort();

        }

        /**
         * HTTPメソッドを取得する。
         * @return HTTPメソッド
         */
        public String getMethod() {
            return request.getMethod();
        }

        /**
         * リクエストパラメータを取得する。
         * @return リクエストパラメータ
         */
        public Map<String, String[]> getParameters() {
            return request.getParamMap();
        }

        /**
         * セッションスコープマップを取得する。
         * @return セッションスコープマップ
         */
        public Map<String, Object> getSessionScopeMap() {
            return context.hasSession() ? context.getSessionScopeMap() : EMPTY_MAP;
        }

        /**
         * クライアント端末IPアドレスを取得する。
         * @return クライアント端末IPアドレス
         */
        public String getClientIpAddress() {
            return getServletRequest().getRemoteAddr();
        }

        /**
         * クライアント端末ホストを取得する。
         * @return クライアント端末ホスト
         */
        public String getClientHost() {
            return getServletRequest().getRemoteHost();
        }

        /**
         * ステータスコードを取得する。
         * @return ステータスコード
         */
        public int getStatusCode() {
            if (response == null) {
                return -1;
            }
            return response.getStatusCode();
        }

        /**
         * 開始日時を取得する。
         * @return 開始日時
         */
        public long getStartTime() {
            return startTime;
        }

        /**
         * 開始日時を設定する。
         * @param startTime 開始日時
         */
        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        /**
         * 終了日時を取得する。
         * @return 終了日時
         */
        public long getEndTime() {
            return endTime;
        }

        /**
         * 終了日時を設定する。
         * @param endTime 終了日時
         */
        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        /**
         * 実行時間を取得する。
         * @return 実行時間
         */
        public long getExecutionTime() {
            return endTime - startTime;
        }

        /**
         * 最大メモリ量を取得する。
         * @return 最大メモリ量
         */
        public long getMaxMemory() {
            return maxMemory;
        }

        /**
         * 最大メモリ量を設定する。
         * @param maxMemory 最大メモリ量
         */
        public void setMaxMemory(long maxMemory) {
            this.maxMemory = maxMemory;
        }

        /**
         * 空きメモリ量(開始時)を取得する。
         * @return 空きメモリ量(開始時)
         */
        public long getFreeMemory() {
            return freeMemory;
        }

        /**
         * 空きメモリ量(開始時)を設定する。
         * @param freeMemory 空きメモリ量(開始時)
         */
        public void setFreeMemory(long freeMemory) {
            this.freeMemory = freeMemory;
        }

        /**
         * リクエストのボディを読み込む。
         *
         * @return ボディの文字列表現
         * @throws IOException 読込に失敗した場合
         */
        public String readRequestBody() throws IOException {
            NablarchHttpServletRequestWrapper servletRequest = context.getServletRequest();
            int contentLength = servletRequest.getContentLength();
            if (contentLength < 1) {
                // マイナス値が返ってくる場合もあるため考慮しておく
                return "";
            }
            // InputStreamではresetに対応できなかったため、Readerを使用する
            BufferedReader reader = servletRequest.getReader();
            char[] peeked = new char[contentLength];
            reader.mark(contentLength);
            try {
                int readSize = reader.read(peeked);
                return CharBuffer.wrap(peeked, 0, readSize).toString();
            } finally {
                // リクエスト処理で改めてボディを読み込めるようにリセットしておく
                reader.reset();
            }
        }

        /**
         * レスポンスのボディを読み込む。
         *
         * @return ボディの文字列表現
         * @throws IOException 読込に失敗した場合
         */
        public String readResponseBody() throws IOException {
            if (response.isBodyEmpty()) {
                return "";
            }
            // Nablarchが提供しているBodyConverterではレスポンスボディをバッファに書き込むため、
            // 現時点ではバッファからの読込のみ対応する。
            // バッファからの読込では取得の都度Streamが生成されるため、Streamの状態はリセットしない。
            InputStream bodyStream = response.getBodyStream();
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while (true) {
                    byte[] bytes = new byte[1024];
                    int readSize = bodyStream.read(bytes);
                    if (readSize == -1) {
                        break;
                    }
                    out.write(bytes, 0, readSize);
                }
                return String.valueOf(response.getCharset().decode(ByteBuffer.wrap(out.toByteArray())));

            } finally {
                bodyStream.close();
            }
        }

    }

    /**
     * リクエストIDを取得するクラス。
     */
    public static class RequestIdItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * リクエストIDを取得する。
         * @param context JaxRsAccessLogContext
         * @return リクエストID
         */
        public String get(JaxRsAccessLogContext context) {
            return ThreadContext.getRequestId();
        }
    }

    /**
     * ユーザIDを取得する。
     */
    public static class UserIdItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * ユーザIDを取得する。
         * @param context JaxRsAccessLogContext
         * @return ユーザID
         */
        public String get(JaxRsAccessLogContext context) {
            return ThreadContext.getUserId();
        }
    }

    /**
     * URLを取得するクラス。
     */
    public static class UrlItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * URLを取得する。
         * @param context JaxRsAccessLogContext
         * @return URL
         */
        public String get(JaxRsAccessLogContext context) {
            return context.getUrl();
        }
    }

    /**
     * クエリ文字列を取得するクラス。
     */
    public static class QueryStringItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * クエリ文字列を取得する。
         * クエリ文字列があれば"?"を含めクエリ文字列を取得する。
         * @param context JaxRsAccessLogContext
         * @return クエリ文字列
         */
        public String get(JaxRsAccessLogContext context) {
            return context.getQueryString();
        }
    }

    /**
     * ポート番号を取得するクラス。
     */
    public static class PortItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * ポート番号を取得する。
         * @param context JaxRsAccessLogContext
         * @return ポート番号
         */
        public String get(JaxRsAccessLogContext context) {
            return String.valueOf(context.getPort());
        }
    }

    /**
     * HTTPメソッドを取得するクラス。
     */
    public static class MethodItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * HTTPメソッドを取得する。
         * @param context JaxRsAccessLogContext
         * @return HTTPメソッド
         */
        public String get(JaxRsAccessLogContext context) {
            return context.getMethod();
        }
    }

    /**
     * リクエストパラメータを取得するクラス。
     */
    public static class ParametersItem extends MaskingMapItemSupport<JaxRsAccessLogContext> {
        /**
         * コンストラクタ。
         * @param maskingChar マスク文字
         * @param maskingPatterns マスク対象のパターン
         * @param paramSeparator パラメータ間の区切り文字
         */
        public ParametersItem(char maskingChar, Pattern[] maskingPatterns, String paramSeparator) {
            super(maskingChar, maskingPatterns, paramSeparator);
        }

        @Override
        protected Map<String, ?> getMap(JaxRsAccessLogContext context) {
            return context.getParameters();
        }
    }

    /**
     * セッションスコープ情報を取得するクラス。
     */
    public static class SessionScopeItem extends MaskingMapItemSupport<JaxRsAccessLogContext> {

        /**
         * コンストラクタ。
         * @param maskingChar マスク文字
         * @param maskingPatterns マスク対象のパターン
         * @param varSeparator 変数間の区切り文字
         */
        public SessionScopeItem(char maskingChar, Pattern[] maskingPatterns, String varSeparator) {
            super(maskingChar, maskingPatterns, varSeparator);
        }

        @Override
        protected Map<String, ?> getMap(JaxRsAccessLogContext context) {
            return context.getSessionScopeMap();
        }
    }

    /**
     * セッションIDを取得するクラス。
     */
    public static class SessionIdItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * セッションIDを取得する。
         * @param context JaxRsAccessLogContext
         * @return セッションID
         */
        public String get(JaxRsAccessLogContext context) {
            return context.getSessionId();
        }
    }

    /**
     * セッションストアIDを取得するクラス。
     */
    public static class SessionStoreIdItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * セッションストアIDを取得する。
         * @param context JaxRsAccessLogContext
         * @return セッションストアID
         */
        public String get(JaxRsAccessLogContext context) {
            return InternalSessionUtil.getId(context.getContext());
        }
    }

    /**
     * ステータスコードを取得するクラス。
     */
    public static class StatusCodeItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * ステータスコードを取得する。
         * @param context JaxRsAccessLogContext
         * @return ステータスコード
         */
        public String get(JaxRsAccessLogContext context) {
            int statusCode = context.getStatusCode();
            return statusCode != -1 ? String.valueOf(statusCode) : "";
        }
    }

    /**
     * クライアント端末IPアドレスを取得するクラス。
     */
    public static class ClientIpAddressItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * クライアント端末IPアドレスを取得する。
         * @param context JaxRsAccessLogContext
         * @return クライアント端末IPアドレス
         */
        public String get(JaxRsAccessLogContext context) {
            return context.getClientIpAddress();
        }
    }

    /**
     * クライアント端末ホストを取得するクラス。
     */
    public static class ClientHostItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * クライアント端末ホストを取得する。
         * @param context JaxRsAccessLogContext
         * @return クライアント端末ホスト
         */
        public String get(JaxRsAccessLogContext context) {
            return context.getClientHost();
        }
    }

    /**
     * 開始日時を取得するクラス。
     */
    public static class StartTimeItem extends DateItemSupport<JaxRsAccessLogContext> {

        /**
         * コンストラクタ。
         * @param dateFormat 日時フォーマット
         */
        public StartTimeItem(DateFormat dateFormat) {
            super(dateFormat);
        }

        @Override
        protected Date getDate(JaxRsAccessLogContext context) {
            return new Date(context.getStartTime());
        }
    }

    /**
     * 終了日時を取得するクラス。
     */
    public static class EndTimeItem extends DateItemSupport<JaxRsAccessLogContext> {

        /**
         * コンストラクタ。
         * @param dateFormat 日時フォーマット
         */
        public EndTimeItem(DateFormat dateFormat) {
            super(dateFormat);
        }

        @Override
        protected Date getDate(JaxRsAccessLogContext context) {
            return new Date(context.getEndTime());
        }
    }
    /**
     * 実行時間を取得するクラス。
     */
    public static class ExecutionTimeItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * 実行時間を取得する。
         * @param context JaxRsAccessLogContext
         * @return 実行時間
         */
        public String get(JaxRsAccessLogContext context) {
            return String.valueOf(context.getExecutionTime());
        }
    }

    /**
     * 最大メモリ量を取得するクラス。
     */
    public static class MaxMemoryItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * 最大メモリ量を取得する。
         * @param context JaxRsAccessLogContext
         * @return 最大メモリ量
         */
        public String get(JaxRsAccessLogContext context) {
            return String.valueOf(context.getMaxMemory());
        }
    }

    /**
     * 空きメモリ量(開始時)を取得するクラス。
     */
    public static class FreeMemoryItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * 開始時の空きメモリ量を取得する。
         * @param context JaxRsAccessLogContext
         * @return 開始時の空きメモリ量
         */
        public String get(JaxRsAccessLogContext context) {
            return String.valueOf(context.getFreeMemory());
        }
    }

    /**
     * HTTPヘッダの User-Agent を取得する。
     */
    public static class ClientUserAgentItem implements LogItem<JaxRsAccessLogContext> {

        /**
         * HTTPヘッダの User-Agent を取得する。
         * @param context JaxRsAccessLogContext
         * @return HTTPヘッダの User-Agent
         */
        public String get(JaxRsAccessLogContext context) {
            String info = context.getServletRequest().getHeader("User-Agent");
            return info == null ? "" : info;
        }
    }

    /**
     * リクエストのボディを取得する。
     */
    public static class RequestBodyItem implements LogItem<JaxRsAccessLogContext> {

        /** ログ出力対象判定 */
        private final MessageBodyLogTargetMatcher logTargetMatcher;

        /** マスク処理フィルタ */
        private final LogContentMaskingFilter maskingFilter;

        /**
         * コンストラクタ
         *
         * @param logTargetMatcher ログ出力対象判定
         * @param maskingFilter マスク処理フィルタ
         */
        public RequestBodyItem(MessageBodyLogTargetMatcher logTargetMatcher, LogContentMaskingFilter maskingFilter) {
            this.logTargetMatcher = logTargetMatcher;
            this.maskingFilter = maskingFilter;
        }

        @Override
        public String get(JaxRsAccessLogContext context) {
            if (logTargetMatcher.isTargetRequest(context.getRequest(), context.getContext())) {
                try {
                    return maskingFilter.mask(context.readRequestBody());
                } catch (Throwable t) {
                    // 本処理に影響が無いようにログ出力のみ行う
                    LOGGER.logWarn("Failed to read Request Body", t);
                }
            }
            return "";
        }
    }

    /**
     * レスポンスのボディを取得する。
     */
    public static class ResponseBodyItem implements LogItem<JaxRsAccessLogContext> {

        /** ログ出力対象判定 */
        private final MessageBodyLogTargetMatcher logTargetMatcher;

        /** マスク処理フィルタ */
        private final LogContentMaskingFilter maskingFilter;

        /**
         * コンストラクタ
         *
         * @param logTargetMatcher ログ出力対象判定
         * @param maskingFilter マスク処理フィルタ
         */
        public ResponseBodyItem(MessageBodyLogTargetMatcher logTargetMatcher, LogContentMaskingFilter maskingFilter) {
            this.logTargetMatcher = logTargetMatcher;
            this.maskingFilter = maskingFilter;
        }

        @Override
        public String get(JaxRsAccessLogContext context) {
            if (logTargetMatcher.isTargetResponse(context.getRequest(), context.getResponse(), context.getContext())) {
                try {
                    return maskingFilter.mask(context.readResponseBody());
                } catch (Throwable t) {
                    // 本処理に影響が無いようにログ出力のみ行う
                    LOGGER.logWarn("Failed to read Response Body", t);
                }
            }
            return "";
        }
    }
}
