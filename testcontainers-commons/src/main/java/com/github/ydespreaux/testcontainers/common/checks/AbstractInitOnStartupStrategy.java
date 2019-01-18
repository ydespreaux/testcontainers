package com.github.ydespreaux.testcontainers.common.checks;

import org.testcontainers.containers.GenericContainer;

/**
 * Define a waiting strategy.
 * This policy allows you to run a shell command once to test the state of the container.
 *
 * @since 1.0.0
 */
public abstract class AbstractInitOnStartupStrategy extends AbstractCommandWaitStrategy {

    private volatile boolean wasExecutedOnce;

    public AbstractInitOnStartupStrategy(GenericContainer container) {
        super(container);
    }

    public abstract String[] getScriptToExecute();

    @Override
    public String[] getCheckCommand() {
        return getScriptToExecute();
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
