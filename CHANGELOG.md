# Changelog

## [Unreleased]
### Added
- Add synomnyms configuration

### Changed
- Criteria query for jpa

### Fixed

### [1.0.0]

- [lib-testcontainers-elasticsearch] :
    - Ajout du container Elasticsearch
- [lib-testcontainers-kafka] :
    - Ajout du container Kafka
    - Création de topics au démarrage du conteneur.
    - Ajout de l'image docker confluentinc/cp-schema-registry
    - Ajout du container ConfluentKafkaConnectContainer basée sur l'image docker confluentinc/cp-kafka-connect
- [lib-testcontainers-cassandra] :
    - Execution de scripts d'initialisation du schéma cassandra
    - Ajout du container Cassandra
- [lib-testcontainers-mysql] :
    - Ajout du container MySQL
    - Ajout de la méthode withMySqlInitDirectory permettant d'ajouter un ensemble de scripts d'initialisation contenus dans un répertoire
    - Execution d'un script d'initialisation de la base de données MySQL
- [lib-testcontainers-mssql] :
    - Ajout du container MSSQLServerContainer basée sur l'image docker microsoft/mssql-server-linux
