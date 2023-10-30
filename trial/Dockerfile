FROM eclipse-temurin:11-jre

COPY target/trial-local-SNAPSHOT-jar-with-dependencies.jar /usr/local/actin.jar

ENTRYPOINT ["java", "-cp", "/usr/local/actin.jar", "com.hartwig.actin.trial.TrialCreatorApplicationKt" ]