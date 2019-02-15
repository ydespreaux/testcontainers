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

package com.github.ydespreaux.testcontainers.cassandra;

import com.github.ydespreaux.testcontainers.common.IContainer;
import com.github.ydespreaux.testcontainers.common.checks.AbstractCommandWaitStrategy;
import com.github.ydespreaux.testcontainers.common.utils.ContainerUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.org.apache.commons.io.FilenameUtils;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.containerLogsConsumer;
import static java.lang.String.format;

/**
 * The Cassandra container.
 *
 */
@Slf4j
public class CassandraContainer extends GenericContainer<CassandraContainer> implements IContainer<CassandraContainer> {

    private static final String CASSANDRA_DEFAULT_VERSION = "3.11";
    private static final String CASSANDRA_DEFAULT_BASE_URL = "cassandra";
    private static final int CASSANDRA_DEFAULT_PORT = 9042;
    private static final int STARTER_TIMOUT_SECONDS = 120;

    private static final String DB_SCHEMA_DIRECTORY = "/tmp/cassandra-init";
    /**
     *
     */
    @Getter
    private final List<String> cqlScripts = new ArrayList<>();

    /**
     * Register springboot properties in environment
     */
    private boolean registerSpringbootProperties = true;

    /**
     * Contact points for spring boot properties.
     */
    @Getter
    private String contactPointsSystemProperty = "spring.data.cassandra.contact-points";
    @Getter
    /**
     * Cassandra port for spring boot properties.
     */
    private String cassandraPortSystemProperty = "spring.data.cassandra.port";

    /**
     * Default constructor.
     * By default, image docker for cassandra is cassandra:3.11
     */
    public CassandraContainer() {
        this(CASSANDRA_DEFAULT_BASE_URL, CASSANDRA_DEFAULT_VERSION);
    }

    /**
     * Create a cassandra container with a specific version
     *
     * @param version the version of the image
     */
    public CassandraContainer(String version) {
        this(CASSANDRA_DEFAULT_BASE_URL, version);
    }

    /**
     * @param baseUrl url for the cassandra image
     * @param version the version of the image
     */
    public CassandraContainer(String baseUrl, String version) {
        super(baseUrl + ":" + version);
    }

