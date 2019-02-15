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

import lombok.extern.slf4j.Slf4j;
import org.rnorth.ducttape.TimeoutException;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * Define a retry wait strategy.
 *
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractRetryingWaitStrategy extends AbstractWaitStrategy {

    /**
     * The current container.
     */
    protected final GenericContainer container;

    /**
     * Default constructor.
     *
     * @param container
     */
    public AbstractRetryingWaitStrategy(GenericContainer container) {
        this.container = container;
    }

    /**
     * Get the container's type.
     *
     * @return
     */
    protected String getContainerType() {
        return getClass().getSimpleName();
    }

    @Override
    protected void waitUntilReady() {
        long seconds = startupTimeout.getSeconds();
        try {
            Unreliables.retryUntilTrue((int) seconds, TimeUnit.SECONDS,
                    () -> getRateLimiter().getWhenReady(this::isReady));
        } catch (TimeoutException e) {
            throw new ContainerLaunchException(
                    format("[%s] notifies that container[%s] is not ready after [%d] seconds, container cannot be started.",
                            getContainerType(), container.getContainerId(), seconds));
        }
    }

    /**
     * Return true if the container is ready.
     *
     * @return
     */
    protected abstract boolean isReady();

}