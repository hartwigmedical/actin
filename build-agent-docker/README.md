# Updating the build agent image

The build agent image is used to build the project and contains a bootstrapped `.m2` repository along with the 
required external tools to run all tests. This copy of the repository is intended as a cache of dependencies to improve build
speed. Over time as dependencies and their versions are updated, the repository will drift from what is prescribed by `pom.xml`.

To update it:

* Run `update.sh` in this directory specifying the desired new version
* Update the Cloud Build configuration files to align with the new version

