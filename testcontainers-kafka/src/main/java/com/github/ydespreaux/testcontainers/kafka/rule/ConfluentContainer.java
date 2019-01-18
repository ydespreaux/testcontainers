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

package com.github.ydespreaux.testcontainers.kafka.rule;

import org.junit.rules.TestRule;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.testcontainers.containers.Network;

/**
 * @param <SELF>
 * @since 1.0.1
 */
public interface ConfluentContainer<SELF extends ConfluentContainer<SELF>> extends TestRule, InitializingBean, DisposableBean {

    default SELF self() {
        return (SELF) this;
    }

    /**
     * Set if the spring boot properties must be registred.
     *
     * @param registerProperties
     * @return
     */
    SELF withRegisterSpringbootProperties(boolean registerProperties);

    /**
     * Set the network
     *
     * @param network
     * @return
     */
    SELF withNetwork(Network network);
}
