package nablarch.fw.jaxrs.integration.app;

import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

import nablarch.core.message.ApplicationException;
import nablarch.core.message.Message;
import nablarch.core.message.MessageLevel;
import nablarch.core.message.StringResource;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpResponse;

public class ErrorAction {

    public HttpResponse app() {
        throw new ApplicationException(new Message(MessageLevel.ERROR, new StringResource() {
            @Override
            public String getId() {
                return "error-id";
            }

            @Override
            public String getValue(Locale locale) {
                return "アプリエラー";
            }
        }));
    }

    public void errorWithoutBody() throws Exception {
        throw new HttpErrorResponse(500);
    }

    public void errorWithBody() throws Exception {
        HttpResponse response = new HttpResponse(404);
        response.write("{\"status\": 404, \"message\": \"ないよ\"}");
        response.setContentType(MediaType.APPLICATION_JSON);
        throw new HttpErrorResponse(response);
    }

    @Consumes(MediaType.APPLICATION_JSON)
    public HttpResponse invalidJson(Req req) {
        return new HttpResponse(200);
    }

    @Consumes(MediaType.APPLICATION_XML)
    public HttpResponse invalidXml(Req req) {
        return new HttpResponse(200);
    }

    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse invalidForm(Req req) {
        return new HttpResponse(200);
    }

    @Consumes(MediaType.APPLICATION_JSON)
    public void notMatchMediaType(Req req) {
    }

    public void throwExceptionWithMessage() throws Exception {
        throw new Exception("throw test Exception with message.");
    }

    public static class Req {

    }
}
