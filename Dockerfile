# 使用 Java 11 版本的 Maven 建構環境
FROM maven:3.8.6-openjdk-11 AS builder

WORKDIR /app

# 先複製 pom.xml 下載基礎依賴以利用 Docker 快取層
COPY pom.xml .
RUN mvn dependency:resolve -B

# 複製源碼進行打包（略過測試）
COPY src ./src
RUN mvn clean package -DskipTests

# 第二階段採用輕量 OpenJDK 11 JRE 映像檔執行
FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV PORT=8080
# 針對 Render 512MB 限制與常駐環境的 JVM 最佳化配置
ENV JAVA_OPTS="-Xms128m -Xmx256m -Xss256k -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom -XX:+UseContainerSupport"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]
