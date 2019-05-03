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

package com.github.ydespreaux.testcontainers.elasticsearch;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Tag("integration")
@Testcontainers
public class ElasticsearchContainerTest {

    @Container
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer("6.4.2")
            .withConfigDirectory("config")
            .withFileInitScript("scripts/init.script")
            .withFileInitScript("scripts/init.json");


    @Test
    void environmentSystemProperty() {
        assertThat(System.getProperty(elasticContainer.getJestUrisSystemProperty()), is(equalTo("http://" + elasticContainer.getContainerIpAddress() + ":" + elasticContainer.getHttpPort())));
        assertThat(System.getProperty(elasticContainer.getRestUrisSystemProperty()), is(equalTo("http://" + elasticContainer.getContainerIpAddress() + ":" + elasticContainer.getHttpPort())));
    }

    @Test
    void getURL() {
        assertThat(elasticContainer.getURL(), is(equalTo("http://" + elasticContainer.getContainerIpAddress() + ":" + elasticContainer.getHttpPort())));
    }

    @Test
    void getInternalURL() {
        assertThat(elasticContainer.getInternalURL(), is(equalTo("http://" + elasticContainer.getNetworkAliases().get(0) + ":" + 9200)));
    }

    private static URI buildURI(String path) {
        return URI.create(elasticContainer.getURL() + path);
    }

    @Test
    void health() throws Exception {
        HttpResponse<String> response = call("/_cluster/health");
        assertThat(response.statusCode(), is(equalTo(200)));
    }

    @Test
    void getIndex() throws Exception {
        HttpResponse<String> response = call("/load_test_index");
        assertThat(response.statusCode(), is(equalTo(200)));
    }

    @Test
    void getIndexFromResource() throws Exception {
        HttpResponse<String> response = call("/index-1");
        assertThat(response.statusCode(), is(equalTo(200)));
    }

    @Test
    void getTemplateFromResource() throws Exception {
        HttpResponse<String> response = call("/_template/template2");
        assertThat(response.statusCode(), is(equalTo(200)));
    }

    @Test
    void getDocument() throws Exception {
        HttpResponse<String> response = call("/load_test_index/test_type/2");
        assertThat(response.statusCode(), is(equalTo(200)));
    }

    private HttpResponse<String> call(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(buildURI(path)).build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

}
