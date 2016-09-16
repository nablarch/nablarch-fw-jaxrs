package nablarch.fw.jaxrs;

import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import static nablarch.fw.jaxrs.HttpResponseMatcher.isStatusCode;

/**
 * {@link FormUrlEncodedConverter}のテスト。
 */
public class FormUrlEncodedConverterTest {

    @Mocked
    private HttpRequest request;

    private FormUrlEncodedConverter sut;
    private ExecutionContext executionContext;

    @Before
    public void setUp() {
        sut = new FormUrlEncodedConverter();
        executionContext = new ExecutionContext();
    }

    /**
     * {@link MediaType#APPLICATION_FORM_URLENCODED}の場合、変換できること。
     */
    @Test
    public void isConvertibleForValidMediaType() {
        assertThat(sut.isConvertible(MediaType.APPLICATION_FORM_URLENCODED), is(true));
        assertThat("前方一致すればOK", sut.isConvertible(MediaType.APPLICATION_FORM_URLENCODED + "; charset=UTF-8"), is(true));
        assertThat("大文字、小文字は問わない", sut.isConvertible("APPlication/x-WWW-form-uRlEnCoDeD"), is(true));
    }

    /**
     * {@link MediaType#APPLICATION_FORM_URLENCODED}以外の場合、変換できないこと。
     */
    @Test
    public void isConvertibleForInvalidMediaType() {
        assertThat("後方一致してもダメ", sut.isConvertible("[" + MediaType.APPLICATION_FORM_URLENCODED), is(false));
        assertThat("他のメディアタイプもダメ", sut.isConvertible(MediaType.APPLICATION_XML), is(false));
    }

    /**
     * リクエストパラメータに応じてBeanが生成されること。
     */
    @Test
    public void convertRequestForParameters() throws Exception {

        JaxRsContext.set(executionContext, new JaxRsContext(Action.class.getMethod("method1", Entity.class)));

        new Expectations() {{
            request.getParamMap();
            result = new HashMap<String, String[]>() {{
                put("strValue", new String[] {"test"});
                put("intValue", new String[] {"123"});
                put("unusedValue", new String[] {"hoge"});
            }};
        }};

        Object object = sut.convertRequest(request, executionContext);

        Entity entity = (Entity) object;
        assertThat(entity.getStrValue(), is("test"));
        assertThat(entity.getIntValue(), is(123));
        assertThat(entity.getInnerVariable(), is(nullValue()));
    }

    /**
     * リクエストパラメータが空の場合、空のBeanが生成されること。
     */
    @Test
    public void convertRequestForEmptyParameters() throws Exception {

        JaxRsContext.set(executionContext, new JaxRsContext(Action.class.getMethod("method1", Entity.class)));

        new Expectations() {{
            request.getParamMap();
            result = Collections.emptyMap();
        }};

        Object object = sut.convertRequest(request, executionContext);

        Entity entity = (Entity) object;
        assertThat(entity.getStrValue(), is(nullValue()));
        assertThat(entity.getIntValue(), is(nullValue()));
        assertThat(entity.getInnerVariable(), is(nullValue()));
    }

    /**
     * Beanに文字列以外の型を使い、パラメータがその型に合わない値の場合、
     * その値のみ変換されないこと。
     */
    @Test
    public void convertRequestForMismatchingBeanPropertyType() throws Exception {

        JaxRsContext.set(executionContext, new JaxRsContext(Action.class.getMethod("method1", Entity.class)));

        new Expectations() {{
            request.getParamMap();
            result = new HashMap<String, String[]>() {{
                put("strValue", new String[] {"test"});
                put("intValue", new String[] {"moji"}); // Integerに対して数字以外の値
                put("unusedValue", new String[] {"hoge"});
            }};
        }};

        Object object = sut.convertRequest(request, executionContext);

        Entity entity = (Entity) object;
        assertThat(entity.getStrValue(), is("test"));
        assertThat(entity.getIntValue(), is(nullValue())); // 変換されない
        assertThat(entity.getInnerVariable(), is(nullValue()));
    }

    /**
     * レスポンスオブジェクトの型が{@link MultivaluedMap}以外の場合、
     * 実行時例外がスローされること。
     */
    @Test
    public void convertResponseForInvalidResponseType() throws Exception {

        JaxRsContext.set(executionContext, new JaxRsContext(Action.class.getMethod("method1", Entity.class)));

        try {
            sut.convertResponse(new Entity(), executionContext);
            fail("IllegalStateExceptionがスローされるはず");
        } catch (IllegalStateException e) {
            assertThat(e.getCause(), is(instanceOf(ClassCastException.class)));
            assertThat(e.getMessage(), is(
                "return type of resource method that specified @Produces({ \"application/x-www-form-urlencoded\" }) "
              + "should be MultivaluedMap<String, String>. "
              + "resource class = [Action], resource method = [method1], return type = [Entity]"));
        }
    }

    /**
     * 空のレスポンスが指定された場合、{@link nablarch.fw.web.HttpResponse.Status#NO_CONTENT}が返されること。
     */
    @Test
    public void convertResponseForEmptyResponse() {

        HttpResponse response = sut.convertResponse(new MultivaluedHashMap<String, String>(), executionContext);

        assertThat(response, isStatusCode(HttpResponse.Status.NO_CONTENT.getStatusCode()).withEmptyBody());
    }

