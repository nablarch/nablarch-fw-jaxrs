package nablarch.fw.jaxrs.integration.app;

import nablarch.core.db.connection.ConnectionFactory;
import nablarch.core.db.connection.TransactionManagerConnection;
import nablarch.core.db.statement.ResultSetIterator;
import nablarch.core.db.statement.SqlPStatement;
import nablarch.core.db.statement.SqlRow;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.ComponentDefinitionLoader;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import org.junit.rules.ExternalResource;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class IntegrationTestResource extends ExternalResource {

    private TransactionManagerConnection connection;

    public void truncatePersonTable() {
        SqlPStatement statement = connection.prepareStatement("truncate table person");
        statement.execute();
        statement.close();
        connection.commit();
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        loadRepository();
        connection = getConnection();
        createTestTable();
    }

    private void createTestTable() {
        System.out.println("create person table");
        SqlPStatement statement = connection.prepareStatement(
                "create table  if not exists  person (id IDENTITY , name varchar(100) not null, PRIMARY KEY (id))");
        statement.execute();
        statement.close();
        connection.commit();
    }


    private void loadRepository() {
        ComponentDefinitionLoader loader = new XmlComponentDefinitionLoader("integration/db.xml");
        SystemRepository.clear();
        SystemRepository.load(new DiContainer(loader));
    }

    private TransactionManagerConnection getConnection() {
        ConnectionFactory connectionFactory = SystemRepository.get("connectionFactory");
        return connectionFactory.getConnection("test-tran");
    }

    @Override
    protected void after() {
        super.after();
        closeConnection();
        SystemRepository.clear();
    }

    private void closeConnection() {
        try {
            connection.terminate();
        } catch (Throwable ignore) {
            ignore.printStackTrace();
        }
    }

    public Long insertPerson(String name) throws Exception {
        SqlPStatement statement = connection.prepareStatement("insert into person (name) values (?)", new int[] {1});
        statement.setString(1, name);
        statement.executeUpdate();
        ResultSet keys = statement.getGeneratedKeys();
        Long id;
        if (keys.next()) {
            id = keys.getLong(1);
        } else {
            throw new RuntimeException("error");
        }
        statement.close();
        connection.commit();
        return id;
    }

    public Person findPerson(Long id) {
        SqlPStatement statement = connection.prepareStatement("select name from person where id = ?");
        statement.setLong(1, id);
        ResultSetIterator rows = statement.executeQuery();
        if (rows.next()) {
            return new Person(id, rows.getString(1));
        } else {
            return null;
        }
    }

    public List<Person> findAllPerson() {
        SqlPStatement statement = connection.prepareStatement("select id, name from person order by id");
        ResultSetIterator rows = statement.executeQuery();
        ArrayList<Person> persons = new ArrayList<Person>();
        for (SqlRow row : rows) {
            persons.add(new Person(row.getLong("id"), row.getString("name")));
        }
        return persons;
    }
}

