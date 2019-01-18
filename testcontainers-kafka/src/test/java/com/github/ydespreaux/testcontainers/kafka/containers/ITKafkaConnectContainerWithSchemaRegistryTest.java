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
public class ITKafkaConnectContainerWithSchemaRegistryTest {

    @ClassRule
    public static ConfluentKafkaContainer kafkaContainer = new ConfluentKafkaContainer()
            .withSchemaRegistry(true)
            .withRegisterSpringbootProperties(false);

    private static KafkaConnectContainer kafkaConnectContainer;


    @BeforeClass
    public static void onSetupClass() {
        kafkaConnectContainer = new KafkaConnectContainer<>("4.1.0", kafkaContainer.getKafkaContainer().getInternalURL(), kafkaContainer.getSchemaRegistryContainer().getInternalURL())
                .withNetwork(kafkaContainer.getNetwork())
                .withKeyConverter("io.confluent.connect.avro.AvroConverter")
                .withValueConverter("io.confluent.connect.avro.AvroConverter");
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
