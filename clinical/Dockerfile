FROM eclipse-temurin:11-jre

COPY target/clinical-${VERSION}-jar-with-dependencies.jar /usr/local/actin.jar

ENTRYPOINT ["java", "-cp", "/usr/local/actin.jar", "com.hartwig.actin.clinical.ClinicalIngestionApplicationKt" ]