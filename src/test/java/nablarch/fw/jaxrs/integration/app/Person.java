package nablarch.fw.jaxrs.integration.app;

import java.io.Serializable;

import nablarch.core.validation.ee.Domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "person")
@XmlRootElement
public class Person implements Serializable {

    private Long id;
    private String name;

    public Person() {
    }

    public Person(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Domain("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
