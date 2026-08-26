FROM maven:3.9.10-eclipse-temurin-21

RUN apt-get update && apt-get install -y ghostscript imagemagick &&  \
    ln -s /usr/bin/convert /usr/bin/magick

COPY .m2/repository /.m2/repository
