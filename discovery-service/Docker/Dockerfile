FROM gitlab.peykasa.ir:4567/docker/java/pa-java:8-jdk-alpine-asia2
MAINTAINER Yaser(Amin) Sadeghi

ADD ./package /tmp
WORKDIR /tmp

RUN tar zxvf discovery-service-*.tar.gz && chmod +x install.sh && dos2unix install.sh && ./install.sh

WORKDIR /usr/local/PeykAsa/discovery-service/

#RUN dos2unix ./*.conf

#CMD ./env.sh
RUN rm /tmp/discovery-service-*
CMD exec java -Xms${JVM_MIN_MEM:-512m} -Xmx${JVM_MAX_MEM:-1g} -Djava.rmi.server.hostname=${JMX_HOST:-127.0.0.1} -Dcom.sun.management.jmxremote.port=${JMX_PORT:-2150} -Dcom.sun.management.jmxremote.rmi.port=${JMX_PORT:-2150} -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -XX:+UseG1GC -jar app.jar --logging.config=file:./config/log4j2.xml spring.config.location=./config/