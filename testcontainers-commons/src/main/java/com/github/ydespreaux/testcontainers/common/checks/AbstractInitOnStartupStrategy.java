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

import org.testcontainers.containers.Container;

/**
 * Define a waiting strategy.
 * This policy allows you to run a shell command once to test the state of the container.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public abstract class AbstractInitOnStartupStrategy<T extends Container> extends AbstractCommandWaitStrategy<T> {

    private volatile boolean wasExecutedOnce;

    public AbstractInitOnStartupStrategy(T container) {
        super(container);
    }

    @Override
    protected void waitUntilReady() {
        if (wasExecutedOnce) {
            return;
        }
        super.waitUntilReady();
        wasExecutedOnce = true;
    }
}
