package nablarch.fw.jaxrs;

import nablarch.core.repository.di.ComponentFactory;
import nablarch.core.util.annotation.Published;
import nablarch.fw.Handler;
import nablarch.fw.web.HttpRequest;

import java.util.List;

/**
 * JAX-RSで実行される{@link Handler}のリストを生成するインタフェース。
 *
 * @author Naoki Yamamoto
 */
@Published(tag = "architect")
public interface JaxRsHandlerListFactory extends ComponentFactory<List<Handler<HttpRequest, ?>>> {
}
