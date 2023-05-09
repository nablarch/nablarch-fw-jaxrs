package nablarch.fw.jaxrs;

import nablarch.common.web.session.InternalSessionUtil;
import nablarch.core.ThreadContext;
import nablarch.core.log.LogUtil.MapValueEditor;
import nablarch.core.log.LogUtil.MaskingMapValueEditor;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.log.app.AppLogUtil;
import nablarch.core.log.app.JsonLogFormatterSupport;
import nablarch.core.log.basic.JsonLogObjectBuilder;
import nablarch.core.text.json.JsonSerializationManager;
import nablarch.core.text.json.JsonSerializationSettings;
import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static nablarch.fw.jaxrs.JsonStringToJsonSerializer.JsonString;

/**
 * RESTfulウェブサービスのアクセスログのメッセージをフォーマットするクラス。
 */
@Published(tag = "architect")
public class JaxRsAccessJsonLogFormatter extends JaxRsAccessLogFormatter {

    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(JaxRsAccessJsonLogFormatter.class);

    /** ラベルの項目名 */
    private static final String TARGET_NAME_LABEL = "label";

    /** リクエストIDの項目名 */
    private static final String TARGET_NAME_REQUEST_ID = "requestId";

    /** ユーザIDの項目名 */
    private static final String TARGET_NAME_USER_ID = "userId";

    /** URLの項目名 */
    private static final String TARGET_NAME_URL = "url";

    /** クエリ文字列の項目名 */
    private static final String TARGET_NAME_QUERY = "queryString";

    /** ポート番号の項目名 */
    private static final String TARGET_NAME_PORT = "port";

    /** HTTPメソッドの項目名 */
    private static final String TARGET_NAME_METHOD = "method";

    /** リクエストパラメータの項目名 */
    private static final String TARGET_NAME_PARAMETERS = "parameters";

    /** セッションスコープ情報の項目名 */
    private static final String TARGET_NAME_SESSION_SCOPE = "sessionScope";

    /** セッションIDの項目名 */
    private static final String TARGET_NAME_SESSION_ID = "sessionId";

    /** セッションストアIDの項目名 */
    private static final String TARGET_NAME_SESSION_STORE_ID = "sessionStoreId";

    /** ステータスコードの項目名 */
    private static final String TARGET_NAME_STATUS_CODE = "statusCode";

    /** クライアントへのレスポンスに使用するステータスコードの項目名 */
    private static final String TARGET_NAME_RESPONSE_STATUS_CODE = "responseStatusCode";

    /** クライアント端末IPアドレスの項目名 */
    private static final String TARGET_NAME_CLIENT_IP_ADDRESS = "clientIpAddress";

    /** クライアント端末ホストの項目名 */
    private static final String TARGET_NAME_CLIENT_HOST = "clientHost";

    /** HTTPヘッダのUser-Agentの項目名 */
    private static final String TARGET_NAME_CLIENT_USER_AGENT = "clientUserAgent";

    /** 開始日時の項目名 */
    private static final String TARGET_NAME_START_TIME = "startTime";

    /** 終了日時の項目名 */
    private static final String TARGET_NAME_END_TIME = "endTime";

    /** 実行時間の項目名 */
    private static final String TARGET_NAME_EXECUTION_TIME = "executionTime";

    /** 最大メモリ量の項目名 */
    private static final String TARGET_NAME_MAX_MEMORY = "maxMemory";

    /** 空きメモリ量(開始時)の項目名 */
    private static final String TARGET_NAME_FREE_MEMORY = "freeMemory";

    /** リクエストボディの項目名 */
    private static final String TARGET_NAME_REQUEST_BODY = "requestBody";

    /** レスポンスボディの項目名 */
    private static final String TARGET_NAME_RESPONSE_BODY = "responseBody";

    /** リクエスト処理開始時の出力項目を取得する際に使用するプロパティ名 */
    private static final String PROPS_BEGIN_TARGETS = PROPS_PREFIX + "beginTargets";

    /** リクエスト処理終了時の出力項目を取得する際に使用するプロパティ名 */
    private static final String PROPS_END_TARGETS = PROPS_PREFIX + "endTargets";

    /** リクエスト処理開始時のラベルのプロパティ名 */
    private static final String PROPS_BEGIN_LABEL = PROPS_PREFIX + "beginLabel";

    /** リクエスト処理終了時のラベルのプロパティ名 */
    private static final String PROPS_END_LABEL = PROPS_PREFIX + "endLabel";

