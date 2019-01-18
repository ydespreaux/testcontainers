package com.github.ydespreaux.testcontainers.kafka.containers;

import com.github.dockerjava.api.model.Link;
import com.github.ydespreaux.testcontainers.common.IContainer;
import com.github.ydespreaux.testcontainers.common.checks.AbstractCommandWaitStrategy;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.FixedHostPortGenericContainer;

import java.util.*;

import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.containerLogsConsumer;
import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.getAvailableMappingPort;
import static java.lang.String.format;

/**
 * Kafka container.
 *
 * @param <SELF>
 * @since 1.0.0
 */
@Slf4j
public class KafkaContainer<SELF extends KafkaContainer<SELF>> extends FixedHostPortGenericContainer<SELF> implements IContainer<SELF> {

    private static final String KAFKA_DEFAULT_BASE_URL = "confluentinc/cp-kafka";

    private static final String EXTERNAL_BROKERS_SERVERS = "BROKER://localhost:%d";
    private static final String INTERNAL_BROKERS_SERVERS = "PLAINTEXT://%s:9092";

    private static final Collection<String> FORMATS_VERSION = Collections.unmodifiableList(Arrays.asList("0.10.0", "0.10.1", "0.10.2", "0.11.0"));

    /**
     * Mapping port value.
     */
    private final int brokersMappingPort;

    /**
     * Register springboot properties in environment
     */
    private boolean registerSpringbootProperties = true;

    /**
     *
     */
    private String brokerServersSystemProperty;

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
    }

    /**
     * Configure the container.
     */
    @Override
    protected void configure() {
        Objects.requireNonNull(this.getNetwork(), "Network attribut must not be null !!");
        this.withLogConsumer(containerLogsConsumer(log))
                .withEnv("KAFKA_ADVERTISED_LISTENERS",
                        format(EXTERNAL_BROKERS_SERVERS, this.brokersMappingPort) + "," +
                                format(INTERNAL_BROKERS_SERVERS, this.getNetworkAliases().get(0)))
                .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "BROKER:PLAINTEXT,PLAINTEXT:PLAINTEXT")
                .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "PLAINTEXT")
                .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", String.valueOf(1))
                .withExposedPorts(this.brokersMappingPort)
                .withFixedExposedPort(this.brokersMappingPort, this.brokersMappingPort)
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("testcontainsers-kafka-" + UUID.randomUUID()))
                .waitingFor(new KafkaStatusCheck());
    }

    /**
     * start the container.
     */
    @Override
    public void start() {
        super.start();
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
    public SELF withZookeeperPort(Integer port) {
        withEnv("KAFKA_ZOOKEEPER_CONNECT", format("zookeeper:%d", port));
        return this.self();
    }

    /**
     * Set of the message compatibility version.
     *
     * @param version
     * @return
     */
    public SELF withFormatMessageVersion(String version) {
        if (version != null) {
            checkFormatMessageVersion(version);
            withEnv("KAFKA_INTER_BROKER_PROTOCOL_VERSION", version);
            withEnv("KAFKA_LOG_MESSAGE_FORMAT_VERSION", version);
        }
        return this.self();
    }

    /**
     * @param version
     */
    private void checkFormatMessageVersion(String version) {
        if (!FORMATS_VERSION.contains(version)) {
            throw new IllegalArgumentException(format("Illegal message format version : %s", version));
        }
    }

    /**
     * @param zookeeperHostname
     * @return
     */
    public SELF withZookeeperHostname(String zookeeperHostname) {
        if (zookeeperHostname != null) {
            withCreateContainerCmdModifier(cmd -> cmd.withLinks(new Link(zookeeperHostname, "zookeeper")));
        }
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
     * @param registerProperties
     * @return
     */
    @Override
    public SELF withRegisterSpringbootProperties(boolean registerProperties) {
        this.registerSpringbootProperties = registerProperties;
        return this.self();
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
        return format(EXTERNAL_BROKERS_SERVERS, this.getFirstMappedPort());
    }

    /**
     * Get the local url.
     *
     * @return
     */
    @Override
    public String getInternalURL() {
        return format(INTERNAL_BROKERS_SERVERS, this.getNetworkAliases().get(0));
    }

    /**
     * @return
     * @deprecated use getInternalURL()
     */
    @Deprecated
    public String getLocalURL() {
        return getInternalURL();
    }

    /**
     * Register spring boot properties.
     */
    protected void registerKafkaEnvironment() {
        if (this.brokerServersSystemProperty != null) {
            System.setProperty(this.brokerServersSystemProperty, getURL());
        }
    }

    /**
     * Kafka status check strategy.
     */
    private final class KafkaStatusCheck extends AbstractCommandWaitStrategy {

        private static final String MIN_BROKERS_COUNT = "1";
        private static final String TIMEOUT_IN_SEC = "30";

        public KafkaStatusCheck() {
            super(KafkaContainer.this);
        }

        @Override
        public String[] getCheckCommand() {
            return new String[]{
                    "cub",
                    "kafka-ready",
                    MIN_BROKERS_COUNT,
                    TIMEOUT_IN_SEC,
                    "-b",
                    getURL()
            };
        }

    }
}
