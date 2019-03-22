package fr.laposte.an.testcontainers.kafka.cmd;

import fr.laposte.an.testcontainers.commons.cmd.AbstractCommand;
import fr.laposte.an.testcontainers.kafka.containers.KafkaContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
public class TopicDescribeCommand extends AbstractCommand<KafkaContainer> {

    private String topicName;

    public TopicDescribeCommand(String topicName) {
        this.topicName = topicName;
    }

    @Override
    protected List<String> buildParameters(KafkaContainer container) {
        String zookeeperUrl = container.getEnvMap().get("KAFKA_ZOOKEEPER_CONNECT");
        return Arrays.asList(
            "kafka-topics",
            "--describe",
            "--topic",
            topicName,
            "--zookeeper", zookeeperUrl
        );
    }
}
