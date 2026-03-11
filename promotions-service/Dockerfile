# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY gateway/pom.xml gateway/pom.xml
COPY customer-service/pom.xml customer-service/pom.xml
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY cart-service/pom.xml cart-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY checkout-service/pom.xml checkout-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY shipping-service/pom.xml shipping-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY pricing-service/pom.xml pricing-service/pom.xml
COPY tax-service/pom.xml tax-service/pom.xml
COPY promotions-service/pom.xml promotions-service/pom.xml
RUN mvn -q -DskipTests dependency:go-offline

COPY . .
RUN mvn -q -DskipTests -pl promotions-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/promotions-service/target/*.jar /app/app.jar
EXPOSE 8092
ENTRYPOINT ["java","-jar","/app/app.jar"]
