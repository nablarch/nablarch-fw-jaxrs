package nablarch.fw.jaxrs.integration.app;

import nablarch.common.dao.UniversalDao;
import nablarch.fw.web.HttpResponse;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.List;

public class PersonAction {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> findJson() {
        return UniversalDao.findAll(Person.class);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Valid
    public HttpResponse saveJson(Person person) {
        UniversalDao.insert(person);
        return new HttpResponse();
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Persons findXml() {
        Persons persons = new Persons();
        persons.setPersonList(UniversalDao.findAll(Person.class));
        return persons;
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Valid
    public HttpResponse saveXml(Person person) {
        UniversalDao.insert(person);
        return new HttpResponse();
    }

    @GET
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    public MultivaluedMap<String, String> findFormUrlencoded() {
        MultivaluedMap<String, String> map = new MultivaluedHashMap<String, String>();
        for (Person person : UniversalDao.findAll(Person.class)) {
            map.add("name", person.getName());
        }
        return map;

    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Valid
    public HttpResponse saveFormUrlencoded(Person person) {
        UniversalDao.insert(person);
        return new HttpResponse();
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Valid
    public HttpResponse saveXmlInvalidSignature(Person person, List<String> list) {
        UniversalDao.insert(person);
        return new HttpResponse();
    }
}
