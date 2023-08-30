FROM eclipse-temurin:11-jre

COPY --from=eu.gcr.io/hmf-build/common-resources-public:0.1 /data/resources/public/disease_ontology /data/resources/public/disease_ontology
COPY --from=europe-west4-docker.pkg.dev/actin-build/build-registry-docker/actin-resources:0.1 /data/resources/actin/ctc_config /data/resources/actin/ctc_config
COPY --from=europe-west4-docker.pkg.dev/actin-build/build-registry-docker/actin-resources:0.1 /data/resources/actin/serve_db/KnownGenes.SERVE.37.tsv /data/resources/actin/serve_db/KnownGenes.SERVE.37.tsv
COPY --from=europe-west4-docker.pkg.dev/actin-build/build-registry-docker/actin-resources:0.1 /data/resources/actin/trial_config /data/resources/actin/trial_config
COPY --from=europe-west4-docker.pkg.dev/actin-build/build-registry-docker/actin-resources:0.1 /data/resources/actin/treatment_db /data/resources/actin/treatment_db

COPY target/treatment-local-SNAPSHOT-jar-with-dependencies.jar /usr/local/actin.jar

ENTRYPOINT ["java", "-cp", "/usr/local/actin.jar", "com.hartwig.actin.treatment.TreatmentCreatorApplicationKt", \
    "-trial_config_directory", "/data/resources/actin/trial_config", \
    "-ctc_config_directory", "/data/resources/actin/ctc_config", \
    "-treatment_directory", "/data/resources/actin/treatment_db", \
    "-doid_json", "/data/resources/public/disease_ontology/doid.json", \
    "-known_genes_tsv", "/data/resources/actin/serve_db/KnownGenes.SERVE.37.tsv" \
]