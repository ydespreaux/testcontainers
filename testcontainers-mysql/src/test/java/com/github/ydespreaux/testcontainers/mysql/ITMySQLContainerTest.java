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

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
public class ITMySQLContainerTest {

    @ClassRule
    public static MySQLContainer mySqlContainer = new MySQLContainer()
            .withDatabaseName("an_database")
            .withUsername("db_user")
            .withPassword("changeme")
            .withRootPassword("rootpwd")
            .withConfigurationOverride("mysql-test-conf")
            .withDriverClassSystemProperty("jdbc.driver")
            .withUrlSystemProperty("jdbc.url")
            .withUsernameSystemProperty("jdbc.username")
            .withPasswordSystemProperty("jdbc.password")
            .withPlatformSystemProperty("jdbc.platform")
            .withStartupTimeoutSeconds(180);

    @Test
    public void environmentSystemProperty() {
        assertThat(System.getProperty(mySqlContainer.getDriverClassSystemProperty()), is(equalTo(mySqlContainer.getDriverClassName())));
        assertThat(System.getProperty(mySqlContainer.getUsernameSystemProperty()), is(equalTo("db_user")));
        assertThat(System.getProperty(mySqlContainer.getPasswordSystemProperty()), is(equalTo("changeme")));
        assertThat(System.getProperty(mySqlContainer.getPlatformSystemProperty()), is(equalTo("mysql")));
        assertThat(System.getProperty(mySqlContainer.getUrlSystemProperty()), is(equalTo("jdbc:mysql://" + mySqlContainer.getContainerIpAddress() + ":" + mySqlContainer.getPort() + "/an_database?useSSL=false")));
    }

    @Test
    public void getURL() {
        String url = format("jdbc:mysql://%s:%d/%s", mySqlContainer.getContainerIpAddress(), mySqlContainer.getPort(), mySqlContainer.getDatabaseName());
        assertThat(mySqlContainer.getURL(), is(equalTo(url)));
    }

    @Test
    public void getInternalURL() {
        String url = format("jdbc:mysql://%s:%d/%s", mySqlContainer.getNetworkAliases().get(0), 3306, mySqlContainer.getDatabaseName());
        assertThat(mySqlContainer.getInternalURL(), is(equalTo(url)));
    }

}
