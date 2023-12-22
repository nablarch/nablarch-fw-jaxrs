package nablarch.fw.jaxrs;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.ServletExecutionContext;

import jakarta.servlet.ServletRequest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JAXBを使用してリクエスト/レスポンスの変換を行う{@link BodyConverter}実装クラス。
 *
 * @author Naoki Yamamoto
 */
@Published(tag = "architect")
public class JaxbBodyConverter extends BodyConverterSupport {

    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(JaxbBodyConverter.class);

    /** {@link JAXBContext}のキャッシュ */
    private static final Map<Class<?>, JAXBContext> JAXB_CONTEXT_MAP = new ConcurrentHashMap<Class<?>, JAXBContext>();

    @Override
    protected Object convertRequest(HttpRequest request, ExecutionContext context) {
        final JaxRsContext jaxRsContext = JaxRsContext.get(context);
        final Class<?> beanClass = jaxRsContext.getRequestClass();
        final ServletRequest servletRequest = ((ServletExecutionContext) context).getServletRequest();

        final Unmarshaller unmarshaller;
        try {
            unmarshaller = getJAXBContext(beanClass).createUnmarshaller();
            configure(unmarshaller);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("failed to configure Unmarshaller.", e);
        }

        try {
            return unmarshaller.unmarshal(new StreamSource(servletRequest.getReader()), beanClass).getValue();
        } catch (JAXBException e) {
            LOGGER.logInfo("failed to read request. cause = [" + e.getMessage() + ']');
            throw new HttpErrorResponse(HttpResponse.Status.BAD_REQUEST.getStatusCode(), e);
        } catch (IOException e) {
            LOGGER.logInfo("failed to read request. cause = [" + e.getMessage() + ']');
            throw new HttpErrorResponse(HttpResponse.Status.BAD_REQUEST.getStatusCode(), e);
        }
    }

    @Override
    protected HttpResponse convertResponse(Object response, ExecutionContext context) {
        final JaxRsContext jaxRsContext = JaxRsContext.get(context);
        final HttpResponse httpResponse = new HttpResponse();

        final ContentType contentType = getContentType(jaxRsContext.getProducesMediaType());
        httpResponse.setContentType(contentType.getValue());

        final Marshaller marshaller;
        try {
            marshaller = getJAXBContext(response.getClass()).createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, contentType.getEncoding().name());
            configure(marshaller);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("failed to configure Marshaller.", e);
        }

        try {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            marshaller.marshal(response, os);
            httpResponse.write(os.toByteArray());
        } catch (JAXBException e) {
            throw new IllegalArgumentException("failed to write response.", e);
        }
        return httpResponse;
    }

    @Override
    public boolean isConvertible(String mediaType) {
        return mediaType.toLowerCase().startsWith("application/xml");
    }

    /**
     * Beanクラスに対応した{@link JAXBContext}を取得する。
     * <p/>
     * キャッシュ上に{@link JAXBContext}情報が存在する場合はその情報を返す。
     * まだキャッシュされていない場合には、{@link JAXBContext}を生成しキャッシュに格納する。
     *
     * @param beanClass Beanクラス
     * @return Beanクラスに対応した{@link JAXBContext}
     * @throws JAXBException {@link JAXBContext}の生成に失敗した場合
     */
    private JAXBContext getJAXBContext(Class<?> beanClass) throws JAXBException {
        if (JAXB_CONTEXT_MAP.containsKey(beanClass)) {
            return JAXB_CONTEXT_MAP.get(beanClass);
        }
        synchronized (JAXB_CONTEXT_MAP) {
            if (JAXB_CONTEXT_MAP.containsKey(beanClass)) {
                return JAXB_CONTEXT_MAP.get(beanClass);
            }
            JAXB_CONTEXT_MAP.put(beanClass, JAXBContext.newInstance(beanClass));
            return JAXB_CONTEXT_MAP.get(beanClass);
        }
    }

    /**
     * {@link Marshaller}に対するオプション設定を行う。
     * <p/>
     * このクラスではデフォルトで以下の設定でXMLの生成を行う。
     * 設定を変更したい場合はサブクラス側で行う必要がある。
     *
     * <ul>
     *     <li>改行、インデントを使用した形式にフォーマットする。</li>
     *     <li>
     *         文字コードはリソースメソッドの{@link jakarta.ws.rs.Produces}に設定された文字コードを使用する。<br/>
     *         文字コードが設定されていない場合はデフォルトエンコーディングを使用する。
     *     </li>
     * </ul>
     *
     * @param marshaller {@link Marshaller}
     * @throws JAXBException オプション設定に失敗した場合
     */
    protected void configure(Marshaller marshaller) throws JAXBException {
    }

    /**
     * {@link Unmarshaller}に対するオプション設定を行う。
     * <p/>
     * このクラスでは特に何も行わないので、オプション設定はサブクラス側で行う必要がある。
     *
     * @param unmarshaller {@link Unmarshaller}
     * @throws JAXBException オプション設定に失敗した場合
     */
    protected void configure(Unmarshaller unmarshaller) throws JAXBException {
    }
}
