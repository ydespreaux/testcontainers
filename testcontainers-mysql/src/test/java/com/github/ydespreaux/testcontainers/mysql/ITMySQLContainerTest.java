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
            .withPlatformSystemProperty("jdbc.platform");

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
