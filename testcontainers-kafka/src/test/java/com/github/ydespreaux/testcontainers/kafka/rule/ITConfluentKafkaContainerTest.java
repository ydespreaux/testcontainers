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
