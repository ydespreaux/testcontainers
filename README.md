Integration tests with testcontainers
=====================================

This library defines docker containers for integration tests.
Its implementation is based on the testcontainers.org library.

Container types:
* MySQL
    * Initializing database with sql scripts
* Cassandra
    * Initializing database with cql scripts
* Elasticsearch
* Kafka
    * Schema registry for AVRO message
    * Kafka-Connect Container


Versions
-----------

|   spring-testcontainers    |   testcontainers | JUnit version compatibility |
|:--------------------------:|:----------------:|--------------:|
|   1.2.1                    |       1.10.6     | Junit 4, Junit 5 |
|   1.2.0                    |       1.10.6     | Junit 4 |
|   1.1.0                    |       1.10.6     | Junit 4 |
|   1.0.0                    |       1.8.3      | Junit 4 |

## Prerequisites

Using this library requires a Docker configuration beforehand so that the docker containers can be running:

* Docker version should be at least 1.6.0
* Docker environment should have more than 2GB free disk space
* File should be mountable
* Expose daemon on tcp://localhost:2375 without TLS

## MySQL

### Add the Maven dependency

```xml
<dependency>
    <groupId>com.github.ydespreaux.testcontainers</groupId>
    <artifactId>testcontainers-mysql</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

#### Using Junit 5 (version 1.2.1)

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.11.1</version>
    <scope>test</scope>
</dependency>
```

### Quick Start

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMySQLTest {

    @ClassRule
    public static final MySQLContainer mySqlContainer = new MySQLContainer();
}
```

By default the MySQL image used is 'mysql: 5.7.22'.
The URL of the image and the version are configurable. Below is an example to use version 5.7.21 of the image.

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMySQLTest {

    @ClassRule
    public static final MySQLContainer mySqlContainer = new MySQLContainer("5.7.21");
}
```

When the docker container has started, the following properties are initialized in the spring boot context:

|   spring boot property                |      Default value                                 |   Note                                                       |
|:-------------------------------------:|:--------------------------------------------------:|:------------------------------------------------------------:|
|   spring.datasource.url               | jdbc:mysql://localhost:<PORT>/db_test?useSSL=false | The port is generated randomly                               |
|   spring.datasource.driver-class-name | com.mysql.jdbc.Driver                              |                                                              |
|   spring.datasource.username          | db_user_test                                       |                                                              |
|   spring.datasource.password          | ***********                                        | The user's password is automatically generated by default    |
|   spring.datasource.platform          | mysql                                              |                                                              |

The withRegisterSpringbootProperties (boolean) method is used to initialize or not the spring boot context of the properties above.
The following example does not update the spring boot properties:

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMySQLTest {

    @ClassRule
    public static final MySQLContainer mySqlContainer = new MySQLContainer("5.7.21")
        .withRegisterSpringbootProperties(false);
}
```

The name of the spring boot properties can be changed using the following methods:

| Method                            | Description                                                           | Default value                         |
|:---------------------------------:|:---------------------------------------------------------------------:|:-------------------------------------:|
| withDriverClassSystemProperty     | Modify the property corresponding to the MySQL driver                 | spring.datasource.driver-class-name   |
| withUrlSystemProperty             | Modify the property corresponding to the URL                          | spring.datasource.url                 |
| withUsernameSystemProperty        | Modify the property corresponding to the name of the MySQL user       | spring.datasource.username            |
| withPasswordSystemProperty        | Modify the property corresponding to the password of the MySQL user   | spring.datasource.password            |
| withPlatformSystemProperty        | Modify the property corresponding to the platform                     | spring.datasource.platform            |

Setting up the MySQL database:

| Method                      | Description                               | Default value             |
|:-----------------------------:|:---------------------------------------:|:-------------------------:|
| withDatabaseName              | Modify the name of the database         | db_test                   |
| withUsername                  | Modify the name of the MySQL user       | db_user_test              |
| withPassword                  | Modify the password of the MySQL user   | Automatically generated   |
| withRootPassword              | Change the ROOT password                | Automatically generated   |
| withConfigurationOverride     | Modify the MySQL configuration file     |                           |

Initialization of the schema of the database:

Running a sql script:

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMySQLTest {

    @ClassRule
    public static final MySQLContainer mySqlContainer = new MySQLContainer("5.7.21")
        .withMySqlInit("mysql-init/mysql-init.sql");
}
```

