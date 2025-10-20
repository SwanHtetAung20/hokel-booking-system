FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn -B -T 1C clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runner

RUN addgroup -S springrp && adduser -S springusr -G springrp

USER springusr

WORKDIR /app

COPY --from=builder /app/target/analytics-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]