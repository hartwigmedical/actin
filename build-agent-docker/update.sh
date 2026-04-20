#!/usr/bin/env bash

set -e

WD="$(readlink -f $(dirname $0))"
REPO="$WD/.m2/repository"
IMG="europe-west4-docker.pkg.dev/actin-build/build-registry-docker/actin-build-agent"

[[ $# -ne 1 ]] && echo "Provide the version for the new image" && exit 1

rm -rf "$REPO" && mkdir -p "$REPO"
cd "$(dirname $WD)"
mvn clean install -Dmaven.repo.local="$REPO"
cd "$WD"

docker build . -t "$IMG:$1" --platform=linux/amd64
docker push "$IMG:$1"

echo "Version $1 built and pushed. Cloud Build configs now need updating!"