    /**
     * Configure the container.
     */
    @Override
    protected void configure() {
        this.withLogConsumer(containerLogsConsumer(log))
                .withExposedPorts(CASSANDRA_DEFAULT_PORT)
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("testcontainsers-cassandra-" + UUID.randomUUID()));
        this.waitingFor(new CassandraWaitStrategy(this).withStartupTimeout(Duration.ofSeconds(this.getStartupTimeoutSeconds())));
    }

    /**
     * Get the list of ports for the liveness check.
     *
     * @return
     */
    @Override
    public Set<Integer> getLivenessCheckPortNumbers() {
        return new HashSet(this.getMappedPort(CASSANDRA_DEFAULT_PORT));
    }

    /**
     * Get the timeout
     *
     * @return
     */
    protected int getStartupTimeoutSeconds() {
        return STARTER_TIMOUT_SECONDS;
    }

    /**
     * Start the container.
     */
    @Override
    public void start() {
        super.start();
        // Execute all cql scripts
        this.cqlScripts.forEach(script -> {
            ContainerUtils.ExecCmdResult result = ContainerUtils.execCmd(this.getDockerClient(), this.getContainerId(), new String[]{"cqlsh", "-f", script});
            if (result.getExitCode() != 0) {
                throw new ContainerLaunchException(format("Execute script %s failed", script), new Exception(result.getOutput()));
            }
        });
        if (registerSpringbootProperties()) {
            // Register cassandra environment
            registerCassandraEnvironment();
        }
    }

    /**
     * Set the contact-points property for spring boot properties.
     * By default the property is 'pring.data.cassandra.contact-points'
     *
     * @param contactPointsSystemProperty the contact-points system property
     * @return
     */
    public CassandraContainer withContactPointsSystemProperty(String contactPointsSystemProperty) {
        this.contactPointsSystemProperty = contactPointsSystemProperty;
        return this.self();
    }

    /**
     * Set the cassandra port property for spring boot properties.
     * By default the property is 'spring.data.cassandra.port'
     *
     * @param cassandraPortSystemProperty the cassandra port system property
     * @return
     */
    public CassandraContainer withCassandraPortSystemProperty(String cassandraPortSystemProperty) {
        this.cassandraPortSystemProperty = cassandraPortSystemProperty;
        return this.self();
    }

    /**
     * Set the scripts directory.
     *
     * @param directory
     * @return
     */
    public CassandraContainer withCqlScriptDirectory(String directory) {
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
        // Create the volume that will be need for scripts
        this.addFileSystemBind(scriptsDir.toString(), DB_SCHEMA_DIRECTORY + '/' + scriptsDir.getFileName(), BindMode.READ_ONLY);
        // Add all scripts in cqlScripts attribute
        scanScripts(scriptsDir);
        return this.self();
    }

    /**
     * @param rootDirectory
     */
    private void scanScripts(Path rootDirectory) {
        this.scanScriptsImpl(rootDirectory, rootDirectory);
    }

    /**
     * @param rootDirectory
     * @param scriptDirectory
     */
    private void scanScriptsImpl(Path rootDirectory, Path scriptDirectory) {
        try (Stream<Path> paths = Files.list(scriptDirectory)) {
            paths
                    .filter(path -> path.toFile().isDirectory() || FilenameUtils.getExtension(path.toFile().getName()).equals("cql"))
                    .sorted()
                    .forEach(path -> {
                        if (path.toFile().isFile()) {
                            String subPath = path.subpath(rootDirectory.getNameCount(), path.getNameCount()).toString().replaceAll("\\\\", "/");
                            this.cqlScripts.add(DB_SCHEMA_DIRECTORY + "/" + rootDirectory.getFileName() + "/" + subPath);
                        } else {
                            scanScriptsImpl(rootDirectory, path);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalArgumentException("Error listing scripts", e);
        }
    }

    /**
     * Register cassandra properties for contact-points and port
     */
    protected void registerCassandraEnvironment() {
        if (this.contactPointsSystemProperty != null) {
            System.setProperty(this.contactPointsSystemProperty, this.getContainerIpAddress());
        }
        if (this.cassandraPortSystemProperty != null) {
            System.setProperty(this.cassandraPortSystemProperty, String.valueOf(this.getMappedPort(CASSANDRA_DEFAULT_PORT)));
        }
    }

    /**
     * Return the mapping cassandra port.
     *
     * @return
     */
    public Integer getCQLNativeTransportPort() {
        return this.getMappedPort(CASSANDRA_DEFAULT_PORT);
    }

    /**
     * @param registerProperties
     * @return
     */
    @Override
    public CassandraContainer withRegisterSpringbootProperties(boolean registerProperties) {
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
     * Return the rl.
     *
     * @return
     */
    @Override
    public String getURL() {
        return this.getContainerIpAddress();
    }

    /**
     * Return the local url.
     *
     * @return
     */
    @Override
    public String getInternalURL() {
        return this.getNetworkAliases().get(0);
    }

    /**
     * Return the internal cassandra port.
     *
     * @return
     */
    public Integer getInternalCQLNativeTransportPort() {
        return CASSANDRA_DEFAULT_PORT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CassandraContainer)) return false;
        if (!super.equals(o)) return false;
        CassandraContainer that = (CassandraContainer) o;
        return registerSpringbootProperties == that.registerSpringbootProperties &&
                Objects.equals(getContactPointsSystemProperty(), that.getContactPointsSystemProperty()) &&
                Objects.equals(getCassandraPortSystemProperty(), that.getCassandraPortSystemProperty());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), registerSpringbootProperties, getContactPointsSystemProperty(), getCassandraPortSystemProperty());
    }

    private static class CassandraWaitStrategy extends AbstractCommandWaitStrategy {

        /**
         * Default constructor.
         *
         * @param container
         */
        public CassandraWaitStrategy(GenericContainer container) {
            super(container);
        }

        /**
         * Returns the schell command that must be executed.
         *
         * @return
         */
        @Override
        public String[] getCheckCommand() {
            return new String[]{"cqlsh", "-e", "SELECT release_version FROM system.local"};
        }

    }

}
