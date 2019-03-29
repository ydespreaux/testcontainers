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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Tag("integration")
@Testcontainers
public class MySQLContainerWithInitSchemaDirectoryTest {

    @Container
    public static MySQLContainer mySqlContainer = new MySQLContainer()
            .withDatabaseName("my_database")
            .withSqlScriptDirectory("mysql-directory/schema")
            .withSqlScriptDirectory("mysql-directory/data")
            .withStartupTimeoutSeconds(180);

    private Connection connection;
    private Statement statement;

    @BeforeEach
    public void onSetup() throws SQLException {
        connection = createConnection();
    }

    @AfterEach
    public void onTeardown() throws SQLException {
        if (statement != null) {
            statement.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void checkDbSchema() throws SQLException {
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
    void checkWorkstationDbSchema() throws SQLException {
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