    /** デフォルトのリクエスト処理開始時の出力項目 */
    private static final String DEFAULT_BEGIN_TARGETS = "label,requestId,userId,sessionId,url,"
            + "method,port,clientIpAddress,clientHost";

    /** デフォルトのリクエスト処理終了時の出力項目 */
    private static final String DEFAULT_END_TARGETS = "label,requestId,userId,sessionId,url,"
            + "statusCode,startTime,endTime,executionTime,maxMemory,freeMemory";

    /** デフォルトのリクエスト処理開始時のラベル */
    private static final String DEFAULT_BEGIN_LABEL = "HTTP ACCESS BEGIN";

    /** デフォルトのリクエスト処理終了時のラベル */
    private static final String DEFAULT_END_LABEL = "HTTP ACCESS END";

    /** リクエスト処理開始時のフォーマット済みのログ出力項目 */
    private List<JsonLogObjectBuilder<JaxRsAccessLogContext>> beginStructuredTargets;

    /** リクエスト処理終了時のフォーマット済みのログ出力項目 */
    private List<JsonLogObjectBuilder<JaxRsAccessLogContext>> endStructuredTargets;

    /** 出力対象にメモリ項目が含まれているか否か。 */
    private boolean containsMemoryItem;

    /** 各種ログのJSONフォーマット支援オブジェクト */
    private JsonLogFormatterSupport support;

    /**
     * 初期化。
     * フォーマット済みのログ出力項目を初期化する。
     * @param props 各種ログ出力の設定情報
     */
    @Override
    public void initialize(Map<String, String> props) {
        initializeEnabled(props);

        JsonSerializationSettings settings = new JsonSerializationSettings(props, PROPS_PREFIX, AppLogUtil.getFilePath());
        JsonSerializationManager serializationManager = createSerializationManager(settings);
        support = new JsonLogFormatterSupport(serializationManager, settings);

        Map<String, JsonLogObjectBuilder<JaxRsAccessLogContext>> objectBuilders = getObjectBuilders(props);

        if (isBeginOutputEnabled()) {
            String label = getProp(props, PROPS_BEGIN_LABEL, DEFAULT_BEGIN_LABEL);
            objectBuilders.put(TARGET_NAME_LABEL, new LabelBuilder(label));
            beginStructuredTargets = getStructuredTargets(objectBuilders, props, PROPS_BEGIN_TARGETS, DEFAULT_BEGIN_TARGETS);
        }

        if (isEndOutputEnabled()) {
            String label = getProp(props, PROPS_END_LABEL, DEFAULT_END_LABEL);
            objectBuilders.put(TARGET_NAME_LABEL, new LabelBuilder(label));
            endStructuredTargets = getStructuredTargets(objectBuilders, props, PROPS_END_TARGETS, DEFAULT_END_TARGETS);

            initContainsMemoryItem();
        }
    }

    /**
     * 変換処理に使用する{@link JsonSerializationManager}を生成する。
     * @param settings 各種ログ出力の設定情報
     * @return {@link JsonSerializationManager}
     */
    protected JsonSerializationManager createSerializationManager(JsonSerializationSettings settings) {
        return new JaxRsAccessLogJsonSerializationManager();
    }

    /**
     * {@link #containsMemoryItem}の値を初期化する。
     * <p>
     * {@link #endStructuredTargets}に{@link MaxMemoryBuilder}か{@link FreeMemoryBuilder}の
     * いずれかが設定されている場合は true を設定する。
     * </p>
     */
    private void initContainsMemoryItem() {
        for (JsonLogObjectBuilder<JaxRsAccessLogContext> target : endStructuredTargets) {
            if (target instanceof MaxMemoryBuilder || target instanceof FreeMemoryBuilder) {
                containsMemoryItem = true;
                return;
            }
        }
    }

