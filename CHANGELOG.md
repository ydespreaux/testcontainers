# Changelog

## [Unreleased]
### Added
    
### Changed

### Fixed

## [1.2.1]
    
### Changed

- Migration to jdk 11
- Compatibility Junit 5

### Fixed

- The call to the withSecurityProtocolSystemProperty () method injects the broker URL instead of the 'SSL' value 

## [1.2.0]
### Added
- [lib-testcontainers-kafka] :
    - SSL connection
    - Acls configuration
    
### Changed

### Fixed

## [1.1.0]
### Added
- [lib-testcontainers-elasticsearch] :
    - Add configuration directory (add method withConfigDirectory(String))
    - Add scripts initialization (add method withFileInitScript(String))

### Changed
- Dependency updates
    - testcontainers from 1.8.3 to 1.10.6
- Remove deprecated methods

### Fixed

### [1.0.0]

- [lib-testcontainers-elasticsearch] :
    - Add Elasticsearch container
- [lib-testcontainers-kafka] :
    - Add kafka container
    - Creating topics when starting the container.
    - Added docker image confluentinc / cp-schema-registry
    - ConfluentKafkaConnectContainer container added based on docker image confluentinc / cp-kafka-connect        
- [lib-testcontainers-cassandra] :
    - Add cassandra container
    - Execution of cassandra schema initialization scripts
    - Addition of the Cassandra container
- [lib-testcontainers-mysql] :
    - Added the MySQL container
    - Added the withMySqlInitDirectory method to add a set of initialization scripts contained in a directory
    - Execution of an initialization script of the MySQL database    
