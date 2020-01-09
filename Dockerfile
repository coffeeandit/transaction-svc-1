FROM openjdk:13
RUN mkdir /tmp/transaction-svc
ADD . /tmp/transaction-svc
RUN chmod +x /tmp/transaction-svc/gradlew
WORKDIR /tmp/transaction-svc
RUN ls -lsah
RUN ./gradlew clean build
RUN mv /tmp/transaction-svc/build/libs/transaction-svc-0.0.1-SNAPSHOT.jar /tmp
RUN rm -rf /tmp/transaction-svc/
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/tmp/transaction-svc-0.0.1-SNAPSHOT.jar"]
