package nablarch.fw.jaxrs;

import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;

import nablarch.core.message.ApplicationException;
import nablarch.core.validation.ee.ValidatorUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.web.HttpRequest;

/**
 * リソースメソッドが受け取るBeanオブジェクトに対してバリデーションを実行するハンドラ実装クラス。
 * <p/>
 * リソースメソッドに対して{@link Valid}アノテーションが設定されている場合、
 * データオブジェクト(リソースメソッドの引数となるBeanオブジェクト)に対してバリデーションを行う。
 * リソースメソッドに{@link ConvertGroup}アノテーションが設定されている場合、バリデーション時にBean Validationのグループを設定できる。
 * バリデーションエラーが発生した場合には、{@link ApplicationException}を送出する。
 * エラーが発生しなかった場合は、後続のハンドラに処理を委譲する。
 *
 * @author Hisaaki Shioiri
 */
public class JaxRsBeanValidationHandler implements Handler<HttpRequest, Object> {

    @Override
    public Object handle(HttpRequest request, ExecutionContext context) {
        final JaxRsContext jaxRsContext = JaxRsContext.get(context);

        if (jaxRsContext.hasValidAnnotation() && jaxRsContext.hasRequest()) {
            if(jaxRsContext.hasConvertGroupAnnotation() && Default.class == jaxRsContext.getFromOfConvertGroupAnnotation()) {
                validateParamWithGroup(jaxRsContext);
            } else {
                validateParam(jaxRsContext);
            }
        }
        return context.handleNext(request);
    }

    /**
     * リソースメソッドのパラメータとなるBeanオブジェクトへのバリデーションを行う。
     *
     * @param jaxRsContext {@link JaxRsContext}
     */
    private void validateParam(final JaxRsContext jaxRsContext) {
        ValidatorUtil.validate(jaxRsContext.getRequest());
    }

    private void validateParamWithGroup(final JaxRsContext jaxRsContext) {
        ValidatorUtil.validateWithGroup(jaxRsContext.getRequest(), jaxRsContext.getToOfConvertGroupAnnotation());
    }
}
