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

import com.github.ydespreaux.testcontainers.common.jdbc.AbstractJdbcContainer;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.BindMode;
import org.testcontainers.shaded.org.apache.commons.io.FilenameUtils;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.containerLogsConsumer;
import static java.lang.String.format;

/**
 * MySQL container.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
public class MySQLContainer extends AbstractJdbcContainer<MySQLContainer> {

    private static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
    private static final String DRIVER_V8_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    /**
     * Default image name
     */
    private static final String MYSQL_DEFAULT_BASE_URL = "mysql";
    /**
     * Default version
     */
    private static final String MYSQL_DEFAULT_VERSION = "5.7.22";

    private static final String MY_CNF_CONFIG_OVERRIDE_PARAM_NAME = "TC_MY_CNF";
    private static final String JDBC_URL = "jdbc:mysql://%s:%d/%s";
    private static final Integer MYSQL_PORT = 3306;

    private static final String MYSQL_INIT_DIRECTORY = "/docker-entrypoint-initdb.d";

    private String rootPassword = UUID.randomUUID().toString();

    /**
     * Register springboot properties in environment
     */
    private boolean registerSpringbootProperties = true;

    private String driverClassName;

    /**
     *
     */
    private AtomicInteger counterFile = new AtomicInteger(0);

    /**
     *
     */
    public MySQLContainer() {
        this(MYSQL_DEFAULT_BASE_URL, MYSQL_DEFAULT_VERSION);
    }

    /**
     * @param version
     */
    public MySQLContainer(String version) {
        this(MYSQL_DEFAULT_BASE_URL, version);
    }

    /**
     * @param version
     */
    public MySQLContainer(String baseUrl, String version) {
        super(baseUrl + ":" + version);
        this.withUsername("db_user_test");
        this.withPassword(UUID.randomUUID().toString());
        this.withDatabaseName("db_test");
        this.driverClassName = retrieveDriverClassName();
    }

    /**
     * @return
     */
    private String retrieveDriverClassName() {
        try {
            Class.forName(DRIVER_V8_CLASS_NAME);
            return DRIVER_V8_CLASS_NAME;
        } catch (ClassNotFoundException e) {
            return DRIVER_CLASS_NAME;
        }
    }

    /**
     * Get the numbers port for the liveness check.
     *
     * @return
     */
    @Override
    public Set<Integer> getLivenessCheckPortNumbers() {
        return new HashSet(this.getMappedPort(MYSQL_PORT.intValue()).intValue());
    }

    /**
     * Configure the container.
     */
    @Override
    protected void configure() {
        this.withLogConsumer(containerLogsConsumer(log))
                .withExposedPorts(MYSQL_PORT)
                .withEnv("MYSQL_DATABASE", getDatabaseName())
                .withEnv("MYSQL_USER", getUsername())
                .withEnv("MYSQL_PASSWORD", getPassword())
                .withEnv("MYSQL_ROOT_PASSWORD", getRootPassword())
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("testcontainsers-mysql-" + UUID.randomUUID()));
        this.optionallyMapResourceParameterAsVolume(MY_CNF_CONFIG_OVERRIDE_PARAM_NAME, "/etc/mysql/conf.d", "mysql-default-conf");
        this.setStartupAttempts(1);
    }

    /**
     * Get the driver class name.
     *
     * @return
     */
    @Override
    public String getDriverClassName() {
        return this.driverClassName;
    }

    /**
     * Get the jdbc url.
     *
     * @return
     */
    @Override
    public String getJdbcUrl() {
        return format(JDBC_URL, this.getContainerIpAddress(), this.getMappedPort(MYSQL_PORT.intValue()), this.getDatabaseName());
    }

    /**
     * Get the mySQL port.
     *
     * @return
     */
    public Integer getPort() {
        return this.getMappedPort(MYSQL_PORT);
    }

    @Override
    protected String constructUrlForConnection(String queryString) {
        StringBuilder url = new StringBuilder(super.constructUrlForConnection(queryString));
        if (url.indexOf("useSSL=") == -1) {
            url.append(url.indexOf("?") == -1 ? "?" : "&").append("useSSL=false");
        }
        return url.toString();
    }

    /**
     * Get the root password.
     *
     * @return
     */
    public String getRootPassword() {
        return this.rootPassword;
    }

    @Override
    public String getTestQueryString() {
        return "SELECT 1";
    }

    /**
     * Start the container.
     */
    @Override
    public void start() {
        super.start();
        if (registerSpringbootProperties()) {
            registerMySqlEnvironment();
        }
    }

    public MySQLContainer withConfigurationOverride(String s) {
        this.parameters.put(MY_CNF_CONFIG_OVERRIDE_PARAM_NAME, s);
        return this.self();
    }

    /**
     * Set the root password.
     *
     * @param password
     * @return
     */
    public MySQLContainer withRootPassword(String password) {
        this.rootPassword = password;
        return this.self();
    }

    /**
     * Add the sql file script
     *
     * @param sqlInit
     * @return
     */
    public MySQLContainer withSqlScriptFile(String sqlInit) {
        if (sqlInit == null) {
            return this.self();
        }
        MountableFile mountableFile = MountableFile.forClasspathResource(sqlInit);
        Path scriptsDir = Paths.get(mountableFile.getResolvedPath());
        File toFile = scriptsDir.toFile();
        if (!toFile.exists()) {
            throw new IllegalArgumentException(format("Resource with path %s could not be found", scriptsDir.toString()));
        }
        if (toFile.isDirectory()) {
            throw new IllegalArgumentException(format("Resource with path %s must be a file", scriptsDir.toString()));
        }
        // Create the volume that will be need for scripts
        this.addFileSystemBind(mountableFile.getResolvedPath(), MYSQL_INIT_DIRECTORY + "/" + generateFile(scriptsDir), BindMode.READ_ONLY);
        return this.self();
    }

    /**
     * Add the scripts directory.
     *
     * @param directory
     * @return
     */
    public MySQLContainer withSqlScriptDirectory(String directory) {
        if (directory == null) {
            return this.self();
        }
        MountableFile mountableFile = MountableFile.forClasspathResource(directory);
        Path scriptsDir = Paths.get(mountableFile.getResolvedPath());
        File scriptFile = scriptsDir.toFile();
        if (!scriptFile.exists()) {
            throw new IllegalArgumentException(format("Resource with path %s could not be found", scriptsDir.toString()));
        }
        if (scriptFile.isFile()) {
            throw new IllegalArgumentException(format("Resource with path %s must be a directory", scriptsDir.toString()));
        }
        // Add all scripts in cqlScripts attribute
        scanScripts(scriptsDir);
        return this.self();
    }

    /**
     * Scan all files and sub directory
     *
     * @param scriptDirectory
     */
    private void scanScripts(Path scriptDirectory) {
        try (Stream<Path> paths = Files.list(scriptDirectory)) {
            paths
                    .filter(path -> path.toFile().isDirectory() || FilenameUtils.getExtension(path.toFile().getName()).equals("sql"))
                    .sorted()
                    .forEach(path -> {
                        if (path.toFile().isFile()) {
                            MountableFile mountableFile = MountableFile.forHostPath(path);
                            // Create the volume that will be need for scripts
                            this.addFileSystemBind(mountableFile.getResolvedPath(), MYSQL_INIT_DIRECTORY + '/' + generateFile(path), BindMode.READ_ONLY);
                        } else {
                            scanScripts(path);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalArgumentException("Error listing scripts", e);
        }
    }

    /**
     * Generate a file name with counter
     *
     * @param path
     * @return
     */
    private String generateFile(Path path) {
        return counterFile.incrementAndGet() + "_" + path.getFileName();
    }

    /**
     * Register all properties
     */
    protected void registerMySqlEnvironment() {
        System.setProperty(this.getDriverClassSystemProperty(), getDriverClassName());
        System.setProperty(this.getUrlSystemProperty(), constructUrlForConnection(""));
        System.setProperty(this.getUsernameSystemProperty(), this.getUsername());
        System.setProperty(this.getPasswordSystemProperty(), this.getPassword());
        System.setProperty(this.getPlatformSystemProperty(), "mysql");
    }

    /**
     * @param registerProperties
     * @return
     */
    @Override
    public MySQLContainer withRegisterSpringbootProperties(boolean registerProperties) {
        this.registerSpringbootProperties = registerProperties;
        return this.self();
    }

    /**
     * @return
     */
    @Override
    public boolean registerSpringbootProperties() {
        return registerSpringbootProperties;
    }

    /**
     * Get the MySQL url
     *
     * @return
     */
    @Override
    public String getURL() {
        return getJdbcUrl();
    }

    /**
     * Get the local MySQL url.
     *
     * @return
     */
    @Override
    public String getInternalURL() {
        return format(JDBC_URL, this.getNetworkAliases().get(0), MYSQL_PORT, this.getDatabaseName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MySQLContainer)) return false;
        if (!super.equals(o)) return false;
        MySQLContainer that = (MySQLContainer) o;
        return registerSpringbootProperties == that.registerSpringbootProperties &&
                Objects.equals(getRootPassword(), that.getRootPassword()) &&
                Objects.equals(getDriverClassName(), that.getDriverClassName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getRootPassword(), registerSpringbootProperties, getDriverClassName());
    }
}
