########## 1) Build Stage ##########
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Gradle wrapper / 설정 먼저 복사 (Docker layer cache 최대화)
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle* settings.gradle* gradle.properties* ./

# gradlew 실행 권한
RUN chmod +x gradlew

# 소스 복사
COPY . .

# BuildKit 캐시(매우 추천): CI에서도 캐시-from/cache-to로 크게 빨라짐
# bootJar 생성 (+ 테스트는 CI 파이프라인에서 별도 수행하는 걸 추천)
RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew --no-daemon clean bootJar -x test


########## 2) Runtime Stage ##########
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Non-root 고정 (UID/GID 고정은 쿠버네티스/보안정책에서 특히 유리)
RUN groupadd -g 10001 app && useradd -m -u 10001 -g 10001 -s /usr/sbin/nologin appuser

COPY --from=builder --chown=appuser:app /app/build/libs/app.jar /app/app.jar

USER appuser
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=25 -Dfile.encoding=UTF-8"
ENTRYPOINT ["java","-jar","/app/app.jar"]