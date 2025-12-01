# 1. 빌드 단계
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
# 실행 권한 부여 및 빌드 (테스트 생략)
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# 2. 실행 단계 (여기 이미지를 변경했습니다!)
# 기존 openjdk:17-jdk-slim 대신 eclipse-temurin 사용
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]