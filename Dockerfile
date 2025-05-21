# 使用 Maven 來建構你的 Spring Boot 專案
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# 複製所有檔案並建構 JAR
COPY . .
RUN mvn clean package -DskipTests

# 第二階段：只放 JAR 檔案，讓映像檔更小
FROM eclipse-temurin:21-jdk

WORKDIR /app

# 複製建構出來的 JAR
COPY --from=builder /app/target/*.jar app.jar

# 開放 port
EXPOSE 8080

# 設定環境 port 可動態注入
ENV PORT=8080
ENV JAVA_OPTS=""

# 啟動指令，支援從環境注入的 port
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]