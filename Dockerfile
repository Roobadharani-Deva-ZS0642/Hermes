# Stage 1: Stage that builds the application, a prerequisite for the running stage
FROM eclipse-temurin:21-jdk-jammy AS builder

# Set working directory for the build
WORKDIR /usr/src/app

# Install necessary build tools (maven in this case)
RUN apt-get update -y && apt-get install -y maven --no-install-recommends && rm -rf /var/lib/apt/lists/*

# Copy pom.xml and dependencies to cache them and speed up future builds
COPY pom.xml ./

# Copy all needed project files to a folder
COPY src src

# Build the Spring Boot JAR (adjust the command if you use Gradle or a different build tool)
RUN mvn clean package -DskipTests

# Stage 2: Running stage
FROM eclipse-temurin:21-jre-jammy AS runtime

# Set working directory for the runtime
WORKDIR /usr/app

# Copy the application.properties file from the source (adjust path if needed)
COPY --from=builder /usr/src/app/src/main/resources/application.properties /usr/app/hermes-application.properties

# Define the build argument
ARG IMAGE_TAG

# Copy the built JAR from the builder stage (assumes the JAR is in target/)
COPY --from=builder /usr/src/app/target/hermes-*.jar /usr/app/hermes-${IMAGE_TAG}.jar