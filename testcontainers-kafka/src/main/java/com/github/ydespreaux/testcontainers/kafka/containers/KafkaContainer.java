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

import com.github.dockerjava.api.model.Link;
import com.github.ydespreaux.testcontainers.common.IContainer;
import com.github.ydespreaux.testcontainers.common.checks.AbstractCommandWaitStrategy;
import com.github.ydespreaux.testcontainers.common.cmd.Command;
import com.github.ydespreaux.testcontainers.kafka.cmd.AclsAddCmd;
import com.github.ydespreaux.testcontainers.kafka.cmd.AclsOperation;
import com.github.ydespreaux.testcontainers.kafka.cmd.KafkaReadyCmd;
import com.github.ydespreaux.testcontainers.kafka.cmd.TopicCreateCommand;
import com.github.ydespreaux.testcontainers.kafka.config.TopicConfiguration;
import com.github.ydespreaux.testcontainers.kafka.security.CertificateUtils;
import com.github.ydespreaux.testcontainers.kafka.security.Certificates;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.FixedHostPortGenericContainer;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.containerLogsConsumer;
import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.getAvailableMappingPort;
import static java.lang.String.format;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Kafka container.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
public class KafkaContainer extends FixedHostPortGenericContainer<KafkaContainer> implements IContainer<KafkaContainer> {

    private static final String SECRETS_DIRECTORY = "/etc/kafka/secrets";
    private static final Command<KafkaContainer> healthCmd = new KafkaReadyCmd(10);

    private static final String KAFKA_DEFAULT_BASE_URL = "confluentinc/cp-kafka";

    private static final String EXTERNAL_BROKERS_SERVERS = "BROKER://localhost:%d";
    private static final String INTERNAL_BROKERS_SERVERS = "PLAINTEXT://%s:9092";
    private static final String SSL_INTERNAL_BROKERS_SERVERS = "SSL://%s:9093";
    private static final String KEYSTORE_CREDENTIALS_FILENAME = "ks_credentials";
    private static final String TRUSTSTORE_CREDENTIALS_FILENAME = "ts_credentials";

    private static final Collection<String> FORMATS_VERSION = Collections.unmodifiableList(Arrays.asList("0.10.0", "0.10.1", "0.10.2", "0.11.0", "1.0.0", "1.1.0", "2.0.0"));

    /**
     *
     */
    private static final String BROKER_SERVERS_SYSTEM_PROPERTY = "spring.kafka.bootstrap-servers";
    private static final String SECURITY_PROTOCOL_SYSTEM_PROPERTY = "spring.kafka.security.protocol";
    private static final String KEY_PASSWORD_SYSTEM_PROPERTY = "spring.kafka.ssl.key-password";
    private static final String KEYSTORE_LOCATION_SYSTEM_PROPERTY = "spring.kafka.ssl.key-store-location";
    private static final String KEYSTORE_PASSWORD_SYSTEM_PROPERTY = "spring.kafka.ssl.key-store-password";
    private static final String TRUSTSTORE_LOCATION_SYSTEM_PROPERTY = "spring.kafka.ssl.trust-store-location";
    private static final String TRUSTSTORE_PASSWORD_SYSTEM_PROPERTY = "spring.kafka.ssl.trust-store-password";
    private static final String IDENTIFICATION_ALGORITHM_SYSTEM_PROPERTY = "spring.kafka.properties.ssl.endpoint.identification.algorithm";

    /**
     * Mapping port value.
     */
    private final int brokersMappingPort;

    /**
     * Register springboot properties in environment
     */
    private boolean registerSpringbootProperties = true;
    /**
     * Default topics list
     */
    private final List<TopicCreateCommand> topicCmds = new ArrayList<>();

    /**
     *
     */
    private final Map<String, String> systemPropertyMap = new HashMap<String, String>() {{
        put(BROKER_SERVERS_SYSTEM_PROPERTY, BROKER_SERVERS_SYSTEM_PROPERTY);
        put(SECURITY_PROTOCOL_SYSTEM_PROPERTY, SECURITY_PROTOCOL_SYSTEM_PROPERTY);
        put(KEY_PASSWORD_SYSTEM_PROPERTY, KEY_PASSWORD_SYSTEM_PROPERTY);
        put(KEYSTORE_LOCATION_SYSTEM_PROPERTY, KEYSTORE_LOCATION_SYSTEM_PROPERTY);
        put(KEYSTORE_PASSWORD_SYSTEM_PROPERTY, KEYSTORE_PASSWORD_SYSTEM_PROPERTY);
        put(TRUSTSTORE_LOCATION_SYSTEM_PROPERTY, TRUSTSTORE_LOCATION_SYSTEM_PROPERTY);
        put(TRUSTSTORE_PASSWORD_SYSTEM_PROPERTY, TRUSTSTORE_PASSWORD_SYSTEM_PROPERTY);
        put(IDENTIFICATION_ALGORITHM_SYSTEM_PROPERTY, IDENTIFICATION_ALGORITHM_SYSTEM_PROPERTY);
    }};
    private final List<AclsAddCmd> aclsCommands = new ArrayList<>();
    @Getter
    private Certificates kafkaServerCertificates;
    @Getter
    private Certificates kafkaClientCertificates;
    private AclsAddCmd administratorAcls;

