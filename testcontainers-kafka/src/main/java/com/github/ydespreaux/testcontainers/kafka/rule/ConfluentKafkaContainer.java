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

package com.github.ydespreaux.testcontainers.kafka.rule;

import com.github.ydespreaux.testcontainers.kafka.config.TopicConfiguration;
import com.github.ydespreaux.testcontainers.kafka.containers.KafkaContainer;
import com.github.ydespreaux.testcontainers.kafka.containers.SchemaRegistryContainer;
import com.github.ydespreaux.testcontainers.kafka.containers.ZookeeperContainer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.Network;

import java.util.*;

import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.execCmd;
import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.getContainerHostname;


/**
 * This class is used to start a zookeeper container, a kafka container, and a schema registry container
 * if it is enabled.
 *
 * @param <SELF>
 * @since 1.0.0
 */
@Slf4j
public class ConfluentKafkaContainer<SELF extends ConfluentKafkaContainer<SELF>> extends ExternalResource implements ConfluentContainer<SELF> {

    private static final String CONFLUENT_DEFAULT_VERSION = "3.3.1";

    /**
     *
     */
    private static final String BROKER_SERVERS_SYSTEM_PROPERTY = "spring.kafka.bootstrap-servers";

    /**
     *
     */
    private static final String SCHEMA_REGISTRY_SYSTEM_PROPERTY = "spring.kafka.properties.schema.registry.url";

    /**
     * Define the confluent version.
     */
    @Getter
    private final String confluentVersion;
    /**
     * Default topics list
     */
    private final List<TopicConfiguration> topics = new ArrayList<>();
    /**
     * Define the network for all containers.
     */
    @Getter
    private Network network;
    /**
     * Define the zookeeper container.
     */
    @Getter
    private ZookeeperContainer zookeeperContainer;
    /**
     * Dfine the kafka container
     */
    @Getter
    private KafkaContainer kafkaContainer;
    /**
     * Define the schema registry container.
     */
    @Getter
    private SchemaRegistryContainer schemaRegistryContainer;
    /**
     * Enable the schema registry container.
     */
    @Getter
    private boolean schemaRegistryEnabled;
    private String brokerServersSystemProperty = BROKER_SERVERS_SYSTEM_PROPERTY;
    private String schemaRegistrySystemProperty = SCHEMA_REGISTRY_SYSTEM_PROPERTY;
    /**
     * Register springboot properties in environment
     */
    private boolean registerSpringbootProperties = true;
    /**
     *
     */
    private String formatMessageVersion;


    /**
     *
     */
    public ConfluentKafkaContainer() {
        this(CONFLUENT_DEFAULT_VERSION);
    }

    /**
     * @param confluentVersion
     */
    public ConfluentKafkaContainer(final String confluentVersion) {
        this.confluentVersion = confluentVersion;
    }

    /**
     * Enable the schema registry.
     *
     * @param schemaRegistryEnabled
     * @return
     */
    public SELF withSchemaRegistry(boolean schemaRegistryEnabled) {
        this.schemaRegistryEnabled = schemaRegistryEnabled;
        return this.self();
    }

    /**
     * Register the spring boot properties.
     *
     * @param registerProperties
     * @return
     */
    public SELF withRegisterSpringbootProperties(boolean registerProperties) {
        this.registerSpringbootProperties = registerProperties;
        return this.self();
    }

    /**
     * @return
     */
    protected boolean isRegisterSpringbootProperties() {
        return this.registerSpringbootProperties;
    }

    /**
     * Set the network.
     *
     * @param network
     * @return
     */
    public SELF withNetwork(Network network) {
        this.network = network;
        return this.self();
    }

    /**
     * @param brokerServersSystemProperty
     * @return
     */
    public SELF withBrokerServersSystemProperty(String brokerServersSystemProperty) {
        this.brokerServersSystemProperty = brokerServersSystemProperty;
        return this.self();
    }

    /**
     * @param schemaRegistrySystemProperty
     * @return
     */
    public SELF withSchemaRegistrySystemProperty(String schemaRegistrySystemProperty) {
        this.schemaRegistrySystemProperty = schemaRegistrySystemProperty;
        return this.self();
    }

    /**
     * @param version
     * @return
     */
    public SELF withFormatMessageVersion(String version) {
        this.formatMessageVersion = version;
        return this.self();
    }

    /**
     * @param topicName
     * @param partitions
     * @param compact
     * @return
     */
    public SELF withTopic(String topicName, int partitions, boolean compact) {
        return withTopic(new TopicConfiguration(topicName, partitions, compact));
    }

