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

package com.github.ydespreaux.testcontainers.kafka.containers;

import com.github.ydespreaux.testcontainers.common.IContainer;
import com.github.ydespreaux.testcontainers.kafka.security.Certificates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
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
import static com.github.ydespreaux.testcontainers.kafka.security.CertificateUtils.addCertificates;
import static java.lang.String.format;

/**
 * Define Kafka connect container.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
public class KafkaConnectContainer extends FixedHostPortGenericContainer<KafkaConnectContainer> implements IContainer<KafkaConnectContainer> {

    private static final String KAFKA_CONNECT_DEFAULT_BASE_URL = "confluentinc/cp-kafka-connect";
    private static final String SECRETS_DIRECTORY = "/etc/kafka-connect/secrets";

    private static final String REST_APP_SYSTEM_PROPERTY = "spring.kafka-connect.rest-app";

    private static final String AVRO_CONVERTER_PATTERN = "AvroConverter";

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
    private String brokersServerUrl;
    /**
     * Schema registry url
     */
    private String schemaRegistryUrl;

    /**
     *
     */
    private String restAppSystemProperty = REST_APP_SYSTEM_PROPERTY;

    private Certificates kafkaServerCertificates;

    private boolean registerSpringbootProperties = true;
    private boolean hasKeyAvroConverter = false;
    private boolean hasValueAvroConverter = false;

    /**
     * @param version
     */
    public KafkaConnectContainer(String version) {
        this(version, getAvailableMappingPort());
    }

    /**
     * @param version
     * @param restAppMappingPort
     */
    public KafkaConnectContainer(String version, int restAppMappingPort) {
        super(KAFKA_CONNECT_DEFAULT_BASE_URL + ":" + version);
        this.restAppMappingPort = restAppMappingPort;
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
        waitingFor(Wait.forHttp("/").withStartupTimeout(Duration.ofSeconds(120L)));
    }

    public KafkaConnectContainer withBrokersServerUrl(String brokersServerUrl) {
        this.brokersServerUrl = brokersServerUrl;
        return this;
    }

    public KafkaConnectContainer withSchemaRegistryUrl(String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
        return this;
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
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("testcontainsers-kafka-connect-" + UUID.randomUUID()));

        if (hasKeyAvroConverter) {
            Objects.requireNonNull(this.schemaRegistryUrl, "Schema registry URL not defined !!");
            this.withEnv(KEY_CONVERTER_SCHEMA_REGISTRY_URL_CONFIG, this.schemaRegistryUrl);
            this.withEnv(KEY_CONVERTER_SCHEMAS_ENABLE_CONFIG, "true");
        }
        if (hasValueAvroConverter) {
            Objects.requireNonNull(this.schemaRegistryUrl, "Schema registry URL not defined !!");
            this.withEnv(VALUE_CONVERTER_SCHEMA_REGISTRY_URL_CONFIG, this.schemaRegistryUrl);
            this.withEnv(VALUE_CONVERTER_SCHEMAS_ENABLE_CONFIG, "true");
        }
    }

    /**
     * @param certificates
     * @return
     */
    public KafkaConnectContainer withServerCertificates(@Nullable Certificates certificates) {
        if (certificates == null) {
            return this;
        }
        if (this.kafkaServerCertificates != null) {
            throw new IllegalArgumentException("Certificates is already initialized.");
        }
        this.kafkaServerCertificates = addCertificates(this, certificates, SECRETS_DIRECTORY, "CONNECT_");
        return this;
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
    public KafkaConnectContainer withGroupId(@Nullable String groupId) {
        if (groupId != null) {
            withEnv(GROUP_ID_CONFIG, groupId);
        }
        return this;
    }

    /**
     * Set the config storage topic.
     *
     * @param topic
     * @return
     */
    public KafkaConnectContainer withConfigStorageTopic(@Nullable String topic) {
        if (topic != null) {
            withEnv(CONFIG_STORAGE_TOPIC_CONFIG, topic);
        }
        return this;
    }

    /**
     * Set the topic name of the storage offsets topic.
     *
     * @param topic
     * @return
     */
    public KafkaConnectContainer withOffsetStorageTopic(@Nullable String topic) {
        if (topic != null) {
            withEnv(OFFSET_STORAGE_TOPIC_CONFIG, topic);
        }
        return this;
    }

    /**
     * Set the offsets storage partition.
     *
     * @param partitions
     * @return
     */
    public KafkaConnectContainer withOffsetStoragePartition(@Nullable Integer partitions) {
        if (partitions != null) {
            withEnv(OFFSET_STORAGE_PARTITIONS_CONFIG, String.valueOf(partitions));
        }
        return this;
    }

    /**
     * Set the status storage topic's name.
     * @param topic
     * @return
     */
    public KafkaConnectContainer withStatusStorageTopic(@Nullable String topic) {
        if (topic != null) {
            withEnv(STATUS_STORAGE_TOPIC_CONFIG, topic);
        }
        return this;
    }

    /**
     * Set the status storage partition.
     *
     * @param partitions
     * @return
     */
    public KafkaConnectContainer withStatusStoragePartition(@Nullable Integer partitions) {
        if (partitions != null) {
            withEnv(STATUS_STORAGE_PARTITIONS_CONFIG, String.valueOf(partitions));
        }
        return this;
    }

    /**
     * Set the offsets storage file name.
     *
     * @param storageFilename
     * @return
     */
    public KafkaConnectContainer withOffsetStorageFilename(@Nullable String storageFilename) {
        if (storageFilename != null) {
            withEnv(OFFSET_STORAGE_FILE_FILENAME_CONFIG, storageFilename);
        }
        return this;
    }

    /**
     * Set the key converter.
     *
     * @param keyConverter
     * @return
     */
    public KafkaConnectContainer withKeyConverter(@Nullable String keyConverter) {
        if (keyConverter != null) {
            withEnv(KEY_CONVERTER_CONFIG, keyConverter);
            this.hasKeyAvroConverter = keyConverter.contains(AVRO_CONVERTER_PATTERN);
        }
        return this;
    }

    /**
     * Set the value converter.
     *
     * @param valueConverter
     * @return
     */
    public KafkaConnectContainer withValueConverter(@Nullable String valueConverter) {
        if (valueConverter != null) {
            withEnv(VALUE_CONVERTER_CONFIG, valueConverter);
            this.hasValueAvroConverter = valueConverter.contains(AVRO_CONVERTER_PATTERN);
        }
        return this;
    }

    /**
     * Set the list of plugins directory.
     *
     * @param plugins
     * @return
     */
    public KafkaConnectContainer withPlugins(@Nullable Set<String> plugins) {
        if (plugins == null) {
            return this;
        }
        plugins.forEach(this::withPlugins);
        return this;
    }

    /**
     * Set the plugins directory.
     *
     * @param plugins
     * @return
     */
    public KafkaConnectContainer withPlugins(@Nullable String plugins) {
        if (plugins == null) {
            return this;
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
        return this;
    }

    /**
     * @param restAppSystemProperty
     * @return
     */
    public KafkaConnectContainer withRestAppSystemProperty(@Nullable String restAppSystemProperty) {
        if (restAppSystemProperty != null) {
            this.restAppSystemProperty = restAppSystemProperty;
        }
        return this;
    }

    /**
     * Set if the sytem properties must be registred.
     *
     * @param registerProperties
     * @return
     */
    @Override
    public KafkaConnectContainer withRegisterSpringbootProperties(boolean registerProperties) {
        this.registerSpringbootProperties = registerProperties;
        return this;
    }

    /**
     * Check if system properties must be registred.
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KafkaConnectContainer)) return false;
        if (!super.equals(o)) return false;
        KafkaConnectContainer that = (KafkaConnectContainer) o;
        return restAppMappingPort == that.restAppMappingPort;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), restAppMappingPort);
    }
}
