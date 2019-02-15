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

package com.github.ydespreaux.testcontainers.kafka.containers;

import com.github.ydespreaux.testcontainers.common.IContainer;
import com.github.ydespreaux.testcontainers.common.checks.AbstractCommandWaitStrategy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.FixedHostPortGenericContainer;

import java.util.UUID;

import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.containerLogsConsumer;
import static com.github.ydespreaux.testcontainers.common.utils.ContainerUtils.getAvailableMappingPort;
import static java.lang.String.format;

/**
 * Zookeeper container.
 *
 * @param <SELF>
 * @since 1.0.0
 */
@Slf4j
public class ZookeeperContainer<SELF extends ZookeeperContainer<SELF>> extends FixedHostPortGenericContainer<SELF> implements IContainer<SELF> {

    private static final String ZOOKEEPER_DEFAULT_BASE_URL = "confluentinc/cp-zookeeper";

    /**
     * zookeeper mapping port
     */
    @Getter
    private final int mappingPort;

    /**
     * @param version
     */
    public ZookeeperContainer(String version) {
        this(version, getAvailableMappingPort());
    }

    /**
     * @param version
     */
    public ZookeeperContainer(String version, int zookeeperPort) {
        super(ZOOKEEPER_DEFAULT_BASE_URL + ":" + version);
        this.mappingPort = zookeeperPort;
    }

    /**
     * Configure the container
     */
    @Override
    protected void configure() {
        this.withLogConsumer(containerLogsConsumer(log))
                .withEnv("ZOOKEEPER_CLIENT_PORT", String.valueOf(this.mappingPort))
                .withExposedPorts(this.mappingPort)
                .withFixedExposedPort(this.mappingPort, this.mappingPort)
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("testcontainsers-zookeeper-" + UUID.randomUUID()))
                .waitingFor(new ZookeeperStatusCheck());
    }

    /**
     * @param registerProperties
     * @return
     */
    @Override
    public SELF withRegisterSpringbootProperties(boolean registerProperties) {
        return this.self();
    }

    /**
     * @return
     */
    @Override
    public boolean registerSpringbootProperties() {
        return false;
    }

    /**
     * Get the url.
     *
     * @return
     */
    public String getURL() {
        return format("%s:%s", this.getContainerIpAddress(), this.mappingPort);
    }

    /**
     * Get the local url.
     *
     * @return
     */
    @Override
    public String getInternalURL() {
        return format("%s:%s", this.getNetworkAliases().get(0), this.mappingPort);
    }

    /**
     * @return
     * @deprecated use getInternalURL()
     */
    @Deprecated
    public String getLocalURL() {
        return getInternalURL();
    }

    /**
     * Define the waiting strategy for the zookeeper container.
     */
    private final class ZookeeperStatusCheck extends AbstractCommandWaitStrategy {

        private static final String TIMEOUT_IN_SEC = "30";

        public ZookeeperStatusCheck() {
            super(ZookeeperContainer.this);
        }

        @Override
        public String[] getCheckCommand() {
            return new String[]{
                    "cub",
                    "zk-ready",
                    getURL(),
                    TIMEOUT_IN_SEC
            };
        }

    }
}
