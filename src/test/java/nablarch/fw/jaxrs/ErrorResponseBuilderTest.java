package nablarch.fw.jaxrs;

import static nablarch.fw.jaxrs.HttpResponseMatcher.*;
import static org.junit.Assert.assertThat;

import nablarch.core.message.ApplicationException;
import nablarch.fw.jaxrs.ErrorResponseBuilder;
import nablarch.fw.web.HttpResponse;

import org.junit.Test;

/**
 * {@link ErrorResponseBuilder}のテストクラス。
 */
public class ErrorResponseBuilderTest {

    private ErrorResponseBuilder sut = new ErrorResponseBuilder();

    /**
     * {@link ApplicationException}の場合、400が生成されること。
     */
    @Test
    public void testApplicationException() throws Exception {
        HttpResponse result = sut.build(null, null, new ApplicationException());
        assertThat("400でBodyが空であること", result, isStatusCode(400).withEmptyBody());
    }

    /**
     * {@link ApplicationException}の場合、500が生成されること。
     */
    @Test
    public void testNotApplicationException() throws Exception {
        HttpResponse result = sut.build(null, null, new IllegalArgumentException());
        assertThat("500でBodyが空であること", result, isStatusCode(500).withEmptyBody());
    }
}

