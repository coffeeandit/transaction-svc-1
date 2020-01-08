FROM openjdk:13
RUN ./gradlew clean build
ADD build/libs/transaction-svc-0.0.1-SNAPSHOT.jar /tmp

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/tmp/transaction-svc-0.0.1-SNAPSHOT.jar"]
