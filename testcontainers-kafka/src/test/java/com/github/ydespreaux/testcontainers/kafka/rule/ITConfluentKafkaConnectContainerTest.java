package com.github.ydespreaux.testcontainers.kafka.rule;


import com.github.ydespreaux.testcontainers.kafka.domain.WorkerInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@RunWith(SpringRunner.class)
public class ITConfluentKafkaConnectContainerTest {

    @ClassRule
    public static final ConfluentKafkaConnectContainer container = new ConfluentKafkaConnectContainer<>()
            .withSchemaRegistry(true)
            .withKeyConverter("org.apache.kafka.connect.storage.StringConverter")
            .withValueConverter("io.confluent.connect.avro.AvroConverter");

    @Test
    public void containerEnvironment() {
        assertThat(container.getBootstrapServers(), is(notNullValue()));
        assertThat(container.getZookeeperServer(), is(notNullValue()));
        assertThat(container.getRestAppServers(), is(notNullValue()));
    }

    @Test
    public void checkAppRest() {
        RestTemplate template = new RestTemplate();
        WorkerInfo info = template.getForObject(container.getRestAppServers(), WorkerInfo.class);
        assertThat(info, is(notNullValue()));
        assertThat(info.getVersion(), is(notNullValue()));
        assertThat(info.getCommit(), is(notNullValue()));
    }

}
