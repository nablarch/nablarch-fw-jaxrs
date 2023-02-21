package nablarch.fw.jaxrs;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.ServletRequest;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.ServletExecutionContext;

/**
 * {@link BodyConverter}の実装クラスをサポートするクラス。
 *
 * @author Hisaaki Shioiri
 */
@Published(tag = "architect")
public abstract class BodyConverterSupport implements BodyConverter {

    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(BodyConverterSupport.class);

    /** Content-Type内のcharsetのパターン */
    private static final Pattern CHARSET_PATTERN = Pattern.compile("^.*?;\\s*charset=\\s*\"?(.*?)\"?\\s*$(;.*)?");

    /** デフォルトエンコーディング */
    private Charset defaultEncoding = Charset.forName("UTF-8");

    /**
     * デフォルトエンコーディングを設定する。
     *
     * @param name エンコーディング名
     */
    public void setDefaultEncoding(final String name) {
        defaultEncoding = Charset.forName(name);
    }

    @Override
    public Object read(HttpRequest request, ExecutionContext executionContext) {
        final JaxRsContext jaxRsContext = JaxRsContext.get(executionContext);

        final Class<?> beanClass = jaxRsContext.getRequestClass();
        if (beanClass == null) {
            throw new IllegalArgumentException("consumes media type and resource method signature is mismatch.");
        }

        final ServletRequest servletRequest = ((ServletExecutionContext) executionContext).getServletRequest();

        if (StringUtil.hasValue(servletRequest.getCharacterEncoding())) {
            validateEncoding(servletRequest.getCharacterEncoding());
        } else {
            changeRequestEncoding(servletRequest);
        }
        return convertRequest(request, executionContext);
    }

    /**
     * リクエストを変換する。
     *
     * @param request リクエスト
     * @param context 実行コンテキスト
     * @return 変換したオブジェクト
     */
    protected abstract Object convertRequest(HttpRequest request, ExecutionContext context);

    @Override
    public HttpResponse write(Object response, ExecutionContext executionContext) {
        if (response instanceof HttpResponse) {
            throw new IllegalArgumentException("produces media type and resource method signature is mismatch.");
        }
        return convertResponse(response, executionContext);
    }

    /**
     * レスポンスを変換する。
     *
     * @param response レスポンスオブジェクト
     * @param context 実行コンテキスト
     * @return 変換したオブジェクト
     */
    protected abstract HttpResponse convertResponse(Object response, ExecutionContext context);

    /**
     * デフォルトのエンコーディングを設定する。
     *
     * @param servletRequest {@link ServletRequest}
     */
    private void changeRequestEncoding(final ServletRequest servletRequest) {
        try {
            servletRequest.setCharacterEncoding(defaultEncoding.name());
        } catch (UnsupportedEncodingException ignore) {
            // not happened.
        }
    }

    /**
     * エンコーディング名の正当性を確認する。
     * <p/>
     * 不正なエンコーディング名であれば{@link HttpErrorResponse}を送出する。
     *
     * @param encoding エンコーディング
     */
    private static void validateEncoding(final String encoding) {
        try {
            Charset.forName(encoding);
        } catch (RuntimeException e) {
            LOGGER.logInfo("consumes charset is invalid. charset = [" + encoding + ']');
            throw new HttpErrorResponse(400, e);
        }
    }

    /**
     * メディアタイプからContent-Typeを取得する。
     * <p/>
     * メディアタイプにcharsetが含まれている場合は、エンコーディング名の正当性を確認し、
     * 問題がなければ、指定されたメディアタイプをそのままContent-Typeの値とする。
     * エンコーディング名が不正な場合は実行時例外を送出する。
     * <p/>
     * メディアタイプにcharsetが含まれていない場合は、デフォルトのエンコーディングを
     * メディアタイプに付けたものをContent-Typeの値とする。
     *
     * @param mediaType メディアタイプ
     * @return {@link ContentType}
     */
    protected ContentType getContentType(final String mediaType) {
        final Matcher matcher = CHARSET_PATTERN.matcher(mediaType);
        if (matcher.matches()) {
            try {
                return new ContentType(mediaType, Charset.forName(matcher.group(1)));
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("produces charset is invalid. charset = [" + matcher.group(1) + ']', e);
            }
        } else {
            return new ContentType(mediaType + ";charset=" + defaultEncoding.name(), defaultEncoding);
        }
    }

    /**
     * ContentTypeを表すクラス。
     *
     * @author Kiyohito Itoh
     */
    public static final class ContentType {

        /** ContentTypeの値 */
        private final String value;

        /** ContentTypeに指定されたエンコーディング */
        private final Charset encoding;

        /**
         * コンストラクタ。
         * @param value ContentTypeの値
         * @param encoding ContentTypeに指定されたエンコーディング
         */
        public ContentType(String value, Charset encoding) {
            this.value = value;
            this.encoding = encoding;
        }

        /**
         * ContentTypeの値を返す。
         * @return ContentTypeの値
         */
        public String getValue() {
            return value;
        }

        /**
         * ContentTypeに指定されたエンコーディングを返す。
         * @return ContentTypeに指定されたエンコーディング
         */
        public Charset getEncoding() {
            return encoding;
        }
    }
}
