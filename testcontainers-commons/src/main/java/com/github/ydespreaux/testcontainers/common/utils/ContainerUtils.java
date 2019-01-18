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

package com.github.ydespreaux.testcontainers.common.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.Value;
import org.slf4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;

import static java.lang.String.format;

/**
 * @since 1.0.0
 */
public class ContainerUtils {

    /**
     *
     */
    private ContainerUtils() {
        // Nothing to do
    }

    /**
     * Generate a available port
     *
     * @return a available port
     */
    public static int getAvailableMappingPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot find available port for mapping: " + e.getMessage(), e);
        }
    }

    /**
     * Get the host name of a container docker
     *
     * @param container the container
     * @return
     */
    public static String getContainerHostname(GenericContainer container) {
        InspectContainerResponse containerInfo = container.getContainerInfo();
        if (containerInfo == null) {
            containerInfo = container.getDockerClient().inspectContainerCmd(container.getContainerId()).exec();
        }

        return containerInfo.getConfig().getHostName();
    }

    /**
     * Add logs consumer
     *
     * @param log
     * @return
     */
    public static Consumer<OutputFrame> containerLogsConsumer(Logger log) {
        return (OutputFrame outputFrame) -> log.debug(outputFrame.getUtf8String());
    }

    /**
     * @param dockerClient
     * @param containerId
     * @param command
     * @return
     */
    public static ExecCmdResult execCmd(DockerClient dockerClient, String containerId, String[] command) {
        ExecCreateCmdResponse cmd = dockerClient.execCreateCmd(containerId)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(command)
                .exec();

        String cmdStdout;
        String cmdStderr;

        try (ByteArrayOutputStream stdout = new ByteArrayOutputStream();
             ByteArrayOutputStream stderr = new ByteArrayOutputStream();
             ExecStartResultCallback cmdCallback = new ExecStartResultCallback(stdout, stderr)) {
            dockerClient.execStartCmd(cmd.getId()).exec(cmdCallback).awaitCompletion();
            cmdStdout = stdout.toString(StandardCharsets.UTF_8.name());
            cmdStderr = stderr.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            String format = format("Exception was thrown when executing: %s, for container: %s ", Arrays.toString(command), containerId);
            throw new IllegalStateException(format, e);
        }

        int exitCode = dockerClient.inspectExecCmd(cmd.getId()).exec().getExitCode();
        String output = cmdStdout.isEmpty() ? cmdStderr : cmdStdout;
        return new ExecCmdResult(exitCode, output);
    }


    @Value
    public static class ExecCmdResult {
        int exitCode;
        String output;
    }
}
