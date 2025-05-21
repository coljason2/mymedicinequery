# 用 Java 8 版本的 Maven 建構環境
FROM maven:3.8.6-openjdk-8 AS builder

WORKDIR /app

COPY . .

# 跳過測試打包
RUN mvn clean package -DskipTests

# 第二階段用 OpenJDK 8 來執行 jar
FROM openjdk:8-jdk-alpine3.9

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV PORT=8080
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]