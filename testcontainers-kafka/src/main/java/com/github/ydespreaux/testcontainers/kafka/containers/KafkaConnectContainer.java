package com.github.ydespreaux.testcontainers.kafka.containers;

import com.github.ydespreaux.testcontainers.common.IContainer;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.containerLogsConsumer;
import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.getAvailableMappingPort;
import static java.lang.String.format;

/**
 * Define Kafka connect container.
 *
 * @param <SELF>
 * @since 1.1.1
 */
@Slf4j
public class KafkaConnectContainer<SELF extends KafkaConnectContainer<SELF>> extends FixedHostPortGenericContainer<SELF> implements IContainer<SELF> {

    /**
     * Key / value for Configuration
     */
    public static final String GROUP_ID_CONFIG = "CONNECT_GROUP_ID";
    public static final String OFFSET_STORAGE_FILE_FILENAME_CONFIG = "CONNECT_OFFSET_STORAGE_FILE_FILENAME";
    public static final String OFFSET_STORAGE_TOPIC_CONFIG = "CONNECT_OFFSET_STORAGE_TOPIC";
    public static final String OFFSET_STORAGE_PARTITIONS_CONFIG = "CONNECT_OFFSET_STORAGE_PARTITIONS";
    public static final String CONFIG_STORAGE_TOPIC_CONFIG = "CONNECT_CONFIG_STORAGE_TOPIC";
    public static final String STATUS_STORAGE_TOPIC_CONFIG = "CONNECT_STATUS_STORAGE_TOPIC";
    public static final String STATUS_STORAGE_PARTITIONS_CONFIG = "CONNECT_STATUS_STORAGE_PARTITIONS";
    public static final String KEY_CONVERTER_CONFIG = "CONNECT_KEY_CONVERTER";
    public static final String KEY_CONVERTER_SCHEMA_REGISTRY_URL_CONFIG = "CONNECT_KEY_CONVERTER_SCHEMA_REGISTRY_URL";
    public static final String VALUE_CONVERTER_CONFIG = "CONNECT_VALUE_CONVERTER";
    public static final String VALUE_CONVERTER_SCHEMA_REGISTRY_URL_CONFIG = "CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_URL";
    private static final String KAFKA_CONNECT_DEFAULT_BASE_URL = "confluentinc/cp-kafka-connect";
    private static final String PLUGIN_PATH_CONTAINER = "/usr/share/java";
    private static final String GROUP_ID_DEFAULT_VALUE = "kafka-connect-group";
    private static final String OFFSET_STORAGE_FILE_FILENAME_DEFAULT_VALUE = "connect-offsets-file.txt";
    private static final String OFFSET_STORAGE_TOPIC_DEFAULT_VALUE = "connect-offsets";
    private static final Integer OFFSET_STORAGE_PARTITIONS_DEFAULT_VALUE = 3;
    private static final String OFFSET_STORAGE_REPLICATION_FACTOR_CONFIG = "CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR";
    private static final Integer OFFSET_STORAGE_REPLICATION_FACTOR_DEFAULT_VALUE = 1;
    private static final String CONFIG_STORAGE_TOPIC_DEFAULT_VALUE = "connect-config";
    private static final String CONFIG_STORAGE_REPLICATION_FACTOR_CONFIG = "CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR";
    private static final Integer CONFIG_STORAGE_REPLICATION_FACTOR_DEFAULT_VALUE = 1;
    private static final String STATUS_STORAGE_TOPIC_DEFAULT_VALUE = "connect-status";
    private static final Integer STATUS_STORAGE_PARTITIONS_DEFAULT_VALUE = 3;
    private static final String STATUS_STORAGE_REPLICATION_FACTOR_CONFIG = "CONNECT_STATUS_STORAGE_REPLICATION_FACTOR";
    private static final Integer STATUS_STORAGE_REPLICATION_FACTOR_DEFAULT_VALUE = 1;
    private static final String KEY_CONVERTER_DEFAULT_VALUE = "org.apache.kafka.connect.json.JsonConverter";
    private static final String KEY_CONVERTER_SCHEMAS_ENABLE_CONFIG = "CONNECT_KEY_CONVERTER_SCHEMAS_ENABLE";
    private static final String VALUE_CONVERTER_DEFAULT_VALUE = "org.apache.kafka.connect.json.JsonConverter";
    private static final String VALUE_CONVERTER_SCHEMAS_ENABLE_CONFIG = "CONNECT_VALUE_CONVERTER_SCHEMAS_ENABLE";
    private static final String INTERNAL_KEY_CONVERTER_CONFIG = "CONNECT_INTERNAL_KEY_CONVERTER";
    private static final String INTERNAL_KEY_CONVERTER_DEFAULT_VALUE = "org.apache.kafka.connect.json.JsonConverter";
    private static final String INTERNAL_VALUE_CONVERTER_CONFIG = "CONNECT_INTERNAL_VALUE_CONVERTER";
    private static final String INTERNAL_VALUE_CONVERTER_DEFAULT_VALUE = "org.apache.kafka.connect.json.JsonConverter";
    /**
     * Api Rest port
     */
    private final int restAppMappingPort;
    /**
     * Brokers server url
     */
    private final String brokersServerUrl;
    /**
     * Schema registry url
     */
    private final String schemaRegistryUrl;

