# Dockerfile for product-service

# Stage 1: Build the application using Maven
FROM maven:3.9.8-eclipse-temurin-21-jammy AS build

# Set the working directory in the build image
WORKDIR /app

# Debug: List what's being copied
RUN pwd
RUN ls -la

# Copy the entire project to the Docker container
COPY . .

# Debug: List what was copied
RUN ls -la
RUN find . -type f -name "*.java"

# Build only the `product-service` module and package it as a JAR
RUN mvn clean install

# Debug: List all compiled classes
RUN find /app/target -type f -name "*.class"

# Debug: List the contents of the JAR
RUN jar tf /app/target/ProductService.jar

RUN find /app/target -name "*.class" | grep ProductServiceApplication

RUN ls -ltr /app/target/*.jar

# Stage 2: Create the runtime image with OpenJDK and application JAR
FROM eclipse-temurin:21-jdk-jammy

# Install necessary packages
RUN apt-get update && apt-get install -y net-tools iputils-ping && apt-get install -y openssl ca-certificates && rm -rf /var/lib/apt/lists/*

# Set the working directory in the runtime image
WORKDIR /app

# Copy the application JAR from the build image
COPY --from=build /app/target/ProductService.jar ProductService.jar

RUN ls -ltr ProductService.jar
RUN jar tf ProductService.jar | grep ProductServiceApplication

# Copy the keystore and truststore files
COPY keys/product-service-keystore-container.jks client-keystore.jks
COPY keys/client-truststore.jks client-truststore.jks

# Set permissions for keystore and truststore files
RUN chmod 644 /app/client-truststore.jks /app/client-keystore.jks

# Set environment variables for keystore and truststore
ENV CLIENT_KEY_STORE=/app/client-keystore.jks
ENV CLIENT_KEY_STORE_PASSWORD=123456
ENV CLIENT_TRUST_STORE=/app/client-truststore.jks
ENV CLIENT_TRUST_STORE_PASSWORD=123456
ENV GATEWAY_SERVICE_URL=api-gateway-service
ENV KEYCLOAK_GATEWAY_URL=keycloak-service
ENV KEYCLOAK_GATEWAY_PORT=8080
ENV CACERTS_PATH=${JAVA_HOME}/lib/security/cacerts

# Verify truststore exists
RUN if [ ! -f "${CLIENT_TRUST_STORE}" ]; then \
        echo "client-truststore.jks not found at ${CLIENT_TRUST_STORE}"; \
        exit 1; \
    fi


# Verify cacerts file exists and is writable
RUN if [ ! -f "${CACERTS_PATH}" ]; then \
        echo "cacerts file not found at ${CACERTS_PATH}"; \
        if [ -d "${CACERTS_PATH}" ]; then \
            echo "cacerts is a directory, listing contents:"; \
            ls -ltr "${CACERTS_PATH}"; \
            exit 1; \
        fi; \
        echo "Attempting to restore cacerts from base image"; \
        cp /opt/java/openjdk/lib/security/cacerts.bak "${CACERTS_PATH}" 2>/dev/null || { \
            echo "Failed to restore cacerts. Ensure base image contains cacerts."; \
            exit 1; \
        }; \
    fi && \
    chown root:root "${CACERTS_PATH}" && \
    chmod 664 "${CACERTS_PATH}"


RUN chown root:root "${CACERTS_PATH}" && chmod 664 "${CACERTS_PATH}"

# Import all certificates from client-truststore.jks into the default cacerts truststore
RUN keytool -importkeystore -srckeystore /app/client-truststore.jks \
            -destkeystore "${CACERTS_PATH}" \
            -srcstorepass "${CLIENT_TRUST_STORE_PASSWORD}" \
            -deststorepass "${CLIENT_TRUST_STORE_PASSWORD}" \
            -noprompt -srcstoretype JKS -deststoretype JKS > keytool_import.log 2>&1 || { \
                cat keytool_import.log; \
                echo "keytool import failed"; \
                exit 1; \
            }

# Verify the import by listing the cacerts contents (optional, for debugging)
RUN keytool -list -cacerts -storepass "${CLIENT_TRUST_STORE_PASSWORD}" > cacerts_list.log

# Run the API product-service application with SSL configuration
ENTRYPOINT ["java", \
            "-Dhttps.protocols=TLSv1.2,TLSv1.3", \
            "-Djavax.net.ssl.keyStore=${CLIENT_KEY_STORE}", \
            "-Djavax.net.ssl.keyStorePassword=${CLIENT_KEY_STORE_PASSWORD}", \
            "-Djavax.net.ssl.trustStore=${CACERTS_PATH}", \
            "-Djavax.net.ssl.trustStorePassword=${CLIENT_TRUST_STORE_PASSWORD}", \
            "-jar", "ProductService.jar"]