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

package com.github.ydespreaux.testcontainers.common.checks;

import com.github.ydespreaux.testcontainers.common.utils.ContainerUtils;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

/**
 * Define a waiting strategy.
 * This policy allows you to execute a shell command in the docker container to verify
 * that the container is started.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractCommandWaitStrategy extends AbstractRetryingWaitStrategy {

    public AbstractCommandWaitStrategy(GenericContainer container) {
        super(container);
    }

    /**
     * Returns the schell command that must be executed.
     *
     * @return
     */
    public abstract String[] getCheckCommand();

    @Override
    protected boolean isReady() {
        String commandName = getContainerType();
        String containerId = container.getContainerId();
        if (log.isDebugEnabled()) {
            log.debug("{} execution of command {} for container id: {} ", commandName, containerId);
        }

        ContainerUtils.ExecCmdResult healthCheckCmdResult = ContainerUtils.execCmd(container.getDockerClient(), containerId, getCheckCommand());

        if (log.isDebugEnabled()) {
            log.debug("{} executed with exitCode: {}, output: {}",
                    commandName, healthCheckCmdResult.getExitCode(), healthCheckCmdResult.getOutput());
        }

        if (healthCheckCmdResult.getExitCode() != 0) {
            if (log.isDebugEnabled()) {
                log.debug("{} executed with exitCode !=0, considering status as unknown", commandName);
            }
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("{} command executed, considering container {} successfully started", commandName, containerId);
        }
        return true;
    }
}