FROM eclipse-temurin:11-jre

ARG VERSION

COPY target/molecular-${VERSION}-jar-with-dependencies.jar /usr/local/actin.jar

ENTRYPOINT ["java", "-cp", "/usr/local/actin.jar", "com.hartwig.actin.molecular.orange.OrangeInterpreterApplicationKt"]