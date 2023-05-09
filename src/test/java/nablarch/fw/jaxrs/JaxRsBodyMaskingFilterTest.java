package nablarch.fw.jaxrs;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link JaxRsBodyMaskingFilter}のテスト。
 */
@RunWith(Enclosed.class)
public class JaxRsBodyMaskingFilterTest {

    /**
     * マスク対象が空文字
     */
    public static class EmptyString {

        /**
         * マスク処理しない。
         */
        @Test
        public void testNotMask() {
            JaxRsBodyMaskingFilter sut = new JaxRsBodyMaskingFilter();
            sut.initialize(new AppLogPropertyBuilder().maskingItemNames("id").build());

            String actual = sut.mask("");

            assertThat(actual, is(""));
        }
    }

    /**
     * マスク対象がXML文字列。
     */
    public static class XmlString {

        /**
         * マスク処理しない。
         */
        @Test
        public void testNotMask() {
            JaxRsBodyMaskingFilter sut = new JaxRsBodyMaskingFilter();
            sut.initialize(new AppLogPropertyBuilder().maskingItemNames("id").build());

            String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><id>hoge</id>";
            String actual = sut.mask(content);

            assertThat(actual, is(content));
        }
    }


    /**
     * マスク対象がJSON文字列。
     */
    public static class JsonString {

        private final JaxRsBodyMaskingFilter sut = new JaxRsBodyMaskingFilter();

        /**
         * マスク対象項目名と一致する項目のみマスクする。
         */
        @Test
        public void testNameMatch() {
            sut.initialize(new AppLogPropertyBuilder().maskingChar("x").maskingItemNames("id").build());

            String content = "{\"id\":\"hoge\"}";
            String actual = sut.mask(content);

            assertThat(actual, is("{\"id\":\"xxxxx\"}"));
        }

        /**
         * マスク対象項目名と一致する項目が複数あれば全てマスクする。
         */
        @Test
        public void testMultipleNameMatch() {
            sut.initialize(new AppLogPropertyBuilder().maskingChar("x").maskingItemNames("id").build());

            String content = "{\"id\":\"hoge\",\"obj\":{\"id\":\"hoge\"}}";
            String actual = sut.mask(content);

            assertThat(actual, is("{\"id\":\"xxxxx\",\"obj\":{\"id\":\"xxxxx\"}}"));
        }

        /**
         * マスク対象項目名が未定義であればマスクしない。
         */
        @Test
        public void testMakingItemNameIfUndefined() {
            sut.initialize(new AppLogPropertyBuilder().build());

            String content = "{\"id\":\"hoge\"}";
            String actual = sut.mask(content);

            assertThat(actual, is(content));
        }

        /**
         * マスク対象項目名が空定義であればマスクしない。
         */
        @Test
        public void testMakingItemNameIfEmpty() {
            sut.initialize(new AppLogPropertyBuilder().maskingItemNames("").build());

            String content = "{\"id\":\"hoge\"}";
            String actual = sut.mask(content);

            assertThat(actual, is(content));
        }

        /**
         * マスク対象項目名が区切り文字だけであればマスクしない。
         */
        @Test
        public void testMakingItemNameIfSeparatorOnly() {
            sut.initialize(new AppLogPropertyBuilder().maskingItemNames(",,").build());

            String content = "{\",,\":\"hoge\"}";
            String actual = sut.mask(content);

            assertThat(actual, is(content));
        }

        /**
         * マスク文字が未定義であればデフォルトのマスク文字でマスクする。
         */
        @Test
        public void testMakingCharIfUndefined() {
            sut.initialize(new AppLogPropertyBuilder().maskingItemNames("id").build());

            String content = "{\"id\":\"hoge\"}";
            String actual = sut.mask(content);

            assertThat(actual, is("{\"id\":\"*****\"}"));
        }

        /**
         * マスク文字が空定義であればデフォルトのマスク文字でマスクする。
         */
        @Test
        public void testMakingCharIfEmpty() {
            sut.initialize(new AppLogPropertyBuilder().maskingItemNames("id").build());

            String content = "{\"id\":\"hoge\"}";
            String actual = sut.mask(content);

            assertThat(actual, is("{\"id\":\"*****\"}"));
        }

