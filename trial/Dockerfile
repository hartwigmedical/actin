FROM eclipse-temurin:17-jre

ARG VERSION

COPY target/trial-${VERSION}-jar-with-dependencies.jar /usr/local/actin.jar

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=95", "-cp", "/usr/local/actin.jar", "com.hartwig.actin.trial.TrialCreatorApplicationKt" ]
