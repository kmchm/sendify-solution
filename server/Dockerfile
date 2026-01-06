FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY . .

# Install Node.js, npm, and Playwright browsers with dependencies
RUN apt-get update && \
    apt-get install -y wget gnupg ca-certificates && \
    wget -qO- https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g playwright && \
    npx playwright install --with-deps && \
    rm -rf /var/lib/apt/lists/*

RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/server-0.0.1-SNAPSHOT.jar"]

EXPOSE 8081