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

package com.github.ydespreaux.testcontainers.kafka.cmd;

import com.github.ydespreaux.testcontainers.common.cmd.AbstractCommand;
import com.github.ydespreaux.testcontainers.kafka.config.TopicConfiguration;
import com.github.ydespreaux.testcontainers.kafka.containers.KafkaContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yoann Despréaux
 * @since 1.1.1
 */
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
        return List.copyOf(parameters);
    }
}
