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


import com.github.ydespreaux.testcontainers.kafka.config.TopicConfiguration;
import kafka.admin.AdminUtils;
import kafka.utils.ZkUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@RunWith(SpringRunner.class)
public class ITConfluentKafkaContainerTest {

    @ClassRule
    public static final ConfluentKafkaContainer container = new ConfluentKafkaContainer()
            .withSchemaRegistry(true);

    @Test
    public void containerEnvironment() {
        assertThat(container.getBootstrapServers(), is(notNullValue()));
        assertThat(container.getZookeeperServer(), is(notNullValue()));
        assertThat(container.getSchemaRegistryServers(), is(notNullValue()));
    }

    @Test
    public void createTopic() {
        container.createTopic(new TopicConfiguration("TOPIC1", 1, false));

        ZkUtils zkUtils = ZkUtils.apply(container.getZookeeperServer(), 6000, 6000, false);
        boolean exists = AdminUtils.topicExists(zkUtils, "TOPIC1");
        assertThat(exists, is(true));
    }

    @Test
    public void createCompactTopic() {
        container.createTopic(new TopicConfiguration("TOPIC_COMPACT_1", 1, true));

        ZkUtils zkUtils = ZkUtils.apply(container.getZookeeperServer(), 6000, 6000, false);
        boolean exists = AdminUtils.topicExists(zkUtils, "TOPIC_COMPACT_1");
        assertThat(exists, is(true));
    }


}
