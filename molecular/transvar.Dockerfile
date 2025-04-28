FROM ubuntu:22.04

RUN apt-get update && apt-get install -fy unzip python-pip python2-dev zlib1g-dev libncurses5-dev openjdk-17-jre \
    && python2 -m pip install TransVar==2.3.4.20161215