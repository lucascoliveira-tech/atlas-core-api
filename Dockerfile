FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw --batch-mode dependency:go-offline
COPY src src
RUN ./mvnw --batch-mode package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S atlas && adduser -S atlas -G atlas
WORKDIR /app
COPY --from=builder /workspace/target/atlas-core-api-*.jar app.jar
USER atlas
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

