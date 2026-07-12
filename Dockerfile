# Stage 1: Build
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Cache gradle wrapper and dependencies
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
ENV GITHUB_ACTOR=$GITHUB_ACTOR
ENV GITHUB_TOKEN=$GITHUB_TOKEN
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Copy source code and build
COPY src src
RUN ./gradlew clean bootJar -x test --no-daemon

# Extract layers
RUN java -Djarmode=tools -jar build/libs/*.jar extract --layers --launcher --destination extracted

# Stage 2: Runtime
FROM eclipse-temurin:25-jre
WORKDIR /app

# Create a non-root user
RUN addgroup --system spring-user && adduser --system --ingroup spring-user spring-user
USER spring-user:spring-user

# Copy extracted layers from builder stage
COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./

# Expose port (adjust if needed)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]


