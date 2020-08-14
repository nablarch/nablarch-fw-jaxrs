package nablarch.fw.jaxrs;

import nablarch.fw.web.HttpResponse;

/**
 * Entityを持つレスポンス。
 *
 * {@link javax.ws.rs.Produces}を使用した場合に
 * レスポンスヘッダとステータスコードを指定したい場合に使用する。
 *
 * @author Kiyohito Itoh
 */
public class EntityResponse extends HttpResponse {

    /** エンティティ */
    private Object entity;

    /** ステータスコードが設定されたか否か */
    private boolean statusCodeSet = false;

    /**
     * エンティティを取得する。
     *
     * @return エンティティ
     */
    public Object getEntity() {
        return entity;
    }

    /**
     * エンティティを設定する。
     *
     * @param entity エンティティ
     */
    public EntityResponse setEntity(Object entity) {
        this.entity = entity;
        return this;
    }

    @Override
    public HttpResponse setStatusCode(int code) {
        statusCodeSet = true;
        return super.setStatusCode(code);
    }

    /**
     * ステータスコードが設定されたかを判定する。
     *
     * @return ステータスコードが設定された場合はtrue
     */
    public boolean isStatusCodeSet() {
        return statusCodeSet;
    }
}
