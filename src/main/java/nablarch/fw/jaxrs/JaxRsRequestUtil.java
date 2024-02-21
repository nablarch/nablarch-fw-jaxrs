package nablarch.fw.jaxrs;

import nablarch.core.beans.BeanUtil;
import nablarch.core.util.annotation.Published;
import nablarch.core.validation.ee.ValidatorUtil;
import nablarch.fw.web.HttpRequest;

/**
 * JAX-RS用のBean関連処理に使用するユーティリティ。
 */
@Published
public final class JaxRsRequestUtil {
    

    /** 隠蔽コンストラクタ */
    private JaxRsRequestUtil() {
    }

    /**
     * HTTPリクエストからBeanを生成し、Bean Validationを行う。
     * 
     * @param beanClass 生成したいBeanクラス
     * @param request HTTPリクエスト
     * @return  プロパティに値が登録されたBeanオブジェクト
     */
    public static <T> T getValidatedBean(Class<T> beanClass, HttpRequest request) {
        T bean = BeanUtil.createAndCopy(beanClass, request.getParamMap());
        ValidatorUtil.validate(bean);
        return bean;
    }
    
    /**
     * HTTPリクエストからパスパラメータを取得する。
     * 
     * @param request HTTPリクエスト
     * @param name パラメータ名
     * @return パラメータの値
     */
    public static String getPathParam(HttpRequest request, String name) {
        String[] params = request.getParam(name);
        return params == null || params.length == 0 ? null : params[0];
    }
}
