FROM europe-west4-docker.pkg.dev/actin-build/build-registry-docker/actin-transvar:1.0.0

ARG VERSION=local-SNAPSHOT

COPY target/molecular-${VERSION}-jar-with-dependencies.jar /usr/local/actin.jar

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=95", "-cp", "/usr/local/actin.jar", "com.hartwig.actin.molecular.MolecularInterpreterApplicationKt"]
