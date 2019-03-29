#!/bin/bash
# README:
# Run this script to produce a ./secrets/ folder which hold generated trustore, keystores and ssl.config files
# The docker-compose file mount ./secrets/ and start Kafka with SSL enpoint on localhost:9093
# 2 certificat are created (and only one trustore):
#     kafka.server.keystore.jks -> The 'admin' user, you must run the "Insert acl" command below to setup kafka cluster acl to provide full authorization for him
#     kafka.client.keystore.jks -> A second user to use in consumer/producer if needed (do not forget to setup ACL for him)
#
# Note: passwords are 0123456789

# check keystore
#keytool -list -v -keystore kafka.server.keystore.jks

# Insert acl
#kafka-acls --authorizer-properties zookeeper.connect=zookeeper1:2181 --add --allow-principal User:"CN=s3local,OU=test,O=suez,L=test,ST=laposte,C=fr" --operation All --group=* --topic=* --cluster kafka-cluster

# Produce message
#kafka-console-producer --broker-list localhost:9093 --topic test --producer.config /etc/kafka/secrets/server.ssl.config

PASSWORD="0123456789"
SERVER_KEYSTORE_JKS="kafka.server.keystore.jks"
CLIENT_KEYSTORE_JKS="kafka.client.keystore.jks"
TRUSTSTORE_JKS="kafka.truststore.jks"


echo "Clearing existing Kafka SSL certs..."
rm -rf secrets
mkdir secrets

echo "Generating new Kafka SSL certs..."
cd secrets

# Create keystores
keytool -keystore $SERVER_KEYSTORE_JKS -alias localhost -validity 1000 -genkey -keyalg RSA -ext SAN=DNS:localhost,DNS:kafka -dname "CN=cn.kafka.server.fr, OU=None, O=github, L=None,ST=None, C=fr" -keypass $PASSWORD -storepass $PASSWORD
keytool -keystore $CLIENT_KEYSTORE_JKS -alias localhost -validity 1000 -genkey -keyalg RSA -ext SAN=DNS:localhost,DNS:kafka -dname "CN=cn.kafka.client.fr, OU=None, O=github, L=None,ST=None, C=fr" -keypass $PASSWORD -storepass $PASSWORD

# Create a CA
# on linux: "/C=fr/ST=None/L=None/O=github/CN=cn.ca"
openssl req -new -x509 -keyout server.ca.key -out server.ca.cert -days 1000 -passout pass:$PASSWORD -subj "//C=fr\ST=None\L=None\O=github\CN=cn.ca"

# Create a truststore and import the CA
keytool -keystore $TRUSTSTORE_JKS -alias CARoot -import -file server.ca.cert -keypass $PASSWORD -storepass $PASSWORD -noprompt

# Export cert:
keytool -keystore $SERVER_KEYSTORE_JKS -alias localhost -certreq -file server.cert -keypass $PASSWORD -storepass $PASSWORD
keytool -keystore $CLIENT_KEYSTORE_JKS -alias localhost -certreq -file client.cert -keypass $PASSWORD -storepass $PASSWORD


# Sign the cert with the CA
openssl x509 -req -CA server.ca.cert -CAkey server.ca.key -in server.cert -out server.signed.cert -days 1000 -CAcreateserial -passin pass:$PASSWORD
openssl x509 -req -CA server.ca.cert -CAkey server.ca.key -in client.cert -out client.signed.cert -days 1000 -CAcreateserial -passin pass:$PASSWORD

# Import CA and signed cert in keystore
keytool -keystore $SERVER_KEYSTORE_JKS -alias CARoot -import -file server.ca.cert -keypass $PASSWORD -storepass $PASSWORD -noprompt
keytool -keystore $SERVER_KEYSTORE_JKS -alias localhost -import -file server.signed.cert -keypass $PASSWORD -storepass $PASSWORD -noprompt

keytool -keystore $CLIENT_KEYSTORE_JKS -alias CARoot -import -file server.ca.cert -keypass $PASSWORD -storepass $PASSWORD -noprompt
keytool -keystore $CLIENT_KEYSTORE_JKS -alias localhost -import -file client.signed.cert -keypass $PASSWORD -storepass $PASSWORD -noprompt

rm -rf *.cert *.srl *.key
