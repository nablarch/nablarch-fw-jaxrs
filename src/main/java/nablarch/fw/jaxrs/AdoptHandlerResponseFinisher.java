package nablarch.fw.jaxrs;

import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpRequestHandler;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.ServletExecutionContext;

/**
 * {@link HttpRequestHandler}を{@link ResponseFinisher}として使用するクラス。
 *
 * このクラスで使用できる{@link HttpRequestHandler}は、自らレスポンスを作成せず、
 * 後続ハンドラが返すレスポンスに変更を加えるハンドラに限定される。
 *
 * @author Kiyohito Itoh
 */
public class AdoptHandlerResponseFinisher implements ResponseFinisher {

    /** ハンドラ */
    private HttpRequestHandler handler;

    /**
     * ハンドラを設定する。
     * @param handler ハンドラ
     */
    public void setHandler(HttpRequestHandler handler) {
        this.handler = handler;
    }

    @Override
    public void finish(HttpRequest request, final HttpResponse response, ExecutionContext context) {
        handler.handle(request, copy(context).addHandler(new HttpRequestHandler() {
            @Override
            public HttpResponse handle(HttpRequest request, ExecutionContext context) {
                return response;
            }
        }));
    }

    /**
     * コンテキストをコピーする。
     *
     * リクエストスコープ、セッションスコープ、セッションストアがコピーされる。
     *
     * @param original コピー元のコンテキスト
     * @return コピーされたコンテキスト
     */
    protected ExecutionContext copy(ExecutionContext original) {
        ServletExecutionContext from = (ServletExecutionContext) original;
        ServletExecutionContext to = new ServletExecutionContext(
                from.getServletRequest(), from.getServletResponse(), from.getServletContext());
        to.setSessionStoreMap(from.getSessionStoreMap());
        return to;
    }
}
