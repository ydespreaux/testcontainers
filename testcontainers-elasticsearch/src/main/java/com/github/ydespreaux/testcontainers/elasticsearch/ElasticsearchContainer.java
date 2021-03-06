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

package com.github.ydespreaux.testcontainers.elasticsearch;

import com.github.ydespreaux.testcontainers.common.IContainer;
import com.github.ydespreaux.testcontainers.elasticsearch.client.ElasticsearchClient;
import com.github.ydespreaux.testcontainers.elasticsearch.client.ElasticsearchCommand;
import com.github.ydespreaux.testcontainers.elasticsearch.client.ElasticsearchCommandParser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.apache.commons.io.FilenameUtils;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.containerLogsConsumer;
import static java.lang.String.format;

/**
 * Elasticsearch container
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
public class ElasticsearchContainer extends GenericContainer<ElasticsearchContainer> implements IContainer<ElasticsearchContainer> {

    private static final int ELASTICSEARCH_DEFAULT_PORT = 9200;
    private static final int ELASTICSEARCH_DEFAULT_TCP_PORT = 9300;
    private static final String ELASTICSEARCH_DEFAULT_BASE_URL = "docker.elastic.co/elasticsearch/elasticsearch";
    private static final String ELASTICSEARCH_DEFAULT_VERSION = "5.6.8";

    private static final String ELASTICSEARCH_CONFIG_DIRECTORY = "/usr/share/elasticsearch/config";

    /**
     * Register springboot properties in environment
     */
    private boolean registerSpringbootProperties = true;

    /**
     * Define the elasticsearch url for spring boot properties.
     */
    @Getter
    private String jestUrisSystemProperty = "spring.elasticsearch.jest.uris";
    @Getter
    private String restUrisSystemProperty = "spring.elasticsearch.rest.uris";

    private List<ElasticsearchCommand> commands = new ArrayList<>();

    /**
     * Default constructor
     * By default, the base url is 'docker.elastic.co/elasticsearch/elasticsearch' and the version '5.6.8'
     */
    public ElasticsearchContainer() {
        this(ELASTICSEARCH_DEFAULT_BASE_URL, ELASTICSEARCH_DEFAULT_VERSION);
    }

    /**
     * Create elasticsearch container with a specific version
     *
     * @param version the image version
     */
    public ElasticsearchContainer(String version) {
        this(ELASTICSEARCH_DEFAULT_BASE_URL, version);
    }

    /**
     * Create elasticsearch container with a specific url and version
     *
     * @param baseUrl
     * @param version
     */
    public ElasticsearchContainer(String baseUrl, String version) {
        super(baseUrl + ":" + version);
        waitingFor(Wait.forHttp("/"));
    }

    /**
     * Set uris for spring boot properties.
     * By default the properties is 'spring.elasticsearch.jest.uris'
     *
     * @param urisSystemProperty
     * @return
     */
    public ElasticsearchContainer withJestUrisSystemProperty(String urisSystemProperty) {
        this.jestUrisSystemProperty = urisSystemProperty;
        return this.self();
    }

    public ElasticsearchContainer withRestUrisSystemProperty(String urisSystemProperty) {
        this.restUrisSystemProperty = urisSystemProperty;
        return this.self();
    }

    /**
     * Return list of ports for liveness check.
     *
     * @return
     */
    @Override
    public Set<Integer> getLivenessCheckPortNumbers() {
        Set<Integer> ports = new HashSet<>();
        ports.add(getMappedPort(ELASTICSEARCH_DEFAULT_PORT));
        return ports;
    }

    /**
     * Configure the container
     */
    @Override
    protected void configure() {
        logger().info("Starting an elasticsearch container using [{}]", this.getDockerImageName());
        this.withLogConsumer(containerLogsConsumer(log));
        withEnv("xpack.security.enabled", "false");
        withEnv("http.host", "0.0.0.0");
        withEnv("transport.host", "127.0.0.1");
        addExposedPort(ELASTICSEARCH_DEFAULT_PORT);
        addExposedPort(ELASTICSEARCH_DEFAULT_TCP_PORT);
        withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("testcontainsers-elasticsearch-" + UUID.randomUUID()));
    }

    /**
     * Start the container
     */
    @Override
    public void start() {
        super.start();
        if (!commands.isEmpty()) {
            ElasticsearchClient client = new ElasticsearchClient(this);
            commands.forEach(client::execute);
        }
        if (registerSpringbootProperties()) {
            registerElasticsearchEnvironment();
        }
    }

    /**
     * Register system properties
     */
    protected void registerElasticsearchEnvironment() {
        if (!StringUtils.isEmpty(this.jestUrisSystemProperty)) {
            System.setProperty(this.jestUrisSystemProperty, getURL());
        }
        if (!StringUtils.isEmpty(this.restUrisSystemProperty)) {
            System.setProperty(this.restUrisSystemProperty, getURL());
        }
    }


    /**
     * @param registerProperties
     * @return
     */
    @Override
    public ElasticsearchContainer withRegisterSpringbootProperties(boolean registerProperties) {
        this.registerSpringbootProperties = registerProperties;
        return this.self();
    }

    /**
     * Check if the system properties must be registred.
     *
     * @return
     */
    @Override
    public boolean registerSpringbootProperties() {
        return registerSpringbootProperties;
    }

    /**
     * Get url
     *
     * @return
     */
    @Override
    public String getURL() {
        return format("http://%s:%d", this.getContainerIpAddress(), getHttpPort());
    }

    /**
     * Get local url.
     *
     * @return
     */
    @Override
    public String getInternalURL() {
        return format("http://%s:%d", this.getNetworkAliases().get(0), ELASTICSEARCH_DEFAULT_PORT);
    }

    /**
     * Get http port
     *
     * @return
     */
    public Integer getHttpPort() {
        return this.getMappedPort(ELASTICSEARCH_DEFAULT_PORT);
    }

    public ElasticsearchContainer withConfigDirectory(String localPath) {
        Objects.requireNonNull(localPath, "localVolume must be provided");
        MountableFile mountableFile = MountableFile.forClasspathResource(localPath);
        Path path = Paths.get(mountableFile.getResolvedPath());
        File file = path.toFile();
        if (!file.exists()) {
            throw new IllegalArgumentException(format("Resource with path %s could not be found", path.toString()));
        }
        if (file.isFile()) {
            addMountableFile(path, ELASTICSEARCH_CONFIG_DIRECTORY);
        } else {
            scanDirectory(path, path, ELASTICSEARCH_CONFIG_DIRECTORY);
        }
        return self();
    }

    /**
     * @param fileInitPath
     * @return
     */
    public ElasticsearchContainer withFileInitScript(String fileInitPath) {
        commands.addAll(ElasticsearchCommandParser.INSTANCE.parse(fileInitPath));
        return self();
    }

    /**
     * @param localPath
     * @param containerPath
     */
    private void scanDirectory(Path localRootPath, Path localPath, String containerPath) {
        try (Stream<Path> paths = Files.list(localPath)) {
            paths
                    .filter(path -> path.toFile().isDirectory() || FilenameUtils.getExtension(path.toFile().getName()).equals("txt"))
                    .sorted()
                    .forEach(path -> {
                        if (path.toFile().isFile()) {
                            String pathFilename = path.toString().substring(localRootPath.toString().length() + 1);
                            addMountableFile(path, containerPath + '/' + pathFilename);
                        } else {
                            scanDirectory(localRootPath, path, containerPath);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalArgumentException("Error listing scripts", e);
        }
    }

    private void addMountableFile(Path localPath, String containerPath) {
        MountableFile mountableFile = MountableFile.forHostPath(localPath);
        // Create the volume that will be need for scripts
        this.addFileSystemBind(mountableFile.getResolvedPath(), containerPath, BindMode.READ_ONLY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ElasticsearchContainer)) return false;
        if (!super.equals(o)) return false;
        ElasticsearchContainer that = (ElasticsearchContainer) o;
        return registerSpringbootProperties == that.registerSpringbootProperties &&
                Objects.equals(getJestUrisSystemProperty(), that.getJestUrisSystemProperty()) &&
                Objects.equals(getRestUrisSystemProperty(), that.getRestUrisSystemProperty());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), registerSpringbootProperties, getJestUrisSystemProperty(), getRestUrisSystemProperty());
    }
}
