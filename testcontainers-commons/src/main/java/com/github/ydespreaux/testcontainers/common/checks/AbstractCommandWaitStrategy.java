package com.github.ydespreaux.testcontainers.common.checks;

import com.github.ydespreaux.testcontainers.common.utils.ContainerUtils;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

/**
 * Define a waiting strategy.
 * This policy allows you to execute a shell command in the docker container to verify
 * that the container is started.
 *
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