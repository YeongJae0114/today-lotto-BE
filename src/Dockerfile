# ---- build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# gradle 캐시 최적화
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies || true

# 소스 복사 후 빌드
COPY . .
RUN ./gradlew --no-daemon clean bootJar

# ---- runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd -ms /bin/bash appuser
USER appuser

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV SERVER_PORT=8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
