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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
public class ITElasticsearchContainer_642_Test {

    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer("6.4.2")
            .withConfigDirectory("config")
            .withFileInitScript("scripts/init.script")
            .withFileInitScript("scripts/init.json");

    private static String testUrl(String path) {
        return elasticContainer.getURL() + path;
    }

    private static OkHttpClient createHttpClient() {
        return createHttpClient(10, TimeUnit.SECONDS);
    }

    private static OkHttpClient createHttpClient(long timeout, TimeUnit timeUnit) {
        return new OkHttpClient.Builder().connectTimeout(timeout, timeUnit)
                .writeTimeout(timeout, timeUnit).readTimeout(timeout, timeUnit).build();
    }

    @Test
    public void environmentSystemProperty() {
        assertThat(System.getProperty(elasticContainer.getJestUrisSystemProperty()), is(equalTo("http://" + elasticContainer.getContainerIpAddress() + ":" + elasticContainer.getHttpPort())));
        assertThat(System.getProperty(elasticContainer.getRestUrisSystemProperty()), is(equalTo("http://" + elasticContainer.getContainerIpAddress() + ":" + elasticContainer.getHttpPort())));
    }

    @Test
    public void getURL() {
        assertThat(elasticContainer.getURL(), is(equalTo("http://" + elasticContainer.getContainerIpAddress() + ":" + elasticContainer.getHttpPort())));
    }

    @Test
    public void getInternalURL() {
        assertThat(elasticContainer.getInternalURL(), is(equalTo("http://" + elasticContainer.getNetworkAliases().get(0) + ":" + 9200)));
    }

    @Test
    public void health() throws IOException {
        Response response = call("/_cluster/health");
        assertThat(response.isSuccessful(), is(true));
    }

    @Test
    public void getIndex() throws IOException {
        Response response = call("/load_test_index");
        assertThat(response.isSuccessful(), is(true));
        assertThat(response.code(), is(equalTo(200)));
    }

    @Test
    public void getIndexFromResource() throws IOException {
        Response response = call("/index-1");
        assertThat(response.isSuccessful(), is(true));
        assertThat(response.code(), is(equalTo(200)));
    }

    @Test
    public void getTemplateFromResource() throws IOException {
        Response response = call("/_template/template2");
        assertThat(response.isSuccessful(), is(true));
        assertThat(response.code(), is(equalTo(200)));
    }

    @Test
    public void getDocument() throws IOException {
        Response response = call("/load_test_index/test_type/2");
        assertThat(response.isSuccessful(), is(true));
        assertThat(response.code(), is(equalTo(200)));
    }


    private Response call(String path) throws IOException {
        OkHttpClient client = createHttpClient();
        Request request = new Request.Builder().get().url(testUrl(path)).build();
        return client.newCall(request).execute();
    }

}
