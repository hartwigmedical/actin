# Updating the ACTIN build agent image

The actin build agent image is used to build actin, and contains a bootstrapped .m2 repository along with the 
required external tools to run all tests. 

The m2 repository will get out of date over time. To update it perform the following steps:

```
# Run these steps from the checkout of the repository
mkdir system/src/test/docker/.m2/repository
mvn clean install -Dmaven.repo.local=system/src/test/docker/.m2/repository
cd system/src/test/docker
docker build . -t europe-west4-docker.pkg.dev/actin-build/build-registry-docker/actin-build-agent:<new_version> --platform=linux/amd64 -f buildagent.Dockerfile
docker push europe-west4-docker.pkg.dev/actin-build/build-registry-docker/actin-build-agent:<new_version>
```