    /**
     * Register springboot properties in environment
     */
    private boolean registerSpringbootProperties = true;
    /**
     *
     */
    private String restAppSystemProperty;

    public KafkaConnectContainer(String version, String brokersServerUrl) {
        this(version, getAvailableMappingPort(), brokersServerUrl, null);
    }

    public KafkaConnectContainer(String version, String brokersServerUrl, String schemaRegistryUrl) {
        this(version, getAvailableMappingPort(), brokersServerUrl, schemaRegistryUrl);
    }

    public KafkaConnectContainer(String version, int restAppMappingPort, String brokersServerUrl, String schemaRegistryUrl) {
        super(KAFKA_CONNECT_DEFAULT_BASE_URL + ":" + version);
        this.restAppMappingPort = restAppMappingPort;
        this.brokersServerUrl = brokersServerUrl;
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.initConfiguration();
    }

    /**
     * Add default configurations.
     */
    private void initConfiguration() {
        this.withEnv(GROUP_ID_CONFIG, GROUP_ID_DEFAULT_VALUE);
        this.withEnv(KEY_CONVERTER_CONFIG, KEY_CONVERTER_DEFAULT_VALUE);
        this.withEnv(VALUE_CONVERTER_CONFIG, VALUE_CONVERTER_DEFAULT_VALUE);
        this.withEnv(OFFSET_STORAGE_FILE_FILENAME_CONFIG, OFFSET_STORAGE_FILE_FILENAME_DEFAULT_VALUE);
        this.withEnv(OFFSET_STORAGE_TOPIC_CONFIG, OFFSET_STORAGE_TOPIC_DEFAULT_VALUE);
        this.withEnv(OFFSET_STORAGE_PARTITIONS_CONFIG, String.valueOf(OFFSET_STORAGE_PARTITIONS_DEFAULT_VALUE));
        this.withEnv(OFFSET_STORAGE_REPLICATION_FACTOR_CONFIG, String.valueOf(OFFSET_STORAGE_REPLICATION_FACTOR_DEFAULT_VALUE));
        this.withEnv(CONFIG_STORAGE_TOPIC_CONFIG, CONFIG_STORAGE_TOPIC_DEFAULT_VALUE);
        this.withEnv(CONFIG_STORAGE_REPLICATION_FACTOR_CONFIG, String.valueOf(CONFIG_STORAGE_REPLICATION_FACTOR_DEFAULT_VALUE));
        this.withEnv(STATUS_STORAGE_TOPIC_CONFIG, STATUS_STORAGE_TOPIC_DEFAULT_VALUE);
        this.withEnv(STATUS_STORAGE_PARTITIONS_CONFIG, String.valueOf(STATUS_STORAGE_PARTITIONS_DEFAULT_VALUE));
        this.withEnv(STATUS_STORAGE_REPLICATION_FACTOR_CONFIG, String.valueOf(STATUS_STORAGE_REPLICATION_FACTOR_DEFAULT_VALUE));
        this.withEnv(INTERNAL_KEY_CONVERTER_CONFIG, INTERNAL_KEY_CONVERTER_DEFAULT_VALUE);
        this.withEnv(INTERNAL_VALUE_CONVERTER_CONFIG, INTERNAL_VALUE_CONVERTER_DEFAULT_VALUE);
    }

