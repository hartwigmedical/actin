FROM eclipse-temurin:11-jre

COPY --from=eu.gcr.io/hmf-build/common-resources-public:0.1 /data/resources/public/disease_ontology /data/resources/public/disease_ontology
COPY --from=europe-west4-docker.pkg.dev/actin-build/build-registry-docker/actin-resources:0.1 /data/resources/actin/serve_db /data/resources/actin/serve_db
COPY --from=europe-west4-docker.pkg.dev/actin-build/build-registry-docker/actin-resources:0.1 /data/resources/actin/evidence_curation /data/resources/actin/evidence_curation

COPY target/molecular-local-SNAPSHOT-jar-with-dependencies.jar /usr/local/actin.jar

ENTRYPOINT ["java", "-cp", "/usr/local/actin.jar", "com.hartwig.actin.molecular.orange.OrangeInterpreterApplication", \
    "-serve_directory", "/data/resources/actin/serve_db", \
    "-external_trial_mapping_tsv", "/data/resources/actin/evidence_curation/external_trial_mapping.tsv", \
    "-doid_json", "/data/resources/public/disease_ontology/doid.json" \
]