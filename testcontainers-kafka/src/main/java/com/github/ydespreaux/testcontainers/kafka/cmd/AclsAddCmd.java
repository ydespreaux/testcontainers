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
import com.github.ydespreaux.testcontainers.kafka.containers.KafkaContainer;
import com.github.ydespreaux.testcontainers.kafka.security.Certificates;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.logging.log4j.util.Strings.isEmpty;

/**
 * @author Yoann Despréaux
 * @since 1.1.1
 */
@Getter
@Setter
@ToString
public class AclsAddCmd extends AbstractCommand<KafkaContainer> {

    private AclsOperation operation;
    private String user;
    private String group;
    private String topic;
    private String cluster;

    public AclsAddCmd(Certificates certificates) {
        user(certificates.getUser());
    }

    public AclsAddCmd user(String user) {
        this.user = user.replaceAll("\\s+", "");
        return this;
    }

    public AclsAddCmd operation(AclsOperation operation) {
        this.operation = operation;
        return this;
    }

    public AclsAddCmd group(String group) {
        this.group = group;
        return this;
    }

    public AclsAddCmd topic(String topic) {
        this.topic = topic;
        return this;
    }

    public AclsAddCmd cluster(String cluster) {
        this.cluster = cluster;
        return this;
    }

    @Override
    protected List<String> buildParameters(KafkaContainer container) {
        String zookeeperUrl = container.getEnvMap().get("KAFKA_ZOOKEEPER_CONNECT");
        List<String> parameters = new ArrayList<>(Arrays.asList(
                "kafka-acls",
                "--authorizer-properties",
                "zookeeper.connect=" + zookeeperUrl,
                "--add",
                "--allow-principal",
                "User:" + this.user,
                "--operation", operation.operationName()));
        if (operation == AclsOperation.ALL
                || operation == AclsOperation.READ
                || operation == AclsOperation.DESCRIBE
                || operation == AclsOperation.DELETE){
            parameters.add("--group=" + this.group);
        }
        if (operation == AclsOperation.ALL
                || operation == AclsOperation.READ
                || operation == AclsOperation.ALTER_CONFIGS
                || operation == AclsOperation.DESCRIBE_CONFIGS
                || operation == AclsOperation.DELETE
                || operation == AclsOperation.WRITE
                || operation == AclsOperation.DESCRIBE
                || operation == AclsOperation.CREATE) {
            parameters.add("--topic=" + this.topic);
        }
        if (operation == AclsOperation.CLUSTER_ACTION
                || operation == AclsOperation.CREATE
                || operation == AclsOperation.IDEMPOTENT_WRITE
                || operation == AclsOperation.DESCRIBE_CONFIGS
                || operation == AclsOperation.ALTER) {
            parameters.addAll(Arrays.asList(
                    "--cluster",
                    "kafka-cluster"
            ));
        }
        if (!isEmpty(this.cluster)) {
            parameters.addAll(Arrays.asList(
                    "--cluster",
                    "kafka-cluster"
            ));
        }
        return List.copyOf(parameters);
    }

}
