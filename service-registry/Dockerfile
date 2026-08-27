FROM gradle:9.5.1-jdk17-alpine AS build

WORKDIR /app

COPY gradlew ./
COPY gradle/ ./gradle/
COPY build.gradle settings.gradle gradle.properties ./

COPY service-registry/build.gradle service-registry/
COPY service-registry/gradle.properties service-registry/

RUN cp settings.gradle settings.gradle.orig \
  && printf "rootProject.name = 'music-microservices'\ninclude 'service-registry'\n" > settings.gradle

RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

COPY service-registry/ ./service-registry/

RUN ./gradlew :service-registry:clean :service-registry:assemble --no-daemon -x test

RUN mv settings.gradle.orig settings.gradle

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/service-registry/build/libs/*.jar app.jar

EXPOSE 8761

CMD ["java", "-jar", "app.jar"]
