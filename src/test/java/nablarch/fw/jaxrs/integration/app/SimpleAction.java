package nablarch.fw.jaxrs.integration.app;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;

import nablarch.core.beans.BeanUtil;
import nablarch.core.validation.ee.Required;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Interceptor;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;

public class SimpleAction {

    @GET
    public HttpResponse success() {
        return new HttpResponse();
    }

    public HttpResponse httpResponseWithBody() {
        return new HttpResponse().write("httpResponseWithBody invoked!!!");
    }

    public HttpResponse httpHeader(HttpRequest request) {
        String testRequestHeader = request.getHeader("TEST-REQUEST-HEADER");
        HttpResponse response = new HttpResponse();
        response.setHeader("TEST-RESPONSE-HEADER", "[" + testRequestHeader + "] received!!!");
        return response;
    }

    public HttpResponse pathParam(HttpRequest request) throws Exception {
        Form form = BeanUtil.createAndCopy(Form.class, request.getParamMap());
        if (form.getId() != 100) {
            throw new IllegalArgumentException("パスパラメータのIDは100じゃないとおかしいです");
        }
        HttpResponse response = new HttpResponse(200);
        response.setContentType(MediaType.TEXT_PLAIN);
        response.write("パスパラメータのID=" + form.getId());
        return response;
    }

    public HttpResponse queryParam(HttpRequest request) throws Exception {
        System.out.println("SimpleAction.queryParam");
        Form form = BeanUtil.createAndCopy(Form.class, request.getParamMap());
        if (form.getId() != 999) {
            throw new IllegalArgumentException("パスパラメータのIDは999じゃないとおかしいです");
        }
        HttpResponse response = new HttpResponse(200);
        response.setContentType(MediaType.TEXT_PLAIN);
        response.write("クエリパラメータのID=" + form.getId());
        return response;
    }

    public void notContent() throws Exception {
    }

    @Consumes(MediaType.APPLICATION_JSON)
    public HttpResponse emptyJson(Form form) {
        return new HttpResponse(200);
    }

    @Consumes(MediaType.APPLICATION_XML)
    public HttpResponse emptyXml(Form form) {
        return new HttpResponse(200);
    }

    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse emptyForm(Form form) {
        return new HttpResponse(200);
    }

    public static class Form implements Serializable {
        private Long id;

        public Long getId() {
            return id;
        }

        @Required
        public void setId(Long id) {
            this.id = id;
        }
    }
    
    @Interceptor1
    @Interceptor2
    public HttpResponse interceptor() throws Exception {
        final HttpResponse response = new HttpResponse();
        response.write("OK");
        return response;
    }

    @Interceptor(Interceptor1.Impl.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Interceptor1 {
        
        class Impl extends Interceptor.Impl<Object, HttpResponse, Interceptor1> {
            @Override
            public HttpResponse handle(final Object o, final ExecutionContext context) {
                final HttpResponse response = getOriginalHandler().handle(o, context);
                final HttpResponse result = new HttpResponse();
                result.write('[' + response.getBodyString());
                return result;
            }
        }
    }
    
    @Interceptor(Interceptor2.Impl.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Interceptor2 {

        class Impl extends Interceptor.Impl<Object, HttpResponse, Interceptor2> {
            @Override
            public HttpResponse handle(final Object o, final ExecutionContext context) {
                final HttpResponse response = getOriginalHandler().handle(o, context);
                final HttpResponse result = new HttpResponse();
                result.write(response.getBodyString() + ']');
                return result;
            }
        }
    }
}
