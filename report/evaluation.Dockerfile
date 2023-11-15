FROM eclipse-temurin:11-jre

COPY target/report-${VERSION}-jar-with-dependencies.jar /usr/local/actin.jar

ENTRYPOINT ["java", "-cp", "/usr/local/actin.jar", "com.hartwig.actin.report.TabularTreatmentMatchWriterApplicationKt"]