# Stage 1: Build the application
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy project files
COPY . .

# Run Maven build (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/moneymanager-0.0.1-SNAPSHOT.jar moneymanager-v1.0.jar

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "moneymanager-v1.0.jar"]