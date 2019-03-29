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

package com.github.ydespreaux.testcontainers.cassandra;

import com.github.ydespreaux.testcontainers.cassandra.cmd.CqlScriptCmd;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class CassandraContainerConfigTest {

    @Test
    void withCqlScriptDirectoryDbSchema() {
        CassandraContainer container = new CassandraContainer()
                .withCqlScriptDirectory("db-schema");
        List<CqlScriptCmd> scripts = container.getCqlScripts();
        assertThat(scripts.size(), is(equalTo(3)));
        assertThat(scripts.get(0).getScript(), is(equalTo("/tmp/cassandra-init/db-schema/1-keyspace/keyspace.cql")));
        assertThat(scripts.get(1).getScript(), is(equalTo("/tmp/cassandra-init/db-schema/2-data/data.cql")));
        assertThat(scripts.get(2).getScript(), is(equalTo("/tmp/cassandra-init/db-schema/model.cql")));
    }

    @Test
    void withCqlScriptDirectoryScripts() {
        CassandraContainer container = new CassandraContainer()
                .withCqlScriptDirectory("scripts/keyspace")
                .withCqlScriptDirectory("scripts/data");
        List<CqlScriptCmd> scripts = container.getCqlScripts();
        assertThat(scripts.size(), is(equalTo(3)));
        assertThat(scripts.get(0).getScript(), is(equalTo("/tmp/cassandra-init/keyspace/ext/ext.cql")));
        assertThat(scripts.get(1).getScript(), is(equalTo("/tmp/cassandra-init/keyspace/keyspace.cql")));
        assertThat(scripts.get(2).getScript(), is(equalTo("/tmp/cassandra-init/data/data.cql")));
    }

    @Test
    void withCqlScriptDirectoryWithFileResource() {
        assertThrows(IllegalArgumentException.class, () -> new CassandraContainer()
                .withCqlScriptDirectory("db-schema/1-keyspace/1-schema.cql"));
    }
}