    /**
     * Configure de the container
     */
    @Override
    protected void configure() {
        super.configure();
        this.withLogConsumer(containerLogsConsumer(log))
                .withEnv("CONNECT_BOOTSTRAP_SERVERS", this.brokersServerUrl)
                .withEnv("CONNECT_REST_ADVERTISED_HOST_NAME", "kafka-connect")
                .withEnv("CONNECT_PLUGIN_PATH", PLUGIN_PATH_CONTAINER)
                .withEnv("CONNECT_LOG4J_LOGGERS", "org.reflections=ERROR")
                .withEnv("CONNECT_REST_PORT", String.valueOf(this.restAppMappingPort))
                .withExposedPorts(this.restAppMappingPort)
                .withFixedExposedPort(this.restAppMappingPort, this.restAppMappingPort)
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("testcontainsers-kafka-connect-" + UUID.randomUUID()))
                .waitingFor(Wait.forHttp("/").withStartupTimeout(Duration.ofSeconds(120L)));
    }

    /**
     * Start the container
     */
    @Override
    public void start() {
        super.start();
        if (this.registerSpringbootProperties()) {
            this.registerKafkaConnectEnvironment();
        }
    }

    /**
     * Set the group id
     *
     * @param groupId
     * @return
     */
    public SELF withGroupId(String groupId) {
        if (groupId != null) {
            withEnv(GROUP_ID_CONFIG, groupId);
        }
        return this.self();
    }

    /**
     * Set the config storage topic.
     *
     * @param topic
     * @return
     */
    public SELF withConfigStorageTopic(String topic) {
        if (topic != null) {
            withEnv(CONFIG_STORAGE_TOPIC_CONFIG, topic);
        }
        return this.self();
    }

    /**
     * Set the replication.
     *
     * @param replication
     * @return
     */
    public SELF withConfigStorageReplicationFactor(Integer replication) {
        if (replication != null) {
            withEnv(CONFIG_STORAGE_REPLICATION_FACTOR_CONFIG, String.valueOf(replication));
        }
        return this.self();
    }

    /**
     * Set the topic name of the storage offsets topic.
     *
     * @param topic
     * @return
     */
    public SELF withOffsetStorageTopic(String topic) {
        if (topic != null) {
            withEnv(OFFSET_STORAGE_TOPIC_CONFIG, topic);
        }
        return this.self();
    }

    /**
     * Set the offsets storage partition.
     *
     * @param partitions
     * @return
     */
    public SELF withOffsetStoragePartition(Integer partitions) {
        if (partitions != null) {
            withEnv(OFFSET_STORAGE_PARTITIONS_CONFIG, String.valueOf(partitions));
        }
        return this.self();
    }

    /**
     * Set the offsets storage replication.
     *
     * @param replication
     * @return
     */
    public SELF withOffsetStorageReplicationFactor(Integer replication) {
        if (replication != null) {
            withEnv(OFFSET_STORAGE_REPLICATION_FACTOR_CONFIG, String.valueOf(replication));
        }
        return this.self();
    }

    /**
     * Set the status storage topic's name.
     *
     * @param topic
     * @return
     */
    public SELF withStatusStorageTopic(String topic) {
        if (topic != null) {
            withEnv(STATUS_STORAGE_TOPIC_CONFIG, topic);
        }
        return this.self();
    }

    /**
     * Set the status storage partition.
     *
     * @param partitions
     * @return
     */
    public SELF withStatusStoragePartition(Integer partitions) {
        if (partitions != null) {
            withEnv(STATUS_STORAGE_PARTITIONS_CONFIG, String.valueOf(partitions));
        }
        return this.self();
    }

    /**
     * Set the status storage replication.
     *
     * @param replication
     * @return
     */
    public SELF withStatusStorageReplicationFactor(Integer replication) {
        if (replication != null) {
            withEnv(STATUS_STORAGE_REPLICATION_FACTOR_CONFIG, String.valueOf(replication));
        }
        return this.self();
    }

    /**
     * Set the offsets storage file name.
     *
     * @param storageFilename
     * @return
     */
    public SELF withOffsetStorageFilename(String storageFilename) {
        if (storageFilename != null) {
            withEnv(OFFSET_STORAGE_FILE_FILENAME_CONFIG, storageFilename);
        }
        return this.self();
    }

    /**
     * Set the key converter.
     *
     * @param keyConverter
     * @return
     */
    public SELF withKeyConverter(String keyConverter) {
        if (keyConverter != null) {
            withEnv(KEY_CONVERTER_CONFIG, keyConverter);
            if (keyConverter.contains("AvroConverter")) {
                Objects.requireNonNull(this.schemaRegistryUrl, "Schema registry URL not defined !!");
                this.withEnv(KEY_CONVERTER_SCHEMA_REGISTRY_URL_CONFIG, this.schemaRegistryUrl);
                this.withEnv(KEY_CONVERTER_SCHEMAS_ENABLE_CONFIG, "true");
            } else {
                this.withEnv(KEY_CONVERTER_SCHEMAS_ENABLE_CONFIG, "false");
            }
        }
        return this.self();
    }

    /**
     * Set the value converter.
     *
     * @param valueConverter
     * @return
     */
    public SELF withValueConverter(String valueConverter) {
        if (valueConverter != null) {
            withEnv(VALUE_CONVERTER_CONFIG, valueConverter);
            if (valueConverter.contains("AvroConverter")) {
                Objects.requireNonNull(this.schemaRegistryUrl, "Schema registry URL not defined !!");
                this.withEnv(VALUE_CONVERTER_SCHEMA_REGISTRY_URL_CONFIG, this.schemaRegistryUrl);
                this.withEnv(VALUE_CONVERTER_SCHEMAS_ENABLE_CONFIG, "true");
            } else {
                this.withEnv(VALUE_CONVERTER_SCHEMAS_ENABLE_CONFIG, "false");
            }

        }
        return this.self();
    }

    /**
     * Set the list of plugins directory.
     *
     * @param plugins
     * @return
     */
    public SELF withPlugins(Set<String> plugins) {
        if (plugins == null) {
            return this.self();
        }
        plugins.forEach(this::withPlugins);
        return this.self();
    }

    /**
     * Set the plugins directory.
     *
     * @param plugins
     * @return
     */
    public SELF withPlugins(String plugins) {
        if (plugins == null) {
            return this.self();
        }
        MountableFile mountableFile = MountableFile.forClasspathResource(plugins);
        Path pluginsPath = Paths.get(mountableFile.getResolvedPath());
        File pluginsFile = pluginsPath.toFile();
        if (!pluginsFile.exists()) {
            throw new IllegalArgumentException(format("Resource with path %s could not be found", pluginsPath.toString()));
        }
        String containerPath = PLUGIN_PATH_CONTAINER;
        if (pluginsFile.isDirectory()) {
            containerPath += "/" + pluginsPath.getFileName();
        } else {
            containerPath += "/" + pluginsPath.getParent().getFileName() + "/" + pluginsPath.getFileName();
        }
        // Create the volume that will be need for scripts
        this.addFileSystemBind(mountableFile.getResolvedPath(), containerPath, BindMode.READ_ONLY);
        return this.self();
    }

    /**
     * @param restAppSystemProperty
     * @return
     */
    public SELF withRestAppSystemProperty(String restAppSystemProperty) {
        if (restAppSystemProperty != null) {
            this.restAppSystemProperty = restAppSystemProperty;
        }
        return this.self();
    }

    /**
     * Set if the sytem properties must be registred.
     *
     * @param registerProperties
     * @return
     */
    @Override
    public SELF withRegisterSpringbootProperties(boolean registerProperties) {
        this.registerSpringbootProperties = registerProperties;
        return this.self();
    }

    /**
     * Check if system properties must be registred.
     *
     * @return
     */
    @Override
    public boolean registerSpringbootProperties() {
        return registerSpringbootProperties;
    }

    /**
     * Get the url.
     *
     * @return
     */
    public String getURL() {
        return format("http://%s:%d", this.getContainerIpAddress(), this.getFirstMappedPort());
    }

    /**
     * Get the local url
     *
     * @return
     */
    @Override
    public String getInternalURL() {
        return format("http://%s:%d", this.getNetworkAliases().get(0), this.getFirstMappedPort());
    }

    /**
     * Register system properties.
     */
    protected void registerKafkaConnectEnvironment() {
        if (this.restAppSystemProperty != null) {
            System.setProperty(this.restAppSystemProperty, getURL());
        }
    }

}
