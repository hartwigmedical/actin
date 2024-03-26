FROM eclipse-temurin:11-jre

ARG VERSION=local-SNAPSHOT

COPY target/algo-${VERSION}-jar-with-dependencies.jar /usr/local/actin.jar

ENTRYPOINT ["java", "-cp", "/usr/local/actin.jar", "com.hartwig.actin.algo.TreatmentMatcherApplicationKt"]
