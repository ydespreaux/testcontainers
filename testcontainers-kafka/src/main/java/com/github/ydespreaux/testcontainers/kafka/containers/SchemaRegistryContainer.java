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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Objects;
import java.util.UUID;

import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.containerLogsConsumer;
import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.getAvailableMappingPort;
import static java.lang.String.format;

/**
 * Schema registry container.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
public class SchemaRegistryContainer extends FixedHostPortGenericContainer<SchemaRegistryContainer> implements IContainer<SchemaRegistryContainer> {

    private static final String SECRETS_DIRECTORY = "/etc/schema-registry/secrets";

    private static final String SCHEMA_REGISTRY_DEFAULT_BASE_URL = "confluentinc/cp-schema-registry";

    private static final String ZOOKEEPER_URL_ENV = "SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL";
    private static final String BOOTSTRAP_SERVERS_URL_ENV = "SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS";

    private static final String SCHEMA_REGISTRY_SYSTEM_PROPERTY = "spring.kafka.properties.schema.registry.url";


    /**
     * Register springboot properties in environment
     */
    private boolean registerSpringbootProperties = true;

    /**
     * Spring boot properties for the schema registry url
     */
    private String schemaRegistrySystemProperty = SCHEMA_REGISTRY_SYSTEM_PROPERTY;

    /**
     * Schema registry mapping port.
     */
    @Getter
    private int mappingPort;

    private Certificates serverCertificates;
    /**
     * @param version
     */
    public SchemaRegistryContainer(String version) {
        this(version, getAvailableMappingPort());
    }

    /**
     * @param version
     * @param mappingPort
     */
    public SchemaRegistryContainer(String version, int mappingPort) {
        super(SCHEMA_REGISTRY_DEFAULT_BASE_URL + ":" + version);
        this.mappingPort = mappingPort;
        waitingFor(Wait.forHttp("/"));
    }

    /**
     * Configure the container
     */
    @Override
    protected void configure() {
        Objects.requireNonNull(this.getEnvMap().get(ZOOKEEPER_URL_ENV), "Zookeeper url must not be null !!!");
        this.withLogConsumer(containerLogsConsumer(log))
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                .withEnv("SCHEMA_REGISTRY_LISTENERS", format("http://0.0.0.0:%d", this.mappingPort))
                .withEnv("SCHEMA_REGISTRY_ACCESS_CONTROL_ALLOW_METHODS", "GET,POST,PUT,OPTIONS")
                .withEnv("SCHEMA_REGISTRY_ACCESS_CONTROL_ALLOW_ORIGIN", "*")
                .withExposedPorts(this.mappingPort)
                .withFixedExposedPort(this.mappingPort, this.mappingPort)
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("testcontainsers-schema-registry-" + UUID.randomUUID()));
        if (isSecured()) {
            this.withEnv("SCHEMA_REGISTRY_KAFKASTORE_SECURITY_PROTOCOL", "SSL")
                    .withEnv("SCHEMA_REGISTRY_KAFKASTORE_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM", "");
        }
    }

    public boolean isSecured() {
        return this.serverCertificates != null;
    }

    public SchemaRegistryContainer withServerCertificates(Certificates certificates) {
        if (certificates == null) {
            return this;
        }
        if (this.serverCertificates != null) {
            throw new IllegalArgumentException("Certificates already initialized");
        }
        this.serverCertificates = certificates;

        this.addFileSystemBind(certificates.getKeystorePath().toString(), SECRETS_DIRECTORY + '/' + certificates.getKeystorePath().getFileName(), BindMode.READ_ONLY);
        if (certificates.getTruststorePath() != null) {
            this.addFileSystemBind(certificates.getTruststorePath().toString(), SECRETS_DIRECTORY + '/' + certificates.getTruststorePath().getFileName(), BindMode.READ_ONLY);
        }

        withEnv("SCHEMA_REGISTRY_KAFKASTORE_SSL_KEYSTORE_LOCATION", SECRETS_DIRECTORY + '/' + certificates.getKeystorePath().getFileName());
        withEnv("SCHEMA_REGISTRY_KAFKASTORE_SSL_KEYSTORE_PASSWORD", certificates.getKeystorePassword());
        withEnv("SCHEMA_REGISTRY_KAFKASTORE_SSL_KEY_PASSWORD", certificates.getKeystorePassword());
        if (certificates.getTruststorePath() != null) {
            withEnv("SCHEMA_REGISTRY_KAFKASTORE_SSL_TRUSTSTORE_LOCATION", SECRETS_DIRECTORY + '/' + certificates.getTruststorePath().getFileName());
            withEnv("SCHEMA_REGISTRY_KAFKASTORE_SSL_TRUSTSTORE_PASSWORD", certificates.getTruststorePassword());
        }
        return this;
    }

    /**
     * Set the zookeeper url for local container.
     *
     * @param internalURL
     * @return
     */
    public SchemaRegistryContainer withZookeeperInternalURL(String internalURL) {
        Objects.requireNonNull(internalURL, "Zookeeper url must not be null !!!");
        withEnv(ZOOKEEPER_URL_ENV, internalURL);
        return this;
    }

    /**
     * Set bootstrap servers for local container.
     *
     * @param internalURL
     * @return
     */
    public SchemaRegistryContainer withBootstrapServersInternalURL(String internalURL) {
        Objects.requireNonNull(internalURL, "Bootstrap servers url must not be null !!!");
        withEnv(BOOTSTRAP_SERVERS_URL_ENV, internalURL);
        return this;
    }

    /**
     * @param schemaRegistrySystemProperty
     * @return
     */
    public SchemaRegistryContainer withSchemaRegistrySystemProperty(String schemaRegistrySystemProperty) {
        this.schemaRegistrySystemProperty = schemaRegistrySystemProperty;
        return this;
    }

    /**
     * Start the container.
     *
     * @throws Exception
     */
    @Override
    public void start() {
        super.start();
        if (registerSpringbootProperties()) {
            this.registerSchemaRegistryEnvironment();
        }
    }

    /**
     * Register the spring boot properties.
     */
    protected void registerSchemaRegistryEnvironment() {
        if (this.schemaRegistrySystemProperty != null) {
            System.setProperty(this.schemaRegistrySystemProperty, getURL());
        }
    }

    /**
     * @param registerProperties
     * @return
     */
    @Override
    public SchemaRegistryContainer withRegisterSpringbootProperties(boolean registerProperties) {
        this.registerSpringbootProperties = registerProperties;
        return this;
    }

    /**
     * @return
     */
    @Override
    public boolean registerSpringbootProperties() {
        return this.registerSpringbootProperties;
    }

    /**
     * Get the url.
     *
     * @return
     */
    @Override
    public String getURL() {
        return format("http://%s:%d", this.getContainerIpAddress(), this.getFirstMappedPort());
    }

    /**
     * Get the local url.
     *
     * @return
     */
    @Override
    public String getInternalURL() {
        return format("http://%s:%d", this.getNetworkAliases().get(0), this.mappingPort);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SchemaRegistryContainer)) return false;
        if (!super.equals(o)) return false;
        SchemaRegistryContainer that = (SchemaRegistryContainer) o;
        return getMappingPort() == that.getMappingPort();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getMappingPort());
    }
}
