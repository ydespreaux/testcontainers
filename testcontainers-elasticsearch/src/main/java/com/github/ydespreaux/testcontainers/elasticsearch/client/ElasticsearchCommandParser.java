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

package com.github.ydespreaux.testcontainers.elasticsearch.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.org.apache.commons.io.FilenameUtils;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author Yoann Despréaux
 * @since 1.0.1
 */
@Slf4j
public class ElasticsearchCommandParser {

    public static final ElasticsearchCommandParser INSTANCE = new ElasticsearchCommandParser();
    private static final Pattern PAYLOAD_RESOURCE_FILE = Pattern.compile("@resource\\((.+)\\)");


    private ElasticsearchCommandParser() {
    }

    public List<ElasticsearchCommand> parse(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return Collections.emptyList();
        }
        Path path = validateFile(filePath);
        if ("json".equalsIgnoreCase(FilenameUtils.getExtension(filePath))) {
            return parseJson(path);
        } else {
            return parseScript(path);
        }
    }

    protected List<ElasticsearchCommand> parseJson(Path path) {
        try {
            String json = new String(Files.readAllBytes(path));
            List<Map<String, Object>> commands = new ObjectMapper().readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
            return commands.stream()
                    .map(this::parseMapCommand)
                    .filter(command -> !command.isSkip())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ContainerLaunchException("Cannot read the init json file", e);
        }
    }

    protected List<ElasticsearchCommand> parseScript(Path path) {
        try {
            return lines(path)
                    .stream()
                    .map(this::parseStringCommand)
                    .filter(command -> !command.isSkip())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ContainerLaunchException(format("Cannot read the init script file %s", path.toString()), e);
        }
    }

    private List<String> lines(Path path) throws IOException {
        List<String> commands = new ArrayList<>();
        List<String> lines = Files.readAllLines(path);
        StringBuilder commandLine = new StringBuilder();
        for (String line : lines) {
            String lineFormatted = line.trim();
            if (lineFormatted.isEmpty() || lineFormatted.charAt(0) == '#') {
                continue;
            }
            if (lineFormatted.startsWith(ElasticsearchCommand.RequestMethod.POST.name())
                    || lineFormatted.startsWith(ElasticsearchCommand.RequestMethod.PUT.name())
                    || lineFormatted.startsWith(ElasticsearchCommand.RequestMethod.DELETE.name())) {
                if (commandLine.length() > 0) {
                    commands.add(commandLine.toString());
                }
                commandLine = new StringBuilder(lineFormatted);
            } else {
                commandLine.append(lineFormatted);
            }
        }
        if (commandLine.length() > 0) {
            commands.add(commandLine.toString());
        }
        return commands;
    }

    protected ElasticsearchCommand parseMapCommand(Map<String, Object> command) {
        if (log.isDebugEnabled()) {
            log.debug("Parsing command: {}", command);
        }
        ElasticsearchCommand esCommand = new ElasticsearchCommand();
        String methodName = (String) command.get("method");
        esCommand.setRequestMethod(ElasticsearchCommand.RequestMethod.valueOf(methodName.trim().toUpperCase()));
        esCommand.setPath((String) command.get("path"));
        Object payload = command.get("payload");
        if (ElasticsearchCommand.RequestMethod.DELETE == esCommand.getRequestMethod()) {
            Assert.isTrue(payload == null, "For DELETE commands the payload should be undefined");
        } else {
            try {
                if (payload instanceof String) {
                    esCommand.setJson(formatPayload((String) payload));
                } else {
                    esCommand.setJson(new ObjectMapper().writeValueAsString(payload));
                }
            } catch (JsonProcessingException e) {
                throw new ContainerLaunchException("Cannot serialize the JSON payload for command '" + command + "'", e);
            }
        }
        return esCommand;
    }


    protected ElasticsearchCommand parseStringCommand(String command) {
        ElasticsearchCommand esCommand = new ElasticsearchCommand();
        String formattedCommand = command.trim();
        // skip empty lines or lines starting with '#'
        if (formattedCommand.isEmpty() || formattedCommand.charAt(0) == '#') {
            esCommand.setSkip(true);
        } else {
            int firstSeparatorIndex = formattedCommand.indexOf(':');
            int secondSeparatorIndex = formattedCommand.indexOf(':', firstSeparatorIndex + 1);
            if (firstSeparatorIndex == -1 || secondSeparatorIndex == -1) {
                throw new ContainerLaunchException(
                        "Command '" + command + "' in the script file is not properly formatted."
                                + " The format is: REQUEST_METHOD:path:json_script."
                                + " Ex: PUT:indexName/typeName/id:{\"shoe_size\":39, \"shoe_color\":\"orange\"}");
            }
            String methodName = formattedCommand.substring(0, firstSeparatorIndex).trim();
            esCommand.setRequestMethod(ElasticsearchCommand.RequestMethod.valueOf(methodName.toUpperCase()));
            esCommand.setPath(formattedCommand.substring(firstSeparatorIndex + 1, secondSeparatorIndex).trim());
            esCommand.setJson(formatPayload(formattedCommand.substring(secondSeparatorIndex + 1).trim()));
        }

        return esCommand;
    }

    /**
     * @param filePath
     * @return
     */
    private Path validateFile(String filePath) {
        Path path = Paths.get(MountableFile.forClasspathResource(filePath).getResolvedPath());
        File file = path.toFile();
        if (!file.exists()) {
            throw new IllegalArgumentException(format("Resource with path %s could not be found", path.toString()));
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(format("Resource with path %s must be a file", path.toString()));
        }
        return path;
    }

    /**
     * @param payload
     * @return
     */
    private String formatPayload(String payload) {
        Matcher matcher = PAYLOAD_RESOURCE_FILE.matcher(payload);
        if (matcher.find()) {
            Path resource = Paths.get(MountableFile.forClasspathResource(matcher.group(1)).getResolvedPath());
            try {
                return new String(Files.readAllBytes(resource));
            } catch (IOException e) {
                throw new ContainerLaunchException(format("Cannot read the init script file '%s'", resource.toString()), e);
            }
        }
        return payload;
    }

}
