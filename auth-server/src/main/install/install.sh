#!/bin/bash

COMPONENT_NAME=@project.artifactId@
NAME=$COMPONENT_NAME-@project.version@
SERVICE_NAME=pa-$COMPONENT_NAME
echo "listing root "
ls -la ./
echo "copying security jar file to jdk"
cp  local_policy.jar /usr/local/PeykAsa/jdk1.8.0_121/jre/lib/security
cp  US_export_policy.jar /usr/local/PeykAsa/jdk1.8.0_121/jre/lib/security
ls -la /usr/local/PeykAsa/jdk1.8.0_121/jre/lib/security
echo "making directory /usr/local/PeykAsa/$NAME/"
mkdir -p /usr/local/PeykAsa/$COMPONENT_NAME/log/
echo "copying 'permission-importer.sh' file to /usr/local/PeykAsa/$COMPONENT_NAME/permission-importer.sh"
cp permission-importer.sh  /usr/local/PeykAsa/$COMPONENT_NAME/
echo "copying '*.json' file to /usr/local/PeykAsa/$COMPONENT_NAME/*.json"
cp *.json  /usr/local/PeykAsa/$COMPONENT_NAME/
echo "copying '$NAME.jar' file to /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.jar"
cp $NAME.jar  /usr/local/PeykAsa/$COMPONENT_NAME/
echo "copying '$NAME.conf' file to /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.conf"
cp $NAME.conf /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.conf
echo "copying config directory to /usr/local/PeykAsa/$COMPONENT_NAME/config"
cp -r ./config /usr/local/PeykAsa/$COMPONENT_NAME/
echo "removing old service at /etc/init.d/$SERVICE_NAME"
rm -rf /etc/init.d/$SERVICE_NAME
echo "creating service at /etc/init.d/$SERVICE_NAME"
chmod -R 755 /usr/local/PeykAsa/$COMPONENT_NAME/
ln -s /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.jar /etc/init.d/$SERVICE_NAME

echo "install completed: "
echo ""
echo "service config file: /usr/local/PeykAsa/$COMPONENT_NAME/$NAME.conf"
echo "config path:         /usr/local/PeykAsa/$COMPONENT_NAME/config"
echo "start service:       /etc/init.d/$SERVICE_NAME start"
echo "log path:            /usr/local/PeykAsa/$COMPONENT_NAME/log"
echo ""
