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


import com.github.ydespreaux.testcontainers.common.cmd.Command;
import com.github.ydespreaux.testcontainers.common.utils.ContainerUtils;
import com.github.ydespreaux.testcontainers.kafka.CertsDefinition;
import com.github.ydespreaux.testcontainers.kafka.cmd.AclsListCmd;
import com.github.ydespreaux.testcontainers.kafka.cmd.AclsOperation;
import com.github.ydespreaux.testcontainers.kafka.config.TopicConfiguration;
import com.github.ydespreaux.testcontainers.kafka.containers.KafkaContainer;
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
            .withKafkaServerCertificates(CertsDefinition.kafkaServerCertificates)
            .withKafkaClientCertificates(CertsDefinition.kafkaClientCertificates)
        .withSchemaRegistry(true);

    @Test
    public void containerEnvironment() {
        assertThat(container.isRunning(), is(true));
        assertThat(container.getBootstrapServers(), is(notNullValue()));
        assertThat(container.getZookeeperServer(), is(notNullValue()));
        assertThat(container.getSchemaRegistryServers(), is(notNullValue()));

        assertThat(System.getProperty("spring.kafka.bootstrap-servers"), is(equalTo(container.getBootstrapServers())));
        assertThat(System.getProperty("spring.kafka.security.protocol"), is(equalTo("SSL")));
        assertThat(System.getProperty("spring.kafka.ssl.key-password"), is(equalTo(CertsDefinition.kafkaClientCertificates.getKeystorePassword())));
        assertThat(System.getProperty("spring.kafka.ssl.key-store-location"), is(equalTo("file:" + CertsDefinition.kafkaClientCertificates.getKeystorePath().toString())));
        assertThat(System.getProperty("spring.kafka.ssl.key-store-password"), is(equalTo(CertsDefinition.kafkaClientCertificates.getKeystorePassword())));
        assertThat(System.getProperty("spring.kafka.ssl.trust-store-location"), is(equalTo("file:" + CertsDefinition.kafkaClientCertificates.getTruststorePath().toString())));
        assertThat(System.getProperty("spring.kafka.ssl.trust-store-password"), is(equalTo(CertsDefinition.kafkaClientCertificates.getTruststorePassword())));
        assertThat(System.getProperty("spring.kafka.properties.ssl.endpoint.identification.algorithm"), is(equalTo("")));
        assertThat(System.getProperty("spring.kafka.properties.schema.registry.url"), is(equalTo(container.getSchemaRegistryServers())));

        Command<KafkaContainer> aclsCommand = new AclsListCmd("kafka-cluster", AclsListCmd.ResourceAcls.CLUSTER);
        ContainerUtils.ExecCmdResult result = aclsCommand.execute(container.getKafkaContainer());
        assertThat(result.getOutput(), containsString("Cluster:LITERAL:kafka-cluster"));
        assertThat(result.getOutput(), containsString("User:CN=cn.kafka.server.fr,OU=None,O=github,L=None,ST=None,C=fr has Allow permission for operations: All from hosts: *"));
    }

    @Test
    public void addAclsRead() {
        container.withTopic(new TopicConfiguration("topic-acls-read", 1, false));
        container.withReadAcls("topic-acls-read", "my-group");
        ContainerUtils.ExecCmdResult output = new AclsListCmd("topic-acls-read").execute(container.getKafkaContainer());
        assertThat(output.getOutput(), containsString("Topic:LITERAL:topic-acls-read"));
        assertThat(output.getOutput(), containsString("User:CN=cn.kafka.client.fr,OU=None,O=github,L=None,ST=None,C=fr has Allow permission for operations: Read from hosts: *"));
    }

    @Test
    public void addAclsWrite() {
        container.withTopic(new TopicConfiguration("topic-acls-write", 1, false));
        container.withWriteAcls("topic-acls-write");
        ContainerUtils.ExecCmdResult output = new AclsListCmd("topic-acls-write").execute(container.getKafkaContainer());
        assertThat(output.getOutput(), containsString("Topic:LITERAL:topic-acls-write"));
        assertThat(output.getOutput(), containsString("User:CN=cn.kafka.client.fr,OU=None,O=github,L=None,ST=None,C=fr has Allow permission for operations: Write from hosts: *"));
    }

    @Test
    public void addAclsDescribe() {
        container.withTopic(new TopicConfiguration("topic-acls-describe", 1, false));
        container.withDescribeAcls("topic-acls-describe", "my-group");
        ContainerUtils.ExecCmdResult output = new AclsListCmd("topic-acls-describe").execute(container.getKafkaContainer());
        assertThat(output.getOutput(), containsString("Topic:LITERAL:topic-acls-describe"));
        assertThat(output.getOutput(), containsString("User:CN=cn.kafka.client.fr,OU=None,O=github,L=None,ST=None,C=fr has Allow permission for operations: Describe from hosts: *"));
    }

    @Test
    public void addAclsAll() {
        container.withTopic(new TopicConfiguration("topic-acls-all", 1, false));
        container.withAllAcls("topic-acls-all", "my-group");
        ContainerUtils.ExecCmdResult output = new AclsListCmd("topic-acls-all").execute(container.getKafkaContainer());
        assertThat(output.getOutput(), containsString("Topic:LITERAL:topic-acls-all"));
        assertThat(output.getOutput(), containsString("User:CN=cn.kafka.client.fr,OU=None,O=github,L=None,ST=None,C=fr has Allow permission for operations: All from hosts: *"));
    }

    @Test
    public void addAcls() {
        container.withTopic(new TopicConfiguration("topic-acls", 1, false));
        container.withAcls(new AclsOperation[]{AclsOperation.READ, AclsOperation.DESCRIBE, AclsOperation.WRITE}, "topic-acls", "my-group");
        ContainerUtils.ExecCmdResult output = new AclsListCmd("topic-acls").execute(container.getKafkaContainer());
        assertThat(output.getOutput(), containsString("Topic:LITERAL:topic-acls"));
        assertThat(output.getOutput(), containsString("User:CN=cn.kafka.client.fr,OU=None,O=github,L=None,ST=None,C=fr has Allow permission for operations: Read from hosts: *"));
        assertThat(output.getOutput(), containsString("User:CN=cn.kafka.client.fr,OU=None,O=github,L=None,ST=None,C=fr has Allow permission for operations: Describe from hosts: *"));
        assertThat(output.getOutput(), containsString("User:CN=cn.kafka.client.fr,OU=None,O=github,L=None,ST=None,C=fr has Allow permission for operations: Write from hosts: *"));
    }
}
