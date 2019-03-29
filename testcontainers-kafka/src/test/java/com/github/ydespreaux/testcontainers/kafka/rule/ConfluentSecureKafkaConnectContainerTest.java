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


import com.github.ydespreaux.testcontainers.kafka.CertsDefinition;
import com.github.ydespreaux.testcontainers.kafka.domain.WorkerInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@Tag("integration")
@Testcontainers
public class ConfluentSecureKafkaConnectContainerTest {

    @Container
    public static final ConfluentKafkaConnectContainer container = new ConfluentKafkaConnectContainer()
            .withKafkaServerCertificates(CertsDefinition.kafkaServerCertificates)
        .withSchemaRegistry(true)
        .withKeyConverter("org.apache.kafka.connect.storage.StringConverter")
        .withValueConverter("io.confluent.connect.avro.AvroConverter");

    @Test
    void containerEnvironment() {
        assertThat(container.getBootstrapServers(), is(notNullValue()));
        assertThat(container.getZookeeperServer(), is(notNullValue()));
        assertThat(container.getRestAppServers(), is(notNullValue()));
    }

    @Test
    void checkAppRest() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        builder = builder.rootUri(container.getRestAppServers());
        RestTemplate template = builder.build();

        WorkerInfo info = template.getForObject("/", WorkerInfo.class);
        assertThat(info, is(notNullValue()));
        assertThat(info.getVersion(), is(notNullValue()));
        assertThat(info.getCommit(), is(notNullValue()));
    }

}
