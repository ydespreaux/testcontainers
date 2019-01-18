package com.github.ydespreaux.testcontainers.cassandra;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Slf4j
@RunWith(SpringRunner.class)
public class CassandraContainerConfigTest {

    @Test
    public void withCqlScriptDirectoryDbSchema() {
        CassandraContainer container = new CassandraContainer()
                .withCqlScriptDirectory("db-schema");
        List<String> scripts = container.getCqlScripts();
        assertThat(scripts.size(), is(equalTo(3)));
        assertThat(scripts.get(0), is(equalTo("/tmp/cassandra-init/db-schema/1-keyspace/keyspace.cql")));
        assertThat(scripts.get(1), is(equalTo("/tmp/cassandra-init/db-schema/2-data/data.cql")));
        assertThat(scripts.get(2), is(equalTo("/tmp/cassandra-init/db-schema/model.cql")));
    }

    @Test
    public void withCqlScriptDirectoryScripts() {
        CassandraContainer container = new CassandraContainer()
                .withCqlScriptDirectory("scripts/keyspace")
                .withCqlScriptDirectory("scripts/data");
        List<String> scripts = container.getCqlScripts();
        assertThat(scripts.size(), is(equalTo(3)));
        assertThat(scripts.get(0), is(equalTo("/tmp/cassandra-init/keyspace/ext/ext.cql")));
        assertThat(scripts.get(1), is(equalTo("/tmp/cassandra-init/keyspace/keyspace.cql")));
        assertThat(scripts.get(2), is(equalTo("/tmp/cassandra-init/data/data.cql")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void withCqlScriptDirectoryWithResourceNotFound() {
        new CassandraContainer()
                .withCqlScriptDirectory("db-schema-notfound");
    }

    @Test(expected = IllegalArgumentException.class)
    public void withCqlScriptDirectoryWithFileResource() {
        new CassandraContainer()
                .withCqlScriptDirectory("db-schema/1-keyspace/1-schema.cql");
    }
}
