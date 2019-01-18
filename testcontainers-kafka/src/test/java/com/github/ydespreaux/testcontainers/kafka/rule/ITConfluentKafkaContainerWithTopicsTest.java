package com.github.ydespreaux.testcontainers.kafka.rule;


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
public class ITConfluentKafkaContainerWithTopicsTest {

    @ClassRule
    public static final ConfluentKafkaContainer container = new ConfluentKafkaContainer()
            .withTopic("topic1", 3, false)
            .withTopic("topic2-compact", 3, true);

    @Test
    public void containerEnvironment() {
        assertThat(container.getBootstrapServers(), is(notNullValue()));
        assertThat(container.getZookeeperServer(), is(notNullValue()));
    }

    @Test
    public void checkTopics() {
        ZkUtils zkUtils = ZkUtils.apply(container.getZookeeperServer(), 6000, 6000, false);
        assertThat(AdminUtils.topicExists(zkUtils, "topic1"), is(true));
        assertThat(AdminUtils.topicExists(zkUtils, "topic2-compact"), is(true));
    }

}
