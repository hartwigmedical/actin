FROM eclipse-temurin:11-jre

ARG VERSION=local-SNAPSHOT

COPY target/clinical-${VERSION}-jar-with-dependencies.jar /usr/local/actin.jar

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=95", "-cp", "/usr/local/actin.jar", "com.hartwig.actin.clinical.ClinicalIngestionApplicationKt" ]
