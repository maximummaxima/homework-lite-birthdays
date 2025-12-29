ARG RUNTIME_IMAGE=eclipse-temurin:24-jre

FROM maven:3.9-eclipse-temurin-24 AS build

WORKDIR /workspace

# Copy sources
COPY . .

# Build the application JAR and collect runtime dependencies.
# (The repository contains mvnw scripts, but does not include Maven Wrapper metadata.
#  Using the Maven official image keeps the build reproducible.)
RUN mvn -B -ntp -DskipTests package \
    && mvn -B -ntp -DskipTests dependency:copy-dependencies \
      -DincludeScope=runtime \
      -DoutputDirectory=target/dependency \
    && mkdir -p /out/libs \
    && JAR_FILE="$(ls -1 target/*.jar | grep -vE '(sources|javadoc|original)' | head -n 1)" \
    && test -n "$JAR_FILE" \
    && cp "$JAR_FILE" /out/app.jar \
    && cp -r target/dependency/. /out/libs/

FROM ${RUNTIME_IMAGE} AS runtime

WORKDIR /app

COPY --from=build /out/app.jar /app/app.jar
COPY --from=build /out/libs /app/libs

# Run the CLI app; arguments from `docker run ...` are appended automatically.
ENTRYPOINT ["java", "-cp", "/app/app.jar:/app/libs/*", "academy.Application"]
