FROM maven:3.9.3-eclipse-temurin-17 AS build

WORKDIR /app
COPY . .

# Выполним production-сборку с frontend-подготовкой
RUN mvn clean install -Pproduction -DskipTests

FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