The mysql-init.sql file (found in the mysql-init directory) will be executed after the container starts.

Running a set of sql scripts:

Example to run 2 sql scripts:
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMySQLTest {

    @ClassRule
    public static final MySQLContainer mySqlContainer = new MySQLContainer("5.7.21")
        .withMySqlInit("mysql-init/mysql-init-1.sql")
        .withMySqlInit("mysql-init/mysql-init-2.sql");
}
```

Example to run all the sql scripts contained in the 'mysql-init' directory:
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMySQLTest {

    @ClassRule
    public static final MySQLContainer mySqlContainer = new MySQLContainer("5.7.21")
        .withMySqlInitDirectory("mysql-init");
}
```

#### Sample with Junit 5 (version 1.2.1)

```java
@Testcontainers
public class ITMySQLTest {

    @Container
    public static final MySQLContainer mySqlContainer = new MySQLContainer();
}
```

## Cassandra

### Add the Maven dependency

```xml
<dependency>
    <groupId>com.github.ydespreaux.testcontainers</groupId>
    <artifactId>testcontainers-cassandra</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

#### Using Junit 5 (version 1.2.1)

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.11.1</version>
    <scope>test</scope>
</dependency>
```

### Quick Start

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITCassandraTest {

    @ClassRule
    public static final CassandraContainer mySqlContainer = new CassandraContainer();
}
```

By default the image cassandra used is 'cassandra: 3.11'.
The URL of the image and the version is configurable. Below is an example to use version 3 of the image.

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITCassandraTest {

    @ClassRule
    public static final CassandraContainer mySqlContainer = new CassandraContainer("3");
}
```

When the docker container has started, the following properties are initialized in the spring boot context:

|   Spring boot property                    |      Default value    | Notes                            |
|:-----------------------------------------:|:---------------------:|:--------------------------------:|
|   spring.data.cassandra.contact-points    | localhost             | The host is the docker hostname  |
|   spring.data.cassandra.port              | 33001                 | The port is generated randomly   |


The withRegisterSpringbootProperties (boolean) method is used to initialize or not the spring boot context of the properties above.

The name of the spring boot properties can be changed using the following methods:

| Method                            | Description                                               | Default value                  |
|:---------------------------------:|:---------------------------------------------------------:|:------------------------------:|
| withContactPointsSystemProperty   | Changes the ownership of cassandra cluster uris           | Docker hostname                |
| withCassandraPortSystemProperty   | Modify the property corresponding to the port cassandra   | The port is generated randomly |


Initialization of the schema of the database:

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMySQLTest {

    @ClassRule
    public static final CassandraContainer cassandraContainer = new CassandraContainer()
        .withCqlScriptDirectory("db-schema");

}
```

The withCqlScriptDirectory (...) method takes a parameter from the directory path. The set of scripts cql will be executed after the start of the container.

#### Sample with Junit 5 (version 1.2.1)

```java
@Testcontainers
public class ITCassandraTest {

    @Container
    public static final CassandraContainer mySqlContainer = new CassandraContainer();
}
```

## Elasticsearch

### Add the Maven dependency

```xml
<dependency>
    <groupId>com.github.ydespreaux.testcontainers</groupId>
    <artifactId>testcontainers-elasticsearch</artifactId>
    <version>1.1.0</version>
    <scope>test</scope>
</dependency>
```

#### Using Junit 5 (version 1.2.1)

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.11.1</version>
    <scope>test</scope>
</dependency>
```

### Quick Start

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITElasticsearchTest {

    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer();
}
```

By default, the elasticsearch image used is 'elasticsearch / elasticsearch: 5.6.8'.
The URL of the image and the version is configurable. Below is an example to use version 6.4.2 of the image.

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITElasticsearchTest {

    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer(Versions.ELASTICSEARCH_VERSION);
}
```

Note that security is disabled.

When the docker container has started, the following properties are initialized in the spring boot context:

|   Spring boot property                |      Default value        |   Notes                           |
|:-------------------------------------:|:-------------------------:|:---------------------------------:|
|   spring.elasticsearch.jest.uris      | http://localhost:33001    | The port is generated randomly    |
|   spring.elasticsearch.rest.uris      | http://localhost:33001    | The port is generated randomly    |

The withRegisterSpringbootProperties (boolean) method is used to initialize or not the spring boot context of the properties above.