    /**
     * @param topic
     * @return
     */
    public SELF withTopic(TopicConfiguration topic) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(topic.getName());
        this.topics.add(topic);
        return this.self();
    }

    /**
     * Get the local zookeeper url.
     *
     * @return
     */
    public String getZookeeperConnect() {
        return this.zookeeperContainer.getInternalURL();
    }

    /**
     * Get the zookeeper url.
     *
     * @return
     */
    public String getZookeeperServer() {
        return this.zookeeperContainer.getURL();
    }

    /**
     * Get the kafka url.
     *
     * @return
     */
    public String getBootstrapServers() {
        return this.kafkaContainer.getURL();
    }

    /**
     * @return
     */
    public String getSchemaRegistryServers() {
        if (!this.schemaRegistryEnabled) {
            throw new IllegalArgumentException("Schema registry not started");
        }
        return schemaRegistryContainer.getURL();
    }

    /**
     * Destroy all containers.
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        after();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        before();
    }

    /**
     * Start all containers.
     *
     * @throws Exception
     */
    @Override
    public void before() throws Exception {
        if (this.network == null) {
            withNetwork(Network.newNetwork());
        }

        zookeeperContainer = new ZookeeperContainer<>(this.confluentVersion)
                .withNetwork(network);
        zookeeperContainer.start();

        kafkaContainer = new KafkaContainer<>(this.confluentVersion)
                .withZookeeperHostname(getContainerHostname(zookeeperContainer))
                .withZookeeperPort(zookeeperContainer.getMappingPort())
                .withRegisterSpringbootProperties(this.registerSpringbootProperties)
                .withBrokerServersSystemProperty(this.brokerServersSystemProperty)
                .withFormatMessageVersion(this.formatMessageVersion)
                .withNetwork(network);
        kafkaContainer.start();
        // Create default topics
        if (!this.topics.isEmpty()) {
            createTopics(this.topics);
        }

        if (this.schemaRegistryEnabled) {
            schemaRegistryContainer = new SchemaRegistryContainer<>(this.confluentVersion)
                    .withZookeeperInternalURL(zookeeperContainer.getInternalURL())
                    .withBootstrapServersInternalURL(kafkaContainer.getInternalURL())
                    .withRegisterSpringbootProperties(this.registerSpringbootProperties)
                    .withSchemaRegistrySystemProperty(this.schemaRegistrySystemProperty)
                    .withNetwork(network);
            schemaRegistryContainer.start();
        }
    }

    /**
     * Stop and remove all containers.
     */
    @Override
    public void after() {
        if (schemaRegistryContainer != null && schemaRegistryContainer.isRunning()) {
            this.schemaRegistryContainer.stop();
        }
        if (kafkaContainer != null && kafkaContainer.isRunning()) {
            kafkaContainer.stop();
        }
        if (zookeeperContainer != null && zookeeperContainer.isRunning()) {
            zookeeperContainer.stop();
        }
    }

    @Override
    public SELF self() {
        return (SELF) this;
    }

    /**
     * @param topic
     */
    public void createTopic(TopicConfiguration topic) {
        createTopics(Collections.singletonList(topic));
    }

    /**
     * @param topics
     */
    public void createTopics(List<TopicConfiguration> topics) {
        if (!topics.isEmpty()) {
            final String zookeeper = this.getZookeeperConnect();
            final KafkaContainer kafkaContainer = this.getKafkaContainer();
            topics.forEach(topic -> {
                execCmd(kafkaContainer.getDockerClient(),
                        kafkaContainer.getContainerId(),
                        getCreateTopicCmd(topic.getName(), topic.getPartitions(), topic.isCompact(), zookeeper, 1));
            });
        }
    }

    /**
     * @param topicName
     * @param partitions
     * @param kafkaZookeeperConnect
     * @param brokersCount
     * @return
     */
    private String[] getCreateTopicCmd(String topicName, int partitions, boolean compact, String kafkaZookeeperConnect, int brokersCount) {
        String[] args = new String[]{
                "kafka-topics",
                "--create", "--topic", topicName,
                "--partitions", String.valueOf(partitions),
                "--replication-factor", String.valueOf(brokersCount),
                "--if-not-exists",
                "--zookeeper", kafkaZookeeperConnect
        };
        if (compact) {
            int orignalLength = args.length;
            args = Arrays.copyOf(args, orignalLength + 2);
            args[orignalLength] = "--config";
            args[orignalLength + 1] = "cleanup.policy=compact";
        }

        return args;
    }

}
