FROM eclipse-temurin:17-jre

ARG VERSION=local-SNAPSHOT

COPY target/report-${VERSION}-jar-with-dependencies.jar /usr/local/actin.jar

ENTRYPOINT ["java", "-cp", "/usr/local/actin.jar", "com.hartwig.actin.report.ReporterApplicationKt"]