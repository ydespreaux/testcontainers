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
import com.github.ydespreaux.testcontainers.kafka.containers.ZookeeperContainer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Yoann Despréaux
 * @since 1.1.1
 */
@Getter
@Setter
@Builder
public class ZookeeperHealthCmd extends AbstractCommand<ZookeeperContainer> {

    @Builder.Default
    private long timeoutInSeconds = 30;

    @Override
    protected List<String> buildParameters(ZookeeperContainer container) {
        return Arrays.asList(
                "cub",
                "zk-ready",
                container.getURL(),
                String.valueOf(timeoutInSeconds)
        );
    }
}
