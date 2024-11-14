package nablarch.fw.jaxrs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

public class MultipartFormDataBodyConverterTest {
    @Test
    public void testMediaType() {
        MultipartFormDataBodyConverter sut = new MultipartFormDataBodyConverter();

        // multipart/form-dataで始まるメディアタイプのみtrueとなる
        assertThat(sut.isConvertible("multipart/form-data"), is(true));

        assertThat(sut.isConvertible("multipart"), is(false));
        assertThat(sut.isConvertible("application/json"), is(false));
        assertThat(sut.isConvertible("text/plain"), is(false));
    }

    @Test
    public void testSerializeDeserialize() {
        MultipartFormDataBodyConverter sut = new MultipartFormDataBodyConverter();

        // read,writeともになにもせずnullを返す
        assertThat(sut.read(null, null), nullValue());
        assertThat(sut.write(null, null), nullValue());
    }
}