    /**
     * フォーマット対象のログ出力項目を取得する。
     * @param props 各種ログ出力の設定情報
     * @return フォーマット対象のログ出力項目
     */
    protected Map<String, JsonLogObjectBuilder<JaxRsAccessLogContext>> getObjectBuilders(Map<String, String> props) {

        Map<String, JsonLogObjectBuilder<JaxRsAccessLogContext>> objectBuilders
                = new HashMap<String, JsonLogObjectBuilder<JaxRsAccessLogContext>>();

        char maskingChar = getMaskingChar(props);
        Pattern[] maskingPatterns = getMaskingPatterns(props);
        MapValueEditor mapValueEditor = new MaskingMapValueEditor(maskingChar, maskingPatterns);

        objectBuilders.put(TARGET_NAME_REQUEST_ID, new RequestIdBuilder());
        objectBuilders.put(TARGET_NAME_USER_ID, new UserIdBuilder());
        objectBuilders.put(TARGET_NAME_URL, new UrlBuilder());
        objectBuilders.put(TARGET_NAME_QUERY, new QueryStringBuilder());
        objectBuilders.put(TARGET_NAME_PORT, new PortBuilder());
        objectBuilders.put(TARGET_NAME_METHOD, new MethodBuilder());
        objectBuilders.put(TARGET_NAME_PARAMETERS, new ParametersBuilder(mapValueEditor));
        objectBuilders.put(TARGET_NAME_SESSION_SCOPE, new SessionScopeBuilder(mapValueEditor));
        objectBuilders.put(TARGET_NAME_SESSION_ID, new SessionIdBuilder());
        objectBuilders.put(TARGET_NAME_SESSION_STORE_ID, new SessionStoreIdBuilder());
        objectBuilders.put(TARGET_NAME_STATUS_CODE, new StatusCodeBuilder());

        objectBuilders.put(TARGET_NAME_CLIENT_IP_ADDRESS, new ClientIpAddressBuilder());
        objectBuilders.put(TARGET_NAME_CLIENT_HOST, new ClientHostBuilder());
        objectBuilders.put(TARGET_NAME_CLIENT_USER_AGENT, new ClientUserAgentBuilder());
        objectBuilders.put(TARGET_NAME_START_TIME, new StartTimeBuilder());
        objectBuilders.put(TARGET_NAME_END_TIME, new EndTimeBuilder());
        objectBuilders.put(TARGET_NAME_EXECUTION_TIME, new ExecutionTimeBuilder());
        objectBuilders.put(TARGET_NAME_MAX_MEMORY, new MaxMemoryBuilder());
        objectBuilders.put(TARGET_NAME_FREE_MEMORY, new FreeMemoryBuilder());

        MessageBodyLogTargetMatcher bodyLogTargetMatcher = createBodyLogTargetMatcher(props);
        LogContentMaskingFilter bodyMaskingFilter = createBodyMaskingFilter(props);
        objectBuilders.put(TARGET_NAME_REQUEST_BODY, new RequestBodyBuilder(bodyLogTargetMatcher, bodyMaskingFilter));
        objectBuilders.put(TARGET_NAME_RESPONSE_BODY, new ResponseBodyBuilder(bodyLogTargetMatcher, bodyMaskingFilter));

        return objectBuilders;
    }

    /**
     * フォーマット済みのログ出力項目を取得する。
     * @param objectBuilders オブジェクトビルダー
     * @param props 各種ログ出力の設定情報
     * @param targetsPropName 出力項目のプロパティ名
     * @param defaultTargets デフォルトの出力項目
     * @return フォーマット済みのログ出力項目
     */
    private List<JsonLogObjectBuilder<JaxRsAccessLogContext>> getStructuredTargets(
            Map<String, JsonLogObjectBuilder<JaxRsAccessLogContext>> objectBuilders,
            Map<String, String> props,
            String targetsPropName, String defaultTargets) {

        String targetsStr = props.get(targetsPropName);
        if (StringUtil.isNullOrEmpty(targetsStr)) targetsStr = defaultTargets;

        List<JsonLogObjectBuilder<JaxRsAccessLogContext>> structuredTargets
                = new ArrayList<JsonLogObjectBuilder<JaxRsAccessLogContext>>();

        String[] targets = targetsStr.split(",");
        Set<String> keys = new HashSet<String>(targets.length);
        for (String target: targets) {
            String key = target.trim();
            if (!StringUtil.isNullOrEmpty(key) && !keys.contains(key)) {
                keys.add(key);
                if (objectBuilders.containsKey(key)) {
                    structuredTargets.add(objectBuilders.get(key));
                } else {
                    throw new IllegalArgumentException(
                            String.format("[%s] is unknown target. property name = [%s]", key, targetsPropName));
                }
            }
        }

        return structuredTargets;
    }

    /**
     * 出力対象にメモリ項目が含まれているか否かを判定する。
     * @return 出力対象にメモリ項目が含まれている場合はtrue
     */
    @Override
    public boolean containsMemoryItem() {
        return containsMemoryItem;
    }

