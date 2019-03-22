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
import com.github.ydespreaux.testcontainers.kafka.security.Certificates;

/**
 * Define the environment for kafka connect container.
 * This class is used to start a zookeeper container, a kafka container, a schema registry container if it is enabled,
 * and a kafka connect container.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class ConfluentKafkaConnectContainer extends ConfluentKafkaContainer<ConfluentKafkaConnectContainer> {

    private final KafkaConnectContainer kafkaConnectContainer;

    private Certificates kafkaConnectCertificates;


    /**
     *
     */
    public ConfluentKafkaConnectContainer() {
        this(CONFLUENT_DEFAULT_VERSION);
    }

    /**
     * @param confluentVersion
     */
    public ConfluentKafkaConnectContainer(String confluentVersion) {
        super(confluentVersion);
        this.kafkaConnectContainer = new KafkaConnectContainer(confluentVersion);
    }

    /**
     * Start all containers.
     *
     * @throws Exception
     */
    @Override
    public void before() throws Exception {
        super.before();
        kafkaConnectContainer
                .withNetwork(getNetwork())
                .withBrokersServerUrl(this.getKafkaContainer().getInternalURL())
                .withServerCertificates(this.kafkaConnectCertificates == null ? this.getKafkaServerCertificates() : this.kafkaConnectCertificates);
        if (this.isSchemaRegistryEnabled()) {
            kafkaConnectContainer.withSchemaRegistryUrl(this.getSchemaRegistryContainer().getInternalURL());

        }
        kafkaConnectContainer.start();
    }

    /**
     * Register the spring boot properties.
     *
     * @param registerProperties
     * @return
     */
    @Override
    public ConfluentKafkaConnectContainer withRegisterSpringbootProperties(boolean registerProperties) {
        super.withRegisterSpringbootProperties(registerProperties);
        this.kafkaConnectContainer.withRegisterSpringbootProperties(registerProperties);
        return this;
    }

    /**
     * @param certificates
     * @return
     */
    public ConfluentKafkaConnectContainer withKafkaConnectCertificates(Certificates certificates) {
        this.kafkaConnectCertificates = certificates;
        return this;
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
    public ConfluentKafkaConnectContainer withGroupId(String groupId) {
        if (groupId != null) {
            this.kafkaConnectContainer.withGroupId(groupId);
        }
        return this;
    }

    /**
     * Set the topic's name for the offsets storage topic.
     * @param topic
     * @return
     */
    public ConfluentKafkaConnectContainer withConfigStorageTopic(String topic) {
        if (topic != null) {
            this.kafkaConnectContainer.withConfigStorageTopic(topic);
        }
        return this;
    }

    /**
     * Set the topic's name for the offsets storage topic.
     *
     * @param topic
     * @return
     */
    public ConfluentKafkaConnectContainer withOffsetStorageTopic(String topic) {
        if (topic != null) {
            this.kafkaConnectContainer.withOffsetStorageTopic(topic);
        }
        return this;
    }

    /**
     * Set the number of partitions for the offsets storage topic.
     * @param partitions
     * @return
     */
    public ConfluentKafkaConnectContainer withOffsetStoragePartition(Integer partitions) {
        if (partitions != null) {
            this.kafkaConnectContainer.withOffsetStoragePartition(partitions);
        }
        return this;
    }

    /**
     * Set the topic's name for the status storage topic.
     * @param topic
     * @return
     */
    public ConfluentKafkaConnectContainer withStatusStorageTopic(String topic) {
        if (topic != null) {
            this.kafkaConnectContainer.withStatusStorageTopic(topic);
        }
        return this;
    }

    /**
     * Set the number of partitions for the status storage topic.
     *
     * @param partitions
     * @return
     */
    public ConfluentKafkaConnectContainer withStatusStoragePartition(Integer partitions) {
        if (partitions != null) {
            this.kafkaConnectContainer.withStatusStoragePartition(partitions);
        }
        return this;
    }

    /**
     * Set the storage file name.
     *
     * @param storageFilename
     * @return
     */
    public ConfluentKafkaConnectContainer withOffsetStorageFilename(String storageFilename) {
        if (storageFilename != null) {
            this.kafkaConnectContainer.withOffsetStorageFilename(storageFilename);
        }
        return this;
    }

    /**
     * Set the key converter.
     *
     * @param keyConverter
     * @return
     */
    public ConfluentKafkaConnectContainer withKeyConverter(String keyConverter) {
        if (keyConverter != null) {
            this.kafkaConnectContainer.withKeyConverter(keyConverter);
        }
        return this;
    }

    /**
     * Set the value converter.
     *
     * @param valueConverter
     * @return
     */
    public ConfluentKafkaConnectContainer withValueConverter(String valueConverter) {
        if (valueConverter != null) {
            this.kafkaConnectContainer.withValueConverter(valueConverter);
        }
        return this;
    }

    /**
     * Set the plugins directory
     *
     * @param plugins
     * @return
     */
    public ConfluentKafkaConnectContainer withPlugins(String plugins) {
        if (plugins != null) {
            this.kafkaConnectContainer.withPlugins(plugins);
        }
        return this;
    }

    /**
     * Set the property 's name for schema registry url.
     *
     * @param restAppSystemProperty
     * @return
     */
    public ConfluentKafkaConnectContainer withRestAppSystemProperty(String restAppSystemProperty) {
        this.kafkaConnectContainer.withRestAppSystemProperty(restAppSystemProperty);
        return this;
    }
}
