FROM gitlab.peykasa.ir:4567/docker/java/pa-java:8-jdk-alpine-asia2
MAINTAINER Yaser(Amin) Sadeghi

ADD ./package /tmp
WORKDIR /tmp

RUN tar zxvf audit-server-*.tar.gz && chmod +x install.sh && dos2unix install.sh && ./install.sh

WORKDIR /usr/local/PeykAsa/audit-server/

#RUN cp /tmp/env.sh ./env.sh && chmod 755 ./env.sh && dos2unix env.sh

#CMD ./env.sh
RUN rm /tmp/audit-server-*
CMD exec java -Xms${JVM_MIN_MEM:-512m} -Xmx${JVM_MAX_MEM:-1g} -Djava.rmi.server.hostname=${JMX_HOST:-127.0.0.1} -Dcom.sun.management.jmxremote.port=${JMX_PORT:-2052} -Dcom.sun.management.jmxremote.rmi.port=${JMX_PORT:-2152} -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -XX:+UseG1GC -jar app.jar --logging.config=file:./config/log4j2.xml spring.config.location=./config/