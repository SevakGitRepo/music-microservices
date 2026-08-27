FROM gradle:9.5.1-jdk17-alpine AS build

WORKDIR /app

COPY gradlew ./
COPY gradle/ ./gradle/
COPY build.gradle settings.gradle gradle.properties ./

COPY api-gateway/build.gradle api-gateway/

RUN cp settings.gradle settings.gradle.orig \
  && printf "rootProject.name = 'music-microservices'\ninclude 'api-gateway'\n" > settings.gradle

RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

COPY api-gateway/ ./api-gateway/

RUN ./gradlew :api-gateway:clean :api-gateway:assemble --no-daemon -x test

RUN mv settings.gradle.orig settings.gradle

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/api-gateway/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
