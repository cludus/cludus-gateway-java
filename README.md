# Cludus Gateway (Java)

Cludus Gateway implementation in java

# Stack

This implementation uses Spring Boot 3, Java 17

# Deployment

The docker image can be deployed using 

    docker run ghcr.io/cludus/gateway-java

For docker compose use the following script

    version: "3.9"
    services:
       gateway:
         ports:
           - 8080:8080
         image: ghcr.io/cludus/gateway-java

# Development

Creating a new release can be done with the following gradle task

    gradle release -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=1.0.0 -Prelease.newVersion=1.1.0-SNAPSHOT
