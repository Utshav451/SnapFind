# Build stage using official Eclipse Temurin JDK 25
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Install Maven package
RUN apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

#Production runtime stage using official Eclipse Temurin JRE 25
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