    /**
     * @param version
     */
    public KafkaContainer(String version) {
        this(version, getAvailableMappingPort());
    }

    /**
     * @param version
     * @param brokersMappingPort
     */
    public KafkaContainer(String version, int brokersMappingPort) {
        super(KAFKA_DEFAULT_BASE_URL + ":" + version);
        this.brokersMappingPort = brokersMappingPort;
        this.withLogConsumer(containerLogsConsumer(log));
        this.waitingFor(new AbstractCommandWaitStrategy(this) {

            @Override
            public List<Command> getCheckCommands() {
                if (isSecured()) {
                    return Arrays.asList(
                            administratorAcls,
                            healthCmd
                    );
                } else {
                    return Arrays.asList(
                            healthCmd
                    );
                }
            }
        }).withStartupTimeout(Duration.ofSeconds(120));
    }

    /**
     * Configure the container.
     */
    @Override
    protected void configure() {
        Objects.requireNonNull(this.getNetwork(), "Network attribut must not be null !!");
        if (isSecured()) {
            this.withEnv("KAFKA_ADVERTISED_LISTENERS",
                    format(EXTERNAL_BROKERS_SERVERS, this.brokersMappingPort) + "," +
                            format(INTERNAL_BROKERS_SERVERS, this.getNetworkAliases().get(0)) + "," +
                            format(SSL_INTERNAL_BROKERS_SERVERS, this.getNetworkAliases().get(0)))
                    .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "BROKER:SSL,SSL:SSL,PLAINTEXT:PLAINTEXT")
                    .withEnv("KAFKA_SECURITY_INTER_BROKER_PROTOCOL", "SSL")
                    .withEnv("KAFKA_SSL_CLIENT_AUTH", "requested")
                    .withEnv("KAFKA_AUTHORIZER_CLASS_NAME", "kafka.security.auth.SimpleAclAuthorizer")
                    .withEnv("KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM", "")
                    .withEnv("KAFKA_LOG4J_LOGGERS", "kafka.authorizer.logger=DEBUG,kafka.authorizer=DEBUG");
        } else {
            this.withEnv("KAFKA_ADVERTISED_LISTENERS",
                    format(EXTERNAL_BROKERS_SERVERS, this.brokersMappingPort) + "," +
                            format(INTERNAL_BROKERS_SERVERS, this.getNetworkAliases().get(0)))
                    .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "BROKER:PLAINTEXT,PLAINTEXT:PLAINTEXT")
                    .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "PLAINTEXT");
        }

        this.withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", String.valueOf(1))
                .withEnv("KAFKA_CONFLUENT_SUPPORT_METRICS_ENABLE", "false")
                .withExposedPorts(this.brokersMappingPort)
                .withFixedExposedPort(this.brokersMappingPort, this.brokersMappingPort)
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("testcontainsers-kafka-" + UUID.randomUUID()));
    }

    public boolean isSecured() {
        return this.kafkaServerCertificates != null;
    }

    /**
     * Get the url.
     *
     * @return
     */
    @Override
    public String getURL() {
        return format(EXTERNAL_BROKERS_SERVERS, this.getFirstMappedPort());
    }

    /**
     * Get the local url.
     *
     * @return
     */
    @Override
    public String getInternalURL() {
        return format(isSecured() ? SSL_INTERNAL_BROKERS_SERVERS : INTERNAL_BROKERS_SERVERS, this.getNetworkAliases().get(0));
    }

    /**
     * start the container.
     */
    @Override
    public void start() {
        super.start();
        // Create default topics
        if (!isEmpty(this.topicCmds)) {
            if (log.isInfoEnabled()) {
                log.info("Start of topics creation...");
            }
            executeCommands(this.topicCmds);
            if (log.isInfoEnabled()) {
                log.info("End of topics creation");
            }
        }
        if (isSecured() && !isEmpty(this.aclsCommands)) {
            if (log.isInfoEnabled()) {
                log.info("Start of acls creation...");
            }
            // Create Acls
            executeCommands(this.aclsCommands);
            if (log.isInfoEnabled()) {
                log.info("End of acls creation");
            }
        }
        if (registerSpringbootProperties()) {
            this.registerKafkaEnvironment();
        }
    }


    /**
     * Set the zookeeper port.
     *
     * @param port
     * @return
     */
    public KafkaContainer withZookeeperPort(Integer port) {
        withEnv("KAFKA_ZOOKEEPER_CONNECT", format("zookeeper:%d", port));
        return this;
    }

    /**
     * Set of the message compatibility version.
     *
     * @param version
     * @return
     */
    public KafkaContainer withFormatMessageVersion(String version) {
        if (version != null) {
            checkFormatMessageVersion(version);
            withEnv("KAFKA_INTER_BROKER_PROTOCOL_VERSION", version);
            withEnv("KAFKA_LOG_MESSAGE_FORMAT_VERSION", version);
        }
        return this;
    }

    /**
     *
     * @param version
     */
    private void checkFormatMessageVersion(String version) {
        if (!FORMATS_VERSION.contains(version)) {
            throw new IllegalArgumentException(format("Illegal message format version : %s", version));
        }
    }

    /**
     *
     * @param zookeeperHostname
     * @return
     */
    public KafkaContainer withZookeeperHostname(String zookeeperHostname) {
        if (zookeeperHostname != null) {
            withCreateContainerCmdModifier(cmd -> cmd.withLinks(new Link(zookeeperHostname, "zookeeper")));
        }
        return this;
    }

    /**
     * @param brokerServersSystemProperty
     * @return
     */
    public KafkaContainer withBrokerServersSystemProperty(String brokerServersSystemProperty) {
        this.systemPropertyMap.put(BROKER_SERVERS_SYSTEM_PROPERTY, brokerServersSystemProperty);
        return this;
    }

    public KafkaContainer withSecurityProtocolSystemProperty(String securityProtocolSystemProperty) {
        this.systemPropertyMap.put(BROKER_SERVERS_SYSTEM_PROPERTY, securityProtocolSystemProperty);
        return this;
    }

    public KafkaContainer withKeyPasswordSystemProperty(String keyPasswordSystemProperty) {
        this.systemPropertyMap.put(KEY_PASSWORD_SYSTEM_PROPERTY, keyPasswordSystemProperty);
        return this;
    }

    public KafkaContainer withKeystoreLocationSystemProperty(String keystoreLocationSystemProperty) {
        this.systemPropertyMap.put(KEYSTORE_LOCATION_SYSTEM_PROPERTY, keystoreLocationSystemProperty);
        return this;
    }

    public KafkaContainer withKeystorePasswordSystemProperty(String keystorePasswordSystemProperty) {
        this.systemPropertyMap.put(KEYSTORE_PASSWORD_SYSTEM_PROPERTY, keystorePasswordSystemProperty);
        return this;
    }

    public KafkaContainer withTruststoreLocationSystemProperty(String truststoreLocationSystemProperty) {
        this.systemPropertyMap.put(TRUSTSTORE_LOCATION_SYSTEM_PROPERTY, truststoreLocationSystemProperty);
        return this;
    }

    public KafkaContainer withTruststorePasswordSystemProperty(String truststorePasswordSystemProperty) {
        this.systemPropertyMap.put(TRUSTSTORE_PASSWORD_SYSTEM_PROPERTY, truststorePasswordSystemProperty);
        return this;
    }

    public KafkaContainer withIdentificationAlgorithmSystemProperty(String identificationAlgorithmSystemProperty) {
        this.systemPropertyMap.put(IDENTIFICATION_ALGORITHM_SYSTEM_PROPERTY, identificationAlgorithmSystemProperty);
        return this;
    }

    /**
     * @param registerProperties
     * @return
     */
    @Override
    public KafkaContainer withRegisterSpringbootProperties(boolean registerProperties) {
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
     * @deprecated use getInternalURL()
     * @return
     */
    @Deprecated
    public String getLocalURL() {
        return getInternalURL();
    }

    /**
     * Register spring boot properties.
     */
    protected void registerKafkaEnvironment() {
        System.setProperty(this.systemPropertyMap.get(BROKER_SERVERS_SYSTEM_PROPERTY), getURL());
        if (isSecured()) {
            if (this.kafkaClientCertificates == null) {
                if (log.isWarnEnabled()) {
                    log.warn("SSL properties not set in system properties. The client certificates is not defined.");
                }
            } else {
                System.setProperty(this.systemPropertyMap.get(SECURITY_PROTOCOL_SYSTEM_PROPERTY), "SSL");
                System.setProperty(this.systemPropertyMap.get(KEY_PASSWORD_SYSTEM_PROPERTY), this.kafkaClientCertificates.getKeystorePassword());
                System.setProperty(this.systemPropertyMap.get(KEYSTORE_LOCATION_SYSTEM_PROPERTY), "file:" + this.kafkaClientCertificates.getKeystorePath());
                System.setProperty(this.systemPropertyMap.get(KEYSTORE_PASSWORD_SYSTEM_PROPERTY), this.kafkaClientCertificates.getKeystorePassword());
                if (this.kafkaClientCertificates.getTruststorePath() != null) {
                    System.setProperty(this.systemPropertyMap.get(TRUSTSTORE_LOCATION_SYSTEM_PROPERTY), "file:" + this.kafkaClientCertificates.getTruststorePath());
                    System.setProperty(this.systemPropertyMap.get(TRUSTSTORE_PASSWORD_SYSTEM_PROPERTY), this.kafkaClientCertificates.getTruststorePassword());
                }
                System.setProperty(this.systemPropertyMap.get(IDENTIFICATION_ALGORITHM_SYSTEM_PROPERTY), "");
            }
        }
    }

    /**
     * @param certificates
     * @return
     */
    public KafkaContainer withKafkaServerCertificates(Certificates certificates) {
        if (certificates == null) {
            return this;
        }
        if (this.kafkaServerCertificates != null) {
            throw new IllegalArgumentException("Certificates is already initialized.");
        }
        this.kafkaServerCertificates = certificates;
        this.addFileSystemBind(certificates.getKeystorePath().toString(), SECRETS_DIRECTORY + '/' + certificates.getKeystorePath().getFileName(), BindMode.READ_ONLY);

        try {
            this.addFileSystemBind(CertificateUtils.generateCredentials(certificates.getKeystorePassword()).toString(), SECRETS_DIRECTORY + "/" + KEYSTORE_CREDENTIALS_FILENAME, BindMode.READ_ONLY);
        } catch (IOException e) {
            throw new ContainerLaunchException("Generating credentials failed: ", e);
        }
        if (certificates.getTruststorePath() != null) {
            this.addFileSystemBind(certificates.getTruststorePath().toString(), SECRETS_DIRECTORY + '/' + certificates.getTruststorePath().getFileName(), BindMode.READ_ONLY);
            try {
                this.addFileSystemBind(CertificateUtils.generateCredentials(certificates.getTruststorePassword()).toString(), SECRETS_DIRECTORY + "/" + TRUSTSTORE_CREDENTIALS_FILENAME, BindMode.READ_ONLY);
            } catch (IOException e) {
                throw new ContainerLaunchException("Generating credentials failed: ", e);
            }
        }
        this.administratorAcls = new AclsAddCmd(certificates)
                .operation(AclsOperation.ALL)
                .topic("*")
                .group("*")
                .cluster("kafka-cluster");
        withEnv("KAFKA_SECURITY_PROTOCOL", "SSL");
        withEnv("KAFKA_SSL_KEYSTORE_FILENAME", certificates.getKeystorePath().getFileName().toString());
        withEnv("KAFKA_SSL_KEYSTORE_CREDENTIALS", KEYSTORE_CREDENTIALS_FILENAME);
        withEnv("KAFKA_SSL_KEY_CREDENTIALS", KEYSTORE_CREDENTIALS_FILENAME);
        if (certificates.getTruststorePath() != null) {
            withEnv("KAFKA_SSL_TRUSTSTORE_FILENAME", certificates.getTruststorePath().getFileName().toString());
            withEnv("KAFKA_SSL_TRUSTSTORE_CREDENTIALS", TRUSTSTORE_CREDENTIALS_FILENAME);
        }
        return this;
    }

    /**
     * @param certificates
     * @return
     */
    public KafkaContainer withKafkaClientCertificates(Certificates certificates) {
        this.kafkaClientCertificates = certificates;
        return this;
    }

    /**
     * @param topics
     * @return
     */
    public KafkaContainer withTopics(List<TopicConfiguration> topics) {
        if (isEmpty(topics)) {
            return this;
        }
        List<TopicCreateCommand> cmds = topics.stream()
                .map(TopicCreateCommand::new)
                .collect(Collectors.toList());
        if (this.isRunning()) {
            this.executeCommands(cmds);
        } else {
            this.topicCmds.addAll(cmds);
        }
        return this;
    }

    public KafkaContainer withAcls(List<AclsAddCmd> commands) {
        if (this.isRunning()) {
            this.executeCommands(commands);
        } else {
            this.aclsCommands.addAll(commands);
        }
        return self();
    }

    /**
     * @param cmds
     */
    private void executeCommands(List<? extends Command> cmds) {
        if (!isEmpty(cmds)) {
            cmds.forEach(cmd -> {
                cmd.execute(this);
                if (log.isInfoEnabled()) {
                    log.info("Command executed : {}", cmd.toString());
                }
            });
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KafkaContainer)) return false;
        if (!super.equals(o)) return false;
        KafkaContainer that = (KafkaContainer) o;
        return brokersMappingPort == that.brokersMappingPort;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), brokersMappingPort);
    }
}
