FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY . .

RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/server-0.0.1-SNAPSHOT.jar"]

EXPOSE 8081