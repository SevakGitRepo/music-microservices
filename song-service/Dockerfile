FROM gradle:9.5.1-jdk17-alpine AS build

WORKDIR /app

COPY gradlew ./
COPY gradle/ ./gradle/
COPY build.gradle settings.gradle gradle.properties ./

COPY song-service/build.gradle song-service/
COPY song-service/gradle.properties song-service/

RUN cp settings.gradle settings.gradle.orig \
  && printf "rootProject.name = 'music-microservices'\ninclude 'song-service'\n" > settings.gradle

RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

RUN mv settings.gradle.orig settings.gradle
COPY resource-service/ ./resource-service/
COPY song-service/ ./song-service/

RUN ./gradlew :song-service:clean :song-service:assemble --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/song-service/build/libs/*.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]
