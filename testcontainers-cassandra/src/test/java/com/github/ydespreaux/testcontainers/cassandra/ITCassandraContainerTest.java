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

import com.datastax.driver.core.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
public class ITCassandraContainerTest {

    @ClassRule
    public static final CassandraContainer cassandraContainer = new CassandraContainer().withCqlScriptDirectory("db-schema");
    private static final String KEYSPACE = "testcontainers_unit";
    private static final String TABLE = "model";
    private static Cluster cluster;
    private Session session;

    @BeforeClass
    public static void onSetupClass() {
        cluster = Cluster.builder()
                .addContactPoint(cassandraContainer.getContainerIpAddress())
                .withPort(cassandraContainer.getCQLNativeTransportPort())
                .build();
    }

    @AfterClass
    public static void onTeardownClass() {
        if (cluster != null) {
            cluster.close();
        }
    }

    @Before
    public void onSetup() {
        session = cluster.connect(KEYSPACE);
    }

    @After
    public void onTeardown() {
        if (session != null) {
            session.close();
        }
    }

    @Test
    public void environmentSystemProperty() {
        assertThat(System.getProperty(cassandraContainer.getContactPointsSystemProperty()), is(equalTo("localhost")));
        assertThat(System.getProperty(cassandraContainer.getCassandraPortSystemProperty()), is(equalTo(String.valueOf(cassandraContainer.getMappedPort(9042)))));
    }


    @Test
    public void getURL() {
        assertThat(cassandraContainer.getURL(), is(equalTo(cassandraContainer.getContainerIpAddress())));
    }

    @Test
    public void getInternalURL() {
        assertThat(cassandraContainer.getInternalURL(), is(equalTo(cassandraContainer.getNetworkAliases().get(0))));
    }

    @Test
    public void checkCassandraSchema() {
        KeyspaceMetadata keyspaceMetadata = cluster.getMetadata().getKeyspace(KEYSPACE);
        assertThat(keyspaceMetadata, notNullValue());
        assertThat(keyspaceMetadata.getName(), is(equalTo(KEYSPACE)));
        Map<String, String> replication = keyspaceMetadata.getReplication();
        assertThat(replication.get("class"), is(equalTo("org.apache.cassandra.locator.SimpleStrategy")));
        assertThat(replication.get("replication_factor"), is(equalTo("1")));
        TableMetadata tableMetadata = keyspaceMetadata.getTable(TABLE);
        assertThat(tableMetadata, notNullValue());
        assertThat(tableMetadata.getName(), is(equalTo("model")));
        assertThat(tableMetadata.getColumns().size(), is(equalTo(2)));
        ColumnMetadata idcolumn = tableMetadata.getColumn("id");
        assertThat(idcolumn, notNullValue());
        assertThat(idcolumn.getName(), is(equalTo("id")));
        assertThat(idcolumn.getType(), is(equalTo(DataType.text())));
        ColumnMetadata timestampColumn = tableMetadata.getColumn("timestamp");
        assertThat(timestampColumn, notNullValue());
        assertThat(timestampColumn.getName(), is(equalTo("timestamp")));
        assertThat(timestampColumn.getType(), is(equalTo(DataType.timestamp())));
    }

    @Test
    public void checkCassandraData() {
        List<Row> result = session.execute("select * from model").all();
        assertThat(result.size(), is(equalTo(4)));
    }


}