    /**
     * 1つの項目を含むレスポンスが指定された場合、レスポンスに応じたボディが生成されること。
     */
    @Test
    public void convertResponseForSingleValue() throws Exception {

        JaxRsContext.set(executionContext, new JaxRsContext(Action.class.getMethod("method1", Entity.class)));

        MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<String, String>();
        multivaluedMap.add("key1", "value1");

        HttpResponse response = sut.convertResponse(multivaluedMap, executionContext);

        assertThat(response, isStatusCode(HttpResponse.Status.OK.getStatusCode()));
        String body = response.getBodyString();
        assertThat(body.length(), is(11));
        assertThat(body, containsString("key1=value1")); // 11
    }

    /**
     * 複数の項目を含むレスポンスが指定された場合、レスポンスに応じたボディが生成されること。
     * コンバータに設定されたデフォルトのエンコーディングを使う場合。
     */
    @Test
    public void convertResponseForMultiValuesUsingDefaultEncoding() throws Exception {

        JaxRsContext.set(executionContext, new JaxRsContext(Action.class.getMethod("method1", Entity.class)));

        MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<String, String>();
        multivaluedMap.add("key1", "value1");
        multivaluedMap.add("key2", "");
        multivaluedMap.add("key3", null); // MultivaluedHashMapの実装だと、値がnullの場合に追加されない。
        multivaluedMap.add("key4", "value4-1");
        multivaluedMap.add("key4", "value4-2");
        multivaluedMap.add("key4", "value4-3");
        multivaluedMap.add("key5", "あいうえお");
        multivaluedMap.add("キー6", "value6");

        HttpResponse response = sut.convertResponse(multivaluedMap, executionContext);

        assertThat(response, isStatusCode(HttpResponse.Status.OK.getStatusCode()));
        String body = response.getBodyString();
        assertThat(body.length(), is(137)); // 11 + 6 + 42(=14*3) + 51 + 27 = 137
        assertThat(body, containsString("key1=value1")); // 11
        assertThat(body, containsString("key2=")); // & + 5 = 6
        assertThat(body, not(containsString("key3"))); // 0
        assertThat(body, containsString("key4=value4-1")); // & + 13 = 14
        assertThat(body, containsString("key4=value4-2")); // & + 13 = 14
        assertThat(body, containsString("key4=value4-3")); // & + 13 = 14
        assertThat(body, containsString("key5=%E3%81%82%E3%81%84%E3%81%86%E3%81%88%E3%81%8A")); // & + 50 = 51
        assertThat(body, containsString("%E3%82%AD%E3%83%BC6=value6")); // & + 26 = 27
    }

    /**
     * 複数の項目を含むレスポンスが指定された場合、レスポンスに応じたボディが生成されること。
     * メディアタイプに指定されたエンコーディングを使う場合。
     */
    @Test
    public void convertResponseForMultiValuesUsingMediaTypeEncoding() throws Exception {

        JaxRsContext.set(executionContext, new JaxRsContext(Action.class.getMethod("method2", Entity.class)));

        MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<String, String>();
        multivaluedMap.add("key1", "value1");
        multivaluedMap.add("key2", "");
        multivaluedMap.add("key3", null); // MultivaluedHashMapの実装だと、値がnullの場合に追加されない。
        multivaluedMap.add("key4", "value4-1");
        multivaluedMap.add("key4", "value4-2");
        multivaluedMap.add("key4", "value4-3");
        multivaluedMap.add("key5", "あいうえお");
        multivaluedMap.add("キー6", "value6");

        HttpResponse response = sut.convertResponse(multivaluedMap, executionContext);

        assertThat(response, isStatusCode(HttpResponse.Status.OK.getStatusCode()));
        String body = response.getBodyString();
        assertThat(body.length(), is(116)); // 11 + 6 + 42(=14*3) + 36 + 21 = 116
        assertThat(body, containsString("key1=value1")); // 11
        assertThat(body, containsString("key2=")); // & + 5 = 6
        assertThat(body, not(containsString("key3"))); // 0
        assertThat(body, containsString("key4=value4-1")); // & + 13 = 14
        assertThat(body, containsString("key4=value4-2")); // & + 13 = 14
        assertThat(body, containsString("key4=value4-3")); // & + 13 = 14
        assertThat(body, containsString("key5=%82%A0%82%A2%82%A4%82%A6%82%A8")); // & + 35 = 36
        assertThat(body, containsString("%83%4C%81%5B6=value6")); // & + 20 = 21
    }

    public static final class Action {

        @Produces({ "application/x-www-form-urlencoded" })
        public MultivaluedMap method1(Entity entity) {
            return null;
        }

        @Produces({ "application/x-www-form-urlencoded; charset=Windows-31J" })
        public MultivaluedMap method2(Entity entity) {
            return null;
        }
    }

    public static final class Entity {
        private String strValue;
        private Integer intValue;
        private String innerVariable;
        public String getStrValue() {
            return strValue;
        }
        public void setStrValue(String strValue) {
            this.strValue = strValue;
        }
        public Integer getIntValue() {
            return intValue;
        }
        public void setIntValue(Integer intValue) {
            this.intValue = intValue;
        }
        public String getInnerVariable() {
            return innerVariable;
        }
        public void setInnerVariable(String innerVariable) {
            this.innerVariable = innerVariable;
        }
    }
}
