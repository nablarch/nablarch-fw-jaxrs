package nablarch.fw.jaxrs;

import nablarch.fw.Handler;
import nablarch.fw.MethodBinder;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.handler.MethodBinderFactory;

import java.util.Collections;
import java.util.List;

/**
 * JAX-RS用の{@link MethodBinder}を生成する。
 *
 * @author Hisaaki Shioiri
 */
public class JaxRsMethodBinderFactory implements MethodBinderFactory<Object> {

    /** ハンドラリスト */
    private List<Handler<HttpRequest, ?>> handlerList = Collections.emptyList();

    @Override
    public MethodBinder<HttpRequest, Object> create(String methodName) {
        return new JaxRsMethodBinder(methodName, handlerList);
    }

    /**
     * ハンドラリストを設定する。
     *
     * @param handlerList ハンドラリスト
     */
    public void setHandlerList(List<Handler<HttpRequest, ?>> handlerList) {
        this.handlerList = handlerList;
    }
}
