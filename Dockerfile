# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle ./gradle
COPY shared-kernel ./shared-kernel
COPY modules ./modules
COPY app ./app
RUN chmod +x gradlew && ./gradlew :app:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S aionn && adduser -S aionn -G aionn
WORKDIR /app
COPY --from=builder /workspace/app/build/libs/*.jar app.jar
RUN chown -R aionn:aionn /app
USER aionn
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8"
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1
