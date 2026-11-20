FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN --mount=type=cache,target=/root/.m2 ./mvnw -q dependency:go-offline
COPY src src
# Os testes sobem um PostgreSQL próprio (Testcontainers) e precisam do Docker, então rodam fora da construção da imagem.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -q package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
USER app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
