package com.github.ydespreaux.testcontainers.common;

import com.github.ydespreaux.testcontainers.common.checks.AbstractCommandWaitStrategy;
import com.github.ydespreaux.testcontainers.common.cmd.AbstractCommand;
import com.github.ydespreaux.testcontainers.common.cmd.Command;
import com.github.ydespreaux.testcontainers.common.cmd.CommandExecutionException;
import com.github.ydespreaux.testcontainers.common.jdbc.AbstractJdbcContainer;
import com.github.ydespreaux.testcontainers.common.utils.ContainerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Tag("integration")
@Testcontainers
public class CommonTest {

    @Container
    static MySQLCustomContainer container = new MySQLCustomContainer()
            .withDatabaseName("db_test")
            .withUsername("junit")
            .withPassword("secret");

    static class MySQLCustomContainer extends AbstractJdbcContainer<MySQLCustomContainer> {

        private static final String JDBC_URL = "jdbc:mysql://%s:%d/%s";

        public MySQLCustomContainer() {
            super("mysql:5.7.22");
            this.waitingFor(new AbstractCommandWaitStrategy<>(this) {
                /**
                 * Returns the schell command that must be executed.
                 *
                 * @return
                 */
                @Override
                public List<Command> getCheckCommands() {
                    return List.of(new MySQLVersionCmd());
                }
            });
        }

        @Override
        protected void configure() {
            this.withExposedPorts(3306)
                    .withEnv("MYSQL_DATABASE", getDatabaseName())
                    .withEnv("MYSQL_USER", getUsername())
                    .withEnv("MYSQL_PASSWORD", getPassword())
                    .withEnv("MYSQL_ROOT_PASSWORD", getPassword());
        }

        /**
         * @param registerProperties
         * @return
         */
        @Override
        public MySQLCustomContainer withRegisterSpringbootProperties(boolean registerProperties) {
            return this;
        }

        /**
         * @return
         */
        @Override
        public boolean registerSpringbootProperties() {
            return false;
        }

        /**
         * @return
         */
        @Override
        public String getURL() {
            return getJdbcUrl();
        }

        /**
         * @return
         */
        @Override
        public String getInternalURL() {
            return null;
        }

        @Override
        public String getDriverClassName() {
            return "com.mysql.jdbc.Driver";
        }

        @Override
        public String getJdbcUrl() {
            return format(JDBC_URL, this.getContainerIpAddress(), this.getMappedPort(3306), getDatabaseName());
        }

        @Override
        protected String constructUrlForConnection(String queryString) {
            return getJdbcUrl() + "?useSSL=false";
        }

        @Override
        protected String getTestQueryString() {
            return "select 1";
        }
    }

    static class MySQLVersionCmd extends AbstractCommand<JdbcDatabaseContainer> {

        @Override
        protected List<String> buildParameters(JdbcDatabaseContainer container) {
            return List.of("mysql", "--version");
        }
    }

    static class MySQLLoginCmd extends AbstractCommand<JdbcDatabaseContainer> {

        private final String user;
        private final String password;

        MySQLLoginCmd(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        protected List<String> buildParameters(JdbcDatabaseContainer container) {
            return List.of("mysql", "-u" + user, "-p" + password);
        }
    }

    @Nested
    class ContainerUtilsTest {

        @Test
        void getAvailableMappingPort() {
            assertThat(ContainerUtils.getAvailableMappingPort(), is(greaterThan(0)));
        }

        @Test
        void getContainerHostname() {
            var containerName = ContainerUtils.getContainerHostname(container);
            assertThat(containerName, is(notNullValue()));
        }

        @Test
        void execCmd() {
            var result = ContainerUtils.execCmd(container.getDockerClient(), container.getContainerId(), new String[]{"mysql", "--version"});
            Assertions.assertAll(
                    () -> assertThat(result.getExitCode(), is(equalTo(0))),
                    () -> assertThat(result.getOutput(), containsString("Distrib 5.7.22"))
            );
        }

        @Test
        void execCmdAccessDenied() {
            var result = ContainerUtils.execCmd(container.getDockerClient(), container.getContainerId(), new String[]{"mysql"});
            Assertions.assertAll(
                    () -> assertThat(result.getExitCode(), is(equalTo(1))),
                    () -> assertThat(result.getOutput(), containsString(" Access denied for user"))
            );
        }
    }

    @Nested
    class CommandTest {

        @Test
        void executeCommandSuccess() {
            Command<JdbcDatabaseContainer> command = new MySQLVersionCmd();
            assertThat(command.execute(container).getOutput(), containsString("Distrib 5.7.22"));
        }

        @Test
        void executeCommandFailed() {
            Command<JdbcDatabaseContainer> command = new MySQLLoginCmd(container.getUsername(), "wrong_password");
            assertThrows(CommandExecutionException.class, () -> command.execute(container));
        }
    }
}
