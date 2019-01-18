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