        /**
         * マスク文字が1文字でなければエラーになる。
         */
        @Test(expected = IllegalArgumentException.class)
        public void testMaskingCharInvalidLength() {
            sut.initialize(new AppLogPropertyBuilder().maskingChar("xx").build());
        }

        /**
         * マスク対象項目名と前方一致する項目はマスクしない。
         */
        @Test
        public void testStartWithMatch() {
            sut.initialize(new AppLogPropertyBuilder().maskingChar("x").maskingItemNames("id").build());

            String content = "{\"idx\":\"hoge\"}";
            String actual = sut.mask(content);

            assertThat(actual, is(content));
        }

        /**
         * マスク対象項目名と後方一致する項目はマスクしない。
         */
        @Test
        public void testEndWithMatch() {
            sut.initialize(new AppLogPropertyBuilder().maskingChar("x").maskingItemNames("id").build());

            String content = "{\"xid\":\"hoge\"}";
            String actual = sut.mask(content);

            assertThat(actual, is(content));
        }

        /**
         * マスク対象項目名と部分一致する項目はマスクしない。
         */
        @Test
        public void testPartialMatch() {
            sut.initialize(new AppLogPropertyBuilder().maskingChar("x").maskingItemNames("id").build());

            String content = "{\"xidx\":\"hoge\"}";
            String actual = sut.mask(content);

            assertThat(actual, is(content));
        }

        /**
         * マスク対象項目の型が文字列であればマスクする。
         */
        @Test
        public void testTypeString() {
            sut.initialize(new AppLogPropertyBuilder().maskingChar("x").maskingItemNames("id").build());

            String escaped = "\\\"" + "\\\\" + "\\/" + "\\b" + "\\r\\n" + "\\t";
            String content = "[{\"id\":\"azAZてすテスﾃｽ検証\"},{\"id\":\"\"},{\"id\":\"" + escaped + "\"}]";
            String actual = sut.mask(content);

            assertThat(actual, is("[{\"id\":\"xxxxx\"},{\"id\":\"xxxxx\"},{\"id\":\"xxxxx\"}]"));
        }

        /**
         * マスク対象項目の型が数値であればマスクする。
         */
        @Test
        public void testTypeNumber() {
            sut.initialize(new AppLogPropertyBuilder().maskingChar("x").maskingItemNames("id").build());

            String content = "[{\"id\":1},{\"id\":-1},{\"id\":1.125e+1}]";
            String actual = sut.mask(content);

            assertThat(actual, is("[{\"id\":xxxxx},{\"id\":xxxxx},{\"id\":xxxxx}]"));
        }

        /**
         * マスク対象項目の型が真偽値であればマスクする。
         */
        @Test
        public void testTypeBoolean() {
            sut.initialize(new AppLogPropertyBuilder().maskingChar("x").maskingItemNames("id").build());

            String content = "[{\"id\":true},{\"id\":false}]";
            String actual = sut.mask(content);

            assertThat(actual, is("[{\"id\":xxxxx},{\"id\":xxxxx}]"));
        }

        /**
         * マスク対象項目の型がnullであればマスクしない。
         */
        @Test
        public void testTypeNull() {
            sut.initialize(new AppLogPropertyBuilder().maskingChar("x").maskingItemNames("id").build());

            String content = "{\"id\":null}";
            String actual = sut.mask(content);

            assertThat(actual, is(content));
        }

        /**
         * マスク対象項目の型が配列であればマスクしない。
         */
        @Test
        public void testTypeArray() {
            sut.initialize(new AppLogPropertyBuilder().maskingChar("x").maskingItemNames("id").build());

            String content = "{\"id\":[\"hoge\",\"fuga\"]}";
            String actual = sut.mask(content);

            assertThat(actual, is(content));
        }

        /**
         * マスク対象項目の型がオブジェクトであればマスクしない。
         */
        @Test
        public void testTypeObject() {
            sut.initialize(new AppLogPropertyBuilder().maskingChar("x").maskingItemNames("id").build());

            String content = "{\"id\":{\"name\":\"hoge\"}}";
            String actual = sut.mask(content);

            assertThat(actual, is(content));
        }
    }
}
