FROM ubuntu:22.04

RUN apt-get update && apt-get install -fy \
    locales \
    unzip \
    python-pip \
    python2-dev \
    zlib1g-dev \
    libncurses5-dev \
    openjdk-17-jre \
    && locale-gen en_US.UTF-8 \
    && rm -rf /var/lib/apt/lists/*

ENV LANG en_US.UTF-8
ENV LC_ALL en_US.UTF-8

RUN python2 -m pip install TransVar==2.3.4.20161215
