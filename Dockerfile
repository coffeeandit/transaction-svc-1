FROM openjdk:13-slim
RUN mkdir /tmp/transaction-svc
ADD . /tmp/transaction-svc
RUN chmod +x /tmp/transaction-svc/gradlew
WORKDIR /tmp/transaction-svc
RUN ls -lsah
RUN ./gradlew clean build
RUN mv /tmp/transaction-svc/build/libs/*.jar /tmp/app.jar
RUN rm -rf /tmp/transaction-svc/
EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/tmp/app.jar"]
