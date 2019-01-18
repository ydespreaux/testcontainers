package com.github.ydespreaux.testcontainers.mysql;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
public class ITMySQLContainerWithInitSchemaFileTest {

    @ClassRule
    public static MySQLContainer mySqlContainer = new MySQLContainer()
            .withDatabaseName("an_springboot_aa")
            .withSqlScriptFile("mysql-init/user-schema-init.sql")
            .withSqlScriptFile("mysql-init/workstation-schema-init.sql");

    private Connection connection;
    private Statement statement;

    @Before
    public void onSetup() throws SQLException {
        connection = createConnection();
    }

    @After
    public void onTeardown() throws SQLException {
        if (statement != null) {
            statement.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void checkUserDbSchema() throws SQLException {
        statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM tb_user");
        assertThat(resultSet.next(), is(true));
        assertThat(resultSet.getInt("id"), is(equalTo(1)));
        assertThat(resultSet.getString("idRh"), is(equalTo("XPAX624")));
        assertThat(resultSet.getString("first_name"), is(equalTo("Jean")));
        assertThat(resultSet.getString("last_name"), is(equalTo("Dupond")));
        assertThat(resultSet.getDate("last_modified"), is(notNullValue()));
    }

    @Test
    public void checkWorkstationDbSchema() throws SQLException {
        statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM tb_workstation");
        assertThat(resultSet.next(), is(true));
        assertThat(resultSet.getInt("id"), is(equalTo(1)));
        assertThat(resultSet.getString("name"), is(equalTo("WS10002")));
        assertThat(resultSet.getString("serial_number"), is(equalTo("WS-1234-5678")));
    }


    /**
     * @return
     * @throws SQLException
     */
    private Connection createConnection() throws SQLException {
        Properties info = new Properties();
        info.put("user", mySqlContainer.getUsername());
        info.put("password", mySqlContainer.getPassword());
        String url = mySqlContainer.constructUrlForConnection("");
        Driver jdbcDriverInstance = mySqlContainer.getJdbcDriverInstance();
        return jdbcDriverInstance.connect(url, info);
    }

}
