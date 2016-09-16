package nablarch.fw.jaxrs.integration.app;

import nablarch.common.dao.UniversalDao;
import nablarch.fw.web.HttpResponse;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

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
