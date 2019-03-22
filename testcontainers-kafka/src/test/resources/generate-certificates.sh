#!/bin/bash
# README:
# Run this script to produce a ./secrets/ folder which hold generated trustore, keystores and ssl.config files
# The docker-compose file mount ./secrets/ and start Kafka with SSL enpoint on localhost:9093
# 2 certificat are created (and only one trustore):
#     server.keystore.jks -> The 'admin' user, you must run the "Insert acl" command below to setup kafka cluster acl to provide full authorization for him
#     client.keystore.jks -> A second user to use in consumer/producer if needed (do not forget to setup ACL for him)
#
# Note: passwords are 0123456789

# check keystore
#keytool -list -v -keystore server.keystore.jks

# Insert acl
#kafka-acls --authorizer-properties zookeeper.connect=zookeeper1:2181 --add --allow-principal User:"CN=s3local,OU=test,O=suez,L=test,ST=laposte,C=fr" --operation All --group=* --topic=* --cluster kafka-cluster

# Produce message
#kafka-console-producer --broker-list localhost:9093 --topic test --producer.config /etc/kafka/secrets/server.ssl.config

mkdir secrets
cd secrets

# Create keystores
keytool -keystore server.keystore.jks -alias localhost -validity 1000 -genkey -keyalg RSA -ext SAN=DNS:localhost,DNS:kafka -dname "CN=kafka.server, OU=test, O=test, L=test,ST=github, C=fr" -keypass 0123456789 -storepass 0123456789
keytool -keystore client.keystore.jks -alias localhost -validity 1000 -genkey -keyalg RSA -ext SAN=DNS:localhost,DNS:kafka -dname "CN=kafka.client, OU=test, O=test, L=test,ST=github, C=fr" -keypass 0123456789 -storepass 0123456789

# Create a CA
openssl req -new -x509 -keyout server.ca.key -out server.ca.cert -days 1000 -passout pass:0123456789 -subj "//C=fr\ST=github\L=test\O=test\CN=kafka.ca"

# Create a truststore and import the CA
keytool -keystore truststore.jks -alias CARoot -import -file server.ca.cert -keypass 0123456789 -storepass 0123456789 -noprompt

# Export cert:
keytool -keystore server.keystore.jks -alias localhost -certreq -file server.cert -keypass 0123456789 -storepass 0123456789
keytool -keystore client.keystore.jks -alias localhost -certreq -file client.cert -keypass 0123456789 -storepass 0123456789


# Sign the cert with the CA
openssl x509 -req -CA server.ca.cert -CAkey server.ca.key -in server.cert -out server.signed.cert -days 1000 -CAcreateserial -passin pass:0123456789
openssl x509 -req -CA server.ca.cert -CAkey server.ca.key -in client.cert -out client.signed.cert -days 1000 -CAcreateserial -passin pass:0123456789

# Import CA and signed cert in keystore
keytool -keystore server.keystore.jks -alias CARoot -import -file server.ca.cert -keypass 0123456789 -storepass 0123456789 -noprompt
keytool -keystore server.keystore.jks -alias localhost -import -file server.signed.cert -keypass 0123456789 -storepass 0123456789 -noprompt

keytool -keystore client.keystore.jks -alias CARoot -import -file server.ca.cert -keypass 0123456789 -storepass 0123456789 -noprompt
keytool -keystore client.keystore.jks -alias localhost -import -file client.signed.cert -keypass 0123456789 -storepass 0123456789 -noprompt
