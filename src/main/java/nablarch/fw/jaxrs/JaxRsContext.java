package nablarch.fw.jaxrs;

import nablarch.core.util.StringUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;

import javax.validation.groups.ConvertGroup;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.lang.reflect.Method;

import javax.validation.Valid;

/**
 * JAX-RSのリソースクラスとしてActionハンドラを呼び出すのに必要な情報を保持するクラス。
 *
 * @author Kiyohito Itoh
 */
public class JaxRsContext {

    /** リクエストスコープに{@link JaxRsContext}を設定する際に使用するキー */
    private static final String CONTEXT_KEY = ExecutionContext.FW_PREFIX + JaxRsContext.class.getSimpleName();

    /**
     * リクエストスコープに設定された{@link JaxRsContext}を取得する。
     *
     * @param context 実行コンテキスト
     * @return {@link JaxRsContext}。設定されていない場合は<code>null</code>
     */
    public static JaxRsContext get(final ExecutionContext context) {
        return context.getRequestScopedVar(CONTEXT_KEY);
    }

    /**
     * {@link JaxRsContext}をリクエストスコープに設定する。
     *
     * @param context 実行コンテキスト
     * @param jaxRsContext {@link JaxRsContext}
     */
    public static void set(final ExecutionContext context, final JaxRsContext jaxRsContext) {
        context.setRequestScopedVar(CONTEXT_KEY, jaxRsContext);
    }

    /** リソースメソッド */
    private final Method resourceMethod;

    /** リクエスト */
    private Object request;

    /**
     * コンストラクタ。
     *
     * @param resourceMethod リソースメソッド
     */
    public JaxRsContext(final Method resourceMethod) {
        this.resourceMethod = resourceMethod;
    }

    /**
     * リソースメソッドを取得する。
     *
     * @return リソースメソッド
     */
    public Method getResourceMethod() {
        return resourceMethod;
    }

    /**
     * リクエストを取得する。
     *
     * @return リクエスト
     */
    public <T> T getRequest() {
        return (T) request;
    }


    /**
     * リクエストを保持しているか否か。
     *
     * @return リクエストを保持している場合は {@code true}
     */
    public boolean hasRequest() {
        return request != null;
    }

    /**
     * リクエストを設定する。
     *
     * @param request リクエスト
     */
    public void setRequest(final Object request) {
        this.request = request;
    }

    /**
     * リソースメソッドに{@link Valid}が設定されているか否か。
     *
     * @return {@link Valid}が設定されている場合 {@code true}
     */
    public boolean hasValidAnnotation() {
        return resourceMethod.getAnnotation(Valid.class) != null;
    }

    /**
     * リソースメソッドに{@link ConvertGroup}が設定されているか否か。
     *
     * @return {@link ConvertGroup}が設定されている場合 {@code true}
     */
    public boolean hasConvertGroupAnnotation() {
        return resourceMethod.getAnnotation(ConvertGroup.class) != null;
    }

    /**
     * リソースメソッドに設定されている{@link ConvertGroup}の{@code from}属性の値を取得する。
     *
     * @return {@code from}属性に設定されているBean Validationのグループ
     */
    public Class<?> getFromAttributesOfConvertGroupAnnotation() {

        ConvertGroup annotation = resourceMethod.getAnnotation(ConvertGroup.class);
        if (null == annotation) {
            throw new IllegalStateException("ConvertGroup annotation is not set for the resource method.");
        }

        return resourceMethod.getAnnotation(ConvertGroup.class).from();
    }

    /**
     * リソースメソッドに設定されている{@link ConvertGroup}の{@code to}属性の値を取得する。
     *
     * @return {@code to}属性に設定されているBean Validationのグループ
     */
    public Class<?> getToAttributesOfConvertGroupAnnotation() {

        ConvertGroup annotation = resourceMethod.getAnnotation(ConvertGroup.class);
        if (null == annotation) {
            throw new IllegalStateException("ConvertGroup annotation is not set for the resource method.");
        }

        return resourceMethod.getAnnotation(ConvertGroup.class).to();
    }

    /**
     * リソースメソッドから{@link Consumes}のメディアタイプを取得する。
     * @return メディアタイプ。指定がない場合は<code>null</code>。
     *          メディアタイプが複数指定されていた場合は先頭。
     */
    public String getConsumesMediaType() {
        Consumes consumes = resourceMethod.getAnnotation(Consumes.class);
        if (consumes == null) {
            return null;
        }
        return StringUtil.hasValue(consumes.value()) ? consumes.value()[0] : null;
    }

    /**
     * リソースメソッドから{@link Produces}のメディアタイプを取得する。
     * @return メディアタイプ。指定がない場合は<code>null</code>。
     *          メディアタイプが複数指定されていた場合は先頭。
     */
    public String getProducesMediaType() {
        Produces produces = resourceMethod.getAnnotation(Produces.class);
        if (produces == null) {
            return null;
        }
        return StringUtil.hasValue(produces.value()) ? produces.value()[0] : null;
    }

    /**
     * リソースメソッドが受け取るBeanの{@link Class}オブジェクトを取得する。
     * <p />
     * リソースメソッドがBeanを受け取らない場合は{@code null}を返却する。
     *
     * @return Beanの{@link Class}インスタンス
     */
    public Class<?> getRequestClass() {
        for (Class<?> paramType : resourceMethod.getParameterTypes()) {
            if (!paramType.equals(HttpRequest.class) && !paramType.equals(ExecutionContext.class)) {
                return paramType;
            }
        }
        return null;
    }

    /**
     * リソースメソッドの文字列表現を返す。
     * @return リソースクラス#リソースメソッド形式の文字列表現
     */
    public String toResourcePath() {
        final Class<?> resourceClass = resourceMethod.getDeclaringClass();
        return resourceClass.getName() + '#' + resourceMethod.getName();
    }
}
