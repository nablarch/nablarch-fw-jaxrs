package nablarch.fw.jaxrs.cors;

import nablarch.fw.ExecutionContext;
import nablarch.fw.jaxrs.ResponseFinisher;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

/**
 * 実際のリクエストに対するレスポンスにCORSのレスポンスヘッダを設定するクラス。
 *
 * @author Kiyohito Itoh
 */
public class CorsResponseFinisher implements ResponseFinisher {

    private Cors cors = new BasicCors();

    @Override
    public void finish(HttpRequest request, HttpResponse response, ExecutionContext context) {
        cors.postProcess(request, response, context);
    }

    /**
     * CORSの処理を行うインタフェースを設定する。
     * @param cors CORSの処理を行うインタフェース
     */
    public void setCors(Cors cors) {
        this.cors = cors;
    }
}
