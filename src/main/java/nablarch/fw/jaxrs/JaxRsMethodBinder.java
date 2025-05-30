package nablarch.fw.jaxrs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.ws.rs.Path;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.HandlerWrapper;
import nablarch.fw.Interceptor.Factory;
import nablarch.fw.MethodBinder;
import nablarch.fw.handler.MethodBinding;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

/**
 * JAX-RS用の{@link MethodBinder}実装クラス。
 *
 * @author Naoki Yamamoto
 */
public class JaxRsMethodBinder implements MethodBinder<HttpRequest, Object> {

    /** ディスパッチするメソッド名 */
    private final String methodName;

    /** JAX-RS用のハンドラリスト */
    private final List<Handler<HttpRequest, ?>> handlerList;

    /**
     * コンストラクタ。
     *
     * @param methodName メソッド名
     * @param handlerList ハンドラリスト
     */
    public JaxRsMethodBinder(final String methodName, final List<Handler<HttpRequest, ?>> handlerList) {
        this.methodName = methodName;
        this.handlerList = handlerList;
    }

    @Override
    public HandlerWrapper<HttpRequest, Object> bind(final Object delegate) {
        return new JaxrsMethodBinding(delegate, methodName, handlerList);
    }

    /**
     * リソースメソッドを表すクラス。
     */
    public static class ResourceMethod {

        /** リソースメソッド */
        private final Method method;

        /** パラメータタイプのリスト */
        private final List<ParameterType> parameterTypes;

        /**
         * パラメータタイプ
         */
        private enum ParameterType {
            /** {@link HttpRequest} */
            HTTP_REQUEST,
            /** {@link ExecutionContext} */
            CONTEXT,
            /** Beanオブジェクト */
            BEAN
        }

        /**
         * リソースメソッドを持つ{@code ResourceMethod}を生成する。
         *
         * @param method リソースメソッド
         */
        public ResourceMethod(final Method method) {
            parameterTypes = createParameterTypeList(method);
            this.method = method;
        }

        /**
         * リソースメソッドを呼び出し、結果を返却する。
         *
         * @param resourceClass リソースクラス
         * @param request 入力データ
         * @param context 実行コンテキスト
         * @return 処理結果
         */
        public Object invoke(final Object resourceClass, final HttpRequest request,
                final ExecutionContext context) {
            try {
                return method.invoke(resourceClass, createParameter(request, context));
            } catch (IllegalAccessException e) {
                // ここには到達しない。
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                // 委譲先のメソッドで例外が送出された場合。
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                if (cause instanceof Error) {
                    throw (Error) cause;
                }
                throw new RuntimeException(cause);
            }
        }

        /**
         * パラメータリストを生成する。
         *
         * @param resourceMethod リソースメソッド
         * @return パラメータのタイプリスト
         */
        private List<ParameterType> createParameterTypeList(final Method resourceMethod) {
            final List<ParameterType> result = new ArrayList<ParameterType>(3);
            for (Class<?> type : resourceMethod.getParameterTypes()) {
                final ParameterType parameterType;
                if (type.equals(HttpRequest.class) || type.equals(JaxRsHttpRequest.class) ) {
                    parameterType = ParameterType.HTTP_REQUEST;
                } else if (type.equals(ExecutionContext.class)) {
                    parameterType = ParameterType.CONTEXT;
                } else {
                    parameterType = ParameterType.BEAN;
                }
                if (result.contains(parameterType)) {
                    throw new IllegalArgumentException(
                            "argument definition is invalid. method = [" + resourceMethod.getName() + ']');
                }
                result.add(parameterType);
            }
            return result;
        }

        /**
         * リソースメソッドのパラメータを生成する。
         *
         * @param request 入力データ
         * @param context 実行コンテキスト
         * @return パラメータ
         */
        private Object[] createParameter(final HttpRequest request, final ExecutionContext context) {
            final List<Object> params = new ArrayList<Object>();
            for (ParameterType paramType : parameterTypes) {
                if (paramType == ParameterType.HTTP_REQUEST) {
                    params.add(new JaxRsHttpRequest(request));
                } else if (paramType == ParameterType.CONTEXT) {
                    params.add(context);
                } else {
                    final JaxRsContext jaxRsContext = JaxRsContext.get(context);
                    params.add(jaxRsContext.getRequest());
                }
            }
            return params.toArray();
        }
    }

