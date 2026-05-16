#!/bin/bash

declare -A PORTS=(
    [session-service]=8081
    [hotel-service]=8082
    [booking-service]=8083
    [payment-service]=8084
    [loyalty-service]=8085
    [report-service]=8086
    [gateway-service]=8087
)

for svc in "${!PORTS[@]}"; do
    port=${PORTS[$svc]}

    cat > "$svc/Dockerfile" << EOF
FROM maven:3.9-eclipse-temurin-11 AS builder
WORKDIR /app
ENV MAVEN_OPTS="-Dmaven.wagon.http.pool=false -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 -Dmaven.wagon.http.retryHandler.count=5 -Dhttp.keepAlive=false"
COPY maven-settings.xml /root/.m2/settings.xml
COPY pom.xml ./
RUN mvn -B -s /root/.m2/settings.xml -Dmaven.wagon.rto=300000 dependency:go-offline
COPY src ./src
RUN mvn -B -s /root/.m2/settings.xml -Dmaven.wagon.rto=300000 package -DskipTests

FROM eclipse-temurin:8-jre-alpine
WORKDIR /app
RUN apk add --no-cache bash netcat-openbsd
COPY --from=builder /app/target/*.jar app.jar
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh
EXPOSE $port
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF

    cat > "$svc/.dockerignore" << 'EOF'
target/
.idea/
.vscode/
*.iml
.git/
.gitignore
EOF

    cat > "$svc/maven-settings.xml" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>aliyun-all</id>
      <name>Aliyun Public — все репозитории</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>*</mirrorOf>
    </mirror>
  </mirrors>
</settings>
EOF

    echo "Создан $svc (порт $port)"
done

echo "Готово!"
