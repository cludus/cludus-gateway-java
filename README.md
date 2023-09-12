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
