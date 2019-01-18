package com.github.ydespreaux.testcontainers.common;

import org.testcontainers.containers.Container;

/**
 * @param <SELF>
 * @since 1.0.0
 */
public interface IContainer<SELF extends IContainer<SELF>> extends Container<SELF> {


    /**
     * @param registerProperties
     * @return
     */
    SELF withRegisterSpringbootProperties(boolean registerProperties);

    /**
     * @return
     */
    boolean registerSpringbootProperties();

    /**
     * @return
     */
    String getURL();

    /**
     * @return
     */
    String getInternalURL();
}
