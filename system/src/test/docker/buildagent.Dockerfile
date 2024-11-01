FROM maven:3.9.2-eclipse-temurin-11

RUN apt-get update && apt-get install -y \
    ghostscript \
    imagemagick 

RUN ln -s /usr/bin/convert /usr/bin/magick

COPY .m2/repository /.m2/repository