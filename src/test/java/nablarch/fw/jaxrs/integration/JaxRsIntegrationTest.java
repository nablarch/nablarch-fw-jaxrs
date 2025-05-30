package nablarch.fw.jaxrs.integration;


import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import nablarch.core.util.Builder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import nablarch.fw.jaxrs.integration.app.IntegrationTestResource;
import nablarch.fw.jaxrs.integration.app.Person;
import nablarch.fw.jaxrs.integration.app.Persons;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JAX-RS上でNablarchが動作することを確認するインテグレーションテストクラス。
 * <p/>
 * 本クラスは、Glassfish-embedded上でテストすることを想定している。
 * <p/>
 */
@RunWith(Arquillian.class)
public class JaxRsIntegrationTest {

    @ClassRule
    public static IntegrationTestResource testResource = new IntegrationTestResource();

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class)
                .addPackage("nablarch.fw.jaxrs.integration.app")
                .addAsResource(new File("src/test/resources/integration/integration-log.properties"), "log.properties")
                .addAsResource(new File("src/test/resources/app-log.properties"))
                .setWebXML(new File("src/test/webapp/WEB-INF/web.xml"));

        for (File file : new File("src/test/resources/integration").listFiles()) {
            if (file.getName().endsWith(".xml")) {
                archive.addAsResource(file);
            }
        }
        return archive;
    }

    @ArquillianResource
    private URL baseUrl;

    private PrintStream originalStandardOutput;
    private ByteArrayOutputStream bufferStandardOutput;

    @Before
    public void setUp() throws Exception {
        testResource.truncatePersonTable();

        originalStandardOutput = System.out;
        bufferStandardOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bufferStandardOutput, true, StandardCharsets.UTF_8));
    }

    @After
    public void tearDown() {
        System.setOut(originalStandardOutput);
    }

    /**
     * リソースの戻りがvoidの場合204(not content)が戻されること
     */
    @Test
    @RunAsClient
    public void testResultNotContent() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/simple/notContent").toURI())
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertThat(response.getStatus(), is(204));
    }

    /**
     * データベースアクセス等がなく、成功のレスポンスのみを返すケース。
     */
    @Test
    @RunAsClient
    public void testWithoutDatabaseAccess() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/simple").toURI())
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.getLength(), is(0));
    }

    /**
     * dispatch先のメソッドのシグネチャがリソースクラスの要求と異なる場合はステータス500が返ること
     */
    @Test
    @RunAsClient
    public void invalidSignature() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/person/saveXmlInvalidSignature").toURI())
                .request(MediaType.APPLICATION_XML)
                .post(Entity.entity(new Person(1L, "test"), MediaType.APPLICATION_XML));

        assertThat(response.getStatus(), is(500));
        assertThat(response.getLength(), is(0));
        assertThat(testResource.findAllPerson().size(), is(0));

        assertLogContains("IllegalArgumentException"," argument definition is invalid. method = [saveXmlInvalidSignature]");
    }

    /**
     * Bodyが指定されたHttpResponseをActionから返すことができること。
     */
    @Test
    @RunAsClient
    public void httpResponseWithBodyInAction() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/simple/httpResponseWithBody").toURI())
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is("httpResponseWithBody invoked!!!"));
        assertThat(response.getLength(), is(31));
    }

    /**
     * リクエストヘッダの参照とレスポンスヘッダの設定ができること。
     */
    @Test
    @RunAsClient
    public void httpHeader() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/simple/httpHeader").toURI())
                .request(MediaType.APPLICATION_JSON)
                .header("TEST-REQUEST-HEADER", "test http header")
                .get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.getLength(), is(0));
        assertThat(response.getHeaderString("TEST-RESPONSE-HEADER"), is("[test http header] received!!!"));
    }

    /**
     * クエリパラメータが正しく扱えること
     */
    @Test
    @RunAsClient
    public void testWithQueryParameter() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/simple/queryParam?id=999").toURI())
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is("クエリパラメータのID=999"));
    }

    /**
     * パスパラメータが正しく扱えること
     */
    @Test
    @RunAsClient
    public void testWithPathParam() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/simple/pathParam/100").toURI())
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is("パスパラメータのID=100"));
    }

    /**
     * データベースにアクセスし、テーブルよりレコードを取得するケース。
     * (JSON形式)
     */
    @Test
    @RunAsClient
    public void testGetPersonJson() throws Exception {
        testResource.insertPerson("test");

        List<Person> response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/person/json").toURI())
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Person>>() {
                });

        assertThat(response.size(), is(1));
        assertThat(response.get(0).getName(), is("test"));
    }

    /**
     * データベースにアクセスし、テーブルよりレコードを取得するケース。
     * (XML形式)
     */
    @Test
    @RunAsClient
    public void testGetPersonXml() throws Exception {
        testResource.insertPerson("test");

        // XML
        Persons response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/person/xml").toURI())
                .request(MediaType.APPLICATION_XML)
                .get(new GenericType<Persons>() {
                });

        assertThat(response.getPersonList().size(), is(1));
        assertThat(response.getPersonList().get(0).getName(), is("test"));
    }

    /**
     * データベースにアクセスし、テーブルよりレコードを取得するケース。
     * (form-urlencoded形式)
     */
    @Test
    @RunAsClient
    public void testGetPersonForm() throws Exception {
        testResource.insertPerson("test");

        // form-urlencoded
        MultivaluedMap<String, String> response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/person/form").toURI())
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .get(new GenericType<MultivaluedMap<String, String>>() {
                });

        assertThat(response.size(), is(1));
        assertThat(response.getFirst("name"), is("test"));
    }

    /**
     * リクエストのバリデーションを行い、テーブルにレコードを挿入するケース。
     * (JSON形式)
     */
    @Test
    @RunAsClient
    public void testSavePersonJson() throws Exception {

        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/person/json").toURI())
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(new Person(1L, "test"), MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(200));
        assertThat(response.getLength(), is(0));
        assertThat(testResource.findAllPerson().size(), is(1));
    }

    /**
     * リクエストのバリデーションを行い、テーブルにレコードを挿入するケース。
     * (XML形式)
     */
    @Test
    @RunAsClient
    public void testSavePersonXml() throws Exception {

        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/person/xml").toURI())
                .request(MediaType.APPLICATION_XML)
                .post(Entity.entity(new Person(1L, "test"), MediaType.APPLICATION_XML));

        assertThat(response.getStatus(), is(200));
        assertThat(response.getLength(), is(0));
        assertThat(testResource.findAllPerson().size(), is(1));
    }

    /**
     * リクエストのバリデーションを行い、テーブルにレコードを挿入するケース。
     * (form-urlencoded形式)
     */
    @Test
    @RunAsClient
    public void testSavePersonForm() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/person/form").toURI())
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .post(Entity.form(new MultivaluedHashMap<String, String>() {{
                    add("id", "1");
                    add("name", "test");
                }}));

        assertThat(response.getStatus(), is(200));
        assertThat(response.getLength(), is(0));
        assertThat(testResource.findAllPerson().size(), is(1));
    }
    
    public static class P {
        Long id = 1L;
        String name = "012345678901234567890123456789012";

        public void setId(final Long id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * テーブルにレコードを登録する際に、バリデーションエラーが発生するケース。
     * (JSON形式)
     */
    @Test
    @RunAsClient
    public void testSavePersonJson_validationError() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/person/json").toURI())
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(new P(), MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(400));
        assertThat(response.getLength(), is(0));
        assertThat(testResource.findAllPerson().size(), is(0));

        assertLogContains(" status code=[400]");
        assertLogNoError();
    }

    /**
     * テーブルにレコードを登録する際に、バリデーションエラーが発生するケース。
     * (XML形式)
     */
    @Test
    @RunAsClient
    public void testSavePersonXml_validationError() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/person/xml").toURI())
                .request(MediaType.APPLICATION_XML)
                .post(Entity.entity(new Person(1L, "012345678901234567890123456789012"), MediaType.APPLICATION_XML));

        assertThat(response.getStatus(), is(400));
        assertThat(response.getLength(), is(0));
        assertThat(testResource.findAllPerson().size(), is(0));

        assertLogContains(" status code=[400]");
        assertLogNoError();
    }

    /**
     * テーブルにレコードを登録する際に、バリデーションエラーが発生するケース。
     * (form-urlencoded形式)
     */
    @Test
    @RunAsClient
    public void testSavePersonForm_validationError() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/person/form").toURI())
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .post(Entity.form(new MultivaluedHashMap<String, String>() {{
                    add("id", "1");
                    add("name", "012345678901234567890123456789012");
                }}));

        assertThat(response.getStatus(), is(400));
        assertThat(response.getLength(), is(0));
        assertThat(testResource.findAllPerson().size(), is(0));

        assertLogContains(" status code=[400]");
        assertLogNoError();
    }

    /**
     * リソースクラスで{@link nablarch.fw.web.HttpErrorResponse}を送出するケース(ボディは空)
     */
    @Test
    @RunAsClient
    public void testThrowHttpErrorResponseWithoutBody() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/errorEmptyBody").toURI())
                .request()
                .get();

        assertThat("指定したステータスであること", response.getStatus(), is(500));
        assertThat("ボディは空", response.getLength(), is(0));
    }

    /**
     * リソースクラスで{@link nablarch.fw.web.HttpErrorResponse}を送出するケース(ボディあり)
     */
    @Test
    @RunAsClient
    public void testThrowHttpErrorResponseWithBody() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/errorBody").toURI())
                .request()
                .get();

        assertThat("指定したステータスであること", response.getStatus(), is(404));
        Res res = response.readEntity(Res.class);
        assertThat(res.status, is(404));
        assertThat(res.message, is("ないよ"));
    }

    /**
     * リソースクラスが見つからない場合(ルーティングの設定とマッチしない場合)
     */
    @Test
    @RunAsClient
    public void testNotMatchResource() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/notfound/fuga/hoge").toURI())
                .request()
                .get();

        assertThat("404であること", response.getStatus(), is(404));
        assertThat("ボディは空であること", response.getLength(), is(0));

        assertLogContains("GET", "action/notfound/fuga/hoge", " status code=[404]");
    }

    /**
     * リソースクラスが見つからない場合(ルーティングの設定にはあるがクラスがない場合)
     */
    @Test
    @RunAsClient
    public void testNotExistResource() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/error/notexist").toURI())
                .request()
                .get();

        assertThat("404であること", response.getStatus(), is(404));
        assertThat("ボディは空であること", response.getLength(), is(0));

        assertLogContains("GET", "action/error/notexist", " status code=[404]");
    }

    /**
     * ルーティングで":controller"と":action"を使えること。
     */
    @Test
    @RunAsClient
    public void testControllerActionRouting() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/simple/success").toURI())
                .request()
                .get();

        assertThat("200であること", response.getStatus(), is(200));
        assertThat("ボディは空であること", response.getLength(), is(0));
    }

    /**
     * ルーティングで":controller"が見つからない場合、404が返ること。
     */
    @Test
    @RunAsClient
    public void testNotFoundController() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/simple2/success").toURI())
                .request()
                .get();

        assertThat("404であること", response.getStatus(), is(404));
        assertThat("ボディは空であること", response.getLength(), is(0));
    }

    /**
     * ルーティングで":action"が見つからない場合、404が返ること。
     */
    @Test
    @RunAsClient
    public void testNotFoundAction() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/simple/success2").toURI())
                .request()
                .get();

        assertThat("404であること", response.getStatus(), is(404));
        assertThat("ボディは空であること", response.getLength(), is(0));
    }

    /**
     * リソースの要求するメディアタイプとリクエストのメディアタイプが不一致の場合415がかえること。
     */
    @Test
    @RunAsClient
    public void testNotMatchMediaType() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/notMathMediaType").toURI())
                .request()
                .post(Entity.entity("<tag></tag>", MediaType.APPLICATION_XML_TYPE));

        assertThat("415であること", response.getStatus(), is(415));
        assertThat("ボディは空であること", response.getLength(), is(0));

        assertLogContains("unsupported media type requested. "
                , "request method = [POST], "
                , "request uri = [/action/notMathMediaType], "
                , "content type = [" + MediaType.APPLICATION_XML_TYPE + "], "
                , "consumes media type = [" + MediaType.APPLICATION_JSON + ']'
                , "status code=[415]"
        );
    }

    /**
     * 不正なJSONフォーマットを送信した場合
     */
    @Test
    @RunAsClient
    public void testInvalidJsonFormat() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/invalidJson").toURI())
                .request()
                .post(Entity.entity("{", MediaType.APPLICATION_JSON));

        assertThat("400であること", response.getStatus(), is(400));
        assertThat("ボディは空であること", response.getLength(), is(0));

        assertLogContains("failed to read request. cause = ", "Unexpected end-of-input", " status code=[400]");
    }

    /**
     * JSON形式の場合でプロパティに存在しない属性がJSONに含まれている場合はエラーとなること。
     */
    @Test
    @RunAsClient
    public void testSaveJsonWithUnknownProperty() throws Exception {

        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/person/json").toURI())
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("{\"id\":123, \"name\":\"あいう\", \"unknown_property\":true}", MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(400));
        assertThat(response.getLength(), is(0));
        assertThat(testResource.findAllPerson().size(), is(0));

        assertLogContains("failed to read request. cause = ", "Unrecognized field \"unknown_property\"", " status code=[400]");
    }
    /**
     * 不正なXMLフォーマットを送信した場合
     */
    @Test
    @RunAsClient
    public void testInvalidXmlFormat() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/invalidXml").toURI())
                .request()
                .post(Entity.entity("<", MediaType.APPLICATION_XML));

        assertThat("400であること", response.getStatus(), is(400));
        assertThat("ボディは空であること", response.getLength(), is(0));

        assertLogContains("failed to read request. cause = ", " status code=[400]");
    }

    /**
     * 想定外のキーが設定されたform-urlencodedフォーマットを送信した場合でも
     * 例外が発生せずに200(success)になること
     */
    @Test
    @RunAsClient
    public void testInvalidFormFormat() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/invalidForm").toURI())
                .request()
                .post(Entity.entity("<", MediaType.APPLICATION_FORM_URLENCODED));

        assertThat("200であること", response.getStatus(), is(200));
        assertThat("ボディは空であること", response.getLength(), is(0));
    }

    /**
     * JSON形式でボディが空の場合は、不正なボディなので400(bad request)になること
     */
    @Test
    @RunAsClient
    public void testPostEmptyJson() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/simple/empty/json").toURI())
                .request()
                .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus(), is(400));
        assertThat(response.getLength(), is(0));
    }

    /**
     * XML形式でボディが空の場合は、不正なボディなので400(bad request)になること
     */
    @Test
    @RunAsClient
    public void testPostEmptyXml() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/simple/empty/xml").toURI())
                .request()
                .post(Entity.entity("", MediaType.APPLICATION_XML));

        assertThat(response.getStatus(), is(400));
        assertThat(response.getLength(), is(0));
    }

    /**
     * form-urlencoded形式でボディが空の場合は、200(success)になること
     */
    @Test
    @RunAsClient
    public void testPostEmptyForm() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/simple/empty/form").toURI())
                .request()
                .post(Entity.entity("", MediaType.APPLICATION_FORM_URLENCODED));

        assertThat(response.getStatus(), is(200));
        assertThat(response.getLength(), is(0));
    }

    /**
     * リソースクラスで{@link nablarch.core.message.ApplicationException}が送出されたケース
     */
    @Test
    @RunAsClient
    public void testThrowApplicationException() throws Exception {
        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/appError").toURI())
                .request()
                .get();

        assertThat("アプリケーションエラーが発生するので400", response.getStatus(), is(400));
        assertThat("ボディは空", response.getLength(), is(0));

        assertLogContains(" status code=[400]");
        assertLogNoError();

    }

    /**
     * リソースクラスで{@link nablarch.core.message.ApplicationException}以外の例外が送出された場合に
     * メッセージがログ出力されることを確認するケース
     */
    @Test
    @RunAsClient
    public void testExceptionWithMessage() throws Exception {

        Response response = ClientBuilder.newClient()
                .target(new URL(baseUrl, "action/exceptionWithMessage").toURI())
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertThat("500であること", response.getStatus(), is(500));
        assertThat("ボディは空", response.getLength(), is(0));

        assertLogContains("throw test Exception with message.", " status code=[500]");
    }

    /**
     * リクエストに不正なcharsetが設定されていた場合に、400が送出されること確認するケース。
     * <p/>
     * 不正なcharsetを設定するため、クライアントにはArquillianではなくgoogle-http-clientを使用する。
     */
    @Test
    @RunAsClient
    public void testInvalidCharset() throws Exception {

        HttpRequest request = new NetHttpTransport()
                .createRequestFactory()
                .buildGetRequest(new GenericUrl(new URL(baseUrl, "action/simple/empty/form")));

        request.getHeaders().setContentType(MediaType.APPLICATION_FORM_URLENCODED + "; charset=test");
        try {
            request.execute();
            fail("charsetが不正なため、400のレスポンスを送出すること。");
        } catch (HttpResponseException e) {
            assertThat("400であること", e.getStatusCode(), is(400));
            assertThat("ボディは空であること", e.getContent(), is(nullValue()));
            assertLogContains("consumes charset is invalid. charset = [test]", "status code=[400]");
        }
    }

    /**
     * Interceptorが適用できることを確認する
     *
     * @throws Exception
     */
    @Test
    @RunAsClient
    public void testInterceptor() throws Exception {
        String response = ClientBuilder.newClient()
                                       .target(new URL(baseUrl, "action/simple/interceptor").toURI())
                                       .request()
                                       .get(String.class);
        assertThat("Interceptorで編集した値が戻されること", response, is("[OK]"));
    }

    public static class Res {
        public int status;
        public String message;
    }

    /**
     * 出力されたログに指定したメッセージが全て含まれることを検証する。
     * @param expectedMessages 出力されていることを期待するメッセージ
     */
    private void assertLogContains(String... expectedMessages) {
        String actualLogText = bufferStandardOutput.toString(StandardCharsets.UTF_8);

        for (String expectedMessage : expectedMessages) {
            if (!actualLogText.contains(expectedMessage)) {
                throw new AssertionError(Builder.concat(
                        "expected log not found. \n",
                        "expected = ", Arrays.toString(expectedMessages), "\n",
                        "actual   = ", actualLogText)
                );
            }
        }
    }

    /**
     * エラーログが出力されていないことを検証する。
     */
    private void assertLogNoError() {
        // MONITOR ロガーにだけ設定した errout ライターによる出力がないことを確認することで、
        // エラーログが出力されていないことを確認する。
        assertLogNotContains("@ERROR_OUT@");
    }

    /**
     * 出力されたログに指定したメッセージが1つも出力されていないことを検証する。
     * @param unexpectedMessages 出力されていないことを期待するメッセージ
     */
    private void assertLogNotContains(String... unexpectedMessages) {
        String actualLogText = bufferStandardOutput.toString(StandardCharsets.UTF_8);

        for (String unexpectedMessage : unexpectedMessages) {
            if (actualLogText.contains(unexpectedMessage)) {
                throw new AssertionError(Builder.concat(
                        "unexpected log is found. \n",
                        "unexpected = ", Arrays.toString(unexpectedMessages), "\n",
                        "actual   = ", actualLogText)
                );
            }
        }
    }
}