The name of the spring boot properties can be changed using the following methods:

| Méthode                      | Description                                                                                     |
|:----------------------------:|:-----------------------------------------------------------------------------------------------:|
| withJestUrisSystemProperty   | Modifies the property corresponding to uris of elasticsearch for jest                           |
| withRestUrisSystemProperty   | Modifies the property corresponding to the uris of elasticsearch for the Rest elasticsearch API |


### Initialization script (since 1.1.0)
An initialization script file can be provided using the fileInitScript parameter, in which case it will be executed against the local Elasticsearch cluster. 
The file extension defines the file format: json for JSON format, anything else for custom format.

#### JSON format
The provided JSON file should contain a list of requests to be sent, one by one, to the Elasticsearch cluster. Each request definition has three properties:
- the name (in uppercase) of the request method to be used for the current request (one of PUT, POST, DELETE)
- the path part of the URL (should not start with slash)
- the payload

The payload should not be defined for DELETE requests.
Some Elasticsearch requests do not require a payload (eg. POST index/_refresh), in which case define the payload as {}

Example:

To send a POST request to http://localhost:9200/test_index/test_type/_mapping, followed by a DELETE request to http://localhost:9200/test_index/test_type/1:
```json
[
    {
        "method": "POST",
        "path": "test_index/test_type/_mapping",
        "payload": {
            "test_type": {
                "properties": {
                    "name": {
                        "type": "keyword"
                    },
                    "lastModified": {
                        "type": "date"
                    }
                }
            }
        }
    },
    {
        "method": "DELETE",
        "path": "test_index/test_type/1"
    }
]
```

#### Custom format

Each line defines a request to be sent to the Elasticsearch cluster, and it has three parts separated by colon ':'

- the name (in uppercase) of the request method to be used for the current request (one of PUT, POST, DELETE)
- the path part of the URL (should not start with slash)
- the JSON to send to Elasticsearch as payload (it should be empty for DELETE requests)

Note: Empty lines are ignored, as well as lines starting with the '#' sign.

Examples :

To send a POST request to http://localhost:9200/test_index/test_type/_mapping:
```text
POST:test_index/test_type/_mapping:{ "test_type" : { "properties" : { "name" : { "type" : "keyword" }, "lastModified" : { "type" : "date" } } } }
```

To send a DELETE request to http://localhost:9200/test_index/test_type/1 without content; note the colon at the end, for there is no JSON data in case of a DELETE.
```text
DELETE:test_index/test_type/1:
```

#### Sample with Junit 5 (version 1.2.1)

```java
@Testcontainers
public class ITElasticsearchTest {

    @Container
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer();
}
```

## Kafka

### Add the Maven dependency

```xml
<dependency>
    <groupId>com.github.ydespreaux.testcontainers</groupId>
    <artifactId>testcontainers-kafka</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

#### Using Junit 5 (version 1.2.1)

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.11.1</version>
    <scope>test</scope>
</dependency>
```

### Container Kafka

#### Quick Start

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITKafkaTest {

    @ClassRule
    public static final ConfluentKafkaContainer kafkaContainer = new ConfluentKafkaContainer();
}
```
By default the Kafka image used is 'confluentinc / cp-kafka: 3.3.1'.

The version is configurable. Below is an example to use version 4.0.0 of the image.
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITKafkaTest {

    @ClassRule
    public static final ConfluentKafkaContainer mySqlContainer = new ConfluentKafkaContainer("4.0.0");
}
```

#### Activate schema registry.

