FROM europe-west4-docker.pkg.dev/actin-build/build-registry-docker/reference-genome-37-resources:1.0.0

ARG VERSION=local-SNAPSHOT

RUN apt-get update && apt-get install -fy unzip python-pip python2-dev zlib1g-dev libncurses5-dev
RUN python2 -m pip install TransVar==2.3.4.20161215

COPY --from=europe-west4-docker.pkg.dev/actin-build/build-registry-docker/common-resources-public:1.0.0 /data/resources/public/ensembl_data_cache /data/resources/ensembl_data_cache
COPY --from=europe-west4-docker.pkg.dev/actin-build/build-registry-docker/crunch-resources:1.0.1 /data/resources/crunch/transvar_db /data/resources/transvar_db

COPY target/molecular-${VERSION}-jar-with-dependencies.jar /usr/local/actin.jar
COPY target/classes/transvar.cfg /home/root/.transvar.cfg

ENTRYPOINT ["java", "-cp", "/usr/local/actin.jar", "com.hartwig.actin.molecular.MolecularInterpreterApplicationKt"]