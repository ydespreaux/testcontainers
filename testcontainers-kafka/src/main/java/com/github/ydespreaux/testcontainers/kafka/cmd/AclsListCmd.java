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

import java.util.Arrays;
import java.util.List;

public class AclsListAbstractCmd extends AbstractCommand<KafkaContainer> {

    private final ResourceAcls resource;

    private final String value;

    public AclsListAbstractCmd(String value) {
        this(value, ResourceAcls.TOPIC);
    }

    public AclsListAbstractCmd(String value, ResourceAcls resource) {
        this.value = value;
        this.resource = resource;

    }

    @Override
    protected List<String> buildParameters(KafkaContainer container) {
        String zookeeperUrl = container.getEnvMap().get("KAFKA_ZOOKEEPER_CONNECT");
        return Arrays.asList(
                "kafka-acls",
                "--authorizer-properties",
                "zookeeper.connect=" + zookeeperUrl,
                "--list",
                "--" + this.resource.resourceName(),
                value
        );
    }

    public enum ResourceAcls {
        TOPIC("topic"), CLUSTER("cluster"), GROUP("group");

        private final String resourceName;

        ResourceAcls(String resourceName) {
            this.resourceName = resourceName;
        }

        public String resourceName() {
            return this.resourceName;
        }
    }
}