# Step 1: Build Java App using Gradle and Java 21
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# Step 2: Run Java App
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy the built jar from the gradle build folder
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 2503
ENTRYPOINT ["java", "-jar", "app.jar"]
