# Step 1: Build Java App using Gradle and Java 21
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# Step 2: Run Java App & Python Scripts
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Install Python 3 and Pip
RUN apt-get update && apt-get install -y python3 python3-pip python3-venv && rm -rf /var/lib/apt/lists/*

# Copy the built jar from the gradle build folder
COPY --from=build /app/build/libs/*.jar app.jar

# Copy the python scripts
COPY youtube_analyzer/ youtube_analyzer/

# Install python requirements
# Render Ubuntu image allows `--break-system-packages` or using a venv.
RUN pip3 install -r youtube_analyzer/requirements.txt --break-system-packages || pip3 install -r youtube_analyzer/requirements.txt

EXPOSE 2503
ENTRYPOINT ["java", "-jar", "app.jar"]
