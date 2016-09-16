package nablarch.fw.jaxrs.integration.app;

import nablarch.core.validation.ee.DomainManager;

/**
 * {@link DomainManager}実装クラス。
 * 本PJで使用するDomainBeanクラスを返却する。
 *
 * @author Naoki Yamamoto
 */
public class JaxRsDomainManager implements DomainManager<BeanDomain> {

    /** {@inheritDoc} */
    @Override
    public Class<BeanDomain> getDomainBean() {
        return BeanDomain.class;
    }
}
