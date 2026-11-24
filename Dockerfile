# Stage 1: Build the application using Maven and Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy project source and package executable jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create lightweight JRE runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Run as non-root user for security best practice
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy compiled jar from build stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
