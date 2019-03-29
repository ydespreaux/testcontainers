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

import com.github.ydespreaux.testcontainers.kafka.cmd.AclsAddCmd;
import com.github.ydespreaux.testcontainers.kafka.cmd.AclsOperation;
import com.github.ydespreaux.testcontainers.kafka.config.TopicConfiguration;
import com.github.ydespreaux.testcontainers.kafka.containers.KafkaContainer;
import com.github.ydespreaux.testcontainers.kafka.containers.SchemaRegistryContainer;
import com.github.ydespreaux.testcontainers.kafka.containers.ZookeeperContainer;
import com.github.ydespreaux.testcontainers.kafka.security.Certificates;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.rules.ExternalResource;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.Network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.getContainerHostname;


/**
 * This class is used to start a zookeeper container, a kafka container, and a schema registry container
 * if it is enabled.
 *
 * @param <S>
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
public class ConfluentKafkaContainer<S extends ConfluentKafkaContainer<S>> extends ExternalResource implements ConfluentContainer<S> {

    static final String CONFLUENT_DEFAULT_VERSION = "5.1.2";

    /**
     * Define the confluent version.
     */
    @Getter
    private final String confluentVersion;

    /**
     * Define the network for all containers.
     */
    @Getter
    private Network network;
    /**
     * Define the zookeeper container.
     */
    @Getter
    private final ZookeeperContainer zookeeperContainer;
    /**
     * Dfine the kafka container
     */
    @Getter
    private final KafkaContainer kafkaContainer;
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

    private String schemaRegistrySystemProperty;

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
        this.zookeeperContainer = new ZookeeperContainer(this.confluentVersion);
        this.kafkaContainer = new KafkaContainer(this.confluentVersion)
                .withRegisterSpringbootProperties(true);
    }

    public S self() {
        return (S) this;
    }

    /**
     * Enable the schema registry.
     *
     * @param schemaRegistryEnabled
     * @return
     */
    public S withSchemaRegistry(boolean schemaRegistryEnabled) {
        this.schemaRegistryEnabled = schemaRegistryEnabled;
        return self();
    }

    /**
     * Register the spring boot properties.
     *
     * @param registerProperties
     * @return
     */
    public S withRegisterSpringbootProperties(boolean registerProperties) {
        this.kafkaContainer.withRegisterSpringbootProperties(registerProperties);
        return self();
    }

    public S withKafkaServerCertificates(Certificates certificates) {
        this.kafkaContainer.withKafkaServerCertificates(certificates);
        return self();
    }

    public S withKafkaClientCertificates(Certificates certificates) {
        this.kafkaContainer.withKafkaClientCertificates(certificates);
        return self();
    }

    /**
     * @return
     */
    public Certificates getKafkaServerCertificates() {
        return this.kafkaContainer.getKafkaServerCertificates();
    }

    /**
     * @return
     */
    public Certificates getKafkaClientCertificates() {
        return this.kafkaContainer.getKafkaClientCertificates();
    }

    /**
     * @return
     */
    public boolean isSecured() {
        return this.kafkaContainer.isSecured();
    }

    /**
     * Set the network.
     *
     * @param network
     * @return
     */
    public S withNetwork(Network network) {
        this.network = network;
        return self();
    }

    public S withBrokerServersSystemProperty(String property) {
        this.kafkaContainer.withBrokerServersSystemProperty(property);
        return self();
    }

    public S withSchemaRegistrySystemProperty(String property) {
        this.schemaRegistrySystemProperty = property;
        return self();
    }

    public S withSecurityProtocolSystemProperty(String property) {
        this.kafkaContainer.withSecurityProtocolSystemProperty(property);
        return self();
    }

    public S withKeyPasswordSystemProperty(String property) {
        this.kafkaContainer.withKeyPasswordSystemProperty(property);
        return self();
    }

    public S withKeystoreLocationSystemProperty(String property) {
        this.kafkaContainer.withKeystoreLocationSystemProperty(property);
        return self();
    }

    public S withKeystorePasswordSystemProperty(String property) {
        this.kafkaContainer.withKeystorePasswordSystemProperty(property);
        return self();
    }

    public S withTruststoreLocationSystemProperty(String property) {
        this.kafkaContainer.withTruststoreLocationSystemProperty(property);
        return self();
    }

    public S withTruststorePasswordSystemProperty(String property) {
        this.kafkaContainer.withTruststorePasswordSystemProperty(property);
        return self();
    }

    public S withIdentificationAlgorithmSystemProperty(String property) {
        this.kafkaContainer.withIdentificationAlgorithmSystemProperty(property);
        return self();
    }

    /**
     *
     * @param version
     * @return
     */
    public S withFormatMessageVersion(String version) {
        this.kafkaContainer.withFormatMessageVersion(version);
        return self();
    }

    /**
     *
     * @param topicName
     * @param partitions
     * @param compact
     * @return
     */
    public S withTopic(String topicName, int partitions, boolean compact) {
        return withTopic(new TopicConfiguration(topicName, partitions, compact));
    }

    /**
     *
     * @param topic
     * @return
     */
    public S withTopic(TopicConfiguration topic) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(topic.getName());
        this.kafkaContainer.withTopics(Arrays.asList(topic));
        return self();
    }

    public S withAllAcls(String topic, String group) {
        return this.withAllAcls(this.kafkaContainer.getKafkaClientCertificates(), topic, group);
    }

    public S withAllAcls(Certificates certificates, String topic, String group) {
        return this.withAcls(certificates, AclsOperation.ALL, topic, group);
    }

    public S withWriteAcls(String topic) {
        return this.withWriteAcls(this.kafkaContainer.getKafkaClientCertificates(), topic);
    }

    public S withWriteAcls(Certificates certificates, String topic) {
        return this.withAcls(certificates, AclsOperation.WRITE, topic, null);
    }

    public S withDescribeAcls(String topic, String group) {
        return this.withDescribeAcls(this.kafkaContainer.getKafkaClientCertificates(), topic, group);
    }

    public S withDescribeAcls(Certificates certificates, String topic, String group) {
        return this.withAcls(certificates, AclsOperation.DESCRIBE, topic, group);
    }

    public S withReadAcls(String topic, String group) {
        return this.withReadAcls(this.kafkaContainer.getKafkaClientCertificates(), topic, group);
    }

    public S withReadAcls(Certificates certificates, String topic, String group) {
        return this.withAcls(certificates, AclsOperation.READ, topic, group);
    }

    public S withAcls(AclsOperation operation, String topic, String group) {
        return withAcls(this.kafkaContainer.getKafkaClientCertificates(), new AclsOperation[]{operation}, topic, group);
    }

    public S withAcls(Certificates certificates, AclsOperation operation, String topic, String group) {
        return withAcls(certificates, new AclsOperation[]{operation}, topic, group);
    }

    public S withAcls(AclsOperation[] operations, String topic, String group) {
        return withAcls(this.kafkaContainer.getKafkaClientCertificates(), operations, topic, group);
    }

    public S withAcls(Certificates certificates, AclsOperation[] operations, String topic, String group) {
        Objects.requireNonNull(certificates, "Client certificates are not initialized. Call the setClientCertificates method before.");
        List<AclsAddCmd> commands = new ArrayList<>(operations.length);
        for (AclsOperation operation : operations) {
            commands.add(new AclsAddCmd(certificates)
                    .operation(operation)
                    .topic(topic)
                    .group(group));
        }
        this.kafkaContainer.withAcls(commands);
        return self();
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
        start();
    }

    /**
     * Start all containers.
     *
     * @throws Exception
     */
    @Override
    public void before() {
        start();
    }

    /**
     * Stop and remove all containers.
     */
    @Override
    public void after() {
        stop();
    }

    public boolean isRunning() {
        return this.kafkaContainer.isRunning();
    }

    @Override
    public void start() {
        if (this.network == null) {
            withNetwork(Network.newNetwork());
        }

        zookeeperContainer.withNetwork(network);
        zookeeperContainer.start();

        kafkaContainer.withZookeeperHostname(getContainerHostname(zookeeperContainer))
                .withZookeeperPort(zookeeperContainer.getMappingPort())
                .withNetwork(network);
        kafkaContainer.start();

        if (this.schemaRegistryEnabled) {
            schemaRegistryContainer = new SchemaRegistryContainer(this.confluentVersion)
                    .withRegisterSpringbootProperties(kafkaContainer.registerSpringbootProperties())
                    .withServerCertificates(kafkaContainer.getKafkaServerCertificates())
                    .withZookeeperInternalURL(zookeeperContainer.getInternalURL())
                    .withBootstrapServersInternalURL(kafkaContainer.getInternalURL())
                    .withNetwork(network);
            if (StringUtils.hasText(this.schemaRegistrySystemProperty)) {
                schemaRegistryContainer.withSchemaRegistrySystemProperty(this.schemaRegistrySystemProperty);
            }
            schemaRegistryContainer.start();
        }
    }

    @Override
    public void stop() {
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
}
