FROM eclipse-temurin:11-jre@sha256:7e474f2d1d3d1291c152d97ee3b9b6689b81e0d535326d8bfc680db35de0969b

RUN apt-get update && apt-get install -fy unzip python-pip python2-dev zlib1g-dev libncurses5-dev
RUN python2 -m pip install TransVar==2.3.4.20161215