package nablarch.fw.jaxrs.integration.app;

import nablarch.core.validation.ee.Length;

public class BeanDomain {
    @Length(min = 0, max = 32, message = "name length error.")
    public String name;
}
