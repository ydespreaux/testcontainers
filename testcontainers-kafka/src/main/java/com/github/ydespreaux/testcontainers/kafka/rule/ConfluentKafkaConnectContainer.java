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


import com.github.ydespreaux.testcontainers.kafka.containers.KafkaConnectContainer;

import java.util.HashSet;
import java.util.Set;

/**
 * Define the environment for kafka connect container.
 * This class is used to start a zookeeper container, a kafka container, a schema registry container if it is enabled,
 * and a kafka connect container.
 *
 * @param <SELF>
 * @since 1.1.1
 */
public class ConfluentKafkaConnectContainer<SELF extends ConfluentKafkaConnectContainer<SELF>> extends ConfluentKafkaContainer<SELF> {

    /**
     *
     */
    private static final String REST_APP_SYSTEM_PROPERTY = "spring.kafka-connect.rest-app";

    private KafkaConnectContainer kafkaConnectContainer;

    private String groupId;
    private String configStorageTopic;
    private String offsetStorageTopic;
    private Integer offsetStoragePartition;
    private String statusStorageTopic;
    private Integer statusStoragePartition;
    private String offsetStorageFilename;
    private String keyConverter;
    private String valueConverter;
    private Set<String> plugins = new HashSet<>();
    private String restAppSystemProperty = REST_APP_SYSTEM_PROPERTY;


    /**
     *
     */
    public ConfluentKafkaConnectContainer() {
        super("4.1.0");
        this.withFormatMessageVersion("0.11.0");
    }

    /**
     * @param confluentVersion
     */
    public ConfluentKafkaConnectContainer(String confluentVersion) {
        super(confluentVersion);
    }

    /**
     * Start all containers.
     *
     * @throws Exception
     */
    @Override
    public void before() throws Exception {
        super.before();
        kafkaConnectContainer = new KafkaConnectContainer<>(
                this.getConfluentVersion(),
                this.getKafkaContainer().getInternalURL(),
                this.getSchemaRegistryContainer() != null ? this.getSchemaRegistryContainer().getInternalURL() : null)
                .withNetwork(getNetwork())

                .withGroupId(this.groupId)

                .withConfigStorageTopic(this.configStorageTopic)
                .withConfigStorageReplicationFactor(1)

                .withOffsetStorageTopic(this.offsetStorageTopic)
                .withOffsetStoragePartition(this.offsetStoragePartition)
                .withOffsetStorageReplicationFactor(1)

                .withStatusStorageTopic(this.statusStorageTopic)
                .withStatusStoragePartition(this.statusStoragePartition)
                .withStatusStorageReplicationFactor(1)

                .withOffsetStorageFilename(this.offsetStorageFilename)

                .withKeyConverter(this.keyConverter)
                .withValueConverter(this.valueConverter)

                .withPlugins(this.plugins)

                .withRegisterSpringbootProperties(isRegisterSpringbootProperties())
                .withRestAppSystemProperty(this.restAppSystemProperty);

        kafkaConnectContainer.start();
    }

    /**
     * Destroy all containers.
     */
    @Override
    public void after() {
        if (kafkaConnectContainer != null && kafkaConnectContainer.isRunning()) {
            kafkaConnectContainer.stop();
        }
        super.after();
    }

    /**
     * Get the url of the rest api.
     *
     * @return
     */
    public String getRestAppServers() {
        return this.kafkaConnectContainer.getURL();
    }

    /**
     * Set the group id.
     *
     * @param groupId
     * @return
     */
    public SELF withGroupId(String groupId) {
        if (groupId != null) {
            this.groupId = groupId;
        }
        return this.self();
    }

    /**
     * Set the topic's name for the offsets storage topic.
     *
     * @param topic
     * @return
     */
    public SELF withConfigStorageTopic(String topic) {
        if (topic != null) {
            this.configStorageTopic = topic;
        }
        return this.self();
    }

    /**
     * Set the topic's name for the offsets storage topic.
     *
     * @param topic
     * @return
     */
    public SELF withOffsetStorageTopic(String topic) {
        if (topic != null) {
            this.offsetStorageTopic = topic;
        }
        return this.self();
    }

    /**
     * Set the number of partitions for the offsets storage topic.
     *
     * @param partitions
     * @return
     */
    public SELF withOffsetStoragePartition(Integer partitions) {
        if (partitions != null) {
            this.offsetStoragePartition = partitions;
        }
        return this.self();
    }

    /**
     * Set the topic's name for the status storage topic.
     *
     * @param topic
     * @return
     */
    public SELF withStatusStorageTopic(String topic) {
        if (topic != null) {
            this.statusStorageTopic = topic;
        }
        return this.self();
    }

    /**
     * Set the number of partitions for the status storage topic.
     *
     * @param partitions
     * @return
     */
    public SELF withStatusStoragePartition(Integer partitions) {
        if (partitions != null) {
            this.statusStoragePartition = partitions;
        }
        return this.self();
    }

    /**
     * Set the storage file name.
     *
     * @param storageFilename
     * @return
     */
    public SELF withOffsetStorageFilename(String storageFilename) {
        if (storageFilename != null) {
            this.offsetStorageFilename = storageFilename;
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
            this.keyConverter = keyConverter;
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
            this.valueConverter = valueConverter;
        }
        return this.self();
    }

    /**
     * Set the plugins directory
     *
     * @param plugins
     * @return
     */
    public SELF withPlugins(String plugins) {
        if (plugins != null) {
            this.plugins.add(plugins);
        }
        return this.self();
    }

    /**
     * Set the property 's name for schema registry url.
     *
     * @param restAppSystemProperty
     * @return
     */
    public SELF withRestAppSystemProperty(String restAppSystemProperty) {
        this.restAppSystemProperty = restAppSystemProperty;
        return this.self();
    }
}
