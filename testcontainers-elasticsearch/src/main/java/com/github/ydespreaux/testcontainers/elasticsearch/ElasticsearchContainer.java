package com.github.ydespreaux.testcontainers.elasticsearch;

import com.github.ydespreaux.testcontainers.common.IContainer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
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
import java.util.stream.Stream;

import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.containerLogsConsumer;
import static java.lang.String.format;

/**
 * Elasticsearch container
 *
 * @param <SELF>
 * @since 1.0.0
 */
@Slf4j
public class ElasticsearchContainer<SELF extends ElasticsearchContainer<SELF>> extends GenericContainer<SELF> implements IContainer<SELF> {

    private static final int ELASTICSEARCH_DEFAULT_PORT = 9200;
    private static final int ELASTICSEARCH_DEFAULT_TCP_PORT = 9300;
    private static final String ELASTICSEARCH_DEFAULT_BASE_URL = "docker.elastic.co/elasticsearch/elasticsearch";
    private static final String ELASTICSEARCH_DEFAULT_VERSION = "5.6.8";

    private static final String ELASTICSEARCH_CONFIG_DIRECTORY = "/usr/share/elasticsearch/config";
    private static final String ELASTICSEARCH_DEFAULT_SYNONYMS_DIRECTORY = "synonyms";

    /**
     * Register springboot properties in environment
     */
    private boolean registerSpringbootProperties = true;

    /**
     * Define the elasticsearch url for spring boot properties.
     */
    @Getter
    private String urisSystemProperty = "spring.elasticsearch.jest.uris";

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
    }

    /**
     * Set uris for spring boot properties.
     * By default the properties is 'spring.elasticsearch.jest.uris'
     *
     * @param urisSystemProperty
     * @return
     */
    public SELF withUrisSystemProperty(String urisSystemProperty) {
        this.urisSystemProperty = urisSystemProperty;
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
        waitingFor(Wait.forHttp("/")); // Wait until elastic start
    }

    /**
     * Start the container
     */
    @Override
    public void start() {
        super.start();
        if (registerSpringbootProperties()) {
            registerElasticsearchEnvironment();
        }
    }

    /**
     * Set the network
     *
     * @param network
     * @return
     */
    @Override
    public SELF withNetwork(Network network) {
        super.withNetwork(network);
        return this.self();
    }

    /**
     * Register system properties
     */
    protected void registerElasticsearchEnvironment() {
        if (!StringUtils.isEmpty(this.urisSystemProperty)) {
            System.setProperty(this.urisSystemProperty, getURL());
        }
    }


    /**
     * @param registerProperties
     * @return
     */
    @Override
    public SELF withRegisterSpringbootProperties(boolean registerProperties) {
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

    public SELF withConfigDirectory(String localPath) {
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
            scanSynonymsDirectory(path, path, ELASTICSEARCH_CONFIG_DIRECTORY);
        }
        return self();
    }

    public SELF withSynonyms(String localPath) {
        return withSynonyms(localPath, ELASTICSEARCH_DEFAULT_SYNONYMS_DIRECTORY);
    }

    public SELF withSynonyms(String localPath, String targetPath) {
        Objects.requireNonNull(localPath, "localVolume must be provided");
        Objects.requireNonNull(targetPath, "targetPath must be provided");
        MountableFile mountableFile = MountableFile.forClasspathResource(localPath);
        Path path = Paths.get(mountableFile.getResolvedPath());
        File file = path.toFile();
        if (!file.exists()) {
            throw new IllegalArgumentException(format("Resource with path %s could not be found", path.toString()));
        }
        if (targetPath.startsWith("/")) {
            targetPath = targetPath.substring(1);
        }
        if (targetPath.endsWith("/")) {
            targetPath = targetPath.substring(0, targetPath.length() - 1);
        }
        if (file.isFile()) {
            addMountableFile(path, ELASTICSEARCH_CONFIG_DIRECTORY + "/" + targetPath);
        } else {
            scanSynonymsDirectory(path, path, ELASTICSEARCH_CONFIG_DIRECTORY + "/" + targetPath);
        }
        return self();
    }

    /**
     * @param localPath
     * @param containerPath
     */
    private void scanSynonymsDirectory(Path localRootPath, Path localPath, String containerPath) {
        try (Stream<Path> paths = Files.list(localPath)) {
            paths
                    .filter(path -> path.toFile().isDirectory() || FilenameUtils.getExtension(path.toFile().getName()).equals("txt"))
                    .sorted()
                    .forEach(path -> {
                        if (path.toFile().isFile()) {
                            String pathFilename = path.toString().substring(localRootPath.toString().length() + 1);
                            addMountableFile(path, containerPath + '/' + pathFilename);
                        } else {
                            scanSynonymsDirectory(localRootPath, path, containerPath);
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

}
