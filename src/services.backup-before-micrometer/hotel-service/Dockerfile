FROM maven:3.9-eclipse-temurin-11 AS builder
WORKDIR /app

RUN echo '163.181.1.176 maven.aliyun.com' >> /etc/hosts && \
    echo '163.181.1.175 maven.aliyun.com' >> /etc/hosts && \
    echo '163.181.1.174 maven.aliyun.com' >> /etc/hosts && \
    echo '104.18.18.12 repo1.maven.org' >> /etc/hosts && \
    echo '104.18.19.12 repo.maven.apache.org' >> /etc/hosts

ENV MAVEN_OPTS="-Dmaven.wagon.http.pool=false -Dmaven.wagon.http.retryHandler.count=20 -Djava.net.preferIPv4Stack=true"
COPY maven-settings.xml /root/.m2/settings.xml
COPY pom.xml ./
COPY src ./src

RUN mvn -B -s /root/.m2/settings.xml \
    -Dmaven.wagon.rto=1800000 \
    -Dmaven.wagon.http.connectionTimeout=300000 \
    -Dmaven.test.skip=true \
    package

FROM eclipse-temurin:8-jre-alpine
WORKDIR /app
RUN apk add --no-cache bash netcat-openbsd
COPY --from=builder /app/target/*.jar app.jar
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
