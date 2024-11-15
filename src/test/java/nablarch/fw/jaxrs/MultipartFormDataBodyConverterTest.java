package nablarch.fw.jaxrs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MultipartFormDataBodyConverterTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testMediaType() {
        MultipartFormDataBodyConverter sut = new MultipartFormDataBodyConverter();

        // multipart/form-dataで始まるメディアタイプのみtrueとなる
        assertThat(sut.isConvertible("multipart/form-data"), is(true));
        assertThat(sut.isConvertible("MULTIPART/FORM-DATA"), is(true));

        assertThat(sut.isConvertible("multipart"), is(false));
        assertThat(sut.isConvertible("application/json"), is(false));
        assertThat(sut.isConvertible("text/plain"), is(false));
    }

    @Test
    public void testDeserialize() {
        MultipartFormDataBodyConverter sut = new MultipartFormDataBodyConverter();

        // readはなにもせずnullを返す
        assertThat(sut.read(null, null), nullValue());
    }

    @Test
    public void testSerialize() {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("multipart/form-data is not supported in response.");

        MultipartFormDataBodyConverter sut = new MultipartFormDataBodyConverter();

        // writeは例外をスローする
        sut.write(null, null);
    }
}