By default, the registry schema is not active. Below is an example to activate the registry schema.

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITKafkaTest {

    @ClassRule
    public static final ConfluentKafkaContainer mySqlContainer = new ConfluentKafkaContainer().withSchemaRegistry(true);
}
```

#### Configuration


When the docker container has started, the following properties are initialized in the spring boot context:

|   Spring boot property                        |      Default value        |   Notes                                                                                                               |
|:---------------------------------------------:|:-------------------------:|:---------------------------------------------------------------------------------------------------------------------:|
|   spring.kafka.bootstrap-servers              | localhost:33001           | The broker's port is generated randomly                                                                               |
|   spring.kafka.properties.schema.registry.url | http://localhost:33002    | The registry schema port is generated randomly. This property is initialized only if the registry schema is enabled.  |

The withRegisterSpringbootProperties (boolean) method is used to initialize or not the spring boot context of the properties above.

The name of the spring boot properties can be changed using the following methods:

| Method                            | Description                                                       | Default value                                 |
|:---------------------------------:|:-----------------------------------------------------------------:|:---------------------------------------------:|
| withBrokerServersSystemProperty   | Modifies the property corresponding to the uris of the brokers    | spring.kafka.bootstrap-servers                |
| withSchemaRegistrySystemProperty  | Modifies the property corresponding to the url of schema registry | spring.kafka.properties.schema.registry.url   |

#### Access to urls

| Method                    | Description                                   | Default value         |
|:-------------------------:|:---------------------------------------------:|:---------------------:|
| getBootstrapServers()     | Return the kafka brokers url                  | localhost:33001       |
| getZookeeperServer()      | Returns the url of the zookeeper server       | localhost:33001       |
| getZookeeperConnect()     | Returns the local url of the zookeeper server | zookeeper:33001       |

#### Topics configuration

Added the withTopic method to create a topic with the name of the topic, the number of partitions and the type of topic compacted or not.

| Method                                | Description                                         |
|:-------------------------------------:|:---------------------------------------------------:|
| withTopic(topic, partitions, compact) | Creating a topic when the container is initialized  |
| withTopic(TopicConfiguration)         | Creating a topic when the container is initialized  |

Example:

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITKafkaTest {

    @ClassRule
    public static final ConfluentKafkaContainer mySqlContainer = new ConfluentKafkaContainer()
        .withTopic("topic1-non-compact", 3, false)
        .withTopic("topic2-compact", 3, true);
}
```
#### Sécurité SSL (version 1.2.0).

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITKafkaTest {

    public static final Certificates kafkaServerCertificates = new Certificates("secrets/kafka.server.keystore.jks", "0123456789", "secrets/kafka.truststore.jks", "0123456789");
    public static final Certificates kafkaClientCertificates = new Certificates("secrets/kafka.client.keystore.jks", "0123456789", "secrets/kafka.truststore.jks", "0123456789");

    @ClassRule
    public static final ConfluentKafkaContainer mySqlContainer = new ConfluentKafkaContainer()
        .withKafkaServerCertificates(kafkaServerCertificates)
        .withKafkaClientCertificates(kafkaClientCertificates)
        .withTopic("TOPIC_1", 1, true)
        .withAcls(new AclsOperation[]{AclsOperation.READ, AclsOperation.WRITE}, "TOPIC_1", "my-group");
}    
```

When the docker container has started, the properties corresponding to the SSL configuration are initialized in the context of spring boot:

|   Spring boot property                       |   Description                                                                                                                       |
|:---------------------------------------------:|:-----------------------------------------------------------------:|
|   spring.kafka.security.protocol              | Security protocol : SSL                                       | 
|   spring.kafka.ssl.key-password               | Keystore password (client)  | 
|   spring.kafka.ssl.key-store-location         | Keystore location (client)| 
|   spring.kafka.ssl.key-store-password         | Keystore password| 
|   spring.kafka.ssl.trust-store-location       | Truststore location| 
|   spring.kafka.ssl.trust-store-password       | Truststore password| 
|   spring.kafka.properties.ssl.endpoint.identification.algorithm | Identification algorithm set to empty ("")| 

If the client certificate is not initialized when the container is launched, the spring boot properties related to the SSL configuration will not be initialized.

The name of the spring boot properties can be changed using the following methods:

| Methode                          | Default value                             |
|:---------------------------------:|:---------------------------------------------:|
| withSecurityProtocolSystemProperty    | spring.kafka.security.protocol                |
| withKeyPasswordSystemProperty  |     spring.kafka.ssl.key-password   |
| withKeystoreLocationSystemProperty  |     spring.kafka.ssl.key-store-location   |
| withKeystorePasswordSystemProperty  |     spring.kafka.ssl.key-store-password   |
| withTruststoreLocationSystemProperty  |     spring.kafka.ssl.trust-store-location   |
| withTruststorePasswordSystemProperty  |     spring.kafka.ssl.trust-store-password   |
| withIdentificationAlgorithmSystemProperty  | spring.kafka.properties.ssl.endpoint.identification.algorithm   |

#### Sample with Junit 5 (version 1.2.1)

```java
@Testcontainers
public class ITKafkaTest {

