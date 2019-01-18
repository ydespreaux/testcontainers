CREATE KEYSPACE IF NOT EXISTS testcontainers_unit WITH replication = {'class':'SimpleStrategy','replication_factor':'1'};

USE testcontainers_unit;

CREATE TABLE IF NOT EXISTS model
(
  id varchar,
  timestamp timestamp,
  PRIMARY KEY (id, timestamp)

) WITH CLUSTERING ORDER BY (timestamp DESC);
