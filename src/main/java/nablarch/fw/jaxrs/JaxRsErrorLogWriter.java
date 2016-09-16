package nablarch.fw.jaxrs;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.log.app.FailureLogUtil;
import nablarch.core.message.ApplicationException;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

/**
 * JAX-RSのエラー情報をログに出力するクラス。
 * <p/>
 * このクラスでは、{@link ApplicationException}以外の例外の場合に、
 * {@link FailureLogUtil}を用いてログ出力を行う。
 * <p/>
 * このクラスで要件を満たせない場合には、サブクラスで実装を置き換えること。
 *
 * @author Hisaaki Shioiri
 */
@Published(tag = "architect")
public class JaxRsErrorLogWriter {

    /** ロガー */
    protected static final Logger LOGGER = LoggerManager.get("jax-rs");

    /**
     * エラー情報をログに出力する。
     *
     * @param request {@link HttpRequest}
     * @param response {@link HttpResponse}
     * @param context {@link ExecutionContext}
     * @param throwable {@link Throwable}
     */
    public void write(final HttpRequest request, final HttpResponse response, final ExecutionContext context,
            final Throwable throwable) {
        if (throwable instanceof ApplicationException) {
            try {
                writeApplicationExceptionLog(request, response, context, (ApplicationException) throwable);
            } catch (RuntimeException e) {
                // ログ出力時に例外が発生した場合は、FATALレベルのログを出力して終える。
                FailureLogUtil.logFatal(e, (Object) null, null);
            }
        } else {
            FailureLogUtil.logFatal(throwable, (Object) null, null);
        }
    }

    /**
     * {@link ApplicationException}の情報をログ出力する。
     * <p/>
     * デフォルト実装では何も出力しない。
     *
     * @param request {@link HttpRequest}
     * @param response {@link HttpResponse}
     * @param context {@link ExecutionContext}
     * @param exception {@link ApplicationException}
     */
    protected void writeApplicationExceptionLog(
            final HttpRequest request, final HttpResponse response, final ExecutionContext context,
            final ApplicationException exception) {
    }
}
