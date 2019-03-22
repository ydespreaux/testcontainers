package fr.laposte.an.testcontainers.kafka.cmd;

import fr.laposte.an.testcontainers.commons.cmd.AbstractCommand;
import fr.laposte.an.testcontainers.kafka.config.TopicConfiguration;
import fr.laposte.an.testcontainers.kafka.containers.KafkaContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
public class TopicCreateCommand extends AbstractCommand<KafkaContainer> {

    private String topicName;
    private int partitions;
    private boolean compact;
    private int brokersCount = 1;

    public TopicCreateCommand(TopicConfiguration topic) {
        this.topicName = topic.getName();
        this.partitions = topic.getPartitions();
        this.compact = topic.isCompact();
    }

    @Override
    protected List<String> buildParameters(KafkaContainer container) {
        String zookeeperUrl = container.getEnvMap().get("KAFKA_ZOOKEEPER_CONNECT");
        List<String> parameters = new ArrayList<>(Arrays.asList(
            "kafka-topics",
            "--create", "--topic", topicName,
            "--partitions", String.valueOf(partitions),
            "--replication-factor", String.valueOf(brokersCount),
            "--if-not-exists",
            "--zookeeper", zookeeperUrl
        ));
        if (compact) {
            parameters.addAll(Arrays.asList(
                "--config", "cleanup.policy=compact"
            ));
        }
        return parameters;
    }
}
