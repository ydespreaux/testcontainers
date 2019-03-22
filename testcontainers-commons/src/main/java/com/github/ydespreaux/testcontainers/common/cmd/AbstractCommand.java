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

package com.github.ydespreaux.testcontainers.common.cmd;

import com.github.ydespreaux.testcontainers.common.utils.ContainerUtils;
import org.testcontainers.containers.Container;

import java.util.List;

public abstract class AbstractCommand<T extends Container> implements Command<T> {

    @Override
    public ContainerUtils.ExecCmdResult execute(T container) {
        ContainerUtils.ExecCmdResult result = ContainerUtils.execCmd(container.getDockerClient(), container.getContainerId(), getParameters(container));
        if (result.getExitCode() != 0) {
            throw new CommandExecutionException(this, result);
        }
        return result;
    }

    public String[] getParameters(T container) {
        return this.buildParameters(container).toArray(new String[0]);
    }

    protected abstract List<String> buildParameters(T container);
}
