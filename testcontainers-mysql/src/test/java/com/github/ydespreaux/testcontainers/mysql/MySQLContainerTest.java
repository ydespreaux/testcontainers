/*
 * Copyright (C) 2018 Yoann Despréaux
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file COPYING . If not, write to the
 * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * Please send bugreports with examples or suggestions to yoann.despreaux@believeit.fr
 */

package com.github.ydespreaux.testcontainers.mysql;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Tag("integration")
@Testcontainers
public class MySQLContainerTest {

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    @AfterEach
    public void onTeardown() throws SQLException {
        if (resultSet != null) {
            resultSet.close();
        }
        if (statement != null) {
            statement.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    @Container
    public static MySQLContainer mySqlContainer = new MySQLContainer()
            .withDatabaseName("my_database")
            .withUsername("db_user")
            .withPassword("changeme")
            .withRootPassword("rootpwd")
            .withConfigurationOverride("mysql-test-conf")
            .withDriverClassSystemProperty("jdbc.driver")
            .withUrlSystemProperty("jdbc.url")
            .withUsernameSystemProperty("jdbc.username")
            .withPasswordSystemProperty("jdbc.password")
            .withPlatformSystemProperty("jdbc.platform")
            .withSqlScriptFile("mysql-init/user-schema-init.sql")
            .withSqlScriptFile("mysql-init/workstation-schema-init.sql")
            .withSqlScriptDirectory("mysql-init/data")
            .withStartupTimeoutSeconds(180);

    @Test
    void environmentSystemProperty() {
        assertThat(System.getProperty(mySqlContainer.getDriverClassSystemProperty()), is(equalTo(mySqlContainer.getDriverClassName())));
        assertThat(System.getProperty(mySqlContainer.getUsernameSystemProperty()), is(equalTo("db_user")));
        assertThat(System.getProperty(mySqlContainer.getPasswordSystemProperty()), is(equalTo("changeme")));
        assertThat(System.getProperty(mySqlContainer.getPlatformSystemProperty()), is(equalTo("mysql")));
        assertThat(System.getProperty(mySqlContainer.getUrlSystemProperty()), is(equalTo("jdbc:mysql://" + mySqlContainer.getContainerIpAddress() + ":" + mySqlContainer.getPort() + "/my_database?useSSL=false")));
    }

    @Test
    void getURL() {
        String url = format("jdbc:mysql://%s:%d/%s", mySqlContainer.getContainerIpAddress(), mySqlContainer.getPort(), mySqlContainer.getDatabaseName());
        assertThat(mySqlContainer.getURL(), is(equalTo(url)));
    }

    @Test
    void getInternalURL() {
        String url = format("jdbc:mysql://%s:%d/%s", mySqlContainer.getNetworkAliases().get(0), 3306, mySqlContainer.getDatabaseName());
        assertThat(mySqlContainer.getInternalURL(), is(equalTo(url)));
    }

    @Test
    void userSchema() throws SQLException {
        connection = createConnection();
        DatabaseMetaData metadata = connection.getMetaData();
        resultSet = metadata.getColumns("my_database", null, "tb_user", "%");
        List<String> columns = new ArrayList<>();
        while (resultSet.next()) {
            columns.add(resultSet.getString(4));
        }
        assertThat(columns, containsInAnyOrder("id", "idRh", "first_name", "last_name", "last_modified"));
    }

    @Test
    void userData() throws SQLException {
        connection = createConnection();
        statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT * FROM tb_user");
        assertThat(resultSet.next(), is(true));
        assertThat(resultSet.getInt("id"), is(equalTo(1)));
        assertThat(resultSet.getString("idRh"), is(equalTo("XPAX624")));
        assertThat(resultSet.getString("first_name"), is(equalTo("Jean")));
        assertThat(resultSet.getString("last_name"), is(equalTo("Dupond")));
        assertThat(resultSet.getDate("last_modified"), is(notNullValue()));
    }

    @Test
    void workstationSchema() throws SQLException {
        connection = createConnection();
        DatabaseMetaData metadata = connection.getMetaData();
        resultSet = metadata.getColumns("my_database", null, "tb_workstation", "%");
        List<String> columns = new ArrayList<>();
        while (resultSet.next()) {
            columns.add(resultSet.getString(4));
        }
        assertThat(columns, containsInAnyOrder("id", "name", "serial_number"));
    }

    @Test
    void workstationData() throws SQLException {
        connection = createConnection();
        statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT * FROM tb_workstation");
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
