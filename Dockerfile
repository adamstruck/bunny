FROM ubuntu:16.04

MAINTAINER Adam Struck <strucka@ohsu.edu>

USER root
ENV PATH /opt/bin:$PATH

RUN apt-get update && \
    apt-get install --yes \
    build-essential \
    python-software-properties \
    software-properties-common \
    wget \
    curl \
    git \
    openjdk-8-jdk \
    maven \
    postgresql \
    postgresql-contrib \
    postgresql-client

# Intsall RabbitMQ
RUN echo 'deb http://www.rabbitmq.com/debian/ testing main' | tee /etc/apt/sources.list.d/rabbitmq.list
RUN wget -O- https://www.rabbitmq.com/rabbitmq-release-signing-key.asc | apt-key add -

RUN apt-get update && \
    apt-get install --yes \
    rabbitmq-server \
    && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Setup RabbitMQ
RUN service rabbitmq-server start && \
    rabbitmqctl add_user bunny bunny && \
    rabbitmqctl add_vhost bunny && \
    rabbitmqctl set_user_tags bunny administrator && \
    rabbitmqctl set_permissions -p bunny bunny ".*" ".*" ".*"

WORKDIR /opt

RUN git clone https://github.com/rabix/bunny.git && \
    cd bunny && \
    git checkout develop && \
    mvn package -P all && \
    cd /opt 

# Initialize DB
RUN psql bunny bunny < /opt/bunny/rabix-engine/src/main/resources/org/rabix/engine/jdbi/dbinit.sql

EXPOSE 8080 8081

WORKDIR /home/
ENTRYPOINT []
CMD []
