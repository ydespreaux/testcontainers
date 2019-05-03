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

import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.ContainerLaunchException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.lang.String.format;

/**
 * @author Yoann Despréaux
 * @since 1.0.1
 */
@Slf4j
public class ElasticsearchClient {

    private static final String APPLICATION_JSON = "application/json";
    private final String baseUrl;

    /**
     * @param container
     */
    public ElasticsearchClient(ElasticsearchContainer container) {
        if (!container.isRunning()) {
            throw new ContainerLaunchException("Container must be runnning !");
        }
        this.baseUrl = container.getURL();
    }

    /**
     * @param command
     */
    public void execute(ElasticsearchCommand command) {
        if (command.isSkip()) {
            return;
        }
        HttpResponse<String> response = null;
        try {
            switch (command.getRequestMethod()) {
                case PUT:
                    response = this.put(command.getPath(), command.getJson());
                    break;
                case POST:
                    response = this.post(command.getPath(), command.getJson());
                    break;
                case DELETE:
                    response = this.delete(command.getPath());
                    break;
                default:
                    throw new ContainerLaunchException(format("Request method %s not supported", command.getRequestMethod()));
            }
            if (!isValidHttpCode(response.statusCode())) {
                throw new ContainerLaunchException(format("Execute command %s success : %s", command.toString(), response.body()));
            }
            if (log.isInfoEnabled()) {
                log.info("Execute command {} success : {}", command.toString(), response.body());
            }
        } catch (Exception e) {
            throw new ContainerLaunchException(format("command '%s' failed : ", command), e);
        }
    }

    private HttpResponse<String> delete(String path) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(this.baseUrl + path))
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String entity) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(this.baseUrl + path))
                .header("Content-Type", APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(entity))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> put(String path, String entity) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(this.baseUrl + path))
                .header("Content-Type", APPLICATION_JSON)
                .PUT(HttpRequest.BodyPublishers.ofString(entity))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private boolean isValidHttpCode(int httpCode) {
        return httpCode == 200 || httpCode == 201;
    }

}
