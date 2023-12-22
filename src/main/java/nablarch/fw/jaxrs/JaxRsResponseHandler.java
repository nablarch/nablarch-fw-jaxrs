package nablarch.fw.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.util.FileUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpRequestHandler;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.ServletExecutionContext;

/**
 * JAX-RS用のレスポンスを返却するハンドラ。
 * <p/>
 * このハンドラでは、後続のハンドラから戻された{@link HttpResponse}の内容を、クライアントへのレスポンスとして書き込む。
 * 後続のハンドラで例外が発生した場合には、{@link ErrorResponseBuilder}を使用してエラー用のレスポンスを作成し、クライアントへのレスポンスとして書き込む。
 * <p/>
 * 後続のハンドラ及び{@link ErrorResponseBuilder}で{@link HttpResponse}を生成する際には、レスポンスヘッダーも含めて設定する必要がある。
 * このハンドラでは、レスポンスヘッダーを自動的に設定するようなことはしない。
 *
 * @author Hisaaki Shioiri
 */
public class JaxRsResponseHandler implements HttpRequestHandler {

    /** ストリームに出力する際のバッファサイズ。 */
    private static final int BUFFER_SIZE = 4096;

    /** エラーレスポンスビルダー */
    private ErrorResponseBuilder errorResponseBuilder = new ErrorResponseBuilder();

    /** エラー情報を出力するライター */
    private JaxRsErrorLogWriter errorLogWriter = new JaxRsErrorLogWriter();

    /** レスポンスフィニッシャー */
    private List<ResponseFinisher> responseFinishers = Collections.emptyList();

    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(JaxRsResponseHandler.class);

    @Override
    public HttpResponse handle(HttpRequest request, ExecutionContext context) {
        HttpResponse response;
        try {
            response = context.handleNext(request);
        } catch (HttpErrorResponse errorResponse) {
            response = errorResponse.getResponse();
        } catch (Throwable e) {
            try {
                response = errorResponseBuilder.build(request, context, e);
            } catch (Throwable responseBuilderException) {
                response = new HttpResponse(500);
                LOGGER.logWarn("An exception was thrown while processing ErrorResponseBuilder. "
                        + "class=[" + errorResponseBuilder.getClass().getName() + "]", responseBuilderException);
            }
            errorLogWriter.write(request, response, context, e);
        }
        finishResponse(request, response, context);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.logDebug(request.getMethod() + ' ' + request.getRequestUri() + " status code=[" + response.getStatusCode() + "], content length=[" + response.getContentLength() + ']');
        }
        writeResponse(response, (ServletExecutionContext) context);
        return response;
    }

    /**
     * レスポンスを仕上げる。
     *
     * @param request リクエスト
     * @param response レスポンス
     * @param context コンテキスト
     */
    protected void finishResponse(HttpRequest request, HttpResponse response, ExecutionContext context) {
        for (ResponseFinisher responseFinisher : responseFinishers) {
            responseFinisher.finish(request, response, context);
        }
    }
    /**
     * レスポンスを書き込む。
     *
     * @param response {@link HttpResponse}
     * @param context {@link ServletExecutionContext}
     */
    protected void writeResponse(final HttpResponse response, final ServletExecutionContext context) {
        final HttpServletResponse nativeResponse = context.getServletResponse();
        writeHeaders(response, nativeResponse);
        final InputStream inputStream = response.getBodyStream();
        if (inputStream != null) {
            try {
                writeBody(inputStream, context.getServletResponse());
            } catch (IOException e) {
                // 応答の書き込みに失敗した場合は、証跡ログのみを残して処理を終了する。
                LOGGER.logWarn("failed to write response.", e);
            } finally {
                response.cleanup();
            }
        }
    }

    /**
     * レスポンスヘッダーを書き込む。
     * <p/>
     * {@link HttpResponse}内のヘッダー情報を、{@link HttpServletResponse}に対して書き込む。
     *
     * @param response {@link HttpResponse}
     * @param nativeResponse {@link HttpServletResponse}
     */
    protected void writeHeaders(HttpResponse response, HttpServletResponse nativeResponse) {
        nativeResponse.setStatus(response.getStatusCode());
        if (response.getContentLength() != null) {
            nativeResponse.setContentLength(Integer.parseInt(response.getContentLength()));
        }
        String contentType = response.getContentType();
        if (contentType != null) {
            nativeResponse.setContentType(response.getContentType());
        }
        for (Map.Entry<String, String> entry : response.getHeaderMap().entrySet()) {
            if (!entry.getKey().equals("Content-Length") && !entry.getKey().equals("Content-Type")) {
                nativeResponse.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * メッセージボディの内容をクライアントに送信する。
     *
     * @param in 入力ストリームの内容
     * @param nativeRes サーブレットレスポンス
     * @throws IOException ソケットI/Oにおけるエラー
     */
    protected static void writeBody(InputStream in, HttpServletResponse nativeRes)
            throws IOException {
        OutputStream out = nativeRes.getOutputStream();
        try {
            while (true) {
                byte[] bytes = new byte[BUFFER_SIZE];
                int readBytes = in.read(bytes);
                if (readBytes == -1) {
                    break;
                }
                out.write(bytes, 0, readBytes);
            }
        } finally {
            FileUtil.closeQuietly(in);
            FileUtil.closeQuietly(out);
        }
    }

    /**
     * エラーレスポンスビルダーを設定する。
     * <p/>
     * デフォルト実装である{@link ErrorResponseBuilder}を差し替えたい場合に拡張クラスを設定する。
     *
     * @param errorResponseBuilder エラーレスポンスビルダー
     */
    public void setErrorResponseBuilder(final ErrorResponseBuilder errorResponseBuilder) {
        this.errorResponseBuilder = errorResponseBuilder;
    }

    /**
     * エラーログライターを設定する。
     * <p/>
     * デフォルト実装である{@link JaxRsErrorLogWriter}を差し替えたい場合に拡張クラスを設定する。
     *
     * @param errorLogWriter エラーログライター
     */
    public void setErrorLogWriter(final JaxRsErrorLogWriter errorLogWriter) {
        this.errorLogWriter = errorLogWriter;
    }

    /**
     * レスポンスフィニッシャーを設定する。
     * @param responseFinishers レスポンスフィニッシャー
     */
    public void setResponseFinishers(List<ResponseFinisher> responseFinishers) {
        this.responseFinishers = responseFinishers;
    }
}