    @Container
    public static final ConfluentKafkaContainer mySqlContainer = new ConfluentKafkaContainer("4.0.0");
}
```

### Container Kafka Connect

#### Quick Start

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITKafkaTest {

    @ClassRule
    public static final ConfluentKafkaConnectContainer kafkaContainer = new ConfluentKafkaConnectContainer();
}
```
By default the Kafka connect image used is 'confluentinc / cp-kafka-connect: 4.1.0'. Compatibility of Kafka messages is fixed with version 0.11.0 by default.

The version is configurable. Below is an example to use version 4.0.0 of the image.
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITKafkaTest {

    @ClassRule
    public static final ConfluentKafkaContainer mySqlContainer = new ConfluentKafkaContainer("4.0.0");
}
```

#### Configuration

When the docker container has started, the following properties are initialized in the spring boot context:

|   Spring boot property                       |      Default value    |   Notes                                                                                                                    |
|:---------------------------------------------:|:-------------------------:|:---------------------------------------------------------------------------------------------------------------------:|
|   spring.kafka.bootstrap-servers              | localhost:<port>          | The broker's port is generated randomly                                                                               |
|   spring.kafka.properties.schema.registry.url | http://localhost:<port>   | The registry schema port is generated randomly. This property is initialized only if the registry schema is enabled.  |
|   spring.kafka-connect.rest-app               | http://localhost:<port>   | The port of the Rest application is generated randomly.                                                               |

The withRegisterSpringbootProperties (boolean) method is used to initialize or not the spring boot context of the properties above.

The name of the spring boot properties can be changed using the following methods:

| Method                            | Description                                                              | Default value                                 |
|:---------------------------------:|:------------------------------------------------------------------------:|:---------------------------------------------:|
| withBrokerServersSystemProperty   | Modifies the property corresponding to the uris of the brokers           | spring.kafka.bootstrap-servers                |
| withSchemaRegistrySystemProperty  | Modifies the property corresponding to the url of schema registry        | spring.kafka.properties.schema.registry.url   |
| withRestAppSystemProperty         | Modifies the property corresponding to the url of the REST application   | spring.kafka-connect.rest-app                 |


Initialization method of the worker:

| Method                            | Description                                           | Default value                                 |
|:---------------------------------:|:-----------------------------------------------------:|:---------------------------------------------:|
| withKeyConverter                  | Defines the type of "Converter" of the message keys   | org.apache.kafka.connect.json.JsonConverter   |
| withValueConverter                | Defines the type of "Converter" message values        | org.apache.kafka.connect.json.JsonConverter   |
| withPlugins                       | Add plugins                                           | No plugin                                     |

In the case of an Avro converter, the registry schema is automatically configured if it has been activated.

Herder initialization method in standalone mode:

| Method                            | Description                       | Default value             |
|:---------------------------------:|:---------------------------------:|:-------------------------:|
| withOffsetStorageFilename         | Name of the offsets storage file  | connect-offsets-file.txt  |

Herder initialization method in distributed mode:

| Method                            | Description                               | Default value         |
|:---------------------------------:|:-----------------------------------------:|:---------------------:|
| withGroupId                       | Name of the group                         | kafka-connect-group   |
| withConfigStorageTopic            | Name of the configuration topic           | connect-config        |
| withOffsetStorageTopic            | Name of the offsets topic                 | connect-offsets       |
| withOffsetStoragePartition        | Number of offsets topic partitions        | 3                     |
| withStatusStorageTopic            | Name of the status topic                  | connect-status        |
| withStatusStoragePartition        | Number of partitions of the status topic  | 3                     |


#### Access to urls

| Method                   | Description                                    | Default value             |
|:-------------------------:|:---------------------------------------------:|:-------------------------:|
| getBootstrapServers()     | Return the kafka brokers url                  | localhost:<port>          |
| getZookeeperServer()      | Returns the url of the zookeeper server       | localhost:<port>          |
| getZookeeperConnect()     | Returns the local url of the zookeeper server | zookeeper:<port>          |
| getRestAppServers()       | Returns the url of the REST application       | http://localhost:<port>   |

#### Sample with Junit 5 (version 1.2.1)

```java
@Testcontainers
public class ITKafkaTest {

    @Container
    public static final ConfluentKafkaContainer mySqlContainer = new ConfluentKafkaContainer("4.0.0");
}
```
