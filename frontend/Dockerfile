# --- Build Stage ---
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /home/app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# --- Run Stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /home/app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]