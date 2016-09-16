package nablarch.fw.jaxrs;

import nablarch.core.util.StringUtil;
import nablarch.fw.web.HttpResponse;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * {@link HttpResponse}をアサートする{@link org.hamcrest.Matcher}
 */
public class HttpResponseMatcher extends TypeSafeMatcher<HttpResponse> {

    private final int statusCode;
    private boolean emptyBody;
    private String body;

    private HttpResponseMatcher(int statusCode) {
        this.statusCode = statusCode;
    }

    public HttpResponseMatcher withEmptyBody() {
        emptyBody = true;
        return this;
    }

    public HttpResponseMatcher withBody(String body) {
        this.body = body;
        return this;
    }

    /**
     * 指定されたステータスコードであることをアサートする{@link HttpResponseMatcher}を生成する。
     * <p/>
     * また、ボディ部が空であることもアサートする。
     *
     * @param statusCode ステータスコード
     * @return {@code HttpResponseMatcher}
     */
    public static HttpResponseMatcher isStatusCode(int statusCode) {
        return new HttpResponseMatcher(statusCode);
    }

    @Override
    protected boolean matchesSafely(HttpResponse item) {
        if (item.getStatusCode() != statusCode) {
            return false;
        }
        if (emptyBody && !item.getBodyString().isEmpty()) {
            return false;
        }
        if (StringUtil.hasValue(body) && !body.equals(item.getBodyString())) {
            return false;
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("statusCode: ")
                .appendValue(statusCode);
        if (emptyBody) {
            description.appendText(", body-length: ")
                    .appendValue(0);
        }

        if (StringUtil.hasValue(body)) {
            description.appendText(", body: ")
                    .appendValue(body);
        }
    }

    @Override
    protected void describeMismatchSafely(HttpResponse item, Description mismatchDescription) {
        mismatchDescription.appendText("was ")
                .appendValue("statusCode: ")
                .appendValue(item.getStatusCode());
        if (emptyBody) {
            mismatchDescription.appendText(", body-length: ")
                    .appendValue(item.getBodyString().length());
        }
        if (StringUtil.hasValue(body)) {
            mismatchDescription.appendText(", body: ")
                    .appendValue(item.getBodyString());
        }
    }

}
