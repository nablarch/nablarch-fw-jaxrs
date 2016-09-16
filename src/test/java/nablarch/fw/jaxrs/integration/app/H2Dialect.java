package nablarch.fw.jaxrs.integration.app;

import nablarch.core.db.dialect.DefaultDialect;

/**
 * identityカラムを使用可能にするための{@link DefaultDialect}実装。
 */
public class H2Dialect extends DefaultDialect {

    @Override
    public boolean supportsIdentity() {
        return true;
    }
}
