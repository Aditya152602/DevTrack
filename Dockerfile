
FROM maven:3.9-eclipse-temurin-26 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

RUN set -eux; \
    JAR="$(find target -maxdepth 1 -type f -name '*.jar' ! -name 'original-*' | head -n 1)"; \
    test -n "$JAR"; \
    cp "$JAR" /workspace/app.jar

FROM eclipse-temurin:26-jre

WORKDIR /app

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
