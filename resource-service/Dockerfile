FROM gradle:9.5.1-jdk17-alpine AS build
WORKDIR /app

COPY gradlew ./
COPY gradle/ ./gradle/
COPY build.gradle settings.gradle gradle.properties ./

COPY resource-service/build.gradle resource-service/
COPY resource-service/gradle.properties resource-service/

RUN cp settings.gradle settings.gradle.orig \
  && printf "rootProject.name = 'music-microservices'\ninclude 'resource-service'\n" > settings.gradle

RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

RUN mv settings.gradle.orig settings.gradle
COPY resource-service/ ./resource-service/
COPY song-service/ ./song-service/

RUN ./gradlew :resource-service:clean :resource-service:assemble --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/resource-service/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
