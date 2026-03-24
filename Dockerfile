# ===== 构建阶段 =====
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
# 预下载依赖（利用 Docker 缓存）
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# ===== 运行阶段 =====
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 时区设置
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

COPY --from=builder /build/target/mediRAG-0.0.1-SNAPSHOT.jar app.jar

# JVM 优化参数
EXPOSE 8080
ENTRYPOINT ["java", \
  "-Xms512m", "-Xmx1g", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
