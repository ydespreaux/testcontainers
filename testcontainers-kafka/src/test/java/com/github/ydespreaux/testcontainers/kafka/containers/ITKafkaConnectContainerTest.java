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

import com.github.ydespreaux.testcontainers.kafka.domain.WorkerInfo;
import com.github.ydespreaux.testcontainers.kafka.rule.ConfluentKafkaContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringRunner.class)
public class ITKafkaConnectContainerTest {

    @ClassRule
    public static ConfluentKafkaContainer kafkaContainer = new ConfluentKafkaContainer()
            .withRegisterSpringbootProperties(false);

    private static KafkaConnectContainer kafkaConnectContainer;


    @BeforeClass
    public static void onSetupClass() {
        kafkaConnectContainer = new KafkaConnectContainer("4.1.0",
                kafkaContainer.getKafkaContainer().getInternalURL())
                .withNetwork(kafkaContainer.getNetwork());
        kafkaConnectContainer.start();
    }

    @AfterClass
    public static void onTeardownClass() {
        if (kafkaConnectContainer != null) {
            kafkaConnectContainer.stop();
        }
    }

    @Test
    public void containerEnvironment() {
        assertThat(kafkaConnectContainer.getURL(), is(notNullValue()));
    }

    @Test
    public void checkAppRest() {
        RestTemplate template = new RestTemplate();
        WorkerInfo info = template.getForObject(kafkaConnectContainer.getURL(), WorkerInfo.class);
        assertThat(info, is(notNullValue()));
        assertThat(info.getVersion(), is(notNullValue()));
        assertThat(info.getCommit(), is(notNullValue()));
    }

}