    /**
     * jax-rsのリソースクラスのメソッドへとディスパッチを行うクラス。
     */
    private static class JaxrsMethodBinding extends MethodBinding<HttpRequest, Object> {

        /** リソースクラス */
        private final Object delegate;

        /** メソッド名 */
        private final String methodName;

        /** ハンドラーリスト */
        private final List<Handler<HttpRequest, ?>> handlerList;

        /**
         * {@code JaxrsMethodBinding}を生成する。
         *
         * @param delegate リソースクラスのインスタンス
         * @param methodName メソッド名
         * @param handlerList ハンドラーリスト
         */
        public JaxrsMethodBinding(final Object delegate, final String methodName, final List<Handler<HttpRequest, ?>> handlerList) {
            super(delegate);
            this.delegate = delegate;
            this.methodName = methodName;
            this.handlerList = handlerList;
        }

        @Override
        protected Method getMethodBoundTo(final HttpRequest httpRequest, final ExecutionContext executionContext) {
            Method method = null;

            Class<?> resourceClass = findJaxRsResourceClass(delegate.getClass());
            Method[] methods;

            if (resourceClass != null) {
                // JAX-RSリソースクラスの場合は、そのクラスにのみ定義されたpublicメソッドを対象とする
                methods =
                        Arrays.stream(resourceClass.getDeclaredMethods())
                                .filter(m -> Modifier.isPublic(m.getModifiers()))
                                .toArray(Method[]::new);
            } else {
                // @Pathアノテーションが付与されたリソースクラスではない場合は、delegateに指定されたクラスをそのまま使用する
                resourceClass = delegate.getClass();
                methods = resourceClass.getMethods();
            }

            for (Method m : methods) {
                if (!m.getName().equals(methodName)) {
                    continue;
                }
                if (method != null) {
                    throw new IllegalArgumentException(
                            "method name is duplicated. class = [" + resourceClass.getName() + "],"
                                    + " method = [" + methodName + ']');
                }
                method = m;
            }
            return method;
        }

        /**
         * リソースクラスを引数に指定したクラス、親クラス、実装しているインターフェースの順で探索する
         *
         * @param clazz 確認対象のクラス
         * @return 探索してリソースクラスが見つかった場合はその{@link Class}クラス、見つからなかった場合は{@code null}
         */
        private Class<?> findJaxRsResourceClass(Class<?> clazz) {
            if (isJaxRsResourceClass(clazz)) {
                return clazz;
            }
            // 親クラスを優先して探索する
            Class<?> superClass = clazz.getSuperclass();
            if (isJaxRsResourceClass(superClass)) {
                return superClass;
            }

            // インタフェースを検索する
            for (Class<?> interfaceClass : clazz.getInterfaces()) {
                if (isJaxRsResourceClass(interfaceClass)) {
                    return interfaceClass;
                }
            }

            if (!superClass.equals(Object.class)) {
                return findJaxRsResourceClass(superClass);
            }

            return null;
        }

        /**
         * リソースクラスかどうかを判定する。
         *
         * @param clazz 判定対象のクラス
         * @return JAX-RSのリソースクラスの場合は{@code true}
         */
        private boolean isJaxRsResourceClass(Class<?> clazz) {
            return clazz.isAnnotationPresent(Path.class);
        }

        @Override
        public HttpResponse handle(final HttpRequest req, final ExecutionContext ctx) {
            final Method boundMethod = getMethodBoundTo(req, ctx);
            if (boundMethod == null) {
                throw new HttpErrorResponse(HttpResponse.Status.NOT_FOUND.getStatusCode());
            }

            final ResourceMethod resourceMethod = new ResourceMethod(boundMethod);

            final Handler<HttpRequest, Object> handler = new Handler<HttpRequest, Object>() {
                @Override
                public Object handle(final HttpRequest req, final ExecutionContext ctx) {
                    saveBoundClassAndMethodToRequestScope(ctx, delegate.getClass(), boundMethod);
                    return resourceMethod.invoke(delegate, req, ctx);
                }
            };

            JaxRsContext.set(ctx, new JaxRsContext(boundMethod));

            if (handlerList != null && !handlerList.isEmpty()) {
                ctx.addHandlers(handlerList);
            }
            final Handler<HttpRequest, Object> wrappedHandler = 
                    Factory.wrap(handler, boundMethod.getAnnotations());
            return ctx.addHandler(wrappedHandler).handleNext(req);
        }
    }
}

