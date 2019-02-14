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
import org.testcontainers.shaded.okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

    private static OkHttpClient createHttpClient() {
        return createHttpClient(10, TimeUnit.SECONDS);
    }

    private static OkHttpClient createHttpClient(long timeout, TimeUnit timeUnit) {
        return new OkHttpClient.Builder().connectTimeout(timeout, timeUnit)
                .writeTimeout(timeout, timeUnit).readTimeout(timeout, timeUnit).build();
    }

    /**
     * @param command
     */
    public void execute(ElasticsearchCommand command) {
        if (command.isSkip()) {
            return;
        }
        Response response = null;
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
            if (response != null && log.isDebugEnabled()) {
                log.debug("Execute command {} success : {}", command.toString(), response.body().string());
            }
        } catch (IOException e) {
            throw new ContainerLaunchException(format("command '%s' failed : ", command), e);
        }
    }

    private Response delete(String path) throws IOException {
        OkHttpClient client = createHttpClient();
        Request request = new Request.Builder()
                .url(this.baseUrl + path)
                .delete()
                .build();
        return client.newCall(request).execute();
    }

    private Response post(String path, String entity) throws IOException {
        OkHttpClient client = createHttpClient();
        Request request = new Request.Builder()
                .addHeader("Content-Type", APPLICATION_JSON)
                .url(this.baseUrl + path)
                .post(RequestBody.create(MediaType.parse(APPLICATION_JSON), entity))
                .build();
        return client.newCall(request).execute();
    }

    private Response put(String path, String entity) throws IOException {
        OkHttpClient client = createHttpClient();
        Request request = new Request.Builder()
                .addHeader("Content-Type", APPLICATION_JSON)
                .url(this.baseUrl + path)
                .put(RequestBody.create(org.testcontainers.shaded.okhttp3.MediaType.parse(APPLICATION_JSON), entity))
                .build();
        return client.newCall(request).execute();
    }

}
