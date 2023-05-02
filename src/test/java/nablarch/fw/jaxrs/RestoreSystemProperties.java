package nablarch.fw.jaxrs;

import org.junit.rules.ExternalResource;

import java.util.Properties;

/**
 * システムプロパティをテスト実行前の状態に戻すルール。
 */
public class RestoreSystemProperties extends ExternalResource {

    /**
     * テスト実行前のシステムプロパティ。
     */
    private Properties systemProps;

    @Override
    protected void before() {
        systemProps = (Properties) System.getProperties().clone();
    }

    @Override
    protected void after() {
        System.setProperties(systemProps);
    }
}
