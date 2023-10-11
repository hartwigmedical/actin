FROM eclipse-temurin:11-jre

COPY --from=eu.gcr.io/hmf-build/common-resources-public:0.2 /data/resources/public/disease_ontology /data/resources/public/disease_ontology
COPY --from=europe-west4-docker.pkg.dev/actin-build/build-registry-docker/actin-resources:0.3.0 /data/resources/actin/atc_config /data/resources/actin/atc_config
COPY --from=europe-west4-docker.pkg.dev/actin-build/build-registry-docker/actin-resources:0.3.0 /data/resources/actin/treatment_db /data/resources/actin/treatment_db

COPY target/clinical-local-SNAPSHOT-jar-with-dependencies.jar /usr/local/actin.jar

ENTRYPOINT ["java", "-cp", "/usr/local/actin.jar", "com.hartwig.actin.clinical.ClinicalIngestionApplicationKt", \
    "-doid_json", "/data/resources/public/disease_ontology/doid.json", \
    "-atc_tsv", "/data/resources/actin/atc_config/atc_tree.tsv", \
    "-treatment_directory", "/data/resources/actin/treatment_db" \
]