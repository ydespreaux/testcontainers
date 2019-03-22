package fr.laposte.an.testcontainers.kafka.rule;


import fr.laposte.an.testcontainers.commons.cmd.Command;
import fr.laposte.an.testcontainers.kafka.Certificates;
import fr.laposte.an.testcontainers.kafka.cmd.AclsListCmd;
import fr.laposte.an.testcontainers.kafka.cmd.AclsOperation;
import fr.laposte.an.testcontainers.kafka.config.TopicConfiguration;
import fr.laposte.an.testcontainers.kafka.containers.KafkaContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@RunWith(SpringRunner.class)
public class ITConfluentSecureKafkaContainerTest {


    @ClassRule
    public static final ConfluentKafkaContainer container = new ConfluentKafkaContainer()
        .withKafkaServerCertificates(Certificates.kafkaServerCertificates)
        .withKafkaClientCertificates(Certificates.kafkaClientCertificates)
        .withSchemaRegistry(true);

    @Test
    public void containerEnvironment() {
        assertThat(container.isRunning(), is(true));
        assertThat(container.getBootstrapServers(), is(notNullValue()));
        assertThat(container.getZookeeperServer(), is(notNullValue()));
        assertThat(container.getSchemaRegistryServers(), is(notNullValue()));

        assertThat(System.getProperty("spring.kafka.bootstrap-servers"), is(equalTo(container.getBootstrapServers())));
        assertThat(System.getProperty("spring.kafka.security.protocol"), is(equalTo("SSL")));
        assertThat(System.getProperty("spring.kafka.ssl.key-password"), is(equalTo(Certificates.kafkaClientCertificates.getKeystorePassword())));
        assertThat(System.getProperty("spring.kafka.ssl.key-store-location"), is(equalTo("file:" + Certificates.kafkaClientCertificates.getKeystorePath().toString())));
        assertThat(System.getProperty("spring.kafka.ssl.key-store-password"), is(equalTo(Certificates.kafkaClientCertificates.getKeystorePassword())));
        assertThat(System.getProperty("spring.kafka.ssl.trust-store-location"), is(equalTo("file:" + Certificates.kafkaClientCertificates.getTruststorePath().toString())));
        assertThat(System.getProperty("spring.kafka.ssl.trust-store-password"), is(equalTo(Certificates.kafkaClientCertificates.getTruststorePassword())));
        assertThat(System.getProperty("spring.kafka.properties.ssl.endpoint.identification.algorithm"), is(equalTo("")));
        assertThat(System.getProperty("spring.kafka.properties.schema.registry.url"), is(equalTo(container.getSchemaRegistryServers())));

        Command<KafkaContainer> aclsCommand = new AclsListCmd("kafka-cluster", AclsListCmd.ResourceAcls.CLUSTER);
        String result = aclsCommand.execute(container.getKafkaContainer());
        assertThat(result, containsString("Cluster:LITERAL:kafka-cluster"));
        assertThat(result, containsString("User:CN=cn.kafka.server.fr,OU=None,O=laposte,L=None,ST=None,C=fr has Allow permission for operations: All from hosts: *"));
    }

    @Test
    public void addAclsRead() {
        container.withTopic(new TopicConfiguration("topic-acls-read", 1, false));
        container.withReadAcls("topic-acls-read", "my-group");
        String output = new AclsListCmd("topic-acls-read").execute(container.getKafkaContainer());
        assertThat(output, containsString("Topic:LITERAL:topic-acls-read"));
        assertThat(output, containsString("User:CN=cn.kafka.client.fr,OU=None,O=laposte,L=None,ST=None,C=fr has Allow permission for operations: Read from hosts: *"));
    }

    @Test
    public void addAclsWrite() {
        container.withTopic(new TopicConfiguration("topic-acls-write", 1, false));
        container.withWriteAcls("topic-acls-write");
        String output = new AclsListCmd("topic-acls-write").execute(container.getKafkaContainer());
        assertThat(output, containsString("Topic:LITERAL:topic-acls-write"));
        assertThat(output, containsString("User:CN=cn.kafka.client.fr,OU=None,O=laposte,L=None,ST=None,C=fr has Allow permission for operations: Write from hosts: *"));
    }

    @Test
    public void addAclsDescribe() {
        container.withTopic(new TopicConfiguration("topic-acls-describe", 1, false));
        container.withDescribeAcls("topic-acls-describe", "my-group");
        String output = new AclsListCmd("topic-acls-describe").execute(container.getKafkaContainer());
        assertThat(output, containsString("Topic:LITERAL:topic-acls-describe"));
        assertThat(output, containsString("User:CN=cn.kafka.client.fr,OU=None,O=laposte,L=None,ST=None,C=fr has Allow permission for operations: Describe from hosts: *"));
    }

    @Test
    public void addAclsAll() {
        container.withTopic(new TopicConfiguration("topic-acls-all", 1, false));
        container.withAllAcls("topic-acls-all", "my-group");
        String output = new AclsListCmd("topic-acls-all").execute(container.getKafkaContainer());
        assertThat(output, containsString("Topic:LITERAL:topic-acls-all"));
        assertThat(output, containsString("User:CN=cn.kafka.client.fr,OU=None,O=laposte,L=None,ST=None,C=fr has Allow permission for operations: All from hosts: *"));
    }

    @Test
    public void addAcls() {
        container.withTopic(new TopicConfiguration("topic-acls", 1, false));
        container.withAcls(new AclsOperation[]{AclsOperation.READ, AclsOperation.DESCRIBE, AclsOperation.WRITE}, "topic-acls", "my-group");
        String output = new AclsListCmd("topic-acls").execute(container.getKafkaContainer());
        assertThat(output, containsString("Topic:LITERAL:topic-acls"));
        assertThat(output, containsString("User:CN=cn.kafka.client.fr,OU=None,O=laposte,L=None,ST=None,C=fr has Allow permission for operations: Read from hosts: *"));
        assertThat(output, containsString("User:CN=cn.kafka.client.fr,OU=None,O=laposte,L=None,ST=None,C=fr has Allow permission for operations: Describe from hosts: *"));
        assertThat(output, containsString("User:CN=cn.kafka.client.fr,OU=None,O=laposte,L=None,ST=None,C=fr has Allow permission for operations: Write from hosts: *"));
    }
}
