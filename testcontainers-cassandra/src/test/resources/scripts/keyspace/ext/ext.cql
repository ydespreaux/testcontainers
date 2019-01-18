CREATE KEYSPACE IF NOT EXISTS testcontainers_ext_unit WITH replication = {'class':'SimpleStrategy','replication_factor':'1'};

USE testcontainers_ext_unit;

CREATE TABLE IF NOT EXISTS ext
(
  id varchar,
  timestamp timestamp,
  PRIMARY KEY (id, timestamp)

) WITH CLUSTERING ORDER BY (timestamp DESC);
