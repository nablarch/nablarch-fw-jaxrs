package nablarch.fw.jaxrs;

import nablarch.core.util.annotation.Published;
import nablarch.fw.web.HttpResponse;

/**
 * Entityを持つレスポンス。
 * <p>
 * {@link jakarta.ws.rs.Produces}を使用した場合に
 * レスポンスヘッダとステータスコードを指定したい場合に使用する。
 *
 * @param <E> Entityの型
 * @author Kiyohito Itoh
 */
public class EntityResponse<E> extends HttpResponse {

    /** エンティティ */
    private E entity;

    /** ステータスコードが設定されたか否か */
    private boolean statusCodeSet = false;

    /**
     * エンティティを取得する。
     *
     * @return エンティティ
     */
    public E getEntity() {
        return entity;
    }

    /**
     * エンティティを設定する。
     *
     * @param entity エンティティ
     */
    @Published
    public EntityResponse<E> setEntity(E entity) {
        this.entity = entity;
        return this;
    }

    @Override
    @Published
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
