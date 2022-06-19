FROM gitlab.peykasa.ir:4567/docker/java/pa-java:8.0_121
MAINTAINER Yaser Sadeghi(yaser.sadeghi@peykasa.ir)

ADD ./package /tmp
WORKDIR /tmp

RUN tar zxvf auth-server-*.tar.gz && \
    chmod +x install.sh && \
    dos2unix install.sh && \
    ./install.sh

WORKDIR /usr/local/PeykAsa/auth-server/
RUN dos2unix ./*.conf && cp /tmp/env.sh ./env.sh && chmod 755 ./env.sh && dos2unix env.sh
CMD ./env.sh &&  /etc/init.d/pa-auth-server run \
    pwd && \
    sleep 5
