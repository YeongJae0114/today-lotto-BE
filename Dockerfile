# ---- build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# gradle wrapper & 설정 파일 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# 전체 소스 복사
COPY . .

# 빌드
RUN ./gradlew --no-daemon clean bootJar

# ---- runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd -ms /bin/bash appuser
USER appuser

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