    /**
     * リクエスト処理開始時のメッセージをフォーマットする。
     * @param context JaxRsAccessLogContext
     * @return フォーマット済みのメッセージ
     */
    @Override
    public String formatBegin(JaxRsAccessLogContext context) {
        return support.getStructuredMessage(beginStructuredTargets, context);
    }

    /**
     * リクエスト処理終了時のメッセージをフォーマットする。
     * @param context JaxRsAccessLogContext
     * @return フォーマット済みのメッセージ
     */
    @Override
    public String formatEnd(JaxRsAccessLogContext context) {
        return support.getStructuredMessage(endStructuredTargets, context);
    }

    /**
     * ラベルを処理するクラス。
     */
    public static class LabelBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        private final String label;

        /**
         * コンストラクタ。
         * @param label ラベル
         */
        public LabelBuilder(String label) {
            this.label = label;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_LABEL, label);
        }
    }

    /**
     * リクエストIDを処理するクラス。
     */
    public static class RequestIdBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_REQUEST_ID, ThreadContext.getRequestId());
        }
    }

    /**
     * ユーザIDを処理するクラス。
     */
    public static class UserIdBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_USER_ID, ThreadContext.getUserId());
        }
    }

    /**
     * URLを処理するクラス。
     */
    public static class UrlBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_URL, context.getUrl());
        }
    }

    /**
     * クエリ文字列を処理するクラス。
     */
    public static class QueryStringBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_QUERY, context.getQueryString());
        }
    }

    /**
     * ポート番号を処理するクラス。
     */
    public static class PortBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_PORT, context.getPort());
        }
    }

    /**
     * HTTPメソッドを処理するクラス。
     */
    public static class MethodBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_METHOD, context.getMethod());
        }
    }

    /**
     * リクエストパラメータを処理するクラス。
     */
    public static class ParametersBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /** マップの値のマスキング */
        private final MapValueEditor mapValueEditor;

        /**
         * コンストラクタ。
         * @param mapValueEditor マップの値のマスキング
         */
        public ParametersBuilder(MapValueEditor mapValueEditor) {
            this.mapValueEditor = mapValueEditor;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            Map<String, String[]> map = new HashMap<String, String[]>();
            for (Map.Entry<String, String[]> entry : context.getParameters().entrySet()) {
                String [] values = new String[entry.getValue().length];
                for (int i = 0; i < entry.getValue().length; i++) {
                    values[i] = mapValueEditor.edit(entry.getKey(), entry.getValue()[i]);
                }
                map.put(entry.getKey(), values);
            }
            structuredObject.put(TARGET_NAME_PARAMETERS, map);
        }
    }

    /**
     * セッションスコープ情報を処理するクラス。
     */
    public static class SessionScopeBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /** マップの値のマスキング */
        private final MapValueEditor mapValueEditor;

        /**
         * コンストラクタ。
         * @param mapValueEditor マップの値のマスキング
         */
        public SessionScopeBuilder(MapValueEditor mapValueEditor) {
            this.mapValueEditor = mapValueEditor;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            Map<String, String> map = new HashMap<String, String>();
            for (Map.Entry<String, Object> entry : context.getSessionScopeMap().entrySet()) {
                String values = mapValueEditor.edit(entry.getKey(), entry.getValue());
                map.put(entry.getKey(), values);
            }
            structuredObject.put(TARGET_NAME_SESSION_SCOPE, map);
        }
    }

    /**
     * セッションIDを処理するクラス。
     */
    public static class SessionIdBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_SESSION_ID, context.getSessionId());
        }
    }

    /**
     * セッションストアIDを処理するクラス。
     */
    public static class SessionStoreIdBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_SESSION_STORE_ID, InternalSessionUtil.getId(context.getContext()));
        }
    }

    /**
     * ステータスコードを処理するクラス。
     */
    public static class StatusCodeBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            int statusCode = context.getStatusCode();
            structuredObject.put(TARGET_NAME_STATUS_CODE, statusCode != -1 ? statusCode : null);
        }
    }

    /**
     * クライアント端末IPアドレスを処理するクラス。
     */
    public static class ClientIpAddressBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_CLIENT_IP_ADDRESS, context.getClientIpAddress());
        }
    }

    /**
     * クライアント端末ホストを処理するクラス。
     */
    public static class ClientHostBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_CLIENT_HOST, context.getClientHost());
        }
    }

    /**
     * HTTPヘッダのUser-Agentを処理するクラス。
     */
    public static class ClientUserAgentBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_CLIENT_USER_AGENT, context.getServletRequest().getHeader("User-Agent"));
        }
    }

    /**
     * 開始日時を処理するクラス。
     */
    public static class StartTimeBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_START_TIME,  new Date(context.getStartTime()));
        }
    }

    /**
     * 終了日時を処理するクラス。
     */
    public static class EndTimeBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_END_TIME, new Date(context.getEndTime()));
        }
    }

    /**
     * 実行時間を処理するクラス。
     */
    public static class ExecutionTimeBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_EXECUTION_TIME, context.getExecutionTime());
        }
    }

    /**
     * 最大メモリ量を処理するクラス。
     */
    public static class MaxMemoryBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_MAX_MEMORY, context.getMaxMemory());
        }
    }

    /** 空きメモリ量(開始時)を処理するクラス。
     */
    public static class FreeMemoryBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            structuredObject.put(TARGET_NAME_FREE_MEMORY, context.getFreeMemory());
        }
    }

    /**
     * リクエストのボディを処理する。
     */
    public static class RequestBodyBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

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
        public RequestBodyBuilder(MessageBodyLogTargetMatcher logTargetMatcher, LogContentMaskingFilter maskingFilter) {
            this.logTargetMatcher = logTargetMatcher;
            this.maskingFilter = maskingFilter;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            if (logTargetMatcher.isTargetRequest(context.getRequest(), context.getContext())) {
                String content = readRequestBody(context);
                structuredObject.put(TARGET_NAME_REQUEST_BODY, isJsonString(content) ? new JsonString(content) : content);
            } else {
                structuredObject.put(TARGET_NAME_REQUEST_BODY, null);
            }
        }

        /**
         * リクエストのボディを読み込む。
         *
         * @param context ログコンテキスト
         * @return ボディの文字列表現
         */
        private String readRequestBody(JaxRsAccessLogContext context) {
            try {
                String content = context.readRequestBody();
                if (content.isEmpty()) {
                    // JSON形式で出力されるため空文字ではなくnullを返す
                    return null;
                }
                return maskingFilter.mask(content);
            } catch (Throwable t) {
                // 本処理に影響が無いようにログ出力のみ行う
                LOGGER.logWarn("Failed to read Request Body", t);
                return null;
            }
        }

        /**
         * 指定の文字列がJSON文字列であるか判定する。
         *
         * @param content 文字列
         * @return JSON文字列である場合は {@code true}
         */
        private boolean isJsonString(String content) {
            if (content == null) {
                return false;
            }
            String trimmed = content.trim();
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                return true;
            }
            return trimmed.startsWith("[") || trimmed.startsWith("]");
        }
    }

    /**
     * レスポンスのボディを処理する。
     */
    public static class ResponseBodyBuilder implements JsonLogObjectBuilder<JaxRsAccessLogContext> {

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
        public ResponseBodyBuilder(MessageBodyLogTargetMatcher logTargetMatcher, LogContentMaskingFilter maskingFilter) {
            this.logTargetMatcher = logTargetMatcher;
            this.maskingFilter = maskingFilter;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void build(Map<String, Object> structuredObject, JaxRsAccessLogContext context) {
            if (logTargetMatcher.isTargetResponse(context.getRequest(), context.getResponse(), context.getContext())) {
                String content = readResponseBody(context);
                structuredObject.put(TARGET_NAME_RESPONSE_BODY, isJsonString(content) ? new JsonString(content) : content);
            } else {
                structuredObject.put(TARGET_NAME_RESPONSE_BODY, null);
            }
        }

        /**
         * レスポンスのボディを読み込む。
         *
         * @param context ログコンテキスト
         * @return ボディの文字列表現
         */
        private String readResponseBody(JaxRsAccessLogContext context) {
            try {
                String content = context.readResponseBody();
                if (content.isEmpty()) {
                    // JSON形式で出力されるため空文字ではなくnullを返す
                    return null;
                }
                return maskingFilter.mask(content);
            } catch (Throwable t) {
                // 本処理に影響が無いようにログ出力のみ行う
                LOGGER.logWarn("Failed to read Response Body", t);
                return null;
            }
        }

        /**
         * 指定の文字列がJSON文字列であるか判定する。
         *
         * @param content 文字列
         * @return JSON文字列である場合は {@code true}
         */
        private boolean isJsonString(String content) {
            if (content == null) {
                return false;
            }
            String trimmed = content.trim();
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                return true;
            }
            return trimmed.startsWith("[") || trimmed.startsWith("]");
        }
    }
}
