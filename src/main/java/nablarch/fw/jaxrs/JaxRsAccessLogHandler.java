package nablarch.fw.jaxrs;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.log.app.AppLogUtil;
import nablarch.core.util.ObjectUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.jaxrs.JaxRsAccessLogFormatter.JaxRsAccessLogContext;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.ServletExecutionContext;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Map;

/**
 * RESTfulウェブサービスのアクセスログを出力するハンドラ。
 */
public class JaxRsAccessLogHandler implements Handler<HttpRequest, HttpResponse> {

    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get("HTTP_ACCESS");

    /** 空のオプション情報 */
    private static final Object[] EMPTY_OPTIONS = new Object[0];

    /** ログフォーマッター */
    private final JaxRsAccessLogFormatter logFormatter;

    /**
     * コンストラクタ。
     */
    public JaxRsAccessLogHandler() {
        Map<String, String> props = AppLogUtil.getProps();
        logFormatter = createLogFormatter(props);
        logFormatter.initialize(props);
    }

    /**
     * HTTPアクセスログを出力する。
     *
     * @param request  {@link HttpRequest}
     * @param context {@link ExecutionContext}
     * @return 次のハンドラの処理結果
     * @throws ClassCastException context の型が {@link ServletExecutionContext} でない場合。
     */
    @Override
    public HttpResponse handle(HttpRequest request, ExecutionContext context) {
        JaxRsAccessLogContext logContext = logFormatter.createAccessLogContext();
        logContext.setContext((ServletExecutionContext) context);
        logContext.setRequest(request);
        writeBeginLog(logContext);

        if (logFormatter.containsMemoryItem()) {
            MemoryUsage heapMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            long max = heapMemory.getMax();
            logContext.setMaxMemory(max);
            logContext.setFreeMemory(max - heapMemory.getUsed());
        }

        logContext.setStartTime(System.currentTimeMillis());
        try {
            HttpResponse response = context.handleNext(request);
            logContext.setResponse(response);
            return response;

        } catch (HttpErrorResponse errorResponse) {
            logContext.setResponse(errorResponse.getResponse());
            throw errorResponse;

        } finally {
            logContext.setEndTime(System.currentTimeMillis());
            writeEndLog(logContext);
        }
    }

    /**
     * 使用する {@link JaxRsAccessLogFormatter} を生成します。
     *
     * @return {@link JaxRsAccessLogFormatter}
     */
    protected JaxRsAccessLogFormatter createLogFormatter(Map<String, String> props) {
        String className = props.get(JaxRsAccessLogFormatter.PROPS_PREFIX + "className");
        if (className == null) {
            return new JaxRsAccessLogFormatter();
        }
        return ObjectUtil.createInstance(className);

    }

    /**
     * リクエスト処理開始時のログを出力する。
     *
     * @param logContext {@link JaxRsAccessLogContext}
     */
    protected void writeBeginLog(JaxRsAccessLogContext logContext) {
        if (logFormatter.isBeginOutputEnabled()) {
            Object[] requestLogOptions = getRequestOptions(logContext.getRequest(), logContext.getContext());
            LOGGER.logInfo(logFormatter.formatBegin(logContext), requestLogOptions);
        }
    }
    
    /**
     * リクエスト処理終了時のログを出力する。
     *
     * @param logContext {@link JaxRsAccessLogContext}
     */
    protected void writeEndLog(JaxRsAccessLogContext logContext) {
        if (logFormatter.isEndOutputEnabled()) {
            Object[] responseOptions = getResponseOptions(logContext.getRequest(), logContext.getResponse(), logContext.getContext());
            LOGGER.logInfo(logFormatter.formatEnd(logContext), responseOptions);
        }
    }

    /**
     * リクエスト処理開始時のログ出力で使用するオプション情報を取得する。
     *
     * @param request {@link HttpRequest}
     * @param context {@link ExecutionContext}
     * @return オプション情報。空の場合は {@code null}を返す。
     */
    protected Object[] getRequestOptions(HttpRequest request, ExecutionContext context) {
        return EMPTY_OPTIONS;
    }
    
    /**
     * リクエスト処理終了時のログ出力で使用するオプション情報を取得する。
     *
     * @param request {@link HttpRequest}
     * @param response {@link HttpResponse}
     * @param context {@link ExecutionContext}
     * @return オプション情報。空の場合は {@code null}を返す。
     */
    protected Object[] getResponseOptions(HttpRequest request, HttpResponse response, ExecutionContext context) {
        return EMPTY_OPTIONS;
    }
}
