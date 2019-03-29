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

import com.github.ydespreaux.testcontainers.common.cmd.Command;
import com.github.ydespreaux.testcontainers.common.cmd.CommandExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container;

import java.util.List;

/**
 * Define a waiting strategy.
 * This policy allows you to execute a shell command in the docker container to verify
 * that the container is started.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractCommandWaitStrategy<T extends Container> extends AbstractRetryingWaitStrategy<T> {

    public AbstractCommandWaitStrategy(T container) {
        super(container);
    }

    /**
     * Returns the schell command that must be executed.
     *
     * @return
     */
    public abstract List<Command> getCheckCommands();


    @Override
    protected boolean isReady() {
        for (Command command : getCheckCommands()) {
            if (!isReady(command)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param command
     * @return
     */
    private boolean isReady(Command command) {
        String commandName = getContainerType();
        String containerId = container.getContainerId();
        if (log.isDebugEnabled()) {
            log.debug("{} execution of command {} for container id: {} ", commandName, containerId);
        }
        try {
            command.execute(container);
        } catch (CommandExecutionException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage());
            }
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("{} command executed, considering container {} successfully started", commandName, containerId);
        }
        return true;
    }
}